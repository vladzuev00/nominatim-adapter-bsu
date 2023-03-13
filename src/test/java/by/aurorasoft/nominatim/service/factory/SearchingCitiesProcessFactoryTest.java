package by.aurorasoft.nominatim.service.factory;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.crud.model.dto.AreaCoordinate;
import by.aurorasoft.nominatim.crud.model.dto.Coordinate;
import by.aurorasoft.nominatim.crud.model.dto.SearchingCitiesProcess;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status.HANDLING;
import static org.junit.Assert.assertEquals;

public final class SearchingCitiesProcessFactoryTest extends AbstractContextTest {

    @Autowired
    private SearchingCitiesProcessFactory processFactory;

    @Autowired
    private GeometryFactory geometryFactory;

    @Test
    public void processShouldBeCreated() {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                new Coordinate(52.959981, 25.903515),
                new Coordinate(52.998760, 25.914997));
        final double givenSearchStep = 0.01;

        final SearchingCitiesProcess actual = this.processFactory.create(givenAreaCoordinate, givenSearchStep);

        final CoordinateXY[] expectedCoordinates = new CoordinateXY[]{
                new CoordinateXY(52.959981, 25.903515),
                new CoordinateXY(52.959981, 25.914997),
                new CoordinateXY(52.998760, 25.914997),
                new CoordinateXY(52.998760, 25.903515),
                new CoordinateXY(52.959981, 25.903515)
        };
        final SearchingCitiesProcess expected = SearchingCitiesProcess.builder()
                .geometry(this.geometryFactory.createPolygon(expectedCoordinates))
                .searchStep(givenSearchStep)
                .totalPoints(8)
                .status(HANDLING)
                .build();

        assertEquals(expected, actual);
    }
}
