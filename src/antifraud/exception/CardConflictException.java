package antifraud.exception;

public class CardConflictException extends RuntimeException {
  public CardConflictException(String message) {
    super(message);
  }
}
