package by.aurorasoft.nominatim.service.mileage;

import by.aurorasoft.nominatim.crud.service.CityService;
import by.aurorasoft.nominatim.rest.model.MileageRequest;
import by.aurorasoft.nominatim.rest.model.MileageResponse;
import by.nhorushko.distancecalculator.*;
import by.nhorushko.trackfilter.TrackFilter;
import lombok.Value;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.rangeClosed;

@Service
public final class MileageService {
    private static final double EPSILON_TO_FILTER_TRACK_POINTS = 0.00015;

    private final TrackFilter trackFilter;
    private final DistanceCalculator distanceCalculator;
    private final GeometryCreatingService geometryCreatingService;
    private final CityService cityService;
    private Map<PreparedGeometry, PreparedGeometry> citiesGeometriesByBoundingBoxes;

    public MileageService(TrackFilter trackFilter, DistanceCalculator distanceCalculator,
                          GeometryCreatingService geometryCreatingService, CityService cityService) {
        this.trackFilter = trackFilter;
        this.distanceCalculator = distanceCalculator;
        this.geometryCreatingService = geometryCreatingService;
        this.cityService = cityService;
        this.citiesGeometriesByBoundingBoxes = null;
    }

    public MileageResponse findMileage(MileageRequest request) {
        final DistanceCalculatorSettings distanceCalculatorSettings = new DistanceCalculatorSettingsImpl(
                request.getMinDetectionSpeed(), request.getMaxMessageTimeout());
        final Map<Boolean, Double> mileagesByLocatedInCity = this.findMileagesByLocatedInCity(
                request.getTrackPoints(), distanceCalculatorSettings);
        return new MileageResponse(
                mileagesByLocatedInCity.get(true),
                mileagesByLocatedInCity.get(false)
        );
    }

    void setCitiesGeometriesByBoundingBoxes(Map<PreparedGeometry, PreparedGeometry> citiesGeometriesByBoundingBoxes) {
        this.citiesGeometriesByBoundingBoxes = citiesGeometriesByBoundingBoxes;
    }

    private Map<Boolean, Double> findMileagesByLocatedInCity(List<? extends LatLngAlt> trackPoints,
                                                             DistanceCalculatorSettings distanceCalculatorSettings) {
        final List<PreparedGeometry> intersectedCitiesGeometries
                = this.findGeometriesIntersectedByLineStringOfPoints(trackPoints);
        final int indexPenultimatePoint = trackPoints.size() - 2;
        return rangeClosed(0, indexPenultimatePoint)
                .mapToObj(i -> new TrackSlice(
                        trackPoints.get(i),
                        trackPoints.get(i + 1),
                        //slices, which is located in city, must have second point, which is located in city
                        this.isAnyGeometryContainPoint(trackPoints.get(i + 1), intersectedCitiesGeometries)
                ))
                .collect(
                        partitioningBy(
                                TrackSlice::isLocatedInCity,
                                summingDouble(slice -> this.calculateDistance(slice, distanceCalculatorSettings))
                        )
                );
    }

    private double calculateDistance(TrackSlice trackSlice, DistanceCalculatorSettings distanceCalculatorSettings) {
        return this.distanceCalculator.calculateDistance(trackSlice.firstPoint, trackSlice.secondPoint,
                distanceCalculatorSettings);
    }

    private List<PreparedGeometry> findGeometriesIntersectedByLineStringOfPoints(
            List<? extends LatLngAlt> trackPoints) {
        final LineString lineString = this.createLineStringByFilteredPoints(trackPoints);
        return this.citiesGeometriesByBoundingBoxes != null
                ? this.findGeometriesIntersectedByLineStringByLoadedPreparedGeometries(lineString)
                : this.cityService.findPreparedGeometriesWhoseBoundingBoxIntersectedByLineString(lineString);
    }

    private LineString createLineStringByFilteredPoints(List<? extends LatLngAlt> notFilteredPoints) {
        final List<? extends LatLngAlt> significantTrackPointsToCreateLineString = this.trackFilter.filter(
                notFilteredPoints, EPSILON_TO_FILTER_TRACK_POINTS);
        return this.geometryCreatingService.createLineString(significantTrackPointsToCreateLineString);
    }

    private List<PreparedGeometry> findGeometriesIntersectedByLineStringByLoadedPreparedGeometries(LineString lineString) {
        return this.citiesGeometriesByBoundingBoxes
                .entrySet()
                .stream()
                .filter(geometryByBoundingBox -> geometryByBoundingBox.getKey().intersects(lineString))
                .map(Map.Entry::getValue)
                .collect(toList());
    }

    private boolean isAnyGeometryContainPoint(LatLngAlt latLngAlt, List<PreparedGeometry> geometries) {
        final Point point = this.geometryCreatingService.createPoint(latLngAlt);
        return geometries
                .stream()
                .anyMatch(geometry -> geometry.contains(point));
    }

    @Value
    private static class TrackSlice {
        LatLngAlt firstPoint;
        LatLngAlt secondPoint;
        boolean locatedInCity;
    }
}
