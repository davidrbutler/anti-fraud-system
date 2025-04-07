package antifraud.exception;

public class IpAddressConflictException extends RuntimeException {
    public IpAddressConflictException(String message) {
        super(message);
    }
}
