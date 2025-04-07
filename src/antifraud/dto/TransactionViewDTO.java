package antifraud.dto;

import antifraud.model.Region;
import antifraud.model.Transaction;
import antifraud.model.TransactionValidationResult;
import com.fasterxml.jackson.annotation.JsonFormat; // For formatting date
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionViewDTO {

    private Long transactionId;
    private Long amount;
    private String ip;
    private String number; // Card number
    private String region; // Region Code as String
    // Format date as required: yyyy-MM-ddTHH:mm:ss
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;
    private String result; // Result as String
    private String feedback; // Feedback as String (can be empty)

    /**
     * Static factory method or constructor to convert from Transaction entity.
     * Handles potential null feedback.
     *
     * @param transaction The Transaction entity.
     * @return A TransactionViewDTO instance.
     */
    public static TransactionViewDTO fromEntity(Transaction transaction) {
        return new TransactionViewDTO(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getIp(),
                transaction.getNumber(),
                transaction.getRegion().name(), // Convert enum to String
                transaction.getDate(),
                transaction.getResult().name(), // Convert enum to String
                transaction.getFeedback() == null ? "" : transaction.getFeedback() // Handle null feedback
        );
    }
}