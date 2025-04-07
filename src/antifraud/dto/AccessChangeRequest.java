package antifraud.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern; // For pattern validation
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessChangeRequest {

    @NotEmpty(message = "Username cannot be empty")
    private String username;

    @NotEmpty(message = "Operation cannot be empty")
    @Pattern(regexp = "^(LOCK|UNLOCK)$", message = "Operation must be LOCK or UNLOCK") // Validate operation
    private String operation;
}