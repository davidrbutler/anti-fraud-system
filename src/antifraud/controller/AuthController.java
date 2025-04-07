package antifraud.controller;

import antifraud.dto.*; // Import new DTOs
import antifraud.model.User;
import antifraud.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // Import Map for access status response
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest request) {
        User newUser = new User(); // Create empty user, service will set defaults
        newUser.setName(request.getName());
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword()); // Password encoded in service

        User registeredUser = userService.registerUser(newUser);
        // Use service's converter which now includes role
        UserResponse response = userService.convertToUserResponse(registeredUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        // Exceptions (409, 400) handled by @ControllerAdvice
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<User> users = userService.listUsers();
        // Use service's converter which now includes role
        List<UserResponse> responseList = users.stream()
                .map(userService::convertToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<UserDeleteResponse> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        // Exception (404) handled by @ControllerAdvice
        UserDeleteResponse response = new UserDeleteResponse(username, "Deleted successfully!");
        return ResponseEntity.ok(response);
    }

    // --- New Stage 3 Endpoints ---

    @PutMapping("/role")
    public ResponseEntity<UserResponse> changeRole(@Valid @RequestBody RoleChangeRequest request) {
        User updatedUser = userService.changeUserRole(request.getUsername(), request.getRole());
        // Convert updated user (with new role) to response DTO
        UserResponse response = userService.convertToUserResponse(updatedUser);
        return ResponseEntity.ok(response);
        // Exceptions (404, 400, 409) handled by @ControllerAdvice
    }

    @PutMapping("/access")
    public ResponseEntity<Map<String, String>> changeAccess(@Valid @RequestBody AccessChangeRequest request) {
        // Service method returns the required Map structure directly
        Map<String, String> statusResponse = userService.changeUserAccess(request.getUsername(), request.getOperation());
        return ResponseEntity.ok(statusResponse);
        // Exceptions (404, 400) handled by @ControllerAdvice
    }
    // --- End Stage 3 Endpoints ---
}