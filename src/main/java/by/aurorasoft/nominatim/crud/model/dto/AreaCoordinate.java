package by.aurorasoft.nominatim.crud.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Value
public class AreaCoordinate {

    @NotNull
    @Valid
    Coordinate leftBottom;

    @NotNull
    @Valid
    Coordinate rightUpper;

    public AreaCoordinate(@JsonProperty("leftBottom") Coordinate leftBottom,
                          @JsonProperty("rightUpper") Coordinate rightUpper) {
        this.leftBottom = leftBottom;
        this.rightUpper = rightUpper;
    }
}
