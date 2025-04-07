package antifraud.service;

import antifraud.dto.UserResponse;
import antifraud.exception.UserNotFoundException;
import antifraud.exception.UsernameAlreadyExistsException;
import antifraud.exception.UserRoleConflictException; // Import the new exception
import antifraud.model.User;
import antifraud.model.UserRole; // Import UserRole enum
import antifraud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Add transactional import

import java.util.List;
import java.util.Map; // Import Map for access status response
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional // Ensure atomicity
    public User registerUser(User requestUser) {
        // Check if username already exists (case-insensitive)
        if (userRepository.findByUsernameIgnoreCase(requestUser.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("User already exists!");
        }

        User newUser = new User();
        newUser.setName(requestUser.getName());
        newUser.setUsername(requestUser.getUsername());
        // Encode password before saving
        newUser.setPassword(passwordEncoder.encode(requestUser.getPassword()));


        // Check if this is the very first user being registered
        if (userRepository.count() == 0) {
            newUser.setRole(UserRole.ADMINISTRATOR);
            newUser.setAccountLocked(false); // Administrator starts unlocked
        } else {
            newUser.setRole(UserRole.MERCHANT);
            newUser.setAccountLocked(true); // Subsequent users (Merchants) start locked
        }


        return userRepository.save(newUser);
    }

    // List users (no change in logic, but response DTO will include role)
    public List<User> listUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    // Delete user
    @Transactional
    public void deleteUser(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));
        userRepository.delete(user);
    }


    @Transactional
    public User changeUserRole(String username, String requestedRole) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        UserRole newRole;
        try {
            // Validate role string - must be MERCHANT or SUPPORT
            if ("MERCHANT".equalsIgnoreCase(requestedRole)) {
                newRole = UserRole.MERCHANT;
            } else if ("SUPPORT".equalsIgnoreCase(requestedRole)) {
                newRole = UserRole.SUPPORT;
            } else {
                // Handles invalid role names and attempts to assign ADMINISTRATOR
                throw new IllegalArgumentException("Invalid role provided!");
            }
        } catch (IllegalArgumentException e) {
            // Rethrow specifically for clarity or if ControllerAdvice needs refinement
            throw new IllegalArgumentException("Role must be MERCHANT or SUPPORT!");
        }

        // Check if the user already has the target role
        if (user.getRole() == newRole) {
            throw new UserRoleConflictException("User already has the role " + newRole.name() + "!");
        }

        user.setRole(newRole);
        return userRepository.save(user); // Return updated User entity
    }

    @Transactional
    public Map<String, String> changeUserAccess(String username, String operation) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        // Cannot lock/unlock the ADMINISTRATOR
        if (user.getRole() == UserRole.ADMINISTRATOR) {
            throw new IllegalArgumentException("Cannot lock/unlock the ADMINISTRATOR!");
        }

        boolean lock; // true if locking, false if unlocking
        if ("LOCK".equalsIgnoreCase(operation)) {
            lock = true;
        } else if ("UNLOCK".equalsIgnoreCase(operation)) {
            lock = false;
        } else {
            // Should not happen if request validation is done, but good practice
            throw new IllegalArgumentException("Invalid access operation!");
        }

        user.setAccountLocked(lock);
        userRepository.save(user);

        String statusAction = lock ? "locked" : "unlocked";
        // Return JSON structure as defined in requirements
        String statusMessage = String.format("User %s %s!", user.getUsername(), statusAction);
        return Map.of("status", statusMessage); // Return a map for automatic JSON conversion
    }


    // Helper method updated to include role
    public UserResponse convertToUserResponse(User user) {
        // Ensure UserResponse DTO has a role field added
        return new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getRole().name());
    }
}
