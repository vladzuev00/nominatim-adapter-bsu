package by.aurorasoft.nominatim.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

@Configuration
public class GeoJsonConfig {

    @Bean
    public GeoJSONReader geoJsonReader() {
        return new GeoJSONReader();
    }

    @Bean
    public GeoJSONWriter geoJSONWriter() {
        return new GeoJSONWriter();
    }
}
