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
@AllArgsConstructor // Added @AllArgsConstructor for convenience if needed
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

    // --- Added for Stage 3 ---
    @Enumerated(EnumType.STRING) // Store role name as String in DB
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean accountLocked = false; // Default to false (representing non-locked)
    // --- End Stage 3 additions ---

    // Optional: Constructor without id (useful for creation)
    public User(String name, String username, String password, UserRole role, boolean accountLocked) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.accountLocked = accountLocked;
    }

    // --- Added helper methods for Stage 3 ---
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
    // --- End Stage 3 additions ---
}