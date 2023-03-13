package by.aurorasoft.nominatim.rest.model;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.rest.model.MileageRequest.TrackPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static java.time.Instant.now;
import static java.time.Instant.parse;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class MileageRequestTest extends AbstractContextTest {

    @Autowired
    private Validator validator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void trackPointShouldBeValid() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(now())
                .latitude(45F)
                .longitude(46F)
                .altitude(15)
                .speed(500)
                .valid(true)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertTrue(constraintViolations.isEmpty());
    }

    @Test
    public void trackPointShouldNotBeValidBecauseOfDateTimeIsNull() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .latitude(45F)
                .longitude(46F)
                .altitude(15)
                .speed(500)
                .valid(true)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void trackPointShouldNotBeValidBecauseOfDateTimeIsFuture() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(now().plus(10, SECONDS))
                .latitude(45F)
                .longitude(46F)
                .altitude(15)
                .speed(500)
                .valid(true)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertEquals(1, constraintViolations.size());
        assertEquals(
                "должно содержать прошедшую дату или сегодняшнее число",
                constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void trackPointShouldNotBeValidBecauseOfLatitudeIsNull() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(now())
                .longitude(46F)
                .altitude(15)
                .speed(500)
                .valid(true)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void trackPointShouldNotBeValidBecauseOfLatitudeIsLessThanMinimalAllowable() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(now())
                .latitude(-90.1F)
                .longitude(46F)
                .altitude(15)
                .speed(500)
                .valid(true)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertEquals(1, constraintViolations.size());
        assertEquals(
                "должно быть больше, чем или равно -90",
                constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void trackPointShouldNotBeValidBecauseOfLatitudeIsBiggerThanMaximalAllowable() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(now())
                .latitude(90.1F)
                .longitude(46F)
                .altitude(15)
                .speed(500)
                .valid(true)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertEquals(1, constraintViolations.size());
        assertEquals(
                "должно быть меньше, чем или равно 90",
                constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void trackPointShouldNotBeValidBecauseOfLongitudeIsNull() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(now())
                .latitude(45F)
                .altitude(15)
                .speed(500)
                .valid(true)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void trackPointShouldNotBeValidBecauseOfLongitudeIsLessThanMinimalAllowable() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(now())
                .latitude(45F)
                .longitude(-180.1F)
                .altitude(15)
                .speed(500)
                .valid(true)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertEquals(1, constraintViolations.size());
        assertEquals("должно быть больше, чем или равно -180", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void trackPointShouldNotBeValidBecauseOfLongitudeIsMoreThanMaximalAllowalbe() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(now())
                .latitude(45F)
                .longitude(180.1F)
                .altitude(15)
                .speed(500)
                .valid(true)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertEquals(1, constraintViolations.size());
        assertEquals("должно быть меньше, чем или равно 180", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void trackPointShouldNotBeValidBecauseOfAltitudeIsNull() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(now())
                .latitude(45F)
                .longitude(45F)
                .speed(500)
                .valid(true)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void trackPointShouldNotBeValidBecauseOfSpeedIsNull() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(now())
                .latitude(45F)
                .longitude(46F)
                .altitude(15)
                .valid(true)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void trackPointShouldNotBeValidBecauseOfSpeedIsLessThanMinimalAllowable() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(now())
                .latitude(45F)
                .longitude(46F)
                .altitude(15)
                .speed(-1)
                .valid(true)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertEquals(1, constraintViolations.size());
        assertEquals("должно быть не меньше 0", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void trackPointShouldNotBeValidBecauseOfSpeedIsMoreThanMaximalAllowable() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(now())
                .latitude(45F)
                .longitude(46F)
                .altitude(15)
                .speed(1001)
                .valid(true)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertEquals(1, constraintViolations.size());
        assertEquals("должно быть не больше 1000", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void trackPointShouldNotBeValidBecauseOfValidIsNull() {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(now())
                .latitude(45F)
                .longitude(46F)
                .altitude(15)
                .speed(500)
                .build();

        final Set<ConstraintViolation<TrackPoint>> constraintViolations = this.validator.validate(givenTrackPoint);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void trackPointShouldBeConvertedToJson()
            throws Exception {
        final TrackPoint givenTrackPoint = TrackPoint.builder()
                .datetime(parse("2007-12-03T10:15:30Z"))
                .latitude(45F)
                .longitude(46F)
                .altitude(15)
                .speed(500)
                .valid(true)
                .build();

        final String actual = this.objectMapper.writeValueAsString(givenTrackPoint);
        final String expectedRegex = "\\{\"datetime\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z\","
                + "\"latitude\":45\\.0,"
                + "\"longitude\":46\\.0,"
                + "\"altitude\":15,"
                + "\"speed\":500,"
                + "\"valid\":true}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void jsonShouldBeConvertedToTrackPoint()
            throws Exception {
        final String givenJson = "{\"datetime\":\"2023-02-14T12:28:04Z\","
                + "\"latitude\":45.0,"
                + "\"longitude\":46.0,"
                + "\"altitude\":15,"
                + "\"speed\":500,"
                + "\"valid\":true}";

        final TrackPoint actual = this.objectMapper.readValue(givenJson, TrackPoint.class);
        final TrackPoint expected = TrackPoint.builder()
                .datetime(parse("2023-02-14T12:28:04Z"))
                .latitude(45F)
                .longitude(46F)
                .altitude(15)
                .speed(500)
                .valid(true)
                .build();
        assertEquals(expected, actual);
    }

    @Test
    public void mileageRequestShouldBeValid() {
        final MileageRequest givenMileageRequest = MileageRequest.builder()
                .trackPoints(List.of(
                        TrackPoint.builder()
                                .datetime(parse("2023-02-14T12:28:04Z"))
                                .latitude(45F)
                                .longitude(46F)
                                .altitude(15)
                                .speed(500)
                                .valid(true)
                                .build(),
                        TrackPoint.builder()
                                .datetime(parse("2023-02-14T12:28:05Z"))
                                .latitude(45.001F)
                                .longitude(46F)
                                .altitude(15)
                                .speed(500)
                                .valid(true)
                                .build()
                ))
                .minDetectionSpeed(10)
                .maxMessageTimeout(11)
                .build();

        final Set<ConstraintViolation<MileageRequest>> constraintViolations = this.validator.validate(
                givenMileageRequest);
        assertTrue(constraintViolations.isEmpty());
    }

    @Test
    public void mileageRequestShouldNotBeValidBecauseOfTrackPointsIsNull() {
        final MileageRequest givenMileageRequest = MileageRequest.builder()
                .minDetectionSpeed(10)
                .maxMessageTimeout(11)
                .build();

        final Set<ConstraintViolation<MileageRequest>> constraintViolations = this.validator.validate(
                givenMileageRequest);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void mileageRequestShouldNotBeValidBecauseOfAmountOfTrackPointsIsLessThanMinimalAllowable() {
        final MileageRequest givenMileageRequest = MileageRequest.builder()
                .trackPoints(List.of(
                        TrackPoint.builder()
                                .datetime(parse("2023-02-14T12:28:04Z"))
                                .latitude(45F)
                                .longitude(46F)
                                .altitude(15)
                                .speed(500)
                                .valid(true)
                                .build()
                ))
                .minDetectionSpeed(10)
                .maxMessageTimeout(11)
                .build();

        final Set<ConstraintViolation<MileageRequest>> constraintViolations = this.validator.validate(
                givenMileageRequest);
        assertEquals(1, constraintViolations.size());
        assertEquals("размер должен находиться в диапазоне от 2 до 2147483647",
                constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void mileageRequestShouldNotBeValidBecauseOfMinDetectionSpeedIsNull() {
        final MileageRequest givenMileageRequest = MileageRequest.builder()
                .trackPoints(List.of(
                        TrackPoint.builder()
                                .datetime(parse("2023-02-14T12:28:04Z"))
                                .latitude(45F)
                                .longitude(46F)
                                .altitude(15)
                                .speed(500)
                                .valid(true)
                                .build(),
                        TrackPoint.builder()
                                .datetime(parse("2023-02-14T12:28:05Z"))
                                .latitude(45.001F)
                                .longitude(46F)
                                .altitude(15)
                                .speed(500)
                                .valid(true)
                                .build()
                ))
                .maxMessageTimeout(11)
                .build();

        final Set<ConstraintViolation<MileageRequest>> constraintViolations = this.validator.validate(
                givenMileageRequest);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void mileageRequestShouldNotBeValidBecauseOfMinDetectionSpeedIsLessThanMinimalAllowable() {
        final MileageRequest givenMileageRequest = MileageRequest.builder()
                .trackPoints(List.of(
                        TrackPoint.builder()
                                .datetime(parse("2023-02-14T12:28:04Z"))
                                .latitude(45F)
                                .longitude(46F)
                                .altitude(15)
                                .speed(500)
                                .valid(true)
                                .build(),
                        TrackPoint.builder()
                                .datetime(parse("2023-02-14T12:28:05Z"))
                                .latitude(45.001F)
                                .longitude(46F)
                                .altitude(15)
                                .speed(500)
                                .valid(true)
                                .build()
                ))
                .minDetectionSpeed(-1)
                .maxMessageTimeout(11)
                .build();

        final Set<ConstraintViolation<MileageRequest>> constraintViolations = this.validator.validate(
                givenMileageRequest);
        assertEquals(1, constraintViolations.size());
        assertEquals("должно быть не меньше 0", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void mileageRequestShouldNotBeValidBecauseOfMaxMessageTimeoutIsNull() {
        final MileageRequest givenMileageRequest = MileageRequest.builder()
                .trackPoints(List.of(
                        TrackPoint.builder()
                                .datetime(parse("2023-02-14T12:28:04Z"))
                                .latitude(45F)
                                .longitude(46F)
                                .altitude(15)
                                .speed(500)
                                .valid(true)
                                .build(),
                        TrackPoint.builder()
                                .datetime(parse("2023-02-14T12:28:05Z"))
                                .latitude(45.001F)
                                .longitude(46F)
                                .altitude(15)
                                .speed(500)
                                .valid(true)
                                .build()
                ))
                .minDetectionSpeed(10)
                .build();

        final Set<ConstraintViolation<MileageRequest>> constraintViolations = this.validator.validate(
                givenMileageRequest);
        assertEquals(1, constraintViolations.size());
        assertEquals("не должно равняться null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void mileageRequestShouldNotBeValidBecauseOfMaxMessageTimeoutIsLessThanMinimalAllowable() {
        final MileageRequest givenMileageRequest = MileageRequest.builder()
                .trackPoints(List.of(
                        TrackPoint.builder()
                                .datetime(parse("2023-02-14T12:28:04Z"))
                                .latitude(45F)
                                .longitude(46F)
                                .altitude(15)
                                .speed(500)
                                .valid(true)
                                .build(),
                        TrackPoint.builder()
                                .datetime(parse("2023-02-14T12:28:05Z"))
                                .latitude(45.001F)
                                .longitude(46F)
                                .altitude(15)
                                .speed(500)
                                .valid(true)
                                .build()
                ))
                .minDetectionSpeed(10)
                .maxMessageTimeout(-1)
                .build();

        final Set<ConstraintViolation<MileageRequest>> constraintViolations = this.validator.validate(
                givenMileageRequest);
        assertEquals(1, constraintViolations.size());
        assertEquals("должно быть не меньше 0", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void mileageRequestShouldBeConvertedToJson()
            throws Exception {
        final MileageRequest givenMileageRequest = MileageRequest.builder()
                .trackPoints(List.of(
                        TrackPoint.builder()
                                .datetime(parse("2007-12-03T10:15:30Z"))
                                .latitude(53F)
                                .longitude(20F)
                                .altitude(10)
                                .speed(15)
                                .valid(true)
                                .build(),
                        TrackPoint.builder()
                                .datetime(parse("2007-12-03T10:15:31Z"))
                                .latitude(53.001F)
                                .longitude(20.001F)
                                .altitude(10)
                                .speed(15)
                                .valid(true)
                                .build()
                ))
                .minDetectionSpeed(10)
                .maxMessageTimeout(11)
                .build();

        final String actual = this.objectMapper.writeValueAsString(givenMileageRequest);
        final String expected = "{\"trackPoints\":[{\"datetime\":\"2007-12-03T10:15:30Z\",\"latitude\":53.0,"
                + "\"longitude\":20.0,\"altitude\":10,\"speed\":15,\"valid\":true},"
                + "{\"datetime\":\"2007-12-03T10:15:31Z\",\"latitude\":53.001,\"longitude\":20.001,\"altitude\":10,"
                + "\"speed\":15,\"valid\":true}],"
                + "\"minDetectionSpeed\":10,\"maxMessageTimeout\":11}";
        assertEquals(expected, actual);
    }

    @Test
    public void jsonShouldBeConvertedToMileageRequest()
            throws Exception {
        final String givenJson = "{\"trackPoints\":[{\"datetime\":\"2007-12-03T10:15:30Z\",\"latitude\":53.0,"
                + "\"longitude\":20.0,\"altitude\":10,\"speed\":15,\"valid\":true},"
                + "{\"datetime\":\"2007-12-03T10:15:31Z\",\"latitude\":53.001,\"longitude\":20.001,\"altitude\":10,"
                + "\"speed\":15,\"valid\":true}],"
                + "\"minDetectionSpeed\":10,\"maxMessageTimeout\":11}";

        final MileageRequest actual = this.objectMapper.readValue(givenJson, MileageRequest.class);
        final MileageRequest expected = MileageRequest.builder()
                .trackPoints(List.of(
                        TrackPoint.builder()
                                .datetime(parse("2007-12-03T10:15:30Z"))
                                .latitude(53F)
                                .longitude(20F)
                                .altitude(10)
                                .speed(15)
                                .valid(true)
                                .build(),
                        TrackPoint.builder()
                                .datetime(parse("2007-12-03T10:15:31Z"))
                                .latitude(53.001F)
                                .longitude(20.001F)
                                .altitude(10)
                                .speed(15)
                                .valid(true)
                                .build()
                ))
                .minDetectionSpeed(10)
                .maxMessageTimeout(11)
                .build();
        assertEquals(expected, actual);
    }
}
