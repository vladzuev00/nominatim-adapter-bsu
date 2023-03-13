package by.aurorasoft.nominatim.service.searchcity;

import by.aurorasoft.nominatim.crud.model.dto.City;
import by.aurorasoft.nominatim.crud.model.dto.Coordinate;
import by.aurorasoft.nominatim.crud.model.dto.NominatimReverseResponse;
import by.aurorasoft.nominatim.crud.model.dto.NominatimReverseResponse.ExtraTags;
import by.aurorasoft.nominatim.service.nominatim.NominatimService;
import by.aurorasoft.nominatim.rest.mapper.NominatimReverseResponseToCityMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class SearchCityServiceTest {

    @Mock
    private NominatimService mockedNominatimService;

    @Mock
    private NominatimReverseResponseToCityMapper mockedMapper;

    private SearchCityService searchCityService;

    @Captor
    private ArgumentCaptor<Coordinate> coordinateArgumentCaptor;

    @Captor
    private ArgumentCaptor<NominatimReverseResponse> responseArgumentCaptor;

    @Before
    public void initializeSearchCityService() {
        this.searchCityService = new SearchCityService(this.mockedNominatimService, this.mockedMapper);
    }

    @Test
    public void citiesShouldBeFoundByCoordinates() {
        final List<Coordinate> givenCoordinates = List.of(
                mock(Coordinate.class),
                mock(Coordinate.class),
                mock(Coordinate.class),
                mock(Coordinate.class),
                mock(Coordinate.class));

        final List<NominatimReverseResponse> givenResponses
                = List.of(
                createResponseByPlace("city"),
                createResponseByPlace("town"),
                createResponseByPlace(null),
                createResponseByPlace("not defined"),
                createResponseByExtraTags(null));
        when(this.mockedNominatimService.reverse(any(Coordinate.class)))
                .thenReturn(givenResponses.get(0))
                .thenReturn(givenResponses.get(1))
                .thenReturn(givenResponses.get(2))
                .thenReturn(givenResponses.get(3))
                .thenReturn(givenResponses.get(4));

        final List<City> expected = List.of(createCity("Minsk"), createCity("Mogilev"));
        when(this.mockedMapper.map(any(NominatimReverseResponse.class)))
                .thenReturn(expected.get(0))
                .thenReturn(expected.get(1));

        final Collection<City> actual = this.searchCityService.findByCoordinates(givenCoordinates);
        assertEquals(expected, actual);

        verify(this.mockedNominatimService, times(5))
                .reverse(this.coordinateArgumentCaptor.capture());
        verify(this.mockedMapper, times(2)).map(this.responseArgumentCaptor.capture());

        assertEquals(givenCoordinates, this.coordinateArgumentCaptor.getAllValues());
        assertEquals(
                List.of(createResponseByPlace("city"), createResponseByPlace("town")),
                this.responseArgumentCaptor.getAllValues());
    }

    @SuppressWarnings("all")
    private static NominatimReverseResponse createResponseByExtraTags(ExtraTags extraTags) {
        return NominatimReverseResponse.builder()
                .extratags(extraTags)
                .build();
    }

    private static NominatimReverseResponse createResponseByPlace(String place) {
        return NominatimReverseResponse.builder()
                .extratags(ExtraTags.builder()
                        .place(place)
                        .build())
                .build();
    }

    private static City createCity(String name) {
        return City.builder()
                .name(name)
                .build();
    }
}
