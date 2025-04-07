package antifraud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty; // Added for consistency, though validation happens in service/controller
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suspicious_ips")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuspiciousIp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(unique = true, nullable = false)
    private String ip;
}