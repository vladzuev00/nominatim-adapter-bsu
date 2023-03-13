package by.aurorasoft.nominatim.rest.model;

import by.aurorasoft.nominatim.crud.model.entity.CityEntity.Type;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import org.wololo.geojson.Geometry;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * equals and hashcode doesn't work correctly because of geometry doesn't override them
 */
@Value
@Builder
public class CityRequest {

    @NotNull
    @Pattern(regexp = "^[a-zA-Z]+(?:[\\s-][a-zA-Z]+)*$")
    String name;

    @NotNull
    Geometry geometry;

    @NotNull
    Type type;

    @JsonCreator
    public CityRequest(@JsonProperty("name") String name,
                       @JsonProperty("geometry") Geometry geometry,
                       @JsonProperty("type") Type type) {
        this.name = name;
        this.geometry = geometry;
        this.type = type;
    }
}
