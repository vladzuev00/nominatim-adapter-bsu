package by.aurorasoft.nominatim.rest.controller.exception;

public final class NoSuchEntityException extends RuntimeException {
    public NoSuchEntityException() {

    }

    public NoSuchEntityException(String description) {
        super(description);
    }

    public NoSuchEntityException(Exception cause) {
        super(cause);
    }

    public NoSuchEntityException(String description, Exception cause) {
        super(description, cause);
    }
}
