package by.aurorasoft.nominatim.service.searchcity;

import by.aurorasoft.nominatim.crud.model.dto.AreaCoordinate;
import by.aurorasoft.nominatim.crud.model.dto.City;
import by.aurorasoft.nominatim.crud.model.dto.SearchingCitiesProcess;
import by.aurorasoft.nominatim.crud.service.CityService;
import by.aurorasoft.nominatim.crud.service.SearchingCitiesProcessService;
import by.aurorasoft.nominatim.service.factory.SearchingCitiesProcessFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status.*;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class EventHandlingSearchCityProcessService {
    private static final String LOG_START_PROCESS_SEARCHING_CITIES = "Process searching cities has been started.";

    private static final String LOG_SUCCESS_SUBTASK_SEARCHING_CITIES
            = "Subtask searching cities has been finished successfully.";
    private static final String LOG_TEMPLATE_FAILURE_SUBTASK_SEARCHING_CITIES
            = "Subtask searching cities has been failed. Exception: {}.";

    private static final String LOG_SUCCESS_PROCESS_SEARCHING_CITIES
            = "Process searching all cities has been finished successfully.";
    private static final String LOG_TEMPLATE_FAILURE_PROCESS_SEARCHING_CITIES
            = "Process searching all cities has been failed. Exception: {}.";

    private final SearchingCitiesProcessService searchingCitiesProcessService;
    private final CityService cityService;
    private final SearchingCitiesProcessFactory searchingCitiesProcessFactory;

    public EventHandlingSearchCityProcessService(SearchingCitiesProcessService searchingCitiesProcessService,
                                                 CityService cityService,
                                                 SearchingCitiesProcessFactory searchingCitiesProcessFactory) {
        this.searchingCitiesProcessService = searchingCitiesProcessService;
        this.cityService = cityService;
        this.searchingCitiesProcessFactory = searchingCitiesProcessFactory;
    }

    public SearchingCitiesProcess onStartSearchCities(AreaCoordinate areaCoordinate, double searchStep) {
        final SearchingCitiesProcess processToBeSaved = this.searchingCitiesProcessFactory
                .create(areaCoordinate, searchStep);
        final SearchingCitiesProcess savedProcess = this.searchingCitiesProcessService.save(processToBeSaved);
        log.info(LOG_START_PROCESS_SEARCHING_CITIES);
        return savedProcess;
    }

    public void onSuccessFindCitiesBySubtask(SearchingCitiesProcess process, long amountHandledPoints) {
        this.searchingCitiesProcessService.increaseHandledPoints(process, amountHandledPoints);
        log.info(LOG_SUCCESS_SUBTASK_SEARCHING_CITIES);
    }

    public void onFailedFindCitiesBySubtask(Exception exception) {
        log.error(LOG_TEMPLATE_FAILURE_SUBTASK_SEARCHING_CITIES, exception.getMessage());
        exception.printStackTrace();
    }

    @Transactional
    public void onSuccessFindAllCities(SearchingCitiesProcess process, Collection<City> foundCities) {
        final List<City> cityWithNotExistsGeometry = foundCities.stream()
                .filter(city -> !this.cityService.isExistByGeometry(city.getGeometry()))
                .collect(toList());
        this.cityService.saveAll(cityWithNotExistsGeometry);
        this.searchingCitiesProcessService.updateStatus(process, SUCCESS);
        log.info(LOG_SUCCESS_PROCESS_SEARCHING_CITIES);
    }

    public void onFailedFindAllCities(SearchingCitiesProcess process, Exception exception) {
        this.searchingCitiesProcessService.updateStatus(process, ERROR);
        log.error(LOG_TEMPLATE_FAILURE_PROCESS_SEARCHING_CITIES, exception.getMessage());
        exception.printStackTrace();
    }
}
