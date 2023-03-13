package by.aurorasoft.nominatim.service.searchcity;

import by.aurorasoft.nominatim.crud.model.dto.City;
import by.aurorasoft.nominatim.crud.model.dto.Coordinate;
import by.aurorasoft.nominatim.crud.model.dto.NominatimReverseResponse;
import by.aurorasoft.nominatim.crud.model.dto.NominatimReverseResponse.ExtraTags;
import by.aurorasoft.nominatim.service.nominatim.NominatimService;
import by.aurorasoft.nominatim.rest.mapper.NominatimReverseResponseToCityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public final class SearchCityService {
    private static final String REGEX_PLACE_VALUE_IN_JSON_OF_CITY = "(city)|(town)";

    private final NominatimService nominatimService;
    private final NominatimReverseResponseToCityMapper mapper;

    public Collection<City> findByCoordinates(List<Coordinate> coordinates) {
        return coordinates.stream()
                .map(this.nominatimService::reverse)
                .filter(SearchCityService::isCity)
                .map(this.mapper::map)
                .collect(toList());
    }

    private static boolean isCity(NominatimReverseResponse response) {
        final ExtraTags extraTags = response.getExtratags();
        return extraTags != null
                && extraTags.getPlace() != null
                && extraTags.getPlace().matches(REGEX_PLACE_VALUE_IN_JSON_OF_CITY);
    }
}
