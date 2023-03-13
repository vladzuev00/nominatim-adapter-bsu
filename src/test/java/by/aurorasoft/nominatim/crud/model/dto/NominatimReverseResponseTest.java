package by.aurorasoft.nominatim.crud.model.dto;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.crud.model.dto.NominatimReverseResponse.ExtraTags;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public final class NominatimReverseResponseTest extends AbstractContextTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void jsonShouldBeConvertedToResponse()
            throws JsonProcessingException {
        final String givenJson = "{"
                + "\"name\": \"Minsk\","
                + "\"extratags\" : {"
                + "\"place\": \"city\","
                + "\"capital\": \"yes\""
                + "},"
                + "\"geojson\": \"geojson\""
                + "}";

        final NominatimReverseResponse actual = this.objectMapper.readValue(givenJson, NominatimReverseResponse.class);
        final NominatimReverseResponse expected = NominatimReverseResponse.builder()
                .name("Minsk")
                .extratags(ExtraTags.builder()
                        .place("city")
                        .capital("yes")
                        .build())
                .geojson("\"geojson\"")
                .build();
        assertEquals(expected, actual);
    }

    @Test
    public void responseShouldBeConvertedToJson() throws JsonProcessingException {
        final NominatimReverseResponse givenResponse = NominatimReverseResponse.builder()
                .name("Minsk")
                .extratags(ExtraTags.builder()
                        .place("city")
                        .capital("yes")
                        .build())
                .geojson("geojson")
                .build();

        final String actual = this.objectMapper.writeValueAsString(givenResponse);
        final String expected = "{"
                + "\"name\":\"Minsk\","
                + "\"extratags\":{"
                + "\"place\":\"city\","
                + "\"capital\":\"yes\""
                + "},"
                + "\"geojson\":\"geojson\""
                + "}";
        assertEquals(expected, actual);
    }
}
