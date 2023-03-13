package by.aurorasoft.nominatim.crud.service;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.crud.model.dto.SearchingCitiesProcess;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status.ERROR;
import static by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status.HANDLING;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class SearchingCitiesProcessServiceTest extends AbstractContextTest {

    @Autowired
    private SearchingCitiesProcessService service;

    @Autowired
    private GeometryFactory geometryFactory;

    @Test
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(255, ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), 0.01, 10000, 1000, 'HANDLING')")
    public void processShouldBeUpdatedByStatus() {
        final SearchingCitiesProcess processToBeUpdated = SearchingCitiesProcess.builder()
                .id(255L)
                .build();
        this.service.updateStatus(processToBeUpdated, ERROR);

        final SearchingCitiesProcess actual = this.service.getById(255L);

        final Coordinate[] expectedCoordinates = new Coordinate[]{
                new CoordinateXY(1, 2),
                new CoordinateXY(3, 4),
                new CoordinateXY(5, 6),
                new CoordinateXY(6, 7),
                new CoordinateXY(1, 2)
        };
        final SearchingCitiesProcess expected = SearchingCitiesProcess.builder()
                .id(255L)
                .geometry(this.geometryFactory.createPolygon(expectedCoordinates))
                .searchStep(0.01)
                .totalPoints(10000)
                .handledPoints(1000)
                .status(ERROR)
                .build();
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(255, ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), 0.01, 10000, 1000, 'HANDLING')")
    public void processShouldBeUpdatedByIncreasingHandledPoints() {
        final SearchingCitiesProcess processToBeUpdated = SearchingCitiesProcess.builder()
                .id(255L)
                .build();
        this.service.increaseHandledPoints(processToBeUpdated, 100);

        final SearchingCitiesProcess actual = this.service.getById(255L);

        final Coordinate[] expectedCoordinates = new Coordinate[]{
                new CoordinateXY(1, 2),
                new CoordinateXY(3, 4),
                new CoordinateXY(5, 6),
                new CoordinateXY(6, 7),
                new CoordinateXY(1, 2)
        };
        final SearchingCitiesProcess expected = SearchingCitiesProcess.builder()
                .id(255L)
                .geometry(this.geometryFactory.createPolygon(expectedCoordinates))
                .searchStep(0.01)
                .totalPoints(10000)
                .handledPoints(1100)
                .status(HANDLING)
                .build();
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(255, ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), 0.01, 10000, 1000, 'HANDLING')")
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(256, ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), 0.01, 10000, 1000, 'HANDLING')")
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(257, ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), 0.01, 10000, 1000, 'ERROR')")
    public void processesShouldBeFoundByStatus() {
        final List<SearchingCitiesProcess> foundProcesses = this.service.findByStatus(HANDLING, 0, 3);
        final List<Long> actualIds = foundProcesses.stream()
                .map(SearchingCitiesProcess::getId)
                .collect(toList());
        final List<Long> expectedIds = List.of(255L, 256L);
        assertEquals(expectedIds, actualIds);
    }

    @Test
    public void processesShouldNotBeFoundByStatus() {
        final List<SearchingCitiesProcess> foundProcesses = this.service.findByStatus(HANDLING, 0, 3);
        assertTrue(foundProcesses.isEmpty());
    }
}
