package antifraud.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRequest {

    @NotEmpty(message = "Card number cannot be empty")
    // Luhn check is performed in the service layer
    private String number;
}