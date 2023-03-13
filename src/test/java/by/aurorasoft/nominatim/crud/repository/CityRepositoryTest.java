package by.aurorasoft.nominatim.crud.repository;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.crud.model.entity.CityEntity;
import org.junit.Test;
import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Map;

import static by.aurorasoft.nominatim.crud.model.entity.CityEntity.Type.CAPITAL;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.*;

public final class CityRepositoryTest extends AbstractContextTest {
    private static final String TUPLE_ALIAS_OF_BOUNDING_BOX = "boundingBox";
    private static final String TUPLE_ALIAS_OF_GEOMETRY = "geometry";

    @Autowired
    private CityRepository repository;

    @Autowired
    private GeometryFactory geometryFactory;

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'Minsk', "
            + "ST_GeomFromText('POLYGON((1 1, 2 1, 2 2, 1 1))', 4326), "
            + "'CAPITAL', "
            + "ST_GeomFromText('POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))', 4326)"
            + ")")
    public void cityShouldBeFoundById() {
        super.startQueryCount();
        final CityEntity actual = this.repository.findById(255L).orElseThrow();
        super.checkQueryCount(1);

        final Coordinate[] expectedGeometryCoordinates = new Coordinate[]{
                new CoordinateXY(1, 1),
                new CoordinateXY(2, 1),
                new CoordinateXY(2, 2),
                new CoordinateXY(1, 1)
        };
        final Coordinate[] expectedBoundingBoxCoordinates = new Coordinate[]{
                new CoordinateXY(1, 1),
                new CoordinateXY(2, 1),
                new CoordinateXY(2, 2),
                new CoordinateXY(1, 2),
                new CoordinateXY(1, 1)
        };
        final CityEntity expected = CityEntity.builder()
                .id(255L)
                .name("Minsk")
                .geometry(this.geometryFactory.createPolygon(expectedGeometryCoordinates))
                .type(CAPITAL)
                .boundingBox(this.geometryFactory.createPolygon(expectedBoundingBoxCoordinates))
                .build();
        checkEquals(expected, actual);
    }

    @Test
    public void cityShouldBeSaved() {
        final Coordinate[] givenGeometryCoordinates = new Coordinate[]{
                new CoordinateXY(1, 1),
                new CoordinateXY(2, 1),
                new CoordinateXY(2, 2),
                new CoordinateXY(1, 1)
        };
        final Coordinate[] givenBoundingBoxCoordinates = new Coordinate[]{
                new CoordinateXY(1, 1),
                new CoordinateXY(2, 1),
                new CoordinateXY(2, 2),
                new CoordinateXY(1, 2),
                new CoordinateXY(1, 1)
        };
        final CityEntity givenCity = CityEntity.builder()
                .name("Minsk")
                .geometry(this.geometryFactory.createPolygon(givenGeometryCoordinates))
                .type(CAPITAL)
                .boundingBox(this.geometryFactory.createPolygon(givenBoundingBoxCoordinates))
                .build();

        super.startQueryCount();
        this.repository.save(givenCity);
        super.checkQueryCount(2);
    }

    @Test
    @Sql(statements = "INSERT INTO city(id, name, geometry, type, bounding_box) VALUES(255, 'First', "
            + "ST_GeomFromText('POLYGON((1 1, 2 1, 2 2, 1 1))', 4326), "
            + "'CAPITAL', "
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

        super.startQueryCount();
        final boolean exists = this.repository.isExistByGeometry(givenGeometry);
        super.checkQueryCount(1);
        assertTrue(exists);
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

        super.startQueryCount();
        final boolean exists = this.repository.isExistByGeometry(givenGeometry);
        super.checkQueryCount(1);
        assertFalse(exists);
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
    public void boundingBoxesWithGeometriesShouldBeFound() {
        super.startQueryCount();
        final List<Tuple> tuplesOfBoundingBoxesWithGeometries = this.repository.findBoundingBoxesWithGeometries();
        super.checkQueryCount(1);

        final int expectedAmountOfTuples = 2;
        assertEquals(expectedAmountOfTuples, tuplesOfBoundingBoxesWithGeometries.size());

        final Map<Geometry, Geometry> actual = tuplesOfBoundingBoxesWithGeometries.stream()
                .collect(
                        toMap(
                                tuple -> (Geometry) tuple.get(TUPLE_ALIAS_OF_BOUNDING_BOX),
                                tuple -> (Geometry) tuple.get(TUPLE_ALIAS_OF_GEOMETRY)
                        )
                );
        final Map<Geometry, Geometry> expected = Map.of(
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
        assertEquals(expected, actual);
    }

    @Test
    public void boundingBoxesWithGeometriesShouldNotBeFound() {
        super.startQueryCount();
        final List<Tuple> tuplesOfBoundingBoxesWithGeometries = this.repository.findBoundingBoxesWithGeometries();
        super.checkQueryCount(1);

        assertTrue(tuplesOfBoundingBoxesWithGeometries.isEmpty());
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
    public void citiesWhoseBoundingBoxIntersectedByLineStringShouldBeFound() {
        final LineString givenLineString = this.geometryFactory.createLineString(new Coordinate[]{
                new CoordinateXY(1.5, 1.5),
                new CoordinateXY(3.5, 3.5),
                new CoordinateXY(4.5, 4.5)
        });

        super.startQueryCount();
        final List<CityEntity> foundCities = this.repository.findCitiesWhoseBoundingBoxIntersectedByLineString(
                givenLineString);
        super.checkQueryCount(1);

        final List<Long> actual = foundCities.stream()
                .map(CityEntity::getId)
                .collect(toList());
        final List<Long> expected = List.of(255L, 256L);
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
    public void citiesWhoseBoundingBoxIntersectedByLineStringShouldNotBeFound() {
        final LineString givenLineString = this.geometryFactory.createLineString(new Coordinate[]{
                new CoordinateXY(5.5, 5.5),
                new CoordinateXY(6.5, 6.5),
                new CoordinateXY(7.5, 7.5)
        });

        super.startQueryCount();
        final List<CityEntity> foundCities = this.repository.findCitiesWhoseBoundingBoxIntersectedByLineString(
                givenLineString);
        super.checkQueryCount(1);

        assertTrue(foundCities.isEmpty());
    }

    private static void checkEquals(CityEntity expected, CityEntity actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getGeometry(), actual.getGeometry());
        assertSame(expected.getType(), actual.getType());
        assertEquals(expected.getBoundingBox(), actual.getBoundingBox());
    }
}
