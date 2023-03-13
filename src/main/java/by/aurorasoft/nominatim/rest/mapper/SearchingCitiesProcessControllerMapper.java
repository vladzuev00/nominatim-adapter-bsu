package by.aurorasoft.nominatim.rest.mapper;

import by.aurorasoft.nominatim.crud.model.dto.SearchingCitiesProcess;
import by.aurorasoft.nominatim.rest.model.SearchingCitiesProcessPageResponse;
import by.aurorasoft.nominatim.rest.model.SearchingCitiesProcessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public final class SearchingCitiesProcessControllerMapper {
    private final GeoJSONWriter geoJSONWriter;

    public SearchingCitiesProcessResponse mapToResponse(SearchingCitiesProcess mapped) {
        return SearchingCitiesProcessResponse.builder()
                .id(mapped.getId())
                .geometry(this.geoJSONWriter.write(mapped.getGeometry()))
                .searchStep(mapped.getSearchStep())
                .totalPoints(mapped.getTotalPoints())
                .handledPoints(mapped.getHandledPoints())
                .status(mapped.getStatus())
                .build();
    }

    public List<SearchingCitiesProcessResponse> mapToResponses(List<SearchingCitiesProcess> mapped) {
        return mapped.stream()
                .map(this::mapToResponse)
                .collect(toList());
    }

    public SearchingCitiesProcessPageResponse mapToResponse(int pageNumber, int pageSize,
                                                            List<SearchingCitiesProcess> processes) {
        return SearchingCitiesProcessPageResponse.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .processes(this.mapToResponses(processes))
                .build();
    }
}
