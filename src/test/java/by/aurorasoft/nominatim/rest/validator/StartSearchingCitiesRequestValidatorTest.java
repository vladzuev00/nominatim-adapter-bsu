package by.aurorasoft.nominatim.rest.validator;

import by.aurorasoft.nominatim.crud.model.dto.AreaCoordinate;
import by.aurorasoft.nominatim.crud.model.dto.Coordinate;
import by.aurorasoft.nominatim.rest.controller.exception.CustomValidationException;
import by.aurorasoft.nominatim.rest.model.StartSearchingCitiesRequest;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public final class StartSearchingCitiesRequestValidatorTest {
    private final StartSearchingCitiesRequestValidator validator;

    public StartSearchingCitiesRequestValidatorTest() {
        this.validator = new StartSearchingCitiesRequestValidator();
    }

    @Test
    public void validationShouldBeSuccessful() {
        final StartSearchingCitiesRequest givenRequest = StartSearchingCitiesRequest.builder()
                .bbox(
                        new AreaCoordinate(
                                new Coordinate(52.959981, 25.903515),
                                new Coordinate(52.959982, 25.903516)
                        ))
                .searchStep(0.01)
                .build();
        this.validator.validate(givenRequest);
    }

    @Test
    public void validationShouldNotBeSuccessfulBecauseOfNotValidAreaCoordinate() {
        final StartSearchingCitiesRequest givenRequest = StartSearchingCitiesRequest.builder()
                .bbox(
                        new AreaCoordinate(
                                new Coordinate(52.959981, 25.903515),
                                new Coordinate(52.959980, 25.903516)
                        ))
                .searchStep(0.01)
                .build();

        CustomValidationException expectedException = null;
        try {
            this.validator.validate(givenRequest);
        } catch (final CustomValidationException exception) {
            expectedException = exception;
        }

        assertNotNull(expectedException);
        assertNotNull(expectedException.getMessage());
    }
}
