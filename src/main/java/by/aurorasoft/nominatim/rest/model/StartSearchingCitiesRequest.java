package by.aurorasoft.nominatim.rest.model;

import by.aurorasoft.nominatim.crud.model.dto.AreaCoordinate;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@Value
@Builder
public class StartSearchingCitiesRequest {

    @NotNull
    @Valid
    AreaCoordinate bbox;

    @NotNull
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "5")
    Double searchStep;

    @JsonCreator
    public StartSearchingCitiesRequest(@JsonProperty("bbox") AreaCoordinate bbox,
                                       @JsonProperty("searchStep") Double searchStep) {
        this.bbox = bbox;
        this.searchStep = searchStep;
    }
}
