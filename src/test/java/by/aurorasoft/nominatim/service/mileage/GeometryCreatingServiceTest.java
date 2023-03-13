package by.aurorasoft.nominatim.service.mileage;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.nhorushko.distancecalculator.LatLngAlt;
import by.nhorushko.distancecalculator.LatLngAltImpl;
import org.junit.Test;
import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public final class GeometryCreatingServiceTest extends AbstractContextTest {
    private static final Supplier<Instant> LAT_LNG_ALT_DATE_TIME_SUPPLIER = Instant::now;
    private static final int LAT_LNG_ALT_ALTITUDE = 15;
    private static final int LAT_LNG_ALT_SPEED = 10;
    private static final boolean LAT_LNG_ALT_VALID = true;

    @Autowired
    private GeometryCreatingService service;

    @Autowired
    private GeometryFactory geometryFactory;

    @Test
    public void lineStringShouldBeCreated() {
        final List<LatLngAlt> givenLatLngAlts = List.of(
                createLatLngAlt(4.4F, 5.5F),
                createLatLngAlt(6.6F, 7.7F),
                createLatLngAlt(8.8F, 9.9F)
        );

        final LineString actual = this.service.createLineString(givenLatLngAlts);
        final LineString expected = this.geometryFactory.createLineString(new Coordinate[]{
                new CoordinateXY(5.5F, 4.4F),
                new CoordinateXY(7.7F, 6.6F),
                new CoordinateXY(9.9F, 8.8F)
        });
        assertEquals(expected, actual);
    }

    @Test
    public void pointShouldBeCreated() {
        final LatLngAlt givenLatLngAlt = createLatLngAlt(4.4F, 5.5F);

        final Point actual = this.service.createPoint(givenLatLngAlt);
        final Point expected = this.geometryFactory.createPoint(new CoordinateXY(5.5F, 4.4F));
        assertEquals(expected, actual);
    }

    private static LatLngAlt createLatLngAlt(float latitude, float longitude) {
        return new LatLngAltImpl(
                LAT_LNG_ALT_DATE_TIME_SUPPLIER.get(),
                latitude, longitude,
                LAT_LNG_ALT_ALTITUDE,
                LAT_LNG_ALT_SPEED,
                LAT_LNG_ALT_VALID
        );
    }
}
