package antifraud.dto;

import antifraud.model.Region; // Import Region if needed for custom validation later
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern; // Import Pattern for date format
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "Amount cannot be null")
    @Min(value = 1, message = "Amount must be greater than 0")
    private Long amount;

    @NotEmpty(message = "IP address cannot be empty")
    // IP format validation happens in controller/service
    private String ip;

    @NotEmpty(message = "Card number cannot be empty")
    // Luhn validation happens in controller/service
    private String number;

    // --- Added for Stage 5 ---
    @NotNull(message = "Region cannot be null") // Use NotNull for Enum (or NotEmpty for String)
    // Region code validation (against Enum) happens in service/controller
    // If kept as String, use @NotEmpty
    @NotEmpty(message = "Region cannot be empty")
    private String region; // Keep as String for input, validate against Enum later

    @NotEmpty(message = "Date cannot be empty")
    // Validate format "yyyy-MM-ddTHH:mm:ss" - basic regex, parsing is definitive check
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$", message = "Date must be in format yyyy-MM-ddTHH:mm:ss")
    private String date; // Keep as String for input, parse to LocalDateTime in service
    // --- End Stage 5 additions ---

}