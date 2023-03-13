package by.aurorasoft.nominatim.crud.model.dto;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class AreaCoordinateTest extends AbstractContextTest {

    @Autowired
    private Validator validator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void areaCoordinateShouldBeValid() {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                new Coordinate(4.4, 5.5),
                new Coordinate(6.6, 7.7));
        final Set<ConstraintViolation<AreaCoordinate>> constraintViolations
                = this.validator.validate(givenAreaCoordinate);
        assertTrue(constraintViolations.isEmpty());
    }

    @Test
    public void areaCoordinateShouldNotBeValidBecauseOfLeftBottomIsNull() {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                null,
                new Coordinate(6.6, 7.7));
        final Set<ConstraintViolation<AreaCoordinate>> constraintViolations
                = this.validator.validate(givenAreaCoordinate);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void areaCoordinateShouldNotBeValidBecauseOfLeftBottomIsNotValid() {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                new Coordinate(4.4, null),
                new Coordinate(6.6, 7.7));
        final Set<ConstraintViolation<AreaCoordinate>> constraintViolations
                = this.validator.validate(givenAreaCoordinate);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void areaCoordinateShouldNotBeValidBecauseOfRightUpperIsNull() {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                new Coordinate(4.4, 5.5),
                null);
        final Set<ConstraintViolation<AreaCoordinate>> constraintViolations
                = this.validator.validate(givenAreaCoordinate);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void areaCoordinateShouldNotBeValidBecauseOfRightUpperIsNotValid() {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                new Coordinate(4.4, 5.5),
                new Coordinate(6.6, null));
        final Set<ConstraintViolation<AreaCoordinate>> constraintViolations
                = this.validator.validate(givenAreaCoordinate);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void jsonShouldBeConvertedToAreaCoordinate()
            throws JsonProcessingException {
        final String givenJson = "{"
                + "\"leftBottom\" : {"
                + "\"latitude\" : 52.959981,"
                + "\"longitude\" : 25.903515"
                + "},"
                + "\"rightUpper\" : {"
                + "\"latitude\" : 52.998760,"
                + "\"longitude\" : 25.992997"
                + "}"
                + "}";
        final AreaCoordinate actual = this.objectMapper.readValue(givenJson, AreaCoordinate.class);
        final AreaCoordinate expected = new AreaCoordinate(
                new Coordinate(52.959981, 25.903515),
                new Coordinate(52.998760, 25.992997)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void areaCoordinateShouldBeConvertedToJson()
            throws JsonProcessingException {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                new Coordinate(52.959981, 25.903515),
                new Coordinate(52.998760, 25.992997));
        final String actual = this.objectMapper.writeValueAsString(givenAreaCoordinate);
        final String expected = "{"
                + "\"leftBottom\":{"
                + "\"latitude\":52.959981,"
                + "\"longitude\":25.903515"
                + "},"
                + "\"rightUpper\":{"
                + "\"latitude\":52.99876,"
                + "\"longitude\":25.992997"
                + "}"
                + "}";
        assertEquals(expected, actual);
    }
}
