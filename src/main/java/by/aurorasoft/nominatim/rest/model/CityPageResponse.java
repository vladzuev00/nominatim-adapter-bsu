package by.aurorasoft.nominatim.rest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * equals and hashcode doesn't work correctly because of geometry in CityResponse doesn't override them
 */
@Value
@AllArgsConstructor
@Builder
public class CityPageResponse {
    int pageNumber;
    int pageSize;
    List<CityResponse> cities;
}
