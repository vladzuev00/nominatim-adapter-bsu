package by.aurorasoft.nominatim.rest.mapper;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.crud.model.dto.City;
import by.aurorasoft.nominatim.rest.model.CityPageResponse;
import by.aurorasoft.nominatim.rest.model.CityRequest;
import by.aurorasoft.nominatim.rest.model.CityResponse;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.wololo.geojson.Polygon;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.util.List;

import static by.aurorasoft.nominatim.crud.model.entity.CityEntity.Type.CAPITAL;
import static by.aurorasoft.nominatim.crud.model.entity.CityEntity.Type.REGIONAL;
import static java.util.Arrays.deepEquals;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.*;

public final class CityControllerMapperTest extends AbstractContextTest {

    @Autowired
    private CityControllerMapper mapper;

    @Autowired
    private GeometryFactory geometryFactory;

    @Autowired
    private GeoJSONWriter geoJSONWriter;

    @Test
    public void cityShouldBeMappedToResponse() {
        final Coordinate[] givenGeometryCoordinates = createGeometryCoordinates();
        final Coordinate[] givenBoundingBoxCoordinates = createBoundingBoxCoordinates();

        final City givenCity = City.builder()
                .id(255L)
                .name("Minsk")
                .geometry(this.geometryFactory.createPolygon(givenGeometryCoordinates))
                .type(CAPITAL)
                .boundingBox(this.geometryFactory.createPolygon(givenBoundingBoxCoordinates))
                .build();

        final CityResponse actual = this.mapper.mapToResponse(givenCity);
        final CityResponse expected = CityResponse.builder()
                .id(255L)
                .name("Minsk")
                .geometry(this.geoJSONWriter.write(givenCity.getGeometry()))
                .type(CAPITAL)
                .build();
        checkEquals(expected, actual);
    }

    @Test
    public void citiesShouldBeMappedToResponses() {
        final Coordinate[] givenGeometryCoordinates = createGeometryCoordinates();
        final Geometry givenGeometry = this.geometryFactory.createPolygon(givenGeometryCoordinates);

        final Coordinate[] givenBoundingBoxCoordinates = createBoundingBoxCoordinates();
        final Geometry givenBoundingBox = this.geometryFactory.createPolygon(givenBoundingBoxCoordinates);

        final List<City> givenCities = List.of(
                City.builder()
                        .id(255L)
                        .name("Minsk")
                        .geometry(givenGeometry)
                        .type(CAPITAL)
                        .boundingBox(givenBoundingBox)
                        .build(),
                City.builder()
                        .id(256L)
                        .name("Mogilev")
                        .geometry(givenGeometry)
                        .type(REGIONAL)
                        .boundingBox(givenBoundingBox)
                        .build());

        final List<CityResponse> actual = this.mapper.mapToResponses(givenCities);
        final List<CityResponse> expected = List.of(
                CityResponse.builder()
                        .id(255L)
                        .name("Minsk")
                        .geometry(this.geoJSONWriter.write(givenCities.get(0).getGeometry()))
                        .type(CAPITAL)
                        .build(),
                CityResponse.builder()
                        .id(256L)
                        .name("Mogilev")
                        .geometry(this.geoJSONWriter.write(givenCities.get(1).getGeometry()))
                        .type(REGIONAL)
                        .build());
        checkEquals(expected.get(0), actual.get(0));
        checkEquals(expected.get(1), actual.get(1));
    }

    @Test
    public void requestShouldBeMappedToCity() {
        final Coordinate[] givenGeometryCoordinates = createGeometryCoordinates();
        final Coordinate[] givenBoundingBoxCoordinates = createBoundingBoxCoordinates();

        final Geometry givenGeometry = this.geometryFactory.createPolygon(givenGeometryCoordinates);
        final CityRequest givenRequest = CityRequest.builder()
                .name("Minsk")
                .geometry(this.geoJSONWriter.write(givenGeometry))
                .type(CAPITAL)
                .build();

        final City actual = this.mapper.mapToCity(givenRequest);
        final City expected = City.builder()
                .name("Minsk")
                .geometry(givenGeometry)
                .type(CAPITAL)
                .boundingBox(this.geometryFactory.createPolygon(givenBoundingBoxCoordinates))
                .build();
        assertEquals(expected, actual);
    }

    @Test
    public void requestShouldBeMappedToCityWithId() {
        final Coordinate[] givenGeometryCoordinates = createGeometryCoordinates();
        final Coordinate[] givenBoundingBoxCoordinates = createBoundingBoxCoordinates();

        final Geometry givenGeometry = this.geometryFactory.createPolygon(givenGeometryCoordinates);
        final CityRequest givenRequest = CityRequest.builder()
                .name("Minsk")
                .geometry(this.geoJSONWriter.write(givenGeometry))
                .type(CAPITAL)
                .build();

        final Long id = 255L;
        final City actual = this.mapper.mapToCity(id, givenRequest);
        final City expected = City.builder()
                .id(id)
                .name("Minsk")
                .geometry(givenGeometry)
                .type(CAPITAL)
                .boundingBox(this.geometryFactory.createPolygon(givenBoundingBoxCoordinates))
                .build();
        assertEquals(expected, actual);
    }

    @Test
    public void citiesShouldBeMappedToCityPageResponse() {
        final Coordinate[] givenGeometryCoordinates = createGeometryCoordinates();
        final Geometry givenGeometry = this.geometryFactory.createPolygon(givenGeometryCoordinates);

        final Coordinate[] givenBoundingBoxCoordinates = createBoundingBoxCoordinates();

        final int givenPageNumber = 0;
        final int givenPageSize = 1;
        final List<City> givenCities = List.of(
                City.builder()
                        .id(255L)
                        .name("Minsk")
                        .geometry(givenGeometry)
                        .type(CAPITAL)
                        .boundingBox(this.geometryFactory.createPolygon(givenBoundingBoxCoordinates))
                        .build()
        );

        final CityPageResponse actual = this.mapper.mapToResponse(givenPageNumber, givenPageSize, givenCities);
        final CityPageResponse expected = new CityPageResponse(givenPageNumber, givenPageSize,
                List.of(
                        CityResponse.builder()
                                .id(255L)
                                .name("Minsk")
                                .geometry(this.geoJSONWriter.write(givenGeometry))
                                .type(CAPITAL)
                                .build()
                )
        );
        checkEquals(expected, actual);
    }

    private static Coordinate[] createGeometryCoordinates() {
        return new Coordinate[]{
                new CoordinateXY(1, 1),
                new CoordinateXY(2, 1),
                new CoordinateXY(2, 2),
                new CoordinateXY(1, 1)
        };
    }

    private static Coordinate[] createBoundingBoxCoordinates() {
        return new Coordinate[]{
                new CoordinateXY(1, 1),
                new CoordinateXY(1, 2),
                new CoordinateXY(2, 2),
                new CoordinateXY(2, 1),
                new CoordinateXY(1, 1)
        };
    }

    private static void checkEquals(CityResponse expected, CityResponse actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        checkEquals(expected.getGeometry(), actual.getGeometry());
        assertSame(expected.getType(), actual.getType());
    }

    private static void checkEquals(org.wololo.geojson.Geometry expected, org.wololo.geojson.Geometry actual) {
        assertArrayEquals(((Polygon) expected).getBbox(), ((Polygon) actual).getBbox(), 0.);
        assertTrue(deepEquals(((Polygon) expected).getCoordinates(), ((Polygon) actual).getCoordinates()));
    }

    private static void checkEquals(CityPageResponse expected, CityPageResponse actual) {
        assertEquals(expected.getPageNumber(), actual.getPageNumber());
        assertEquals(expected.getPageSize(), actual.getPageSize());
        assertEquals(expected.getCities().size(), actual.getCities().size());
        range(0, expected.getCities().size()).forEach(i ->
                checkEquals(
                        expected.getCities().get(i),
                        actual.getCities().get(i)
                )
        );
    }
}
