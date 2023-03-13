package by.aurorasoft.nominatim.config;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeometryFactoryConfig {
    private static final int GEOMETRY_FACTORY_SRID = 4326;

    @Bean
    public GeometryFactory geometryFactory() {
        return new GeometryFactory(this.precisionModel(), GEOMETRY_FACTORY_SRID);
    }

    @Bean
    public PrecisionModel precisionModel() {
        return new PrecisionModel();
    }
}
