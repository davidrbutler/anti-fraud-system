package antifraud.exception;

public class UserRoleConflictException extends RuntimeException {
    public UserRoleConflictException(String message) {
        super(message);
    }
}
