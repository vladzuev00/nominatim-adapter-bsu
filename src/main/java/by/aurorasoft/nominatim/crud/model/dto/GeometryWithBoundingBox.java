package by.aurorasoft.nominatim.crud.model.dto;

import lombok.Value;
import org.locationtech.jts.geom.Geometry;

@Value
public class GeometryWithBoundingBox {
    Geometry geometry;
    Geometry boundingBox;
}
