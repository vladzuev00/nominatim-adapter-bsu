package by.aurorasoft.nominatim.rest.controller.exception;

public final class CustomValidationException extends RuntimeException {
    public CustomValidationException() {

    }

    public CustomValidationException(String description) {
        super(description);
    }

    public CustomValidationException(Exception cause) {
        super(cause);
    }

    public CustomValidationException(String description, Exception cause) {
        super(description, cause);
    }
}
