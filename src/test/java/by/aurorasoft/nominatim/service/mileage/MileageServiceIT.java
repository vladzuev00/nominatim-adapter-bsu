package by.aurorasoft.nominatim.service.mileage;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.crud.service.CityService;
import by.aurorasoft.nominatim.rest.model.MileageRequest;
import by.aurorasoft.nominatim.rest.model.MileageRequest.TrackPoint;
import by.aurorasoft.nominatim.rest.model.MileageResponse;
import by.nhorushko.distancecalculator.*;
import com.opencsv.CSVReader;
import org.junit.After;
import org.junit.Test;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.io.FileReader;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.time.Instant.now;
import static java.time.LocalDateTime.parse;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public final class MileageServiceIT extends AbstractContextTest {
    private static final String FIELD_NAME_CITIES_GEOMETRIES_BY_BOUNDING_BOX = "citiesGeometriesByBoundingBoxes";

    private static final String FOLDER_PATH_WITH_TRACK_POINTS = "./src/test/resources/tracks";
    private static final String SLASH = "/";

    private static final String FILE_NAME_WITH_FIRST_TRACK_POINTS = "track_460_40000.csv";
    private static final String FILE_NAME_WITH_SECOND_TRACK_POINTS = "track_460_64000.csv";
    private static final String FILE_NAME_WITH_THIRD_TRACK_POINTS = "track_460_131000.csv";
    private static final String FILE_NAME_WITH_FOURTH_TRACK_POINTS = "unit_460_13000.csv";

    private static final int MIN_DETECTION_SPEED = 0;
    private static final int MAX_MESSAGE_TIMEOUT = 10;

    private static final int TRACK_POINT_ALTITUDE = 15;
    private static final int TRACK_POINT_SPEED = 15;
    private static final boolean TRACK_POINT_VALID = true;


    private static final double ALLOWABLE_INACCURACY_OF_DISTANCE = 0.00001;

    @Autowired
    private MileageService mileageService;

    @Autowired
    private DistanceCalculator distanceCalculator;

    @Autowired
    private CityService cityService;

    private final TrackPointFactory trackPointFactory;

    public MileageServiceIT() {
        this.trackPointFactory = new TrackPointFactory();
    }

    @After
    public void setCitiesGeometriesByBoundingBoxesAsNull()
            throws Exception {
        final Field field = MileageService.class.getDeclaredField(FIELD_NAME_CITIES_GEOMETRIES_BY_BOUNDING_BOX);
        field.setAccessible(true);
        try {
            field.set(this.mileageService, null);
        } finally {
            field.setAccessible(false);
        }
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case1WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.015F, 2.025F),
                        createTrackPoint(1.025F, 2.025F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0, 1.111948206008131);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case1WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.015F, 2.025F),
                        createTrackPoint(1.025F, 2.025F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0, 1.111948206008131);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case2WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.015F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.111948206008131, 0);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case2WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.015F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.111948206008131, 0);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case3WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0, 2.223896412016262);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case3WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0, 2.223896412016262);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case4WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.015F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0., 1.111948206008131);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case4WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.015F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0., 1.111948206008131);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case5WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.013F, 2.015F),
                        createTrackPoint(1.016F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0.3335871128960806, 0.);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case5WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.013F, 2.015F),
                        createTrackPoint(1.016F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0.3335871128960806, 0.);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case6WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.02F, 2.015F),
                        createTrackPoint(1.03F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0, 1.111948206008131);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case6WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.02F, 2.015F),
                        createTrackPoint(1.03F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0, 1.111948206008131);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case7WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.01F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0, 0.5559741030040655);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case7WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.01F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0, 0.5559741030040655);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case8WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.015F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.111948206008131,
                1.111948206008131);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case8WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.015F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.111948206008131,
                1.111948206008131);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED',"
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case9WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.015F, 2.018F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.1608862638235358,
                1.1608859675523793);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED',"
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case9WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.015F, 2.018F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.1608862638235358,
                1.1608859675523793);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case10WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.013F, 2.015F),
                        createTrackPoint(1.017F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0.44477398021596953,
                0.8895612159001461);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case10WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.013F, 2.015F),
                        createTrackPoint(1.017F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0.44477398021596953,
                0.8895612159001461);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case11WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.013F, 2.015F),
                        createTrackPoint(1.017F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.3343351961161156,
                0.8895612159001461);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case11WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.013F, 2.015F),
                        createTrackPoint(1.017F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.3343351961161156,
                0.8895612159001461);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case12WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.013F, 2.013F),
                        createTrackPoint(1.017F, 2.017F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.545888809855105,
                0.9169261331290726);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Something', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case12WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.013F, 2.013F),
                        createTrackPoint(1.017F, 2.017F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.545888809855105,
                0.9169261331290726);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326))")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(256, 'Second', "
            + "ST_GeomFromText('POLYGON((2.01 1.005, 2 1.01, 2.02 1.025, 2.03 1.02, 2.01 1.005))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2 1.005, 2 1.025, 2.03 1.025, 2.03 1.005, 2 1.005))', 4326)"
            + ")")
    public void case13WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.015F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.111948206008131,
                1.111948206008131);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326))")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(256, 'Second', "
            + "ST_GeomFromText('POLYGON((2.01 1.005, 2 1.01, 2.02 1.025, 2.03 1.02, 2.01 1.005))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2 1.005, 2 1.025, 2.03 1.025, 2.03 1.005, 2 1.005))', 4326)"
            + ")")
    public void case13WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.015F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.111948206008131,
                1.111948206008131);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326))")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(256, 'Second', "
            + "ST_GeomFromText('POLYGON((2.01 1.005, 2 1.01, 2.02 1.025, 2.03 1.02, 2.01 1.005))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2 1.005, 2 1.025, 2.03 1.025, 2.03 1.005, 2 1.005))', 4326)"
            + ")")
    public void case14WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.012F, 2.018F),
                        createTrackPoint(1.015F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.3185101144130753,
                1.111948206008131);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(256, 'Second', "
            + "ST_GeomFromText('POLYGON((2.01 1.005, 2 1.01, 2.02 1.025, 2.03 1.02, 2.01 1.005))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2 1.005, 2 1.025, 2.03 1.025, 2.03 1.005, 2 1.005))', 4326)"
            + ")")
    public void case14WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.012F, 2.018F),
                        createTrackPoint(1.015F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1.3185101144130753,
                1.111948206008131);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case15WithLoadingBoundingBoxAndGeometries() {
        this.loadCitiesBoundingBoxesAndGeometries();

        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.015F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0, 2.223896412016262);
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.01 1.01))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((2.01 1.01, 2.01 1.02, 2.02 1.02, 2.02 1.01, 2.01 1.01))', 4326)"
            + ")")
    public void case15WithoutLoadingBoundingBoxAndGeometries() {
        final MileageRequest givenMileageRequest = createMileageRequest(
                List.of(
                        createTrackPoint(1.005F, 2.015F),
                        createTrackPoint(1.015F, 2.015F),
                        createTrackPoint(1.025F, 2.015F)
                )
        );

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(0, 2.223896412016262);
        assertEquals(expected, actual);
    }

    @Test
    @Sql("classpath:sql/insert-belarus-city.sql")
    public void mileageShouldBeCalculatedForFirstTrackPointsWithLoadingCitiesBoundingBoxesAndGeometries()
            throws Exception {
        this.loadCitiesBoundingBoxesAndGeometries();

        final List<TrackPoint> givenTrackPoints = this.readTrackPoints(FILE_NAME_WITH_FIRST_TRACK_POINTS);
        final MileageRequest givenMileageRequest = createMileageRequest(givenTrackPoints);

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(355.72511991398096,
                357.73277661649564);
        assertEquals(expected, actual);

        final double actualAllDistance = actual.getUrban() + actual.getCountry();
        final double expectedAllDistance = this.findExpectedAllDistance(givenTrackPoints);
        assertEquals(expectedAllDistance, actualAllDistance, ALLOWABLE_INACCURACY_OF_DISTANCE);
    }

    @Test
    @Sql("classpath:sql/insert-belarus-city.sql")
    public void mileageShouldBeCalculatedForFirstTrackPointsWithoutLoadingCitiesBoundingBoxesAndGeometries()
            throws Exception {
        final List<TrackPoint> givenTrackPoints = this.readTrackPoints(FILE_NAME_WITH_FIRST_TRACK_POINTS);
        final MileageRequest givenMileageRequest = createMileageRequest(givenTrackPoints);

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(355.72511991398096,
                357.73277661649564);
        assertEquals(expected, actual);

        final double actualAllDistance = actual.getUrban() + actual.getCountry();
        final double expectedAllDistance = this.findExpectedAllDistance(givenTrackPoints);
        assertEquals(expectedAllDistance, actualAllDistance, ALLOWABLE_INACCURACY_OF_DISTANCE);
    }

    @Test
    @Sql("classpath:sql/insert-belarus-city.sql")
    public void mileageShouldBeCalculatedForSecondTrackPointsWithLoadingBoundingBoxesAndGeometries()
            throws Exception {
        this.loadCitiesBoundingBoxesAndGeometries();

        final List<TrackPoint> givenTrackPoints = this.readTrackPoints(FILE_NAME_WITH_SECOND_TRACK_POINTS);
        final MileageRequest givenMileageRequest = createMileageRequest(givenTrackPoints);

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(562.8989367618942,
                518.031547816519);
        assertEquals(expected, actual);

        final double actualAllDistance = actual.getUrban() + actual.getCountry();
        final double expectedAllDistance = this.findExpectedAllDistance(givenTrackPoints);
        assertEquals(expectedAllDistance, actualAllDistance, ALLOWABLE_INACCURACY_OF_DISTANCE);
    }

    @Test
    @Sql("classpath:sql/insert-belarus-city.sql")
    public void mileageShouldBeCalculatedForSecondTrackPointsWithoutLoadingBoundingBoxesAndGeometries()
            throws Exception {
        final List<TrackPoint> givenTrackPoints = this.readTrackPoints(FILE_NAME_WITH_SECOND_TRACK_POINTS);
        final MileageRequest givenMileageRequest = createMileageRequest(givenTrackPoints);

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(562.8989367618942,
                518.031547816519);
        assertEquals(expected, actual);

        final double actualAllDistance = actual.getUrban() + actual.getCountry();
        final double expectedAllDistance = this.findExpectedAllDistance(givenTrackPoints);
        assertEquals(expectedAllDistance, actualAllDistance, ALLOWABLE_INACCURACY_OF_DISTANCE);
    }

    @Test
    @Sql("classpath:sql/insert-belarus-city.sql")
    public void mileageShouldBeCalculatedForThirdTrackPointsWithLoadingBoundingBoxesAndGeometries()
            throws Exception {
        this.loadCitiesBoundingBoxesAndGeometries();

        final List<TrackPoint> givenTrackPoints = this.readTrackPoints(FILE_NAME_WITH_THIRD_TRACK_POINTS);
        final MileageRequest givenMileageRequest = createMileageRequest(givenTrackPoints);

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1207.6871617415154,
                1241.2063545061649);
        assertEquals(expected, actual);

        final double actualAllDistance = actual.getUrban() + actual.getCountry();
        final double expectedAllDistance = this.findExpectedAllDistance(givenTrackPoints);
        assertEquals(expectedAllDistance, actualAllDistance, ALLOWABLE_INACCURACY_OF_DISTANCE);
    }

    @Test
    @Sql("classpath:sql/insert-belarus-city.sql")
    public void mileageShouldBeCalculatedForThirdTrackPointsWithoutLoadingBoundingBoxesAndGeometries()
            throws Exception {
        final List<TrackPoint> givenTrackPoints = this.readTrackPoints(FILE_NAME_WITH_THIRD_TRACK_POINTS);
        final MileageRequest givenMileageRequest = createMileageRequest(givenTrackPoints);

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(1207.6871617415154,
                1241.2063545061649);
        assertEquals(expected, actual);

        final double actualAllDistance = actual.getUrban() + actual.getCountry();
        final double expectedAllDistance = this.findExpectedAllDistance(givenTrackPoints);
        assertEquals(expectedAllDistance, actualAllDistance, ALLOWABLE_INACCURACY_OF_DISTANCE);
    }

    @Test
    @Sql("classpath:sql/insert-belarus-city.sql")
    public void mileageShouldBeCalculatedByForFourthTrackPointsWithLoadingCitiesBoundingBoxesAndGeometries()
            throws Exception {
        this.loadCitiesBoundingBoxesAndGeometries();

        final List<TrackPoint> givenTrackPoints = this.readTrackPoints(FILE_NAME_WITH_FOURTH_TRACK_POINTS);
        final MileageRequest givenMileageRequest = createMileageRequest(givenTrackPoints);

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(125.27265649913504,
                117.8709566688135);
        assertEquals(expected, actual);

        final double actualAllDistance = actual.getUrban() + actual.getCountry();
        final double expectedAllDistance = this.findExpectedAllDistance(givenTrackPoints);
        assertEquals(expectedAllDistance, actualAllDistance, ALLOWABLE_INACCURACY_OF_DISTANCE);
    }

    @Test
    @Sql("classpath:sql/insert-belarus-city.sql")
    public void mileageShouldBeCalculatedByForFourthTrackPointsWithoutLoadingCitiesBoundingBoxesAndGeometries()
            throws Exception {
        final List<TrackPoint> givenTrackPoints = this.readTrackPoints(FILE_NAME_WITH_FOURTH_TRACK_POINTS);
        final MileageRequest givenMileageRequest = createMileageRequest(givenTrackPoints);

        final MileageResponse actual = this.mileageService.findMileage(givenMileageRequest);
        final MileageResponse expected = new MileageResponse(125.27265649913504,
                117.8709566688135);
        assertEquals(expected, actual);

        final double actualAllDistance = actual.getUrban() + actual.getCountry();
        final double expectedAllDistance = this.findExpectedAllDistance(givenTrackPoints);
        assertEquals(expectedAllDistance, actualAllDistance, ALLOWABLE_INACCURACY_OF_DISTANCE);
    }

    private void loadCitiesBoundingBoxesAndGeometries() {
        final Map<PreparedGeometry, PreparedGeometry> geometriesByBoundingBoxes = this.cityService
                .findPreparedGeometriesByPreparedBoundingBoxes();
        this.mileageService.setCitiesGeometriesByBoundingBoxes(geometriesByBoundingBoxes);
    }

    private static TrackPoint createTrackPoint(float latitude, float longitude) {
        return TrackPoint.builder()
                .datetime(now())
                .latitude(latitude)
                .longitude(longitude)
                .altitude(TRACK_POINT_ALTITUDE)
                .speed(TRACK_POINT_SPEED)
                .valid(TRACK_POINT_VALID)
                .build();
    }

    private static MileageRequest createMileageRequest(List<TrackPoint> trackPoints) {
        return MileageRequest.builder()
                .trackPoints(trackPoints)
                .minDetectionSpeed(MIN_DETECTION_SPEED)
                .maxMessageTimeout(MAX_MESSAGE_TIMEOUT)
                .build();
    }

    private List<TrackPoint> readTrackPoints(String fileName)
            throws Exception {
        final String filePath = FOLDER_PATH_WITH_TRACK_POINTS + SLASH + fileName;
        try (final CSVReader csvReader = new CSVReader(new FileReader(filePath))) {
            final List<String[]> readTrackPointsProperties = csvReader.readAll();
            return readTrackPointsProperties.stream()
                    .map(this.trackPointFactory::create)
                    .collect(toList());
        }
    }

    private double findExpectedAllDistance(List<TrackPoint> trackPoints) {
        final List<LatLngAlt> latLngAlts = trackPoints.stream()
                .map(MileageServiceIT::mapToLatLngAlt)
                .collect(toList());
        final DistanceCalculatorSettings distanceCalculatorSettings = new DistanceCalculatorSettingsImpl(
                MIN_DETECTION_SPEED, MAX_MESSAGE_TIMEOUT);
        return this.distanceCalculator.calculateDistance(latLngAlts, distanceCalculatorSettings);
    }

    private static LatLngAlt mapToLatLngAlt(TrackPoint trackPoint) {
        return new LatLngAltImpl(trackPoint.getDatetime(), trackPoint.getLatitude(), trackPoint.getLongitude(),
                trackPoint.getAltitude(), trackPoint.getSpeed(), trackPoint.isValid());
    }

    private static final class TrackPointFactory {
        private static final int INDEX_READ_PROPERTY_DATE_TIME = 1;
        private static final int INDEX_READ_PROPERTY_LATITUDE = 2;
        private static final int INDEX_READ_PROPERTY_LONGITUDE = 3;
        private static final int INDEX_READ_PROPERTY_ALTITUDE = 4;
        private static final int INDEX_READ_PROPERTY_SPEED = 5;
        private static final int INDEX_READ_PROPERTY_VALID = 6;

        private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
        private static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern(DATE_TIME_PATTERN);

        private static final String READ_PROPERTY_VALUE_OF_VALID_TO_BE_TRUE = "VALID";

        public TrackPoint create(String[] readProperties) {
            return TrackPoint.builder()
                    .datetime(parseDateTime(readProperties))
                    .latitude(parseLatitude(readProperties))
                    .longitude(parseLongitude(readProperties))
                    .altitude(parseAltitude(readProperties))
                    .speed(parseSpeed(readProperties))
                    .valid(parseValid(readProperties))
                    .build();
        }

        private static Instant parseDateTime(String[] readProperties) {
            return parse(readProperties[INDEX_READ_PROPERTY_DATE_TIME], DATE_TIME_FORMATTER).toInstant(UTC);
        }

        private static float parseLatitude(String[] readProperties) {
            return parseFloat(readProperties[INDEX_READ_PROPERTY_LATITUDE]);
        }

        private static float parseLongitude(String[] readProperties) {
            return parseFloat(readProperties[INDEX_READ_PROPERTY_LONGITUDE]);
        }

        private static int parseAltitude(String[] readProperties) {
            return parseInt(readProperties[INDEX_READ_PROPERTY_ALTITUDE]);
        }

        private static int parseSpeed(String[] readProperties) {
            return parseInt(readProperties[INDEX_READ_PROPERTY_SPEED]);
        }

        private static boolean parseValid(String[] readProperties) {
            return READ_PROPERTY_VALUE_OF_VALID_TO_BE_TRUE.equals(readProperties[INDEX_READ_PROPERTY_VALID]);
        }
    }
}
