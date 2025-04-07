package antifraud.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern; // Import for pattern validation
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {

    @NotNull(message = "Transaction ID cannot be null")
    private Long transactionId;

    @NotEmpty(message = "Feedback cannot be empty")
    @Pattern(regexp = "^(?i)(ALLOWED|MANUAL_PROCESSING|PROHIBITED)$", message = "Feedback must be ALLOWED, MANUAL_PROCESSING, or PROHIBITED")
    private String feedback;
}