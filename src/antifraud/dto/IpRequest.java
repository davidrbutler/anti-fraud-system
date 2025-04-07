package antifraud.dto;

import jakarta.validation.constraints.NotEmpty;
// Optional: Add pattern validation here too, though service validates
// import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpRequest {

    @NotEmpty(message = "IP address cannot be empty")
    // Example optional pattern validation:
    // @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", message = "Invalid IPv4 format")
    private String ip;
}