package by.aurorasoft.nominatim.crud.model.dto;

import by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status;
import by.nhorushko.crudgeneric.v2.domain.AbstractDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.locationtech.jts.geom.Geometry;

@Value
@AllArgsConstructor
@Builder
public class SearchingCitiesProcess implements AbstractDto<Long> {
    Long id;
    Geometry geometry;
    double searchStep;
    long totalPoints;
    long handledPoints;
    Status status;
}
