package by.aurorasoft.nominatim.service.mileage;

import by.aurorasoft.nominatim.crud.service.CityService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class LoaderCitiesGeometriesAndBoundingBoxesTest {

    @Mock
    private MileageService mockedMileageService;

    @Mock
    private CityService mockedCityService;

    @Captor
    private ArgumentCaptor<Map<PreparedGeometry, PreparedGeometry>> geometriesByGeometriesArgumentCaptor;

    @Test
    @SuppressWarnings("unchecked")
    public void cityGeometriesShouldBeLoaded() {
        final LoaderCitiesGeometriesAndBoundingBoxes givenLoader = this.createLoader(true);

        final Map<PreparedGeometry, PreparedGeometry> givenGeometriesByBoundingBoxes = mock(Map.class);
        when(this.mockedCityService.findPreparedGeometriesByPreparedBoundingBoxes())
                .thenReturn(givenGeometriesByBoundingBoxes);

        givenLoader.injectCitiesGeometriesAndBoundingBoxes();

        verify(this.mockedCityService, times(1))
                .findPreparedGeometriesByPreparedBoundingBoxes();
        verify(this.mockedMileageService, times(1))
                .setCitiesGeometriesByBoundingBoxes(this.geometriesByGeometriesArgumentCaptor.capture());

        assertSame(givenGeometriesByBoundingBoxes, this.geometriesByGeometriesArgumentCaptor.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void cityGeometriesShouldNotBeLoaded() {
        final LoaderCitiesGeometriesAndBoundingBoxes givenLoader = this.createLoader(false);

        givenLoader.injectCitiesGeometriesAndBoundingBoxes();

        verify(this.mockedCityService, times(0))
                .findPreparedGeometriesByPreparedBoundingBoxes();
        verify(this.mockedMileageService, times(0))
                .setCitiesGeometriesByBoundingBoxes(any(Map.class));
    }

    private LoaderCitiesGeometriesAndBoundingBoxes createLoader(boolean cityGeometriesShouldBeLoaded) {
        return new LoaderCitiesGeometriesAndBoundingBoxes(
                this.mockedMileageService,
                this.mockedCityService,
                cityGeometriesShouldBeLoaded
        );
    }
}
