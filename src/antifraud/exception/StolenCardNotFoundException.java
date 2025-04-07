package antifraud.exception;

public class StolenCardNotFoundException extends RuntimeException {
  public StolenCardNotFoundException(String message) {
    super(message);
  }
}
