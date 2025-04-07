package antifraud.controller;

import antifraud.dto.FeedbackRequest; // Import FeedbackRequest DTO
import antifraud.dto.TransactionRequest;
import antifraud.dto.TransactionResponse;
import antifraud.dto.TransactionViewDTO; // Import DTO for responses
import antifraud.model.Transaction; // Import Transaction entity
import antifraud.model.TransactionValidationResult;
import antifraud.service.TransactionService;
import antifraud.util.ValidationUtil; // Import validation utility
import jakarta.validation.Valid;
// Import Authentication and GrantedAuthority
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
// Other imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/antifraud")
public class TransactionController {

    private final TransactionService transactionService;
    private final ValidationUtil validationUtil;

    @Autowired
    public TransactionController(TransactionService transactionService, ValidationUtil validationUtil) {
        this.transactionService = transactionService;
        this.validationUtil = validationUtil;
    }

    // POST /api/antifraud/transaction (Unchanged from last version)
    @PostMapping("/transaction")
    public ResponseEntity<TransactionResponse> validateTransaction(@Valid @RequestBody TransactionRequest request) {
        if (!validationUtil.isValidIpV4(request.getIp())) {
            throw new IllegalArgumentException("Invalid IP address format!");
        }
        if (!validationUtil.isValidLuhn(request.getNumber())) {
            throw new IllegalArgumentException("Invalid card number format (Luhn check failed)!");
        }

        Map<String, Object> validationDetails = transactionService.validateTransaction(request);
        TransactionValidationResult resultEnum = (TransactionValidationResult) validationDetails.get("result");
        String info = (String) validationDetails.get("info");
        TransactionResponse response = new TransactionResponse(resultEnum.toString(), info);
        return ResponseEntity.ok(response);
    }


    // PUT /api/antifraud/transaction (Added logging)
    @PutMapping("/transaction")
    public ResponseEntity<TransactionViewDTO> addTransactionFeedback(
            @Valid @RequestBody FeedbackRequest request,
            Authentication authentication) { // Inject Authentication object

        // --- Added Logging for Role Debugging ---
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            String authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", "));
            System.out.printf("[Feedback Request] User: %s, Roles: [%s], TX_ID: %d, Feedback: %s%n",
                    username, authorities, request.getTransactionId(), request.getFeedback());
        } else {
            System.out.println("[Feedback Request] Received for TX_ID: " + request.getTransactionId() + " by UNAUTHENTICATED user?");
        }
        // --- End Logging ---

        // Call the service method (unchanged)
        Transaction updatedTransaction = transactionService.addFeedback(request.getTransactionId(), request.getFeedback());
        TransactionViewDTO response = TransactionViewDTO.fromEntity(updatedTransaction);
        return ResponseEntity.ok(response);
    }

    // GET /api/antifraud/history (Unchanged from last version)
    @GetMapping("/history")
    public ResponseEntity<List<TransactionViewDTO>> getFullTransactionHistory() {
        List<Transaction> history = transactionService.getTransactionHistory();
        List<TransactionViewDTO> response = history.stream()
                .map(TransactionViewDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // GET /api/antifraud/history/{number} (Unchanged from last version)
    @GetMapping("/history/{number}")
    public ResponseEntity<List<TransactionViewDTO>> getTransactionHistoryByNumber(@PathVariable String number) {
        if (!validationUtil.isValidLuhn(number)) {
            throw new IllegalArgumentException("Invalid card number format (Luhn check failed) in path variable!");
        }
        List<Transaction> history = transactionService.getTransactionHistoryByNumber(number);
        List<TransactionViewDTO> response = history.stream()
                .map(TransactionViewDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}