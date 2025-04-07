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
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword());

        User registeredUser = userService.registerUser(newUser);
        UserResponse response = userService.convertToUserResponse(registeredUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<User> users = userService.listUsers();
        List<UserResponse> responseList = users.stream()
                .map(userService::convertToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<UserDeleteResponse> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        UserDeleteResponse response = new UserDeleteResponse(username, "Deleted successfully!");
        return ResponseEntity.ok(response);
    }


    @PutMapping("/role")
    public ResponseEntity<UserResponse> changeRole(@Valid @RequestBody RoleChangeRequest request) {
        User updatedUser = userService.changeUserRole(request.getUsername(), request.getRole());
        UserResponse response = userService.convertToUserResponse(updatedUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/access")
    public ResponseEntity<Map<String, String>> changeAccess(@Valid @RequestBody AccessChangeRequest request) {
        Map<String, String> statusResponse = userService.changeUserAccess(request.getUsername(), request.getOperation());
        return ResponseEntity.ok(statusResponse);
    }
}