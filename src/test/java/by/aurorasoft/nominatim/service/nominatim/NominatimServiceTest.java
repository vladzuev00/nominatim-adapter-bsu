package by.aurorasoft.nominatim.service.nominatim;

import by.aurorasoft.nominatim.config.RestTemplateConfig;
import by.aurorasoft.nominatim.crud.model.dto.Coordinate;
import by.aurorasoft.nominatim.crud.model.dto.NominatimReverseResponse;
import by.aurorasoft.nominatim.crud.model.dto.NominatimReverseResponse.ExtraTags;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Locale.ROOT;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@Import(RestTemplateConfig.class)
@RestClientTest(NominatimService.class)
public class NominatimServiceTest {
    private static final String TEMPLATE_REVERSE_URI
            = "http://geo.aurora-soft.by:8081/reverse?lat=%f&lon=%f&zoom=10&format=jsonv2&polygon_geojson=1&extratags=1";

    @Autowired
    private NominatimService service;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${nominatim.millis-between-requests}")
    private long millisBetweenRequests;

    @Test
    public void reverseOperationShouldBeSuccessForEachCoordinateAndDurationBetweenRequestShouldBeRespected()
            throws JsonProcessingException {
        final TimeSendingRequestControllingInterceptor interceptor = new TimeSendingRequestControllingInterceptor();
        this.restTemplate.getInterceptors().add(interceptor);

        final List<Coordinate> givenCoordinates = List.of(
                new Coordinate(53.881033, 27.544367),
                new Coordinate(54.881033, 27.544367),
                new Coordinate(55.881033, 27.544367));

        final NominatimReverseResponse givenResponse = NominatimReverseResponse.builder()
                .name("Minsk")
                .extratags(ExtraTags.builder()
                        .place("city")
                        .capital("yes")
                        .build())
                .geojson("geojson")
                .build();
        final String givenJsonResponse = this.objectMapper.writeValueAsString(givenResponse);

        this.server.expect(
                        times(givenCoordinates.size()),
                        requestTo(
                                oneOf(
                                        createReverseUriByCoordinate(givenCoordinates.get(0)),
                                        createReverseUriByCoordinate(givenCoordinates.get(1)),
                                        createReverseUriByCoordinate(givenCoordinates.get(2))
                                )
                        )
                )
                .andRespond(withSuccess(givenJsonResponse, APPLICATION_JSON));

        givenCoordinates.forEach(this.service::reverse);

        assertTrue(interceptor.isDurationBetweenRequestsRespected);
    }

    private static String createReverseUriByCoordinate(Coordinate coordinate) {
        return format(ROOT, TEMPLATE_REVERSE_URI, coordinate.getLatitude(), coordinate.getLongitude());
    }

    private final class TimeSendingRequestControllingInterceptor implements ClientHttpRequestInterceptor {
        private static final long INITIAL_TIME_MILLIS_PREVIOUS_REQUEST = 0;
        private static final boolean INITIAL_IS_DURATION_BETWEEN_REQUESTS_RESPECTED = true;

        private long timeMillisPreviousRequest;
        private boolean isDurationBetweenRequestsRespected;

        public TimeSendingRequestControllingInterceptor() {
            this.timeMillisPreviousRequest = INITIAL_TIME_MILLIS_PREVIOUS_REQUEST;
            this.isDurationBetweenRequestsRespected = INITIAL_IS_DURATION_BETWEEN_REQUESTS_RESPECTED;
        }

        @Override
        public synchronized @NotNull ClientHttpResponse intercept(@NotNull HttpRequest request,
                                                                  byte @NotNull [] body,
                                                                  @NotNull ClientHttpRequestExecution execution)
                throws IOException {
            final long currentTimeMillis = currentTimeMillis();
            if (this.isDurationBetweenRequestsRespected
                    && currentTimeMillis - this.timeMillisPreviousRequest < NominatimServiceTest.this.millisBetweenRequests) {
                this.isDurationBetweenRequestsRespected = false;
            } else if (this.isDurationBetweenRequestsRespected) {
                this.timeMillisPreviousRequest = currentTimeMillis;
            }
            return execution.execute(request, body);
        }
    }
}
