package by.aurorasoft.nominatim.rest.controller;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.crud.model.dto.City;
import by.aurorasoft.nominatim.crud.model.dto.SearchingCitiesProcess;
import by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status;
import by.aurorasoft.nominatim.crud.service.CityService;
import by.aurorasoft.nominatim.crud.service.SearchingCitiesProcessService;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.wololo.jts2geojson.GeoJSONReader;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static by.aurorasoft.nominatim.crud.model.entity.CityEntity.Type.NOT_DEFINED;
import static by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status.HANDLING;
import static by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status.SUCCESS;
import static java.lang.Integer.MIN_VALUE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Pattern.compile;
import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class SearchCityProcessControllerIT extends AbstractContextTest {

    private static final String CONTROLLER_URL = "/api/v1/searchCity";
    private static final String SLASH = "/";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CityService cityService;

    @Autowired
    private GeoJSONReader geoJSONReader;

    @Autowired
    private SearchingCitiesProcessService processService;

    @Autowired
    private GeometryFactory geometryFactory;

    @Test
    @Transactional(propagation = NOT_SUPPORTED)
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(255, ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), 0.01, 10000, 1000, 'HANDLING')")
    @Sql(statements = "DELETE FROM searching_cities_process", executionPhase = AFTER_TEST_METHOD)
    public void processShouldBeFoundById() {
        final Long givenId = 255L;

        final String url = createUrlToFindProcessById(givenId);
        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);

        assertSame(OK, responseEntity.getStatusCode());

        final String actual = responseEntity.getBody();
        final String expected = "{\"id\":255,\"geometry\":{\"type\":\"Polygon\","
                + "\"coordinates\":[[[1.0,2.0],[3.0,4.0],[5.0,6.0],[6.0,7.0],[1.0,2.0]]]},"
                + "\"searchStep\":0.01,\"totalPoints\":10000,\"handledPoints\":1000,\"status\":\"HANDLING\"}";
        assertEquals(expected, actual);
    }

    @Test
    public void processShouldNotBeFoundByNotExistingId() {
        final Long givenId = 255L;

        final String url = createUrlToFindProcessById(givenId);
        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);

        assertSame(NOT_FOUND, responseEntity.getStatusCode());

        final String actual = responseEntity.getBody();
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_FOUND\",\"message\":\"Process with id '255' doesn't exist.\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    @Transactional(propagation = NOT_SUPPORTED)
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(255, ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), 0.01, 10000, 1000, 'HANDLING')")
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(256, ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), 0.01, 10000, 1000, 'HANDLING')")
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(257, ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), 0.01, 10000, 10000, 'SUCCESS')")
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(258, ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), 0.01, 10000, 5000, 'ERROR')")
    @Sql(statements = "DELETE FROM searching_cities_process", executionPhase = AFTER_TEST_METHOD)
    public void processesShouldBeFoundByStatus() {
        final int givenPageNumber = 0;
        final int givenPageSize = 3;

        final String url = createUrlToFindProcessesByStatus(HANDLING, givenPageNumber, givenPageSize);
        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);

        assertSame(OK, responseEntity.getStatusCode());

        final String actual = responseEntity.getBody();
        final String expected = "{\"pageNumber\":0,\"pageSize\":3,\"processes\":[{\"id\":255,"
                + "\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[1.0,2.0],[3.0,4.0],[5.0,6.0],[6.0,7.0],[1.0,2.0]]]},"
                + "\"searchStep\":0.01,\"totalPoints\":10000,\"handledPoints\":1000,\"status\":\"HANDLING\"},"
                + "{\"id\":256,\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[1.0,2.0],[3.0,4.0],[5.0,6.0],[6.0,7.0],[1.0,2.0]]]},"
                + "\"searchStep\":0.01,\"totalPoints\":10000,\"handledPoints\":1000,\"status\":\"HANDLING\"}]}";
        assertEquals(expected, actual);
    }

    @Test
    public void processesShouldNotBeFoundByNotValidStatus() {
        final String url = CONTROLLER_URL + "?status=NOTVALID&pageNumber=0&pageSize=3";

        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);

        assertSame(NOT_ACCEPTABLE, responseEntity.getStatusCode());

        final String actual = responseEntity.getBody();
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"'NOTVALID' should be replaced by one of: HANDLING, SUCCESS, ERROR.\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void processesShouldNotBeFoundBecauseOfPageNumberIsLessThanMinimalAllowable() {
        final int givenPageNumber = -1;
        final int givenPageSize = 3;

        final String url = createUrlToFindProcessesByStatus(HANDLING, givenPageNumber, givenPageSize);
        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);

        assertSame(NOT_ACCEPTABLE, responseEntity.getStatusCode());

        final String actual = responseEntity.getBody();
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"findByStatus.pageNumber: должно быть не меньше 0\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void processesShouldNotBeFoundBecauseOfPageNumberIsMoreThanMaximalAllowable() {
        final int givenPageNumber = 10001;
        final int givenPageSize = 3;

        final String url = createUrlToFindProcessesByStatus(HANDLING, givenPageNumber, givenPageSize);
        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);

        assertSame(NOT_ACCEPTABLE, responseEntity.getStatusCode());

        final String actual = responseEntity.getBody();
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"findByStatus.pageNumber: должно быть не больше 10000\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void processesShouldNotBeFoundBecauseOfPageSizeIsLessThanMinimalAllowable() {
        final int givenPageNumber = 0;
        final int givenPageSize = 0;

        final String url = createUrlToFindProcessesByStatus(HANDLING, givenPageNumber, givenPageSize);

        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);

        assertSame(NOT_ACCEPTABLE, responseEntity.getStatusCode());

        final String actual = responseEntity.getBody();
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"findByStatus.pageSize: должно быть не меньше 1\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void processesShouldNotBeFoundBecauseOfPageSizeIsMoreThanMaximalAllowable() {
        final int givenPageNumber = 0;
        final int givenPageSize = 10001;

        final String url = createUrlToFindProcessesByStatus(HANDLING, givenPageNumber, givenPageSize);

        final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);

        assertSame(NOT_ACCEPTABLE, responseEntity.getStatusCode());

        final String actual = responseEntity.getBody();
        final String expectedRegex = "\\{\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"findByStatus.pageSize: должно быть не больше 10000\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\"}";
        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    @Transactional(propagation = NOT_SUPPORTED)
    @Sql(statements = "DELETE FROM city", executionPhase = AFTER_TEST_METHOD)
    @Sql(statements = "DELETE FROM searching_cities_process", executionPhase = AFTER_TEST_METHOD)
    public void processShouldBeStartedAndFindCities()
            throws InterruptedException {
        final String givenJson = "{"
                + "\"bbox\" : {"
                + "\"leftBottom\" : {"
                + "\"latitude\" : 53.669375,"
                + "\"longitude\" : 27.053689"
                + "},"
                + "\"rightUpper\" : {"
                + "\"latitude\" : 53.896085,"
                + "\"longitude\" : 27.443176"
                + "}"
                + "},"
                + "\"searchStep\": 0.04"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String url = createUrlToStartProcess();

        final String actualResponse = this.restTemplate.postForObject(url, givenHttpEntity, String.class);
        final String expectedResponseRegex = "\\{\"id\":(\\d+),\"geometry\":\\{\"type\":\"Polygon\","
                + "\"coordinates\":\\[\\[\\[53.669375,27.053689],\\[53.669375,27.443176],\\[53.896085,27.443176],"
                + "\\[53.896085,27.053689],\\[53.669375,27.053689]]]},\"searchStep\":0.04,\"totalPoints\":60,"
                + "\"handledPoints\":0,\"status\":\"HANDLING\"}";
        assertNotNull(actualResponse);

        final Pattern pattern = compile(expectedResponseRegex);
        final Matcher matcher = pattern.matcher(actualResponse);
        assertTrue(matcher.matches());

        SECONDS.sleep(70);

        final List<City> actualFoundCities = this.cityService.findAll(0, 10);

        final Geometry expectedGeometryFoundCity = this.geoJSONReader.read(findFanipolGeoJson());
        final List<City> expectedFoundCities = List.of(
                City.builder()
                        .name("Фаниполь")
                        .geometry(expectedGeometryFoundCity)
                        .type(NOT_DEFINED)
                        .boundingBox(expectedGeometryFoundCity.getEnvelope())
                        .build()
        );

        assertEquals(1, actualFoundCities.size());
        checkEqualsWithoutId(expectedFoundCities.get(0), actualFoundCities.get(0));

        final Long createdProcessId = Long.valueOf(matcher.group(1));

        final SearchingCitiesProcess actualProcess = this.processService.getById(createdProcessId);
        final SearchingCitiesProcess expectedProcess = SearchingCitiesProcess.builder()
                .id(createdProcessId)
                .geometry(this.geometryFactory.createPolygon(new CoordinateXY[]{
                        new CoordinateXY(53.669375, 27.053689),
                        new CoordinateXY(53.669375, 27.443176),
                        new CoordinateXY(53.896085, 27.443176),
                        new CoordinateXY(53.896085, 27.053689),
                        new CoordinateXY(53.669375, 27.053689)
                }))
                .searchStep(0.04)
                .totalPoints(60)
                .handledPoints(60)
                .status(SUCCESS)
                .build();
        assertEquals(expectedProcess, actualProcess);
    }

    @Test
    public void processSearchingCitiesShouldNotBeStartBecauseOfAreaCoordinateIsNull() {
        final String givenJson = "{"
                + "\"bbox\" : {"
                + "\"leftBottom\" : {"
                + "\"latitude\" : 53.669375,"
                + "\"longitude\" : 27.053689"
                + "},"
                + "\"rightUpper\" : {"
                + "\"latitude\" : 53.646085,"
                + "\"longitude\" : 27.443176"
                + "}"
                + "},"
                + "\"searchStep\": 0.04"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String url = createUrlToStartProcess();

        final String actual = this.restTemplate.postForObject(url, givenHttpEntity, String.class);
        final String expectedRegex = "\\{"
                + "\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"Left bottom point's coordinates should be less than right upper point's coordinates.\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\""
                + "}";

        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void processSearchingCitiesShouldNotBeStartBecauseOfSearchStepIsNull() {
        final String givenJson = "{"
                + "\"bbox\" : {"
                + "\"leftBottom\" : {"
                + "\"latitude\" : 53.669375,"
                + "\"longitude\" : 27.053689"
                + "},"
                + "\"rightUpper\" : {"
                + "\"latitude\" : 53.646085,"
                + "\"longitude\" : 27.443176"
                + "}"
                + "}"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String url = createUrlToStartProcess();

        final String actual = this.restTemplate.postForObject(url, givenHttpEntity, String.class);
        final String expectedRegex = "\\{"
                + "\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"searchStep : не должно равняться null\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\""
                + "}";

        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void processSearchingCitiesShouldNotBeStartBecauseOfSearchStepIsLessThanMinimalAllowable() {
        final String givenJson = "{"
                + "\"bbox\" : {"
                + "\"leftBottom\" : {"
                + "\"latitude\" : 53.669375,"
                + "\"longitude\" : 27.053689"
                + "},"
                + "\"rightUpper\" : {"
                + "\"latitude\" : 53.896085,"
                + "\"longitude\" : 27.443176"
                + "}"
                + "},"
                + "\"searchStep\": 0.001"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String url = createUrlToStartProcess();

        final String actual = this.restTemplate.postForObject(url, givenHttpEntity, String.class);
        final String expectedRegex = "\\{"
                + "\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"searchStep : должно быть больше, чем или равно 0.01\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\""
                + "}";

        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    @Test
    public void processSearchingCitiesShouldNotBeStartBecauseOfSearchStepIsGreaterThanMaximalAllowable() {
        final String givenJson = "{"
                + "\"bbox\" : {"
                + "\"leftBottom\" : {"
                + "\"latitude\" : 53.669375,"
                + "\"longitude\" : 27.053689"
                + "},"
                + "\"rightUpper\" : {"
                + "\"latitude\" : 53.896085,"
                + "\"longitude\" : 27.443176"
                + "}"
                + "},"
                + "\"searchStep\": 5.1"
                + "}";

        final HttpHeaders givenHeaders = new HttpHeaders();
        givenHeaders.setContentType(APPLICATION_JSON);

        final HttpEntity<String> givenHttpEntity = new HttpEntity<>(givenJson, givenHeaders);

        final String url = createUrlToStartProcess();

        final String actual = this.restTemplate.postForObject(url, givenHttpEntity, String.class);
        final String expectedRegex = "\\{"
                + "\"httpStatus\":\"NOT_ACCEPTABLE\","
                + "\"message\":\"searchStep : должно быть меньше, чем или равно 5\","
                + "\"dateTime\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\""
                + "}";

        assertNotNull(actual);
        assertTrue(actual.matches(expectedRegex));
    }

    private static String createUrlToFindProcessById(Long id) {
        return CONTROLLER_URL + SLASH + id;
    }

    @SuppressWarnings("all")
    private static String createUrlToFindProcessesByStatus(Status status, int pageNumber, int pageSize) {
        return new UrlToFindProcessesByStatusBuilder()
                .catalogStatus(status)
                .catalogPageNumber(pageNumber)
                .catalogPageSize(pageSize)
                .build();
    }

    private static String createUrlToStartProcess() {
        return CONTROLLER_URL;
    }

    private static void checkEqualsWithoutId(City expected, City actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getGeometry(), actual.getGeometry());
        assertSame(expected.getType(), actual.getType());
        assertEquals(expected.getGeometry(), actual.getGeometry());
    }

    private static String findFanipolGeoJson() {
        return "{\n" +
                "        \"type\": \"Polygon\",\n" +
                "        \"coordinates\": [\n" +
                "            [\n" +
                "                [\n" +
                "                    27.3085345,\n" +
                "                    53.7380168\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.308911,\n" +
                "                    53.7379308\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3111454,\n" +
                "                    53.7377759\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3115449,\n" +
                "                    53.7377727\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3121176,\n" +
                "                    53.7378979\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3126945,\n" +
                "                    53.7380964\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3130636,\n" +
                "                    53.7382674\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3133334,\n" +
                "                    53.7384117\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3135298,\n" +
                "                    53.7382383\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3196085,\n" +
                "                    53.7374708\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3094137,\n" +
                "                    53.7315646\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3086376,\n" +
                "                    53.7287284\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3109372,\n" +
                "                    53.7283702\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3114729,\n" +
                "                    53.7283395\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3124298,\n" +
                "                    53.7283813\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3132617,\n" +
                "                    53.7286141\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3147458,\n" +
                "                    53.7294366\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.317842,\n" +
                "                    53.7288979\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3229611,\n" +
                "                    53.7272274\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.323416,\n" +
                "                    53.7269034\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3233823,\n" +
                "                    53.7260098\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3234131,\n" +
                "                    53.724638\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3258688,\n" +
                "                    53.7246704\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3272354,\n" +
                "                    53.7249562\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3281196,\n" +
                "                    53.7256106\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3309275,\n" +
                "                    53.7286128\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3325927,\n" +
                "                    53.7304098\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3370624,\n" +
                "                    53.73447\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.344529,\n" +
                "                    53.7338651\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3444137,\n" +
                "                    53.7328612\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3452581,\n" +
                "                    53.7328254\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3452977,\n" +
                "                    53.7321248\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3622788,\n" +
                "                    53.7308288\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.363894,\n" +
                "                    53.738831\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3631161,\n" +
                "                    53.7399728\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3633086,\n" +
                "                    53.7456278\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3579576,\n" +
                "                    53.7464324\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.359414,\n" +
                "                    53.7499366\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3595246,\n" +
                "                    53.7557003\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3590352,\n" +
                "                    53.756363\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3519396,\n" +
                "                    53.7646249\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3509943,\n" +
                "                    53.7656445\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.338056,\n" +
                "                    53.7588182\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3324682,\n" +
                "                    53.753294\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3314644,\n" +
                "                    53.7538488\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.330268,\n" +
                "                    53.7542742\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3297308,\n" +
                "                    53.7544361\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3253934,\n" +
                "                    53.7513632\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3211448,\n" +
                "                    53.7478867\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3118993,\n" +
                "                    53.7404969\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3086038,\n" +
                "                    53.7381521\n" +
                "                ],\n" +
                "                [\n" +
                "                    27.3085345,\n" +
                "                    53.7380168\n" +
                "                ]\n" +
                "            ]\n" +
                "        ]\n" +
                "    }";
    }

    private static final class UrlToFindProcessesByStatusBuilder {
        private static final String EXCEPTION_DESCRIPTION_URI_BUILDING_BY_NOT_DEFINED_PARAMETERS
                = "Url was built by not defined parameters.";

        private static final String PARAM_NAME_STATUS = "status";
        private static final String PARAM_NAME_PAGE_SIZE = "pageSize";
        private static final String PARAM_NAME_PAGE_NUMBER = "pageNumber";

        private Status status;
        private int pageNumber;
        private int pageSize;

        public UrlToFindProcessesByStatusBuilder() {
            this.status = null;
            this.pageNumber = MIN_VALUE;
            this.pageSize = MIN_VALUE;
        }

        public UrlToFindProcessesByStatusBuilder catalogStatus(Status status) {
            this.status = status;
            return this;
        }

        public UrlToFindProcessesByStatusBuilder catalogPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public UrlToFindProcessesByStatusBuilder catalogPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public String build() {
            if (this.status == null || this.pageNumber == MIN_VALUE || this.pageSize == MIN_VALUE) {
                throw new IllegalStateException(EXCEPTION_DESCRIPTION_URI_BUILDING_BY_NOT_DEFINED_PARAMETERS);
            }
            return fromUriString(CONTROLLER_URL)
                    .queryParam(PARAM_NAME_STATUS, this.status)
                    .queryParam(PARAM_NAME_PAGE_NUMBER, this.pageNumber)
                    .queryParam(PARAM_NAME_PAGE_SIZE, this.pageSize)
                    .build()
                    .toUriString();
        }
    }
}
