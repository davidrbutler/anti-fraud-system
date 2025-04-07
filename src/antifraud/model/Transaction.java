package antifraud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty; // For fields coming from request
import jakarta.validation.constraints.NotNull; // For fields coming from request
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Use LocalDateTime for database storage

@Entity
@Table(name = "transactions") // Explicit table name
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId; // Use a descriptive name for the ID

    @NotNull // From request
    @Column(nullable = false)
    private Long amount;

    @NotEmpty // From request
    @Column(nullable = false)
    private String ip;

    @NotEmpty // From request
    @Column(nullable = false)
    private String number; // Card number

    @NotNull // From request
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region; // Use the Region enum

    @NotNull // From request (after parsing)
    @Column(nullable = false)
    private LocalDateTime date; // Store as timestamp in DB

    @NotNull // Determined by validation service
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionValidationResult result; // Validation outcome


    // Feedback field (from Stage 6), nullable
    // Initialize to empty string or allow null based on preference/requirements
    @Column(nullable = true) // Or false if empty string is preferred over null
    private String feedback = ""; // Default to empty string
    // Alternatively: private String feedback; (defaults to null)

    // Constructor excluding ID (useful when creating before saving)
    public Transaction(Long amount, String ip, String number, Region region, LocalDateTime date, TransactionValidationResult result) {
        this.amount = amount;
        this.ip = ip;
        this.number = number;
        this.region = region;
        this.date = date;
        this.result = result;
        this.feedback = ""; // Ensure feedback is initialized if not using null
    }
}