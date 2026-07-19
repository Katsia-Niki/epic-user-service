package by.nikiforova.userservice.exception;

public class CardLimitExceededException extends RuntimeException{
    public CardLimitExceededException() {
        super();
    }

    public CardLimitExceededException(String message) {
        super(message);
    }

    public CardLimitExceededException(String message, Throwable cause) {
        super(message,  cause);
    }
}
