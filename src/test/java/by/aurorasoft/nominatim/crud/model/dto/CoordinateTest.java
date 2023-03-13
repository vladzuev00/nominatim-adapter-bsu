package by.aurorasoft.nominatim.crud.model.dto;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class CoordinateTest extends AbstractContextTest {

    @Autowired
    private Validator validator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void coordinateShouldBeValid() {
        final Coordinate givenCoordinate = new Coordinate(90., 180.);
        final Set<ConstraintViolation<Coordinate>> constraintViolations = this.validator.validate(givenCoordinate);
        assertTrue(constraintViolations.isEmpty());
    }

    @Test
    public void coordinateShouldNotBeValidBecauseOfLatitudeIsNull() {
        final Coordinate givenCoordinate = new Coordinate(null, 180.);
        final Set<ConstraintViolation<Coordinate>> constraintViolations = this.validator.validate(givenCoordinate);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void coordinateShouldNotBeValidBecauseOfLatitudeMoreThanMaxAllowableValue() {
        final Coordinate givenCoordinate = new Coordinate(91., 180.);
        final Set<ConstraintViolation<Coordinate>> constraintViolations = this.validator.validate(givenCoordinate);
        assertEquals(1, constraintViolations.size());
        assertEquals(
                "должно быть меньше, чем или равно 90",
                constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void coordinateShouldNotBeValidBecauseOfLatitudeLessThanMinAllowableValue() {
        final Coordinate givenCoordinate = new Coordinate(-91., 180.);
        final Set<ConstraintViolation<Coordinate>> constraintViolations = this.validator.validate(givenCoordinate);
        assertEquals(1, constraintViolations.size());
        assertEquals(
                "должно быть больше, чем или равно -90",
                constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void coordinateShouldNotBeValidBecauseOfLongitudeIsNull() {
        final Coordinate givenCoordinate = new Coordinate(90., null);
        final Set<ConstraintViolation<Coordinate>> constraintViolations = this.validator.validate(givenCoordinate);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void coordinateShouldNotBeValidBecauseOfLongitudeMoreThanMaxAllowableValue() {
        final Coordinate givenCoordinate = new Coordinate(90., 181.);
        final Set<ConstraintViolation<Coordinate>> constraintViolations = this.validator.validate(givenCoordinate);
        assertEquals(1, constraintViolations.size());
        assertEquals(
                "должно быть меньше, чем или равно 180",
                constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void coordinateShouldNotBeValidBecauseOfLongitudeLessThanMinAllowableValue() {
        final Coordinate givenCoordinate = new Coordinate(90., -181.);
        final Set<ConstraintViolation<Coordinate>> constraintViolations = this.validator.validate(givenCoordinate);
        assertEquals(1, constraintViolations.size());
        assertEquals(
                "должно быть больше, чем или равно -180",
                constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void jsonShouldBeConvertedToCoordinate()
            throws IOException {
        final String givenJson = "{"
                + "\"latitude\" : 4.4,"
                + "\"longitude\" : 5.5"
                + "}";
        final Coordinate actual = this.objectMapper.readValue(givenJson, Coordinate.class);
        final Coordinate expected = new Coordinate(4.4, 5.5);
        assertEquals(expected, actual);
    }

    @Test
    public void coordinateShouldBeConvertedToJson()
            throws JsonProcessingException {
        final Coordinate givenCoordinate = new Coordinate(4.4, 5.5);
        final String actual = this.objectMapper.writeValueAsString(givenCoordinate);
        final String expected = "{"
                + "\"latitude\":4.4,"
                + "\"longitude\":5.5"
                + "}";
        assertEquals(expected, actual);
    }
}
