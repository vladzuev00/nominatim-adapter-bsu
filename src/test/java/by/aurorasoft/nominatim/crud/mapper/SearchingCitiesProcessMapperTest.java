package by.aurorasoft.nominatim.crud.mapper;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.crud.model.dto.SearchingCitiesProcess;
import by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public final class SearchingCitiesProcessMapperTest extends AbstractContextTest {

    @Autowired
    private SearchingCitiesProcessMapper mapper;

    @Autowired
    private GeometryFactory geometryFactory;

    @Test
    public void dtoShouldBeMappedToEntity() {
        final Coordinate[] givenGeometryCoordinates = createGeometryCoordinates();
        final SearchingCitiesProcess givenDto = SearchingCitiesProcess.builder()
                .id(255L)
                .geometry(this.geometryFactory.createPolygon(givenGeometryCoordinates))
                .searchStep(0.01)
                .totalPoints(100)
                .handledPoints(50)
                .status(SUCCESS)
                .build();

        final SearchingCitiesProcessEntity actual = this.mapper.toEntity(givenDto);
        final SearchingCitiesProcessEntity expected = SearchingCitiesProcessEntity.builder()
                .id(255L)
                .geometry(this.geometryFactory.createPolygon(givenGeometryCoordinates))
                .searchStep(0.01)
                .totalPoints(100)
                .handledPoints(50)
                .status(SUCCESS)
                .build();
        checkEquals(expected, actual);
    }

    @Test
    public void entityShouldBeMappedToDto() {
        final Coordinate[] givenGeometryCoordinates = createGeometryCoordinates();
        final SearchingCitiesProcessEntity givenEntity = SearchingCitiesProcessEntity.builder()
                .id(255L)
                .geometry(this.geometryFactory.createPolygon(givenGeometryCoordinates))
                .searchStep(0.01)
                .totalPoints(100)
                .handledPoints(50)
                .status(SUCCESS)
                .build();

        final SearchingCitiesProcess actual = this.mapper.toDto(givenEntity);
        final SearchingCitiesProcess expected = SearchingCitiesProcess.builder()
                .id(255L)
                .geometry(this.geometryFactory.createPolygon(givenGeometryCoordinates))
                .searchStep(0.01)
                .totalPoints(100)
                .handledPoints(50)
                .status(SUCCESS)
                .build();
        assertEquals(expected, actual);
    }

    private static Coordinate[] createGeometryCoordinates() {
        return new Coordinate[]{
                new Coordinate(1, 2),
                new Coordinate(2, 3),
                new Coordinate(3, 4),
                new Coordinate(4, 5),
                new Coordinate(1, 2)
        };
    }

    private static void checkEquals(SearchingCitiesProcessEntity expected, SearchingCitiesProcessEntity actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getGeometry(), actual.getGeometry());
        assertEquals(expected.getSearchStep(), actual.getSearchStep(), 0.);
        assertEquals(expected.getTotalPoints(), actual.getTotalPoints());
        assertEquals(expected.getHandledPoints(), actual.getHandledPoints());
        assertSame(expected.getStatus(), actual.getStatus());
    }
}
