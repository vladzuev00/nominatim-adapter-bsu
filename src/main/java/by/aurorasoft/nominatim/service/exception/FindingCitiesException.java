package by.aurorasoft.nominatim.service.exception;

public final class FindingCitiesException extends RuntimeException {
    public FindingCitiesException() {

    }

    public FindingCitiesException(String description) {
        super(description);
    }

    public FindingCitiesException(Throwable cause) {
        super(cause);
    }

    public FindingCitiesException(String description, Throwable cause) {
        super(description, cause);
    }
}
