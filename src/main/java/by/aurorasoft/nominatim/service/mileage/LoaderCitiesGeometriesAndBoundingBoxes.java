package by.aurorasoft.nominatim.service.mileage;

import by.aurorasoft.nominatim.crud.service.CityService;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LoaderCitiesGeometriesAndBoundingBoxes {
    private final MileageService mileageService;
    private final CityService cityService;
    private final boolean cityGeometriesShouldBeLoaded;

    public LoaderCitiesGeometriesAndBoundingBoxes(MileageService mileageService, CityService cityService,
                                                  @Value("${search-mileage.load-city-geometries-on-start-application}")
                                                  boolean cityGeometriesShouldBeLoaded) {
        this.mileageService = mileageService;
        this.cityService = cityService;
        this.cityGeometriesShouldBeLoaded = cityGeometriesShouldBeLoaded;
    }

    @EventListener(classes = ApplicationReadyEvent.class)
    public void injectCitiesGeometriesAndBoundingBoxes() {
        if (this.cityGeometriesShouldBeLoaded) {
            final Map<PreparedGeometry, PreparedGeometry> citiesGeometriesByBoundingBoxes = this.cityService
                    .findPreparedGeometriesByPreparedBoundingBoxes();
            this.mileageService.setCitiesGeometriesByBoundingBoxes(citiesGeometriesByBoundingBoxes);
        }
    }
}
