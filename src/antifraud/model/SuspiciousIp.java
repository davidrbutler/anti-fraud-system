package antifraud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty; // Added for consistency, though validation happens in service/controller
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suspicious_ips") // Explicit table name
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuspiciousIp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(unique = true, nullable = false) // IP should be unique and not null
    private String ip;
}