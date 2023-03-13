package by.aurorasoft.nominatim.rest.mapper;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.crud.model.dto.City;
import by.aurorasoft.nominatim.crud.model.dto.NominatimReverseResponse;
import by.aurorasoft.nominatim.crud.model.dto.NominatimReverseResponse.ExtraTags;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.wololo.jts2geojson.GeoJSONReader;

import static by.aurorasoft.nominatim.crud.model.entity.CityEntity.Type.*;
import static org.junit.Assert.assertEquals;

public final class NominatimReverseResponseToCityMapperTest extends AbstractContextTest {

    @Autowired
    private NominatimReverseResponseToCityMapper mapper;

    @Autowired
    private GeoJSONReader geoJSONReader;

    @Test
    public void responseShouldBeMappedToCapitalCity() {
        final NominatimReverseResponse givenResponse = NominatimReverseResponse.builder()
                .name("Minsk")
                .extratags(ExtraTags.builder()
                        .place("city")
                        .capital("yes")
                        .build())
                .geojson(findGeoJson())
                .build();

        final City actual = this.mapper.map(givenResponse);

        final Geometry expectedGeometry = this.geoJSONReader.read(findGeoJson());
        final City expected = City.builder()
                .name("Minsk")
                .geometry(expectedGeometry)
                .type(CAPITAL)
                .boundingBox(expectedGeometry.getEnvelope())
                .build();

        assertEquals(expected, actual);
    }

    @Test
    public void responseShouldBeMappedToRegionalCity() {
        final NominatimReverseResponse givenResponse = NominatimReverseResponse.builder()
                .name("Mogilev")
                .extratags(ExtraTags.builder()
                        .place("city")
                        .capital("4")
                        .build())
                .geojson(findGeoJson())
                .build();

        final City actual = this.mapper.map(givenResponse);

        final Geometry expectedGeometry = this.geoJSONReader.read(findGeoJson());
        final City expected = City.builder()
                .name("Mogilev")
                .geometry(expectedGeometry)
                .type(REGIONAL)
                .boundingBox(expectedGeometry.getEnvelope())
                .build();

        assertEquals(expected, actual);
    }

    @Test
    public void responseShouldBeMappedToCityWithNotDefinedType() {
        final NominatimReverseResponse givenResponse = NominatimReverseResponse.builder()
                .name("Vileyka")
                .extratags(ExtraTags.builder()
                        .place("city")
                        .capital("any text")
                        .build())
                .geojson(findGeoJson())
                .build();

        final City actual = this.mapper.map(givenResponse);

        final Geometry expectedGeometry = this.geoJSONReader.read(findGeoJson());
        final City expected = City.builder()
                .name("Vileyka")
                .geometry(expectedGeometry)
                .type(NOT_DEFINED)
                .boundingBox(expectedGeometry.getEnvelope())
                .build();

        assertEquals(expected, actual);
    }

    private static String findGeoJson() {
        return "{\n" +
                "  \"type\": \"MultiPolygon\",\n" +
                "  \"coordinates\": [\n" +
                "    [\n" +
                "        [\n" +
                "            [-99.028, 46.985], [-99.028, 50.979],\n" +
                "            [-82.062, 50.979], [-82.062, 47.002],\n" +
                "            [-99.028, 46.985]\n" +
                "        ]\n" +
                "    ],\n" +
                "    [\n" +
                "        [\n" +
                "            [-109.028, 36.985], [-109.028, 40.979],\n" +
                "            [-102.062, 40.979], [-102.062, 37.002],\n" +
                "            [-109.028, 36.985]\n" +
                "        ]\n" +
                "     ]\n" +
                "  ]\n" +
                "}";
    }
}
