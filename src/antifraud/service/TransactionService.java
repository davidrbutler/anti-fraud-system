package antifraud.service;

import antifraud.dto.FeedbackRequest; // Assuming this DTO exists { long transactionId; String feedback; }
import antifraud.dto.TransactionRequest;
import antifraud.exception.*; // Import custom exceptions
import antifraud.model.Region;
import antifraud.model.Transaction;
import antifraud.model.TransactionValidationResult;
import antifraud.repository.TransactionRepository;
import antifraud.util.ValidationUtil; // Import validation utility
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort; // For sorting history
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    // Stateful limits
    private long maxAllowed = 200;
    private long maxManual = 1500;

    // Correlation constants
    private static final int MANUAL_CORRELATION_LIMIT = 2;
    private static final int PROHIBITED_CORRELATION_LIMIT = 3;

    private final SuspiciousIpService suspiciousIpService;
    private final StolenCardService stolenCardService;
    private final TransactionRepository transactionRepository;
    private final ValidationUtil validationUtil;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");


    @Autowired
    public TransactionService(SuspiciousIpService suspiciousIpService,
                              StolenCardService stolenCardService,
                              TransactionRepository transactionRepository,
                              ValidationUtil validationUtil
    ) {
        this.suspiciousIpService = suspiciousIpService;
        this.stolenCardService = stolenCardService;
        this.transactionRepository = transactionRepository;
        this.validationUtil = validationUtil;
    }

    @Transactional
    public Map<String, Object> validateTransaction(TransactionRequest request) {
        // --- Logging added for Stage 6 Debugging ---
        System.out.printf("[Validate Tx] Current limits: maxAllowed=%d, maxManual=%d | Request Amount: %d%n",
                this.maxAllowed, this.maxManual, request.getAmount());
        // --- End Logging ---

        // --- Parse and Validate Date and Region ---
        LocalDateTime transactionDate;
        try {
            transactionDate = LocalDateTime.parse(request.getDate(), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format! Use format yyyy-MM-dd'T'HH:mm:ss");
        }
        Region transactionRegion;
        try {
            transactionRegion = Region.valueOf(request.getRegion());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid region code!");
        }

        Long amount = request.getAmount();
        String ip = request.getIp();
        String number = request.getNumber();
        Set<String> reasons = new HashSet<>();
        TransactionValidationResult finalResult;
        String info;

        // --- Query History ---
        LocalDateTime oneHourBefore = transactionDate.minusHours(1);
        List<Transaction> history = transactionRepository.findByNumberAndDateBetween(number, oneHourBefore, transactionDate);

        // --- Calculate Correlations ---
        long distinctRegionCount = history.stream().map(Transaction::getRegion).filter(region -> region != transactionRegion).distinct().count();
        long distinctIpCount = history.stream().map(Transaction::getIp).filter(histIp -> !histIp.equals(ip)).distinct().count();

        // --- Check all individual conditions using current limits ---
        boolean amountCausesManual = amount > this.maxAllowed && amount <= this.maxManual;
        boolean amountCausesProhibited = amount > this.maxManual;
        boolean ipIsSuspicious = suspiciousIpService.isIpSuspicious(ip);
        boolean cardIsStolen = stolenCardService.isCardStolen(number);
        boolean ipCorrelationCausesManual = distinctIpCount == MANUAL_CORRELATION_LIMIT;
        boolean ipCorrelationCausesProhibited = distinctIpCount >= PROHIBITED_CORRELATION_LIMIT;
        boolean regionCorrelationCausesManual = distinctRegionCount == MANUAL_CORRELATION_LIMIT;
        boolean regionCorrelationCausesProhibited = distinctRegionCount >= PROHIBITED_CORRELATION_LIMIT;

        // --- Determine Final Result based on highest severity ---
        if (amountCausesProhibited || ipIsSuspicious || cardIsStolen || ipCorrelationCausesProhibited || regionCorrelationCausesProhibited) {
            finalResult = TransactionValidationResult.PROHIBITED;
        } else if (amountCausesManual || ipCorrelationCausesManual || regionCorrelationCausesManual) {
            finalResult = TransactionValidationResult.MANUAL_PROCESSING;
        } else {
            finalResult = TransactionValidationResult.ALLOWED;
        }

        // --- Determine Info String based ONLY on Final Result ---
        if (finalResult == TransactionValidationResult.ALLOWED) {
            info = "none";
        } else {
            if (finalResult == TransactionValidationResult.MANUAL_PROCESSING) {
                if (amountCausesManual) reasons.add("amount");
                if (ipCorrelationCausesManual) reasons.add("ip-correlation");
                if (regionCorrelationCausesManual) reasons.add("region-correlation");
            } else { // PROHIBITED
                if (amountCausesProhibited) reasons.add("amount");
                if (ipIsSuspicious) reasons.add("ip");
                if (cardIsStolen) reasons.add("card-number");
                if (ipCorrelationCausesProhibited) reasons.add("ip-correlation");
                if (regionCorrelationCausesProhibited) reasons.add("region-correlation");
            }
            List<String> sortedReasons = new ArrayList<>(reasons);
            Collections.sort(sortedReasons);
            info = String.join(", ", sortedReasons);
            if (info.isEmpty() && finalResult != TransactionValidationResult.ALLOWED) {
                if (finalResult == TransactionValidationResult.MANUAL_PROCESSING && amountCausesManual) info = "amount";
                else info = "unknown";
            }
        }
        // --- Save Transaction Attempt ---
        Transaction transactionToSave = new Transaction(amount, ip, number, transactionRegion, transactionDate, finalResult);
        if (transactionToSave.getFeedback() == null) {
            transactionToSave.setFeedback("");
        }
        transactionRepository.save(transactionToSave);

        Map<String, Object> response = new HashMap<>();
        response.put("result", finalResult);
        response.put("info", info);
        return response;
    }


    // --- addFeedback method (No Logging Here) ---
    @Transactional
    public Transaction addFeedback(long transactionId, String feedbackValue) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found!"));

        if (transaction.getFeedback() != null && !transaction.getFeedback().isEmpty()) {
            throw new FeedbackConflictException("Feedback already provided for this transaction!");
        }

        TransactionValidationResult feedbackEnum;
        try {
            feedbackEnum = TransactionValidationResult.valueOf(feedbackValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid feedback value!");
        }

        if (feedbackEnum == transaction.getResult()) {
            throw new UnprocessableFeedbackException("Feedback matches original result!");
        }

        adjustLimits(transaction.getAmount(), transaction.getResult(), feedbackEnum);

        transaction.setFeedback(feedbackEnum.name());
        return transactionRepository.save(transaction);
    }

    // --- getTransactionHistory and getTransactionHistoryByNumber (No Logging Here) ---
    public List<Transaction> getTransactionHistory() {
        return transactionRepository.findAll(Sort.by(Sort.Direction.ASC, "transactionId"));
    }

    public List<Transaction> getTransactionHistoryByNumber(String number) {
        if (!validationUtil.isValidLuhn(number)) {
            throw new IllegalArgumentException("Invalid card number format (Luhn check failed)!");
        }
        List<Transaction> history = transactionRepository.findByNumberOrderByTransactionIdAsc(number);
        if (history.isEmpty()) {
            throw new TransactionNotFoundException("No history found for card number!");
        }
        return history;
    }

    // --- adjustLimits method (No Logging Here) ---
    private void adjustLimits(long transactionAmount, TransactionValidationResult originalResult, TransactionValidationResult feedbackResult) {
        double weight = 0.2;
        long oldMaxAllowed = this.maxAllowed;
        long oldMaxManual = this.maxManual;
        double currentMaxAllowed = (double) oldMaxAllowed;
        double currentMaxManual = (double) oldMaxManual;
        double amount = (double) transactionAmount;

        if (originalResult == TransactionValidationResult.ALLOWED) {
            if (feedbackResult == TransactionValidationResult.MANUAL_PROCESSING) {
                this.maxAllowed = (long) Math.ceil((1 - weight) * currentMaxAllowed - weight * amount);
            } else { // PROHIBITED
                this.maxAllowed = (long) Math.ceil((1 - weight) * currentMaxAllowed - weight * amount);
                this.maxManual = (long) Math.ceil((1 - weight) * currentMaxManual - weight * amount);
            }
        } else if (originalResult == TransactionValidationResult.MANUAL_PROCESSING) {
            if (feedbackResult == TransactionValidationResult.ALLOWED) {
                this.maxAllowed = (long) Math.ceil((1 - weight) * currentMaxAllowed + weight * amount);
            } else { // PROHIBITED
                this.maxManual = (long) Math.ceil((1 - weight) * currentMaxManual - weight * amount);
            }
        } else { // PROHIBITED
            if (feedbackResult == TransactionValidationResult.ALLOWED) {
                this.maxAllowed = (long) Math.ceil((1 - weight) * currentMaxAllowed + weight * amount);
                this.maxManual = (long) Math.ceil((1 - weight) * currentMaxManual + weight * amount);
            } else { // MANUAL_PROCESSING
                this.maxManual = (long) Math.ceil((1 - weight) * currentMaxManual + weight * amount);
            }
        }

        if (this.maxAllowed <= 0) this.maxAllowed = 1;
        if (this.maxManual <= this.maxAllowed) {
            this.maxManual = this.maxAllowed + 1;
        }
    }

    // --- getAmountResult helper (No Logging Here) ---
    private TransactionValidationResult getAmountResult(Long amount) {
        if (amount <= this.maxAllowed) {
            return TransactionValidationResult.ALLOWED;
        } else if (amount <= this.maxManual) {
            return TransactionValidationResult.MANUAL_PROCESSING;
        } else {
            return TransactionValidationResult.PROHIBITED;
        }
    }
}