package sp25.swp391.se1809.group4.bookstore.exceptions;

public class CategoryExceptionNotFound extends RuntimeException {
   public CategoryExceptionNotFound(){
   }

    public CategoryExceptionNotFound(String message) {
        super(message);
    }

    public CategoryExceptionNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public CategoryExceptionNotFound(Throwable cause) {
        super(cause);
    }

    public CategoryExceptionNotFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
