package by.aurorasoft.nominatim.service.factory;

import by.aurorasoft.nominatim.crud.model.dto.AreaCoordinate;
import by.aurorasoft.nominatim.crud.model.dto.Coordinate;
import by.aurorasoft.nominatim.crud.model.dto.SearchingCitiesProcess;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Component;

import static by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status.HANDLING;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.UP;

@Component
@RequiredArgsConstructor
public class SearchingCitiesProcessFactory {
    private final GeometryFactory geometryFactory;

    public SearchingCitiesProcess create(AreaCoordinate areaCoordinate, double searchStep) {
        return SearchingCitiesProcess.builder()
                .geometry(this.findGeometry(areaCoordinate))
                .searchStep(searchStep)
                .totalPoints(findTotalPoints(areaCoordinate, searchStep))
                .status(HANDLING)
                .build();
    }

    private static long findTotalPoints(AreaCoordinate areaCoordinate, double searchStep) {
        return findTotalPointsInBottomSide(areaCoordinate, searchStep)
                * findTotalPointsInLeftSide(areaCoordinate, searchStep);
    }

    private static long findTotalPointsInBottomSide(AreaCoordinate areaCoordinate, double searchStep) {
        return findTotalPointsInLine(
                areaCoordinate.getLeftBottom().getLatitude(),
                areaCoordinate.getRightUpper().getLatitude(),
                searchStep);
    }

    private static long findTotalPointsInLeftSide(AreaCoordinate areaCoordinate, double searchStep) {
        return findTotalPointsInLine(
                areaCoordinate.getLeftBottom().getLongitude(),
                areaCoordinate.getRightUpper().getLongitude(),
                searchStep);
    }

    private static long findTotalPointsInLine(double startCoordinate, double endCoordinate, double searchStep) {
        return valueOf(endCoordinate)
                .subtract(valueOf(startCoordinate))
                .divide(valueOf(searchStep), UP)
                .longValue() + 1;
    }

    private Geometry findGeometry(AreaCoordinate areaCoordinate) {
        final CoordinateXY leftBottom = findLeftBottom(areaCoordinate);
        return this.geometryFactory.createPolygon(new CoordinateXY[]{
                leftBottom,
                findLeftUpper(areaCoordinate),
                findRightUpper(areaCoordinate),
                findRightBottom(areaCoordinate),
                leftBottom
        });
    }

    private static CoordinateXY findLeftBottom(AreaCoordinate areaCoordinate) {
        final Coordinate leftBottom = areaCoordinate.getLeftBottom();
        return new CoordinateXY(leftBottom.getLatitude(), leftBottom.getLongitude());
    }

    private static CoordinateXY findLeftUpper(AreaCoordinate areaCoordinate) {
        final Coordinate leftBottom = areaCoordinate.getLeftBottom();
        final Coordinate rightUpper = areaCoordinate.getRightUpper();
        return new CoordinateXY(leftBottom.getLatitude(), rightUpper.getLongitude());
    }

    private static CoordinateXY findRightUpper(AreaCoordinate areaCoordinate) {
        final Coordinate rightUpper = areaCoordinate.getRightUpper();
        return new CoordinateXY(rightUpper.getLatitude(), rightUpper.getLongitude());
    }

    private static CoordinateXY findRightBottom(AreaCoordinate areaCoordinate) {
        final Coordinate leftBottom = areaCoordinate.getLeftBottom();
        final Coordinate rightUpper = areaCoordinate.getRightUpper();
        return new CoordinateXY(rightUpper.getLatitude(), leftBottom.getLongitude());
    }
}
