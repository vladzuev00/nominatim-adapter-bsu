package by.aurorasoft.nominatim.crud.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@Value
public class Coordinate {

    @NotNull
    @DecimalMin("-90")
    @DecimalMax("90")
    Double latitude;

    @NotNull
    @DecimalMin("-180")
    @DecimalMax("180")
    Double longitude;

    public Coordinate(@JsonProperty("latitude") Double latitude,
                      @JsonProperty("longitude") Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
