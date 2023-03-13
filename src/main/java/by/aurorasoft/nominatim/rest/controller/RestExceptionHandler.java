package by.aurorasoft.nominatim.rest.controller;

import by.aurorasoft.nominatim.rest.controller.exception.CustomValidationException;
import by.aurorasoft.nominatim.rest.controller.exception.NoSuchEntityException;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Value;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public final class RestExceptionHandler {
    private static final String SEPARATOR_FIELD_NAME_AND_MESSAGE_IN_ERROR = " : ";
    private static final String MESSAGE_UNKNOWN_ERROR = "unknown error";

    /**
     * first %s - inbound param
     * second %s - list of allowable param's values
     */
    private static final String MESSAGE_TEMPLATE_NOT_VALID_ENUM_PARAM = "'%s' should be replaced by one of: %s.";
    private static final String DELIMITER_ENUM_PARAM_ALLOWABLE_VALUE = ", ";

    @ExceptionHandler
    public ResponseEntity<RestErrorResponse> handleException(ConstraintViolationException exception) {
        return handleValidationException(exception.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<RestErrorResponse> handleException(MethodArgumentNotValidException exception) {
        return handleValidationException(findMessage(exception.getFieldError()));
    }

    @ExceptionHandler
    public ResponseEntity<RestErrorResponse> handleException(CustomValidationException exception) {
        return handleValidationException(exception.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<RestErrorResponse> handleException(NoSuchEntityException exception) {
        return handleNotFoundException(exception.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<RestErrorResponse> handleException(ConversionFailedException exception) {
        return handleValidationException(findMessage(exception));
    }

    private static ResponseEntity<RestErrorResponse> handleNotFoundException(String message) {
        return handleException(NOT_FOUND, message);
    }

    private static ResponseEntity<RestErrorResponse> handleValidationException(String message) {
        return handleException(NOT_ACCEPTABLE, message);
    }

    private static ResponseEntity<RestErrorResponse> handleException(HttpStatus httpStatus, String message) {
        final RestErrorResponse restErrorResponse = new RestErrorResponse(httpStatus, message, now());
        return new ResponseEntity<>(restErrorResponse, httpStatus);
    }

    private static String findMessage(FieldError fieldError) {
        return fieldError != null
                ? fieldError.getField() + SEPARATOR_FIELD_NAME_AND_MESSAGE_IN_ERROR + fieldError.getDefaultMessage()
                : MESSAGE_UNKNOWN_ERROR;
    }

    @SuppressWarnings("unchecked")
    private static String findMessage(ConversionFailedException exception) {
        final TypeDescriptor targetTypeDescriptor = exception.getTargetType();
        final Class<?> targetType = targetTypeDescriptor.getType();
        return targetType.isEnum()
                ? findMessageOfEnumParamConversionFailedException(
                exception.getValue(), (Class<? extends Enum<?>>) targetType)
                : exception.getMessage();
    }

    private static String findMessageOfEnumParamConversionFailedException(Object inboundValue,
                                                                          Class<? extends Enum<?>> enumType) {
        return format(MESSAGE_TEMPLATE_NOT_VALID_ENUM_PARAM, inboundValue, findSeparatedAllowableValues(enumType));
    }

    private static String findSeparatedAllowableValues(Class<? extends Enum<?>> enumType) {
        return stream(enumType.getEnumConstants())
                .map(Enum::name)
                .collect(joining(DELIMITER_ENUM_PARAM_ALLOWABLE_VALUE));
    }

    @Value
    private static class RestErrorResponse {
        HttpStatus httpStatus;
        String message;

        @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd HH-mm-ss")
        LocalDateTime dateTime;
    }
}
