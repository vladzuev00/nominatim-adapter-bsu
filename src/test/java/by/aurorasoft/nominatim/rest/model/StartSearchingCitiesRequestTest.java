package by.aurorasoft.nominatim.rest.model;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.crud.model.dto.AreaCoordinate;
import by.aurorasoft.nominatim.crud.model.dto.Coordinate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class StartSearchingCitiesRequestTest extends AbstractContextTest {

    @Autowired
    private Validator validator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void requestShouldBeValid() {
        final StartSearchingCitiesRequest givenRequest = StartSearchingCitiesRequest.builder()
                .bbox(new AreaCoordinate(new Coordinate(4.4, 5.5), new Coordinate(6.6, 7.7)))
                .searchStep(0.01)
                .build();
        final Set<ConstraintViolation<StartSearchingCitiesRequest>> constraintViolations
                = this.validator.validate(givenRequest);
        assertTrue(constraintViolations.isEmpty());
    }

    @Test
    public void requestShouldNotBeValidBecauseOfAreaCoordinateIsNull() {
        final StartSearchingCitiesRequest givenRequest = StartSearchingCitiesRequest.builder()
                .searchStep(0.01)
                .build();
        final Set<ConstraintViolation<StartSearchingCitiesRequest>> constraintViolations
                = this.validator.validate(givenRequest);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void requestShouldNotBeValidBecauseOfAreaCoordinateIsNotValid() {
        final StartSearchingCitiesRequest givenRequest = StartSearchingCitiesRequest.builder()
                .bbox(new AreaCoordinate(new Coordinate(null, 5.5), new Coordinate(6.6, 7.7)))
                .searchStep(0.01)
                .build();
        final Set<ConstraintViolation<StartSearchingCitiesRequest>> constraintViolations
                = this.validator.validate(givenRequest);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void requestShouldNotBeValidBecauseOfSearchStepIsNull() {
        final StartSearchingCitiesRequest givenRequest = StartSearchingCitiesRequest.builder()
                .bbox(new AreaCoordinate(new Coordinate(4.4, 5.5), new Coordinate(6.6, 7.7)))
                .build();
        final Set<ConstraintViolation<StartSearchingCitiesRequest>> constraintViolations
                = this.validator.validate(givenRequest);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void requestShouldNotBeValidBecauseOfSearchStepIsLessThenMinimalAllowableValue() {
        final StartSearchingCitiesRequest givenRequest = StartSearchingCitiesRequest.builder()
                .bbox(new AreaCoordinate(new Coordinate(4.4, 5.5), new Coordinate(6.6, 7.7)))
                .searchStep(0.001)
                .build();
        final Set<ConstraintViolation<StartSearchingCitiesRequest>> constraintViolations
                = this.validator.validate(givenRequest);
        assertEquals(1, constraintViolations.size());
        assertEquals(
                "должно быть больше, чем или равно 0.01",
                constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void requestShouldNotBeValidBecauseOfSearchStepIsMoreThenMaximalAllowableValue() {
        final StartSearchingCitiesRequest givenRequest = StartSearchingCitiesRequest.builder()
                .bbox(new AreaCoordinate(new Coordinate(4.4, 5.5), new Coordinate(6.6, 7.7)))
                .searchStep(5.1)
                .build();
        final Set<ConstraintViolation<StartSearchingCitiesRequest>> constraintViolations
                = this.validator.validate(givenRequest);
        assertEquals(1, constraintViolations.size());
        assertEquals(
                "должно быть меньше, чем или равно 5",
                constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void jsonShouldBeConvertedToRequest()
            throws JsonProcessingException {
        final String givenJson = "{"
                + "\"bbox\" : {"
                + "\"leftBottom\" : {"
                + "\"latitude\" : 52.959981,"
                + "\"longitude\" : 25.903515"
                + "},"
                + "\"rightUpper\" : {"
                + "\"latitude\" : 54.998760,"
                + "\"longitude\" : 27.992997"
                + "}"
                + "},"
                + "\"searchStep\": 0.1"
                + "}";
        final StartSearchingCitiesRequest actual = this.objectMapper.readValue(
                givenJson, StartSearchingCitiesRequest.class);
        final StartSearchingCitiesRequest expected = StartSearchingCitiesRequest.builder()
                .bbox(new AreaCoordinate(
                        new Coordinate(52.959981, 25.903515),
                        new Coordinate(54.998760, 27.992997)))
                .searchStep(0.1)
                .build();
        assertEquals(expected, actual);
    }

    @Test
    public void requestShouldBeConvertedToJson()
            throws JsonProcessingException {
        final StartSearchingCitiesRequest givenRequest = StartSearchingCitiesRequest.builder()
                .bbox(new AreaCoordinate(
                        new Coordinate(52.959981, 25.903515),
                        new Coordinate(54.998760, 27.992997)))
                .searchStep(0.1)
                .build();
        final String actual = this.objectMapper.writeValueAsString(givenRequest);
        final String expected = "{"
                + "\"bbox\":{"
                + "\"leftBottom\":{"
                + "\"latitude\":52.959981,"
                + "\"longitude\":25.903515"
                + "},"
                + "\"rightUpper\":{"
                + "\"latitude\":54.99876,"
                + "\"longitude\":27.992997"
                + "}"
                + "},"
                + "\"searchStep\":0.1"
                + "}";
        assertEquals(expected, actual);
    }
}
