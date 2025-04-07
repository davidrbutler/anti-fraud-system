package antifraud.handler;

// Import new exceptions for Stage 6
import antifraud.exception.FeedbackConflictException;
import antifraud.exception.TransactionNotFoundException;
import antifraud.exception.UnprocessableFeedbackException;
// Other existing imports
import antifraud.exception.IpAddressConflictException;
import antifraud.exception.IpAddressNotFoundException;
import antifraud.exception.StolenCardConflictException;
import antifraud.exception.StolenCardNotFoundException;
import antifraud.exception.UserNotFoundException;
import antifraud.exception.UsernameAlreadyExistsException;
import antifraud.exception.UserRoleConflictException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    // --- Existing Handlers (Stages 2, 3, 4) ---
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Object> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UserRoleConflictException.class)
    public ResponseEntity<Object> handleUserRoleConflict(UserRoleConflictException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = "Validation failed: " + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IpAddressConflictException.class)
    public ResponseEntity<Object> handleIpAddressConflict(IpAddressConflictException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(StolenCardConflictException.class)
    public ResponseEntity<Object> handleStolenCardConflict(StolenCardConflictException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IpAddressNotFoundException.class)
    public ResponseEntity<Object> handleIpAddressNotFound(IpAddressNotFoundException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(StolenCardNotFoundException.class)
    public ResponseEntity<Object> handleStolenCardNotFound(StolenCardNotFoundException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    // --- New Handlers for Stage 6 ---

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<Object> handleTransactionNotFound(TransactionNotFoundException ex, HttpServletRequest request) {
        // Handles 404 for feedback and history endpoints
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(FeedbackConflictException.class)
    public ResponseEntity<Object> handleFeedbackConflict(FeedbackConflictException ex, HttpServletRequest request) {
        // Handles 409 when feedback already exists
        return buildResponseEntity(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnprocessableFeedbackException.class)
    public ResponseEntity<Object> handleUnprocessableFeedback(UnprocessableFeedbackException ex, HttpServletRequest request) {
        // Handles 422 when feedback contradicts original result
        return buildResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI());
    }

    // --- Helper Method to Build Response ---
    private ResponseEntity<Object> buildResponseEntity(HttpStatus status, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return new ResponseEntity<>(body, status);
    }

}