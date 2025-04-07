package antifraud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor; // Keep if using @AllArgsConstructor
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String name;

    @NotEmpty
    @Column(unique = true, nullable = false)
    private String username;

    @NotEmpty
    @Column(nullable = false)
    private String password;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean accountLocked = false;


    public User(String name, String username, String password, UserRole role, boolean accountLocked) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.accountLocked = accountLocked;
    }


    // Method expected by Spring Security UserDetails builder (.accountLocked())
    // Returns true if account IS locked, false otherwise
    public boolean isAccountLocked() {
        return this.accountLocked;
    }

    // Convenience method often used logically
    // Returns true if account is NOT locked, false otherwise
    public boolean isAccountNonLocked() {
        return !this.accountLocked;
    }

    // Setter to explicitly set locked status based on NON-LOCKED state
    // (Matches logic potentially used in UserService)
    public void setAccountNonLocked(boolean nonLocked) {
        this.accountLocked = !nonLocked;
    }

}