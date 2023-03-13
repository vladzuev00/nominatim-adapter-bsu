package by.aurorasoft.nominatim.rest.controller;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.Integer.MIN_VALUE;
import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CityControllerIT extends AbstractContextTest {
    private static final String CONTROLLER_URL = "/api/v1/city";
    private static final String SLASH = "/";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Transactional(propagation = NOT_SUPPORTED)
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Minsk', "
            + "ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), "
            + "'CAPITAL', "
            + "ST_Envelope(ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326))"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(256, 'Mogilev', "
            + "ST_GeomFromText('POLYGON((1 2, 3 5, 5 6, 6 7, 1 2))', 4326), "
            + "'REGIONAL', "
            + "ST_Envelope(ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326))"
            + ")")
    @Sql(statements = "DELETE FROM city", executionPhase = AFTER_TEST_METHOD)
    public void allCitiesShouldBeFound() {
        final int givenPageNumber = 0;
        final int givenPageSize = 3;

        final String url = createUrlToFindAllCities(givenPageNumber, givenPageSize);
        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);

        assertSame(OK, responseEntity.getStatusCode());

        final String actual = responseEntity.getBody();
        final String expected = "{\"pageNumber\":0,\"pageSize\":3,"
                + "\"cities\":[{\"id\":255,\"name\":\"Minsk\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\""
                + ":[[[1.0,2.0],[3.0,4.0],[5.0,6.0],[6.0,7.0],[1.0,2.0]]]},\"type\":\"CAPITAL\"},{\"id\":256,\"name\""
                + ":\"Mogilev\",\"geometry\":{\"type\":\"Polygon\","
                + "\"coordinates\":[[[1.0,2.0],[3.0,5.0],[5.0,6.0],[6.0,7.0],[1.0,2.0]]]},\"type\":\"REGIONAL\"}]}";
        assertEquals(expected, actual);
    }

    @Test
    public void allCitiesShouldNotBeFoundBecauseOfPageNumberIsLessThanAllowableMinimal() {
        final int givenPageNumber = -1;
        final int givenPageSize = 3;

        final String url = createUrlToFindAllCities(givenPageNumber, givenPageSize);
        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);

        assertSame(NOT_ACCEPTABLE, responseEntity.getStatusCode());

        final String actual = responseEntity.getBody();
        final String expectedRegex = "\\{"
                + "\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"findAll.pageNumber: должно быть не меньше 0\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\""
                + "}";
        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void allCitiesShouldNotBeFoundBecauseOfPageNumberIsMoreThanAllowableMaximal() {
        final int givenPageNumber = 3000001;
        final int givenPageSize = 3;

        final String url = createUrlToFindAllCities(givenPageNumber, givenPageSize);
        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);

        assertSame(NOT_ACCEPTABLE, responseEntity.getStatusCode());

        final String actual = responseEntity.getBody();
        final String expectedRegex = "\\{"
                + "\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"findAll.pageNumber: должно быть не больше 10000\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\""
                + "}";
        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void allCitiesShouldNotBeFoundBecauseOfPageSizeIsLessThanAllowableMinimal() {
        final int givenPageNumber = 0;
        final int givenPageSize = 0;

        final String url = createUrlToFindAllCities(givenPageNumber, givenPageSize);
        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);

        assertSame(NOT_ACCEPTABLE, responseEntity.getStatusCode());

        final String actual = responseEntity.getBody();
        final String expectedRegex = "\\{"
                + "\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"findAll.pageSize: должно быть не меньше 1\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\""
                + "}";
        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void allCitiesShouldNotBeFoundBecauseOfPageSizeIsMoreThanAllowableMaximal() {
        final int givenPageNumber = 0;
        final int givenPageSize = 3000001;

        final String url = createUrlToFindAllCities(givenPageNumber, givenPageSize);
        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);

        assertSame(NOT_ACCEPTABLE, responseEntity.getStatusCode());

        final String actual = responseEntity.getBody();
        final String expectedRegex = "\\{"
                + "\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"findAll.pageSize: должно быть не больше 10000\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\""
                + "}";
        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    @Transactional(propagation = NOT_SUPPORTED)
    @Sql(statements = "DELETE FROM city", executionPhase = AFTER_TEST_METHOD)
    public void cityShouldBeSaved() {
        final String givenJson = "{\"name\":\"Minsk\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\""
                + ":[[[1.0,2.0],[3.0,4.0],[5.0,6.0],[6.0,7.0],[1.0,2.0]]]},\"type\":\"CAPITAL\"}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);
        final String url = createUrlToSaveCity();

        final String actual = this.restTemplate.postForObject(url, givenHttpEntity, String.class);
        final String expectedRegex = "\\{\"id\":\\d+,\"name\":\"Minsk\",\"geometry\":\\{\"type\":\"Polygon\",\"coordinates\""
                + ":\\[\\[\\[1.0,2.0],\\[3.0,4.0],\\[5.0,6.0],\\[6.0,7.0],\\[1.0,2.0]]]},\"type\":\"CAPITAL\"}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void cityShouldNotBeSavedBecauseOfRequestIsNotValid() {
        final String givenJson = "{\"geometry\":{\"type\":\"Polygon\",\"coordinates\""
                + ":[[[1.0,2.0],[3.0,4.0],[5.0,6.0],[6.0,7.0],[1.0,2.0]]]},\"type\":\"CAPITAL\"}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);
        final String url = createUrlToSaveCity();

        final String actual = this.restTemplate.postForObject(url, givenHttpEntity, String.class);
        final String expectedRegex = "\\{"
                + "\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"name : не должно равняться null\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\""
                + "}";
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    @Transactional(propagation = NOT_SUPPORTED)
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Minsk', "
            + "ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_Envelope(ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326))"
            + ")")
    @Sql(statements = "DELETE FROM city", executionPhase = AFTER_TEST_METHOD)
    public void cityShouldBeUpdated() {
        final String givenJson = "{\"name\":\"Minsk\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\""
                + ":[[[1.0,2.0],[3.0,4.0],[5.0,6.0],[6.0,7.0],[1.0,2.0]]]},\"type\":\"CAPITAL\"}";
        final Long givenId = 255L;

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String url = createUrlWithIdVariable(givenId);
        final ResponseEntity<String> responseEntity
                = this.restTemplate.exchange(url, PUT, givenHttpEntity, String.class);

        final String actual = responseEntity.getBody();
        final String expected = "{\"id\":255,\"name\":\"Minsk\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\""
                + ":[[[1.0,2.0],[3.0,4.0],[5.0,6.0],[6.0,7.0],[1.0,2.0]]]},\"type\":\"CAPITAL\"}";
        assertEquals(expected, actual);
    }

    @Test
    public void cityShouldNotBeUpdatedByNotExistingId() {
        final String givenJson = "{\"name\":\"Minsk\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\""
                + ":[[[1.0,2.0],[3.0,4.0],[5.0,6.0],[6.0,7.0],[1.0,2.0]]]},\"type\":\"CAPITAL\"}";
        final Long givenId = 255L;

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String url = createUrlWithIdVariable(givenId);
        final ResponseEntity<String> responseEntity
                = this.restTemplate.exchange(url, PUT, givenHttpEntity, String.class);

        final String actual = responseEntity.getBody();
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_FOUND\",\"message\":\"City with id '255' doesn't exist.\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    @Transactional(propagation = NOT_SUPPORTED)
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Minsk', "
            + "ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_Envelope(ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326))"
            + ")")
    @Sql(statements = "DELETE FROM city", executionPhase = AFTER_TEST_METHOD)
    public void cityShouldNotBeUpdatedByNotValidRequest() {
        final String givenJson = "{\"geometry\":{\"type\":\"Polygon\",\"coordinates\""
                + ":[[[1.0,2.0],[3.0,4.0],[5.0,6.0],[6.0,7.0],[1.0,2.0]]]},\"type\":\"CAPITAL\"}";
        final Long givenId = 255L;

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String url = createUrlWithIdVariable(givenId);
        final ResponseEntity<String> responseEntity
                = this.restTemplate.exchange(url, PUT, givenHttpEntity, String.class);

        final String actual = responseEntity.getBody();
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\",\"message\":\"name : не должно равняться null\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    @Transactional(propagation = NOT_SUPPORTED)
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Minsk', "
            + "ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), "
            + "'CAPITAL', "
            + "ST_Envelope(ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326))"
            + ")")
    @Sql(statements = "DELETE FROM city", executionPhase = AFTER_TEST_METHOD)
    public void cityShouldBeRemoved() {
        final Long givenId = 255L;

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenHeaders);

        final String url = createUrlWithIdVariable(givenId);
        final ResponseEntity<String> responseEntity = this.restTemplate.exchange(
                url, DELETE, givenHttpEntity, String.class);

        final String actual = responseEntity.getBody();
        final String expected = "{\"id\":255,\"name\":\"Minsk\",\"geometry\":{\"type\":\"Polygon\","
                + "\"coordinates\":[[[1.0,2.0],[3.0,4.0],[5.0,6.0],[6.0,7.0],[1.0,2.0]]]},\"type\":\"CAPITAL\"}";
        assertEquals(expected, actual);
    }

    @Test
    public void cityShouldNotBeRemovedByIdBecauseOfItIsNotExist() {
        final Long givenId = 255L;

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenHeaders);

        final String url = createUrlWithIdVariable(givenId);
        final ResponseEntity<String> responseEntity = this.restTemplate.exchange(
                url, DELETE, givenHttpEntity, String.class);

        final String actual = responseEntity.getBody();
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_FOUND\",\"message\":\"City with id '255' doesn't exist.\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    private static String createUrlToFindAllCities(int pageNumber, int pageSize) {
        return new UrlToFindAllCitiesBuilder()
                .catalogPageNumber(pageNumber)
                .catalogPageSize(pageSize)
                .build();
    }

    private static String createUrlToSaveCity() {
        return CONTROLLER_URL;
    }

    private static String createUrlWithIdVariable(Long id) {
        return CONTROLLER_URL + SLASH + id;
    }

    private static final class UrlToFindAllCitiesBuilder {
        private static final String EXCEPTION_DESCRIPTION_URI_BUILDING_BY_NOT_DEFINED_PARAMETERS
                = "Uri was build by not defined parameters.";
        private static final String PARAM_NAME_PAGE_NUMBER = "pageNumber";
        private static final String PARAM_NAME_PAGE_SIZE = "pageSize";

        private int pageNumber;
        private int pageSize;

        public UrlToFindAllCitiesBuilder() {
            this.pageNumber = MIN_VALUE;
            this.pageSize = MIN_VALUE;
        }

        public UrlToFindAllCitiesBuilder catalogPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public UrlToFindAllCitiesBuilder catalogPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public String build() {
            if (this.pageNumber == MIN_VALUE || this.pageSize == MIN_VALUE) {
                throw new IllegalStateException(EXCEPTION_DESCRIPTION_URI_BUILDING_BY_NOT_DEFINED_PARAMETERS);
            }
            return fromUriString(CONTROLLER_URL)
                    .queryParam(PARAM_NAME_PAGE_NUMBER, this.pageNumber)
                    .queryParam(PARAM_NAME_PAGE_SIZE, this.pageSize)
                    .build()
                    .toUriString();
        }
    }
}
