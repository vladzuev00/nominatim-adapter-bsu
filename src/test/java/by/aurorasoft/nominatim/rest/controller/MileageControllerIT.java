package by.aurorasoft.nominatim.rest.controller;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class MileageControllerIT extends AbstractContextTest {
    private static final String CONTROLLER_URL = "/api/v1/mileage";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Transactional(propagation = NOT_SUPPORTED)
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))', 4326), "
            + "'CAPITAL', "
            + "ST_GeomFromText('POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(257, 'Second', "
            + "ST_GeomFromText('POLYGON((3 3, 4 3, 4 4, 3 3))', 4326), "
            + "'REGIONAL', "
            + "ST_GeomFromText('POLYGON((3 3, 4 3, 4 4, 3 4, 3 3))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(258, 'Third', "
            + "ST_GeomFromText('POLYGON((3 1, 3 2, 4 2, 4 1, 3 1))', 4326), "
            + "'REGIONAL', "
            + "ST_GeomFromText('POLYGON((3 3, 4 3, 4 4, 3 3))', 4326)"
            + ")")
    @Sql(statements = "DELETE FROM city", executionPhase = AFTER_TEST_METHOD)
    public void mileageShouldBeFound() {
        final String givenJson = "{\"trackPoints\":[{\"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "\"latitude\":1.5,\"longitude\":1.5,\"altitude\":15,\"speed\":20,\"valid\":true},"
                + "{\"datetime\":\"2023-02-15T10:23:00.576033Z\",\"latitude\":3.5,\"longitude\":3.5,"
                + "\"altitude\":15,\"speed\":20,\"valid\":true},{\"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "\"latitude\":4.5,\"longitude\":4.5,\"altitude\":15,\"speed\":20,\"valid\":true}],"
                + "\"minDetectionSpeed\":1,\"maxMessageTimeout\":15}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expected = "{\"urban\":0.0,\"country\":471.41002413306734}";
        assertEquals(expected, actual);
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointDateTimeIsNull() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"latitude\":1.5,"
                + "         \"longitude\":1.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints\\[0]\\.datetime : не должно равняться null\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointDateTimeIsFuture() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"3000-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.5,"
                + "         \"longitude\":1.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints\\[0]\\.datetime : должно содержать прошедшую дату или сегодняшнее число\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointLatitudeIsNull() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"longitude\":1.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints\\[0]\\.latitude : не должно равняться null\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointLatitudeIsLessThanMinimalAllowable() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":-90.1,"
                + "         \"longitude\":1.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints\\[0]\\.latitude : должно быть больше, чем или равно -90\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointLatitudeIsBiggerThanMaximalAllowable() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":90.1,"
                + "         \"longitude\":1.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints\\[0]\\.latitude : должно быть меньше, чем или равно 90\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointLongitudeIsNull() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints\\[0]\\.longitude : не должно равняться null\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointLongitudeIsLessThanMinimalAllowable() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.1,"
                + "         \"longitude\":-180.1,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints\\[0]\\.longitude : должно быть больше, чем или равно -180\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointLongitudeIsBiggerThanMaximalAllowable() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.1,"
                + "         \"longitude\":180.1,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints\\[0]\\.longitude : должно быть меньше, чем или равно 180\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointAltitudeIsNull() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.1,"
                + "         \"longitude\":1.1,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints\\[0]\\.altitude : не должно равняться null\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointSpeedIsNull() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.1,"
                + "         \"longitude\":1.1,"
                + "         \"altitude\":15,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints\\[0]\\.speed : не должно равняться null\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointSpeedIsLessThanMinimalAllowable() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.1,"
                + "         \"longitude\":1.1,"
                + "         \"altitude\":15,"
                + "         \"speed\":-1,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints\\[0]\\.speed : должно быть не меньше 0\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointSpeedIsBiggerThanMaximalAllowable() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.1,"
                + "         \"longitude\":1.1,"
                + "         \"altitude\":15,"
                + "         \"speed\":1001,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints\\[0]\\.speed : должно быть не больше 1000\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointValidIsNull() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.1,"
                + "         \"longitude\":1.1,"
                + "         \"altitude\":15,"
                + "         \"speed\":50"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints\\[0]\\.valid : не должно равняться null\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfTrackPointsIsNull() {
        final String givenJson = "{\"minDetectionSpeed\":1,\"maxMessageTimeout\":15}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints : не должно равняться null\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfAmountTrackPointsIsLessThanMinimalAllowable() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.1,"
                + "         \"longitude\":1.1,"
                + "         \"altitude\":15,"
                + "         \"speed\":50,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"trackPoints : размер должен находиться в диапазоне от 2 до 2147483647\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfMinDetectionSpeedIsNull() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.1,"
                + "         \"longitude\":1.1,"
                + "         \"altitude\":15,"
                + "         \"speed\":50,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"minDetectionSpeed : не должно равняться null\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfMinDetectionSpeedIsLessThanMinimalAllowable() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.1,"
                + "         \"longitude\":1.1,"
                + "         \"altitude\":15,"
                + "         \"speed\":50,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":-1,"
                + "   \"maxMessageTimeout\":15"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"minDetectionSpeed : должно быть не меньше 0\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfMaxMessageTimeoutIsNull() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.1,"
                + "         \"longitude\":1.1,"
                + "         \"altitude\":15,"
                + "         \"speed\":50,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":10"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"maxMessageTimeout : не должно равняться null\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void mileageShouldNotBeFoundBecauseOfMaxMessageTimeoutIsLessThanMinimalAllowable() {
        final String givenJson = "{"
                + "   \"trackPoints\":["
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":1.1,"
                + "         \"longitude\":1.1,"
                + "         \"altitude\":15,"
                + "         \"speed\":50,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":3.5,"
                + "         \"longitude\":3.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      },"
                + "      {"
                + "         \"datetime\":\"2023-02-15T10:23:00.576033Z\","
                + "         \"latitude\":4.5,"
                + "         \"longitude\":4.5,"
                + "         \"altitude\":15,"
                + "         \"speed\":20,"
                + "         \"valid\":true"
                + "      }"
                + "   ],"
                + "   \"minDetectionSpeed\":10,"
                + "   \"maxMessageTimeout\":-1"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String actual = this.restTemplate.postForObject(CONTROLLER_URL, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"maxMessageTimeout : должно быть не меньше 0\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertTrue(actual.matches(expectedRegex));
    }
}
