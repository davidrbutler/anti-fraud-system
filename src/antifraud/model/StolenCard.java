package antifraud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty; // Added for consistency
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stolen_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StolenCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(unique = true, nullable = false)
    private String number;
}