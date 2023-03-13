package by.aurorasoft.nominatim.config;

import by.nhorushko.distancecalculator.DistanceCalculator;
import by.nhorushko.distancecalculator.DistanceCalculatorImpl;
import by.nhorushko.trackfilter.TrackFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MileageConfig {

    @Bean
    public TrackFilter trackFilter() {
        return new TrackFilter();
    }

    @Bean
    public DistanceCalculator distanceCalculator() {
        return new DistanceCalculatorImpl();
    }
}
