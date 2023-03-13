package by.aurorasoft.nominatim.rest.model;

import by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.wololo.geojson.Geometry;

/**
 * equals and hashcode doesn't work correctly because of geometry doesn't override them
 */
@Value
@Builder
@AllArgsConstructor
public class SearchingCitiesProcessResponse {
    Long id;
    Geometry geometry;
    double searchStep;
    long totalPoints;
    long handledPoints;
    Status status;
}