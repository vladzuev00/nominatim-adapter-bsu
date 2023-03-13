package by.aurorasoft.nominatim.rest.mapper;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.crud.model.dto.SearchingCitiesProcess;
import by.aurorasoft.nominatim.rest.model.SearchingCitiesProcessResponse;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.wololo.geojson.Polygon;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.util.List;

import static by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status.HANDLING;
import static by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status.SUCCESS;
import static java.util.Arrays.deepEquals;
import static org.junit.Assert.*;

public final class SearchingCitiesProcessControllerMapperTest extends AbstractContextTest {

    @Autowired
    private SearchingCitiesProcessControllerMapper mapper;

    @Autowired
    private GeometryFactory geometryFactory;

    @Autowired
    private GeoJSONWriter geoJSONWriter;

    @Test
    public void processShouldBeMappedToResponse() {
        final Coordinate[] givenCoordinates = new Coordinate[]{
                new CoordinateXY(1, 2),
                new CoordinateXY(3, 4),
                new CoordinateXY(5, 6),
                new CoordinateXY(6, 7),
                new CoordinateXY(1, 2)
        };
        final Geometry givenGeometry = this.geometryFactory.createPolygon(givenCoordinates);
        final SearchingCitiesProcess givenProcess = SearchingCitiesProcess.builder()
                .id(256L)
                .geometry(givenGeometry)
                .searchStep(0.01)
                .totalPoints(120)
                .handledPoints(60)
                .status(HANDLING)
                .build();

        final SearchingCitiesProcessResponse actual = this.mapper.mapToResponse(givenProcess);
        final SearchingCitiesProcessResponse expected = SearchingCitiesProcessResponse.builder()
                .id(256L)
                .geometry(this.geoJSONWriter.write(givenGeometry))
                .searchStep(0.01)
                .totalPoints(120)
                .handledPoints(60)
                .status(HANDLING)
                .build();
        checkEquals(expected, actual);
    }

    @Test
    public void processesShouldBeMappedToResponses() {
        final Coordinate[] givenCoordinates = new Coordinate[]{
                new CoordinateXY(1, 2),
                new CoordinateXY(3, 4),
                new CoordinateXY(5, 6),
                new CoordinateXY(6, 7),
                new CoordinateXY(1, 2)
        };
        final Geometry givenGeometry = this.geometryFactory.createPolygon(givenCoordinates);
        final List<SearchingCitiesProcess> givenProcesses = List.of(
                SearchingCitiesProcess.builder()
                        .id(256L)
                        .geometry(givenGeometry)
                        .searchStep(0.01)
                        .totalPoints(120)
                        .handledPoints(60)
                        .status(HANDLING)
                        .build(),
                SearchingCitiesProcess.builder()
                        .id(257L)
                        .geometry(givenGeometry)
                        .searchStep(0.02)
                        .totalPoints(120)
                        .handledPoints(120)
                        .status(SUCCESS)
                        .build());

        final List<SearchingCitiesProcessResponse> actual = this.mapper.mapToResponses(givenProcesses);
        final List<SearchingCitiesProcessResponse> expected = List.of(
                SearchingCitiesProcessResponse.builder()
                        .id(256L)
                        .geometry(this.geoJSONWriter.write(givenGeometry))
                        .searchStep(0.01)
                        .totalPoints(120)
                        .handledPoints(60)
                        .status(HANDLING)
                        .build(),
                SearchingCitiesProcessResponse.builder()
                        .id(257L)
                        .geometry(this.geoJSONWriter.write(givenGeometry))
                        .searchStep(0.02)
                        .totalPoints(120)
                        .handledPoints(120)
                        .status(SUCCESS)
                        .build());

        checkEquals(expected.get(0), actual.get(0));
        checkEquals(expected.get(1), actual.get(1));
    }

    private static void checkEquals(SearchingCitiesProcessResponse expected, SearchingCitiesProcessResponse actual) {
        assertEquals(expected.getId(), actual.getId());
        checkEquals(expected.getGeometry(), actual.getGeometry());
        assertEquals(expected.getSearchStep(), actual.getSearchStep(), 0.);
        assertEquals(expected.getTotalPoints(), actual.getTotalPoints());
        assertEquals(expected.getHandledPoints(), actual.getHandledPoints());
        assertSame(expected.getStatus(), actual.getStatus());
    }

    private static void checkEquals(org.wololo.geojson.Geometry expected, org.wololo.geojson.Geometry actual) {
        assertArrayEquals(((Polygon) expected).getBbox(), ((Polygon) actual).getBbox(), 0.);
        assertTrue(deepEquals(((Polygon) expected).getCoordinates(), ((Polygon) actual).getCoordinates()));
    }
}
