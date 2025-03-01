package sp25.swp391.se1809.group4.bookstore.exceptions;


public class AccountExceptionNotFound extends RuntimeException {
    public AccountExceptionNotFound() {
    }

    public AccountExceptionNotFound(String message) {
        super(message);
    }

    public AccountExceptionNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountExceptionNotFound(Throwable cause) {
        super(cause);
    }

    public AccountExceptionNotFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
