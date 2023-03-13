package by.aurorasoft.nominatim.crud.service;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.crud.model.dto.City;
import org.junit.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.*;

public final class CityServiceTest extends AbstractContextTest {

    @Autowired
    private CityService service;

    @Autowired
    private GeometryFactory geometryFactory;

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((1 1, 2 1, 2 2, 1 1))', 4326), "
            + "'CAPITAL',"
            + "ST_GeomFromText('POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(256, 'Second', "
            + "ST_GeomFromText('POLYGON((4 4, 5 4, 5 5, 4 4))', 4326), "
            + "'REGIONAL', "
            + "ST_GeomFromText('POLYGON((4 4, 5 4, 5 5, 4 5, 4 4))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(257, 'Third', "
            + "ST_GeomFromText('POLYGON((6 6, 7 6, 7 7, 6 6))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((6 6, 7 6, 7 7, 6 7, 6 6))', 4326)"
            + ")")
    public void pageOfAllCitiesShouldBeFound() {
        final List<City> foundCities = this.service.findAll(0, 3);
        final List<Long> actualIds = foundCities.stream()
                .map(City::getId)
                .collect(toList());
        final List<Long> expectedIds = List.of(255L, 256L, 257L);
        assertEquals(expectedIds, actualIds);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((1 1, 2 1, 2 2, 1 1))', 4326), "
            + "'CAPITAL',"
            + "ST_GeomFromText('POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(256, 'Second', "
            + "ST_GeomFromText('POLYGON((4 4, 5 4, 5 5, 4 4))', 4326), "
            + "'REGIONAL', "
            + "ST_GeomFromText('POLYGON((4 4, 5 4, 5 5, 4 5, 4 4))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(257, 'Third', "
            + "ST_GeomFromText('POLYGON((6 6, 7 6, 7 7, 6 6))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((6 6, 7 6, 7 7, 6 7, 6 6))', 4326)"
            + ")")
    public void pageOfAllCitiesShouldNotBeFound() {
        final List<City> foundCities = this.service.findAll(1, 3);
        assertTrue(foundCities.isEmpty());
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((1 1, 2 1, 2 2, 1 1))', 4326), "
            + "'CAPITAL',"
            + "ST_GeomFromText('POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(256, 'Second', "
            + "ST_GeomFromText('POLYGON((4 4, 5 4, 5 5, 4 4))', 4326), "
            + "'REGIONAL', "
            + "ST_GeomFromText('POLYGON((4 4, 5 4, 5 5, 4 5, 4 4))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(257, 'Third', "
            + "ST_GeomFromText('POLYGON((6 6, 7 6, 7 7, 6 6))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((6 6, 7 6, 7 7, 6 7, 6 6))', 4326)"
            + ")")
    public void cityWithGivenGeometryShouldExist() {
        final Coordinate[] givenCoordinates = new Coordinate[]{
                new CoordinateXY(1, 1),
                new CoordinateXY(2, 1),
                new CoordinateXY(2, 2),
                new CoordinateXY(1, 1)
        };
        final Geometry givenGeometry = this.geometryFactory.createPolygon(givenCoordinates);
        assertTrue(this.service.isExistByGeometry(givenGeometry));
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((1 1, 2 1, 2 2, 1 1))', 4326), "
            + "'CAPITAL',"
            + "ST_GeomFromText('POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(256, 'Second', "
            + "ST_GeomFromText('POLYGON((4 4, 5 4, 5 5, 4 4))', 4326), "
            + "'REGIONAL', "
            + "ST_GeomFromText('POLYGON((4 4, 5 4, 5 5, 4 5, 4 4))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(257, 'Third', "
            + "ST_GeomFromText('POLYGON((6 6, 7 6, 7 7, 6 6))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((6 6, 7 6, 7 7, 6 7, 6 6))', 4326)"
            + ")")
    public void cityWithGivenGeometryShouldNotExist() {
        final Coordinate[] givenCoordinates = new Coordinate[]{
                new CoordinateXY(1, 2),
                new CoordinateXY(3, 4),
                new CoordinateXY(5, 6),
                new CoordinateXY(6, 7),
                new CoordinateXY(1, 2)
        };
        final Geometry givenGeometry = this.geometryFactory.createPolygon(givenCoordinates);
        assertFalse(this.service.isExistByGeometry(givenGeometry));
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((1 1, 2 1, 2 2, 1 1))', 4326), "
            + "'CAPITAL',"
            + "ST_GeomFromText('POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(256, 'Second', "
            + "ST_GeomFromText('POLYGON((4 4, 5 4, 5 5, 4 4))', 4326), "
            + "'REGIONAL', "
            + "ST_GeomFromText('POLYGON((4 4, 5 4, 5 5, 4 5, 4 4))', 4326)"
            + ")")
    public void preparedGeometriesByPreparedBoundingBoxesShouldBeFound() {
        final Map<PreparedGeometry, PreparedGeometry> actual = this.service
                .findPreparedGeometriesByPreparedBoundingBoxes();
        final Map<Geometry, Geometry> actualGeometries = actual.entrySet()
                .stream()
                .collect(
                        toMap(
                                entry -> entry.getKey().getGeometry(),
                                entry -> entry.getValue().getGeometry()
                        )
                );
        final Map<Geometry, Geometry> expectedGeometries = Map.of(
                this.geometryFactory.createPolygon(
                        new Coordinate[]{
                                new CoordinateXY(1, 1),
                                new CoordinateXY(2, 1),
                                new CoordinateXY(2, 2),
                                new CoordinateXY(1, 2),
                                new CoordinateXY(1, 1)
                        }
                ),
                this.geometryFactory.createPolygon(
                        new Coordinate[]{
                                new CoordinateXY(1, 1),
                                new CoordinateXY(2, 1),
                                new CoordinateXY(2, 2),
                                new CoordinateXY(1, 1)
                        }
                ),
                this.geometryFactory.createPolygon(
                        new Coordinate[]{
                                new CoordinateXY(4, 4),
                                new CoordinateXY(5, 4),
                                new CoordinateXY(5, 5),
                                new CoordinateXY(4, 5),
                                new CoordinateXY(4, 4)
                        }
                ),
                this.geometryFactory.createPolygon(
                        new Coordinate[]{
                                new CoordinateXY(4, 4),
                                new CoordinateXY(5, 4),
                                new CoordinateXY(5, 5),
                                new CoordinateXY(4, 4)
                        }
                )
        );
        assertEquals(expectedGeometries, actualGeometries);
    }

    @Test
    public void preparedGeometriesByPreparedBoundingBoxesShouldNotBeFound() {
        final Map<PreparedGeometry, PreparedGeometry> preparedGeometriesByPreparedBoundingBoxes = this.service
                .findPreparedGeometriesByPreparedBoundingBoxes();
        assertTrue(preparedGeometriesByPreparedBoundingBoxes.isEmpty());
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))', 4326), "
            + "'CAPITAL',"
            + "ST_GeomFromText('POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(256, 'Second', "
            + "ST_GeomFromText('POLYGON((3 3, 4 3, 4 4, 3 3))', 4326), "
            + "'REGIONAL', "
            + "ST_GeomFromText('POLYGON((3 3, 3 4, 4 4, 4 3, 3 3))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(257, 'Third', "
            + "ST_GeomFromText('POLYGON((3 1, 3 2, 4 2, 4 1, 3 1))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((3 1, 3 2, 4 2, 4 1, 3 1))', 4326)"
            + ")")
    public void preparedGeometriesWhoseBoundingBoxIntersectedByLineStringShouldBeFound() {
        final LineString givenLineString = this.geometryFactory.createLineString(new Coordinate[]{
                new CoordinateXY(1.5, 1.5),
                new CoordinateXY(3.5, 3.5),
                new CoordinateXY(4.5, 4.5)
        });

        final List<PreparedGeometry> foundPreparedGeometries = this.service
                .findPreparedGeometriesWhoseBoundingBoxIntersectedByLineString(givenLineString);
        final List<Geometry> actual = foundPreparedGeometries.stream()
                .map(PreparedGeometry::getGeometry)
                .collect(toList());

        final List<Geometry> expected = List.of(
                this.geometryFactory.createPolygon(new Coordinate[]{
                        new CoordinateXY(1, 1),
                        new CoordinateXY(1, 2),
                        new CoordinateXY(2, 2),
                        new CoordinateXY(2, 1),
                        new CoordinateXY(1, 1)
                }),
                this.geometryFactory.createPolygon(new Coordinate[]{
                        new CoordinateXY(3, 3),
                        new CoordinateXY(4, 3),
                        new CoordinateXY(4, 4),
                        new CoordinateXY(3, 3)
                })
        );

        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))', 4326), "
            + "'CAPITAL',"
            + "ST_GeomFromText('POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(256, 'Second', "
            + "ST_GeomFromText('POLYGON((3 3, 4 3, 4 4, 3 3))', 4326), "
            + "'REGIONAL', "
            + "ST_GeomFromText('POLYGON((3 3, 3 4, 4 4, 4 3, 3 3))', 4326)"
            + ")")
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(257, 'Third', "
            + "ST_GeomFromText('POLYGON((3 1, 3 2, 4 2, 4 1, 3 1))', 4326), "
            + "'NOT_DEFINED', "
            + "ST_GeomFromText('POLYGON((3 1, 3 2, 4 2, 4 1, 3 1))', 4326)"
            + ")")
    public void preparedGeometriesWhoseBoundingBoxIntersectedByLineStringShouldNotBeFound() {
        final LineString givenLineString = this.geometryFactory.createLineString(new Coordinate[]{
                new CoordinateXY(5.5, 5.5),
                new CoordinateXY(6.5, 6.5),
                new CoordinateXY(7.5, 7.5)
        });

        final List<PreparedGeometry> foundPreparedGeometries = this.service
                .findPreparedGeometriesWhoseBoundingBoxIntersectedByLineString(givenLineString);
        assertTrue(foundPreparedGeometries.isEmpty());
    }
}
