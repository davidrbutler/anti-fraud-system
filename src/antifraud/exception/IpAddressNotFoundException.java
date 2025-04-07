package antifraud.exception;

public class IpAddressNotFoundException extends RuntimeException {
    public IpAddressNotFoundException(String message) {
        super(message);
    }
}
