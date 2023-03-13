package by.aurorasoft.nominatim.service.searchcity;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import by.aurorasoft.nominatim.crud.model.dto.*;
import by.aurorasoft.nominatim.crud.model.dto.NominatimReverseResponse.ExtraTags;
import by.aurorasoft.nominatim.crud.model.entity.CityEntity;
import by.aurorasoft.nominatim.crud.model.entity.CityEntity.Type;
import by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity;
import by.aurorasoft.nominatim.crud.service.SearchingCitiesProcessService;
import by.aurorasoft.nominatim.service.exception.FindingCitiesException;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestTemplate;
import org.wololo.jts2geojson.GeoJSONReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static by.aurorasoft.nominatim.crud.model.entity.CityEntity.Type.CAPITAL;
import static by.aurorasoft.nominatim.crud.model.entity.CityEntity.Type.REGIONAL;
import static by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status.ERROR;
import static by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status.SUCCESS;
import static by.aurorasoft.nominatim.util.StreamUtil.asStream;
import static java.lang.Class.forName;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpEntity.EMPTY;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

public final class StartingSearchingCitiesProcessServiceIT extends AbstractContextTest {
    private static final String CLASS_NAME_AREA_ITERATOR
            = "by.aurorasoft.nominatim.service.searchcity.StartingSearchingCitiesProcessService$AreaIterator";
    private static final String CLASS_NAME_SUB_AREA_ITERATOR
            = "by.aurorasoft.nominatim.service.searchcity.StartingSearchingCitiesProcessService$SubAreaIterator";
    private static final String CLASS_NAME_SUBTASK_SEARCHING_CITIES
            = "by.aurorasoft.nominatim.service.searchcity.StartingSearchingCitiesProcessService$SubtaskSearchingCities";
    private static final String CLASS_NAME_TASK_SEARCHING_CITIES
            = "by.aurorasoft.nominatim.service.searchcity.StartingSearchingCitiesProcessService$TaskSearchingCities";

    private static final String METHOD_NAME_OF_EXECUTING_SUBTASK_SEARCHING_CITIES = "execute";
    private static final String METHOD_NAME_OF_EXECUTING_TASK_SEARCHING_CITIES = "run";

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

    private static final ParameterizedTypeReference<NominatimReverseResponse> PARAMETERIZED_TYPE_REFERENCE
            = new ParameterizedTypeReference<>() {
    };

    private static final String HQL_QUERY_TO_FIND_ALL_CITIES = "SELECT ce FROM CityEntity ce";

    @Autowired
    private StartingSearchingCitiesProcessService service;

    @Autowired
    private SearchingCitiesProcessService searchingCitiesProcessService;

    @Autowired
    private GeoJSONReader geoJSONReader;

    @Autowired
    private GeometryFactory geometryFactory;

    @MockBean
    private RestTemplate mockedTestTemplate;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Test
    public void areaShouldBeIteratedByAreaIteratorFirstCase()
            throws Exception {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                new Coordinate(1., 1.),
                new Coordinate(5., 4.)
        );
        final double givenSearchStep = 1;
        final Iterator<Coordinate> givenAreaIterator = createAreaIterator(givenAreaCoordinate, givenSearchStep);

        final List<Coordinate> actual = asStream(givenAreaIterator)
                .collect(toList());
        final List<Coordinate> expected = List.of(
                new Coordinate(1., 1.),
                new Coordinate(2., 1.),
                new Coordinate(3., 1.),
                new Coordinate(4., 1.),
                new Coordinate(5., 1.),

                new Coordinate(1., 2.),
                new Coordinate(2., 2.),
                new Coordinate(3., 2.),
                new Coordinate(4., 2.),
                new Coordinate(5., 2.),

                new Coordinate(1., 3.),
                new Coordinate(2., 3.),
                new Coordinate(3., 3.),
                new Coordinate(4., 3.),
                new Coordinate(5., 3.),

                new Coordinate(1., 4.),
                new Coordinate(2., 4.),
                new Coordinate(3., 4.),
                new Coordinate(4., 4.),
                new Coordinate(5., 4.)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void areaShouldBeIteratedByAreaIteratorSecondCase()
            throws Exception {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                new Coordinate(1., 1.),
                new Coordinate(5.5, 4.5)
        );
        final double givenSearchStep = 1;
        final Iterator<Coordinate> givenAreaIterator = createAreaIterator(givenAreaCoordinate, givenSearchStep);

        final List<Coordinate> actual = asStream(givenAreaIterator)
                .collect(toList());
        final List<Coordinate> expected = List.of(
                new Coordinate(1., 1.),
                new Coordinate(2., 1.),
                new Coordinate(3., 1.),
                new Coordinate(4., 1.),
                new Coordinate(5., 1.),

                new Coordinate(1., 2.),
                new Coordinate(2., 2.),
                new Coordinate(3., 2.),
                new Coordinate(4., 2.),
                new Coordinate(5., 2.),

                new Coordinate(1., 3.),
                new Coordinate(2., 3.),
                new Coordinate(3., 3.),
                new Coordinate(4., 3.),
                new Coordinate(5., 3.),

                new Coordinate(1., 4.),
                new Coordinate(2., 4.),
                new Coordinate(3., 4.),
                new Coordinate(4., 4.),
                new Coordinate(5., 4.)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void areaShouldBeIteratedByAreaIteratorThirdCase()
            throws Exception {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                new Coordinate(1., 1.),
                new Coordinate(5., 4.)
        );
        final double givenSearchStep = 10;
        final Iterator<Coordinate> givenAreaIterator = createAreaIterator(givenAreaCoordinate, givenSearchStep);

        final List<Coordinate> actual = asStream(givenAreaIterator)
                .collect(toList());
        final List<Coordinate> expected = List.of(new Coordinate(1., 1.));
        assertEquals(expected, actual);
    }

    @Test
    public void subAreaShouldBeIteratedBySubAreaIteratorFirstCase()
            throws Exception {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                new Coordinate(1., 1.),
                new Coordinate(5., 4.)
        );
        final double givenSearchStep = 1;
        final Iterator<Coordinate> givenAreaIterator = createAreaIterator(givenAreaCoordinate, givenSearchStep);
        final Iterator<Coordinate> givenSubAreaIterator = this.createSubAreaIterator(givenAreaIterator);

        final List<Coordinate> actual = asStream(givenSubAreaIterator)
                .collect(toList());
        final List<Coordinate> expected = List.of(
                new Coordinate(1., 1.),
                new Coordinate(2., 1.)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void subAreaShouldBeIteratedBySubAreaIteratorSecondCase()
            throws Exception {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                new Coordinate(1., 1.),
                new Coordinate(5., 4.)
        );
        final double givenSearchStep = 10;
        final Iterator<Coordinate> givenAreaIterator = createAreaIterator(givenAreaCoordinate, givenSearchStep);
        final Iterator<Coordinate> givenSubAreaIterator = this.createSubAreaIterator(givenAreaIterator);

        final List<Coordinate> actual = asStream(givenSubAreaIterator)
                .collect(toList());
        final List<Coordinate> expected = List.of(new Coordinate(1., 1.));
        assertEquals(expected, actual);
    }

    @Test
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(255, ST_GeomFromText('POLYGON((1 1, 5 1, 5 5, 1 5, 1 1))', 4326), 1, 25, 6, 'HANDLING')")
    public void subTaskSearchingCitiesShouldFoundCities()
            throws Exception {
        final List<Coordinate> givenCoordinates = List.of(
                new Coordinate(2., 2.),
                new Coordinate(2., 3.),
                new Coordinate(2., 4.)
        );
        final SearchingCitiesProcess givenProcess = this.searchingCitiesProcessService.getById(255L);
        final Object givenSubtaskSearchingCities = this.createSubtaskSearchingCities(givenCoordinates, givenProcess);

        final String firstGivenResponseName = "first-city";
        final ExtraTags firstGivenExtraTags = new ExtraTags("town", "yes");
        final String firstGivenGeoJson = "{\"type\":\"Polygon\",\"coordinates\":[[[1,1],[5,1],[5,5],[1,5],[1,1]]]}";
        final NominatimReverseResponse firstGivenResponse = NominatimReverseResponse.builder()
                .name(firstGivenResponseName)
                .extratags(firstGivenExtraTags)
                .geojson(firstGivenGeoJson)
                .build();

        final String secondGivenResponseName = "second-city";
        final ExtraTags secondGivenExtraTags = new ExtraTags("city", "4");
        final String secondGivenGeoJson = "{\"type\":\"Polygon\",\"coordinates\":[[[3,1],[5,1],[5,5],[1,5],[3,1]]]}";
        final NominatimReverseResponse secondGivenResponse = NominatimReverseResponse.builder()
                .name(secondGivenResponseName)
                .extratags(secondGivenExtraTags)
                .geojson(secondGivenGeoJson)
                .build();

        final String thirdGivenResponseName = "village";
        final ExtraTags thirdGivenExtraTags = new ExtraTags("village", "no");
        final String givenGeoJson = "{\"type\":\"Polygon\",\"coordinates\":[[[6,1],[5,1],[5,5],[1,5],[6,1]]]}";
        final NominatimReverseResponse thirdGivenResponse = NominatimReverseResponse.builder()
                .name(thirdGivenResponseName)
                .extratags(thirdGivenExtraTags)
                .geojson(givenGeoJson)
                .build();

        when(this.mockedTestTemplate.exchange(anyString(), same(GET), same(EMPTY), eq(PARAMETERIZED_TYPE_REFERENCE)))
                .thenReturn(ok(firstGivenResponse))
                .thenReturn(ok(secondGivenResponse))
                .thenReturn(ok(thirdGivenResponse));

        final Collection<City> actual = callExecutingMethodSubtaskSearchingCities(givenSubtaskSearchingCities);
        final Collection<City> expected = List.of(
                this.createCity(firstGivenResponseName, firstGivenGeoJson, CAPITAL),
                this.createCity(secondGivenResponseName, secondGivenGeoJson, REGIONAL)
        );
        assertEquals(expected, actual);

        verify(this.mockedTestTemplate, times(3))
                .exchange(this.stringArgumentCaptor.capture(), same(GET), same(EMPTY),
                        eq(PARAMETERIZED_TYPE_REFERENCE));

        final List<String> expectedCapturedUrls = givenCoordinates.stream()
                .map(StartingSearchingCitiesProcessServiceIT::createUrlReverse)
                .collect(toList());
        assertEquals(expectedCapturedUrls, this.stringArgumentCaptor.getAllValues());

        super.entityManager.flush();
        super.entityManager.clear();
        final SearchingCitiesProcess actualProcessAfterSubtaskFinished
                = this.searchingCitiesProcessService.getById(255L);
        final SearchingCitiesProcess expectedProcessAfterSubtaskFinished = createWithIncreasingHandledPoints(
                givenProcess, 3);
        assertEquals(expectedProcessAfterSubtaskFinished, actualProcessAfterSubtaskFinished);
    }

    @Test
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(255, ST_GeomFromText('POLYGON((1 1, 5 1, 5 5, 1 5, 1 1))', 4326), 1, 25, 6, 'HANDLING')")
    public void subTaskSearchingCitiesShouldBeFailed()
            throws Exception {
        final List<Coordinate> givenCoordinates = List.of(
                new Coordinate(2., 2.),
                new Coordinate(2., 3.),
                new Coordinate(2., 4.)
        );
        final SearchingCitiesProcess givenProcess = this.searchingCitiesProcessService.getById(255L);
        final Object givenSubtaskSearchingCities = this.createSubtaskSearchingCities(givenCoordinates, givenProcess);

        when(this.mockedTestTemplate.exchange(anyString(), same(GET), same(EMPTY), eq(PARAMETERIZED_TYPE_REFERENCE)))
                .thenThrow(RuntimeException.class);

        try {
            callExecutingMethodSubtaskSearchingCities(givenSubtaskSearchingCities);
        } catch (final InvocationTargetException invocationTargetException) {
            final Throwable actualCause = invocationTargetException.getCause();
            assertSame(FindingCitiesException.class, actualCause.getClass());
        }
    }

    @Test
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(255, ST_GeomFromText('POLYGON((1 1, 5 1, 5 5, 1 5, 1 1))', 4326), 0.5, 9, 0, 'HANDLING')")
    public void taskSearchingCitiesShouldBeSuccess()
            throws Exception {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                new Coordinate(1., 1.),
                new Coordinate(2., 2.)
        );
        final double givenSearchStep = 0.5;
        final SearchingCitiesProcess givenProcess = this.searchingCitiesProcessService.getById(255L);
        final Object givenTaskSearchingCities = this.createTaskSearchingCites(givenAreaCoordinate,
                givenSearchStep, givenProcess);

        final String firstGivenResponseName = "first-city";
        final ExtraTags firstGivenExtraTags = new ExtraTags("town", "yes");
        final String firstGivenGeoJson = "{\"type\":\"Polygon\",\"coordinates\":[[[1,1],[5,1],[5,5],[1,5],[1,1]]]}";
        final NominatimReverseResponse firstGivenResponse = NominatimReverseResponse.builder()
                .name(firstGivenResponseName)
                .extratags(firstGivenExtraTags)
                .geojson(firstGivenGeoJson)
                .build();

        final String secondGivenResponseName = "second-city";
        final ExtraTags secondGivenExtraTags = new ExtraTags("city", "4");
        final String secondGivenGeoJson = "{\"type\":\"Polygon\",\"coordinates\":[[[3,1],[5,1],[5,5],[1,5],[3,1]]]}";
        final NominatimReverseResponse secondGivenResponse = NominatimReverseResponse.builder()
                .name(secondGivenResponseName)
                .extratags(secondGivenExtraTags)
                .geojson(secondGivenGeoJson)
                .build();

        final NominatimReverseResponse thirdGivenResponse = NominatimReverseResponse.builder()
                .name(secondGivenResponseName)
                .extratags(secondGivenExtraTags)
                .geojson(secondGivenGeoJson)
                .build();

        when(this.mockedTestTemplate.exchange(anyString(), same(GET), same(EMPTY), eq(PARAMETERIZED_TYPE_REFERENCE)))
                .thenReturn(ok(firstGivenResponse))
                .thenReturn(ok(secondGivenResponse))
                .thenReturn(ok(thirdGivenResponse));

        callExecutingMethodTaskSearchingCities(givenTaskSearchingCities);

        verify(this.mockedTestTemplate, times(9))
                .exchange(this.stringArgumentCaptor.capture(), same(GET), same(EMPTY),
                        eq(PARAMETERIZED_TYPE_REFERENCE));

        final List<Coordinate> expectedCoordinates = List.of(
                new Coordinate(1., 1.),
                new Coordinate(1.5, 1.),
                new Coordinate(2., 1.),

                new Coordinate(1., 1.5),
                new Coordinate(1.5, 1.5),
                new Coordinate(2., 1.5),

                new Coordinate(1., 2.),
                new Coordinate(1.5, 2.),
                new Coordinate(2., 2.)
        );
        final List<String> expectedCapturedUrls = expectedCoordinates.stream()
                .map(StartingSearchingCitiesProcessServiceIT::createUrlReverse)
                .collect(toList());
        assertEquals(expectedCapturedUrls, this.stringArgumentCaptor.getAllValues());

        final List<CityEntity> actualFoundCities = this.findAllCities();
        final List<CityEntity> expectedFoundCities = List.of(
                this.createCityEntity(firstGivenResponseName, firstGivenGeoJson, CAPITAL),
                this.createCityEntity(secondGivenResponseName, secondGivenGeoJson, REGIONAL)
        );
        assertNotNull(actualFoundCities);
        checkEqualsExceptId(expectedFoundCities, actualFoundCities);

        super.entityManager.flush();
        super.entityManager.clear();
        final SearchingCitiesProcessEntity actualProcessAfterTaskFinished = super.entityManager.find(
                SearchingCitiesProcessEntity.class, 255L);
        final SearchingCitiesProcessEntity expectedProcessAfterTaskFinished
                = SearchingCitiesProcessEntity.builder()
                .id(255L)
                .geometry(this.geometryFactory.createPolygon(new org.locationtech.jts.geom.Coordinate[]{
                        new CoordinateXY(1, 1),
                        new CoordinateXY(5, 1),
                        new CoordinateXY(5, 5),
                        new CoordinateXY(1, 5),
                        new CoordinateXY(1, 1)
                }))
                .searchStep(givenSearchStep)
                .totalPoints(9)
                .handledPoints(9)
                .status(SUCCESS)
                .build();
        checkEquals(expectedProcessAfterTaskFinished, actualProcessAfterTaskFinished);
    }

    @Test
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(255, ST_GeomFromText('POLYGON((1 1, 5 1, 5 5, 1 5, 1 1))', 4326), 0.5, 9, 0, 'HANDLING')")
    public void taskSearchingCitiesShouldBeFailedBecauseOfSubtaskWasFailed()
            throws Exception {
        final AreaCoordinate givenAreaCoordinate = new AreaCoordinate(
                new Coordinate(1., 1.),
                new Coordinate(2., 2.)
        );
        final double givenSearchStep = 0.5;
        final SearchingCitiesProcess givenProcess = this.searchingCitiesProcessService.getById(255L);
        final Object givenTaskSearchingCities = this.createTaskSearchingCites(givenAreaCoordinate,
                givenSearchStep, givenProcess);

        when(this.mockedTestTemplate.exchange(anyString(), same(GET), same(EMPTY), eq(PARAMETERIZED_TYPE_REFERENCE)))
                .thenThrow(RuntimeException.class);

        callExecutingMethodTaskSearchingCities(givenTaskSearchingCities);

        super.entityManager.flush();
        super.entityManager.clear();
        final SearchingCitiesProcessEntity actualProcessAfterTaskFinished = super.entityManager.find(
                SearchingCitiesProcessEntity.class, 255L);
        final SearchingCitiesProcessEntity expectedProcessAfterTaskFinished
                = SearchingCitiesProcessEntity.builder()
                .id(255L)
                .geometry(this.geometryFactory.createPolygon(new org.locationtech.jts.geom.Coordinate[]{
                        new CoordinateXY(1, 1),
                        new CoordinateXY(5, 1),
                        new CoordinateXY(5, 5),
                        new CoordinateXY(1, 5),
                        new CoordinateXY(1, 1)
                }))
                .searchStep(givenSearchStep)
                .totalPoints(9)
                .handledPoints(0)
                .status(ERROR)
                .build();
        checkEquals(expectedProcessAfterTaskFinished, actualProcessAfterTaskFinished);
    }

    private static Iterator<Coordinate> createAreaIterator(AreaCoordinate areaCoordinate, double searchStep)
            throws Exception {
        return createObject(
                CLASS_NAME_AREA_ITERATOR,
                new Class<?>[]{AreaCoordinate.class, double.class},
                new Object[]{areaCoordinate, searchStep}
        );
    }

    private Iterator<Coordinate> createSubAreaIterator(Iterator<Coordinate> areaIterator)
            throws Exception {
        final Class<? extends Iterator<Coordinate>> classAreaIterator
                = findClass(CLASS_NAME_AREA_ITERATOR);
        return createObject(
                CLASS_NAME_SUB_AREA_ITERATOR,
                new Class<?>[]{StartingSearchingCitiesProcessService.class, classAreaIterator},
                new Object[]{this.service, areaIterator}
        );
    }

    private Object createSubtaskSearchingCities(List<Coordinate> coordinates, SearchingCitiesProcess process)
            throws Exception {
        return createObject(
                CLASS_NAME_SUBTASK_SEARCHING_CITIES,
                new Class<?>[]{
                        StartingSearchingCitiesProcessService.class,
                        List.class,
                        SearchingCitiesProcess.class
                },
                new Object[]{this.service, coordinates, process}
        );
    }

    private Object createTaskSearchingCites(AreaCoordinate areaCoordinate, double searchStep,
                                            SearchingCitiesProcess process)
            throws Exception {
        return createObject(
                CLASS_NAME_TASK_SEARCHING_CITIES,
                new Class<?>[]{
                        StartingSearchingCitiesProcessService.class,
                        AreaCoordinate.class,
                        double.class,
                        SearchingCitiesProcess.class
                },
                new Object[]{this.service, areaCoordinate, searchStep, process}
        );
    }

    private static <ObjectType> ObjectType createObject(String className,
                                                        Class<?>[] constructorArgumentsTypes,
                                                        Object[] constructorArguments)
            throws Exception {
        final Class<? extends ObjectType> classObject = findClass(className);
        final Constructor<? extends ObjectType> constructorObject = classObject.getConstructor(
                constructorArgumentsTypes);
        return constructorObject.newInstance(constructorArguments);
    }

    @SuppressWarnings("all")
    private static <ObjectType> Class<? extends ObjectType> findClass(String className)
            throws Exception {
        return (Class<? extends ObjectType>) forName(className);
    }

    @SuppressWarnings("unchecked")
    private static Collection<City> callExecutingMethodSubtaskSearchingCities(Object subtaskSearchingCities)
            throws Exception {
        final Method executingMethodSubtaskSearchingCities = findExecutingMethodSubtaskSearchingCities();
        return (Collection<City>) executingMethodSubtaskSearchingCities.invoke(subtaskSearchingCities);
    }

    private static Method findExecutingMethodSubtaskSearchingCities()
            throws Exception {
        final Class<?> classSubtaskSearchingCities = findClass(CLASS_NAME_SUBTASK_SEARCHING_CITIES);
        return classSubtaskSearchingCities.getMethod(METHOD_NAME_OF_EXECUTING_SUBTASK_SEARCHING_CITIES);
    }

    private static void callExecutingMethodTaskSearchingCities(Object taskSearchingCities)
            throws Exception {
        final Method executingMethodTaskSearchingCities = findExecutingMethodTaskSearchingCities();
        executingMethodTaskSearchingCities.invoke(taskSearchingCities);
    }

    private static Method findExecutingMethodTaskSearchingCities()
            throws Exception {
        final Class<?> classTaskSearchingCities = findClass(CLASS_NAME_TASK_SEARCHING_CITIES);
        return classTaskSearchingCities.getMethod(METHOD_NAME_OF_EXECUTING_TASK_SEARCHING_CITIES);
    }

    private City createCity(String name, String geoJson, Type type) {
        final Geometry geometry = this.geoJSONReader.read(geoJson);
        return City.builder()
                .name(name)
                .geometry(geometry)
                .type(type)
                .boundingBox(geometry.getEnvelope())
                .build();
    }

    private static String createUrlReverse(Coordinate coordinate) {
        return fromUriString(URI_WITHOUT_PARAMETERS)
                .queryParam(PARAM_NAME_LATITUDE, coordinate.getLatitude())
                .queryParam(PARAM_NAME_LONGITUDE, coordinate.getLongitude())
                .queryParam(PARAM_NAME_ZOOM, PARAM_VALUE_ZOOM)
                .queryParam(PARAM_NAME_FORMAT, PARAM_VALUE_FORMAT)
                .queryParam(PARAM_NAME_POLYGON_GEOJSON, PARAM_VALUE_POLYGON_GEOJSON)
                .queryParam(PARAM_NAME_EXTRATAGS, PARAM_VALUE_EXTRATAGS)
                .build()
                .toUriString();
    }

    private static SearchingCitiesProcess createWithIncreasingHandledPoints(SearchingCitiesProcess source,
                                                                            final long delta) {
        return SearchingCitiesProcess.builder()
                .id(source.getId())
                .geometry(source.getGeometry())
                .searchStep(source.getSearchStep())
                .totalPoints(source.getTotalPoints())
                .handledPoints(source.getHandledPoints() + delta)
                .status(source.getStatus())
                .build();
    }

    private static void checkEqualsExceptId(List<CityEntity> expected, List<CityEntity> actual) {
        assertEquals(expected.size(), actual.size());
        range(0, expected.size()).forEach(i -> checkEqualsExceptId(expected.get(i), actual.get(i)));
    }

    private static void checkEqualsExceptId(CityEntity expected, CityEntity actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getGeometry(), actual.getGeometry());
        assertSame(expected.getType(), actual.getType());
        assertEquals(expected.getBoundingBox(), actual.getBoundingBox());
    }

    private List<CityEntity> findAllCities() {
        return super.entityManager.createQuery(HQL_QUERY_TO_FIND_ALL_CITIES, CityEntity.class).getResultList();
    }

    private CityEntity createCityEntity(String name, String geoJson, final Type type) {
        final Geometry geometry = this.geoJSONReader.read(geoJson);
        return CityEntity.builder()
                .name(name)
                .geometry(geometry)
                .type(type)
                .boundingBox(geometry.getEnvelope())
                .build();
    }

    private static void checkEquals(SearchingCitiesProcessEntity expected, SearchingCitiesProcessEntity actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getGeometry(), actual.getGeometry());
        assertEquals(expected.getSearchStep(), actual.getSearchStep(), 0.);
        assertEquals(expected.getTotalPoints(), actual.getTotalPoints());
        assertEquals(expected.getHandledPoints(), actual.getHandledPoints());
        assertSame(expected.getStatus(), actual.getStatus());
    }
}
