package by.aurorasoft.nominatim.crud.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Builder;
import lombok.Value;

import java.io.IOException;

@Value
@Builder
public class NominatimReverseResponse {

    String name;

    ExtraTags extratags;

    String geojson;

    @JsonCreator
    public NominatimReverseResponse(@JsonProperty("name") String name,
                                    @JsonProperty("extratags") ExtraTags extratags,
                                    @JsonDeserialize(using = ToStringJsonDeserializer.class)
                                    @JsonProperty("geojson") String geojson) {
        this.name = name;
        this.extratags = extratags;
        this.geojson = geojson;
    }

    @Value
    @Builder
    public static class ExtraTags {
        String place;
        String capital;

        @JsonCreator
        public ExtraTags(@JsonProperty("place") String place,
                         @JsonProperty("capital") String capital) {
            this.place = place;
            this.capital = capital;
        }
    }

    static final class ToStringJsonDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser jsonParser, DeserializationContext context)
                throws IOException {
            final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
            final JsonNode node = mapper.readTree(jsonParser);
            return mapper.writeValueAsString(node);
        }
    }
}
