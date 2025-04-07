package antifraud.exception;

public class StolenCardConflictException extends RuntimeException {
    public StolenCardConflictException(String message) {
        super(message);
    }
}
