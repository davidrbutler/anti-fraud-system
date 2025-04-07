package antifraud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty; // For fields coming from request
import jakarta.validation.constraints.NotNull; // For fields coming from request
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Use LocalDateTime for database storage

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @NotNull
    @Column(nullable = false)
    private Long amount;

    @NotEmpty
    @Column(nullable = false)
    private String ip;

    @NotEmpty
    @Column(nullable = false)
    private String number;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime date;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionValidationResult result; // Validation outcome



    @Column(nullable = true)
    private String feedback;


    public Transaction(Long amount, String ip, String number, Region region, LocalDateTime date, TransactionValidationResult result) {
        this.amount = amount;
        this.ip = ip;
        this.number = number;
        this.region = region;
        this.date = date;
        this.result = result;
        this.feedback = "";
    }
}