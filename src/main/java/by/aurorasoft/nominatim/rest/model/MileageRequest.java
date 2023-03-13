package by.aurorasoft.nominatim.rest.model;

import by.nhorushko.distancecalculator.LatLngAlt;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.Instant;
import java.util.List;

import static java.lang.Float.NaN;
import static java.lang.Integer.MIN_VALUE;

@Value
@Builder
public class MileageRequest {

    @NotNull
    @Size(min = 2)
    List<@Valid TrackPoint> trackPoints;

    @NotNull
    @Min(0)
    Integer minDetectionSpeed;

    @NotNull
    @Min(0)
    Integer maxMessageTimeout;

    @JsonCreator
    public MileageRequest(@JsonProperty("trackPoints") List<TrackPoint> trackPoints,
                          @JsonProperty("minDetectionSpeed") Integer minDetectionSpeed,
                          @JsonProperty("maxMessageTimeout") Integer maxMessageTimeout) {
        this.trackPoints = trackPoints;
        this.minDetectionSpeed = minDetectionSpeed;
        this.maxMessageTimeout = maxMessageTimeout;
    }

    @Value
    @Builder
    public static class TrackPoint implements LatLngAlt {
        private static final float NOT_DEFINED_LATITUDE_VALUE = NaN;
        private static final float NOT_DEFINED_LONGITUDE_VALUE = NaN;
        private static final int NOT_DEFINED_ALTITUDE_VALUE = MIN_VALUE;
        private static final int NOT_DEFINED_SPEED_VALUE = MIN_VALUE;
        private static final boolean NOT_DEFINED_VALID_VALUE = false;

        @NotNull
        @PastOrPresent
        Instant datetime;

        @NotNull
        @DecimalMin("-90")
        @DecimalMax("90")
        Float latitude;

        @NotNull
        @DecimalMin("-180")
        @DecimalMax("180")
        Float longitude;

        @NotNull
        Integer altitude;

        @NotNull
        @Min(0)
        @Max(1000)
        Integer speed;

        @NotNull
        Boolean valid;

        @JsonCreator
        public TrackPoint(@JsonProperty("datetime") Instant datetime,
                          @JsonProperty("latitude") Float latitude,
                          @JsonProperty("longitude") Float longitude,
                          @JsonProperty("altitude") Integer altitude,
                          @JsonProperty("speed") Integer speed,
                          @JsonProperty("valid") Boolean valid) {
            this.datetime = datetime;
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
            this.speed = speed;
            this.valid = valid;
        }

        @Override
        public float getLatitude() {
            return this.latitude != null ? this.latitude : NOT_DEFINED_LATITUDE_VALUE;
        }

        @Override
        public float getLongitude() {
            return this.longitude != null ? this.longitude : NOT_DEFINED_LONGITUDE_VALUE;
        }

        @Override
        public int getAltitude() {
            return this.altitude != null ? this.altitude : NOT_DEFINED_ALTITUDE_VALUE;
        }

        @Override
        public int getSpeed() {
            return this.speed != null ? this.speed : NOT_DEFINED_SPEED_VALUE;
        }

        @Override
        public boolean isValid() {
            return this.valid != null ? this.valid : NOT_DEFINED_VALID_VALUE;
        }
    }
}
