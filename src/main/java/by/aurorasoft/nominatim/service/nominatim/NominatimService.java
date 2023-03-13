package by.aurorasoft.nominatim.service.nominatim;

import by.aurorasoft.nominatim.crud.model.dto.Coordinate;
import by.aurorasoft.nominatim.crud.model.dto.NominatimReverseResponse;
import by.aurorasoft.nominatim.service.nominatim.exception.NominatimClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.springframework.http.HttpEntity.EMPTY;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@Service
public final class NominatimService implements AutoCloseable {
    private static final ParameterizedTypeReference<NominatimReverseResponse> PARAMETERIZED_TYPE_REFERENCE
            = new ParameterizedTypeReference<>() {
    };

    private final RestTemplate restTemplate;
    private final long millisBetweenRequests;
    private final ExecutorService executorService;
    private final Lock lock;
    private final Condition condition;
    private boolean durationBetweenRequestsPassed;

    public NominatimService(RestTemplate restTemplate,
                            @Value("${nominatim.millis-between-requests}") long millisBetweenRequests) {
        this.restTemplate = restTemplate;
        this.millisBetweenRequests = millisBetweenRequests;
        this.executorService = newSingleThreadExecutor();
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
        this.durationBetweenRequestsPassed = true;
    }

    @PostConstruct
    public void runTaskWaitingNecessaryDurationBetweenRequests() {
        this.executorService.submit(this.createTaskWaitingNecessaryDurationBetweenRequests());
    }

    public NominatimReverseResponse reverse(Coordinate coordinate) {
        this.lock.lock();
        try {
            while (!this.durationBetweenRequestsPassed) {
                this.condition.await();
            }
            final NominatimReverseResponse response = this.doRequest(coordinate);
            this.durationBetweenRequestsPassed = false;
            this.condition.signalAll();
            return response;
        } catch (final InterruptedException cause) {
            currentThread().interrupt();
            throw new NominatimClientException(cause);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    @PreDestroy
    public void close() {
        this.executorService.shutdownNow();
    }

    private Runnable createTaskWaitingNecessaryDurationBetweenRequests() {
        return () -> {
            this.lock.lock();
            try {
                while (!currentThread().isInterrupted()) {
                    while (this.durationBetweenRequestsPassed) {
                        this.condition.await();
                    }
                    MILLISECONDS.sleep(this.millisBetweenRequests);
                    this.durationBetweenRequestsPassed = true;
                    this.condition.signalAll();
                }
            } catch (final InterruptedException cause) {
                currentThread().interrupt();
            } finally {
                this.lock.unlock();
            }
        };
    }

    private NominatimReverseResponse doRequest(Coordinate coordinate) {
        final String uri = createUri(coordinate);
        final ResponseEntity<NominatimReverseResponse> responseEntity = this.restTemplate.exchange(
                uri, GET, EMPTY, PARAMETERIZED_TYPE_REFERENCE);
        return responseEntity.getBody();
    }

    private static String createUri(Coordinate coordinate) {
        return new NominatimReverseUriBuilder()
                .catalogLatitude(coordinate.getLatitude())
                .catalogLongitude(coordinate.getLongitude())
                .build();
    }

    private static final class NominatimReverseUriBuilder {
        private static final String EXCEPTION_DESCRIPTION_URI_BUILDING_BY_NOT_DEFINED_COORDINATE
                = "Uri was build by not defined coordinates.";

        private static final String URI_WITHOUT_PARAMETERS = "http://geo.aurora-soft.by:8081/reverse";
        private static final String PARAM_NAME_LATITUDE = "lat";
        private static final String PARAM_NAME_LONGITUDE = "lon";
        private static final String PARAM_NAME_ZOOM = "zoom";
        private static final String PARAM_NAME_FORMAT = "format";
        private static final String PARAM_NAME_POLYGON_GEOJSON = "polygon_geojson";
        private static final String PARAM_NAME_EXTRATAGS = "extratags";

        private static final int PARAM_VALUE_ZOOM = 10;
        private static final String PARAM_VALUE_FORMAT = "jsonv2";
        private static final int PARAM_VALUE_POLYGON_GEOJSON = 1;
        private static final int PARAM_VALUE_EXTRATAGS = 1;

        private double latitude;
        private double longitude;

        public NominatimReverseUriBuilder() {
            this.latitude = NaN;
            this.longitude = NaN;
        }

        public NominatimReverseUriBuilder catalogLatitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public NominatimReverseUriBuilder catalogLongitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public String build() {
            if (isNaN(this.latitude) || isNaN(this.longitude)) {
                throw new IllegalStateException(EXCEPTION_DESCRIPTION_URI_BUILDING_BY_NOT_DEFINED_COORDINATE);
            }
            return fromUriString(URI_WITHOUT_PARAMETERS)
                    .queryParam(PARAM_NAME_LATITUDE, this.latitude)
                    .queryParam(PARAM_NAME_LONGITUDE, this.longitude)
                    .queryParam(PARAM_NAME_ZOOM, PARAM_VALUE_ZOOM)
                    .queryParam(PARAM_NAME_FORMAT, PARAM_VALUE_FORMAT)
                    .queryParam(PARAM_NAME_POLYGON_GEOJSON, PARAM_VALUE_POLYGON_GEOJSON)
                    .queryParam(PARAM_NAME_EXTRATAGS, PARAM_VALUE_EXTRATAGS)
                    .build()
                    .toUriString();
        }
    }
}
