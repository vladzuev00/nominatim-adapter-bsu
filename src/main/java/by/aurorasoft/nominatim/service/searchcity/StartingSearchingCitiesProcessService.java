package by.aurorasoft.nominatim.service.searchcity;

import by.aurorasoft.nominatim.crud.model.dto.AreaCoordinate;
import by.aurorasoft.nominatim.crud.model.dto.City;
import by.aurorasoft.nominatim.crud.model.dto.Coordinate;
import by.aurorasoft.nominatim.crud.model.dto.SearchingCitiesProcess;
import by.aurorasoft.nominatim.service.exception.FindingCitiesException;
import by.aurorasoft.nominatim.util.StreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;

import static java.lang.Double.compare;
import static java.lang.Math.ceil;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.Collectors.toList;
import static java.util.stream.LongStream.range;

@Slf4j
@Service
public class StartingSearchingCitiesProcessService {
    private final SearchCityService searchCityService;
    private final EventHandlingSearchCityProcessService eventHandlingSearchCityProcessService;
    private final ExecutorService executorService;
    private final int amountHandledPointsToSaveState;

    public StartingSearchingCitiesProcessService(SearchCityService searchCityService,
                                                 EventHandlingSearchCityProcessService eventHandlingSearchCityProcessService,
                                                 @Qualifier("executorServiceToSearchCities") ExecutorService executorService,
                                                 @Value("${search-cities.amount-handled-points-to-save-state}") int amountHandledPointsToSaveState) {
        this.searchCityService = searchCityService;
        this.eventHandlingSearchCityProcessService = eventHandlingSearchCityProcessService;
        this.executorService = executorService;
        this.amountHandledPointsToSaveState = amountHandledPointsToSaveState;
    }

    public SearchingCitiesProcess start(AreaCoordinate areaCoordinate, double searchStep) {
        final SearchingCitiesProcess process = this.eventHandlingSearchCityProcessService
                .onStartSearchCities(areaCoordinate, searchStep);
        runAsync(new TaskSearchingCities(areaCoordinate, searchStep, process), this.executorService);
        return process;
    }

    private final class TaskSearchingCities implements Runnable {
        private final AreaCoordinate areaCoordinate;
        private final double searchStep;
        private final SearchingCitiesProcess process;

        public TaskSearchingCities(AreaCoordinate areaCoordinate, double searchStep, SearchingCitiesProcess process) {
            this.areaCoordinate = areaCoordinate;
            this.searchStep = searchStep;
            this.process = process;
        }

        @Override
        public void run() {
            try {
                final Set<String> namesAlreadyFoundCities = new HashSet<>();
                final AreaIterator areaIterator = new AreaIterator(this.areaCoordinate, this.searchStep);
                final long amountOfSubAreas = this.findAmountOfSubAreas();
                final Collection<City> foundUniqueCities = range(0, amountOfSubAreas)
                        .mapToObj(i -> new SubAreaIterator(areaIterator))
                        .map(StreamUtil::asStream)
                        .map(subAreaCoordinateStream -> subAreaCoordinateStream.collect(toList()))
                        .map(subAreaCoordinates -> new SubtaskSearchingCities(subAreaCoordinates, this.process))
                        .map(SubtaskSearchingCities::execute)
                        .flatMap(Collection::stream)
                        .filter(city -> namesAlreadyFoundCities.add(city.getName()))
                        .collect(toList());
                eventHandlingSearchCityProcessService.onSuccessFindAllCities(this.process, foundUniqueCities);
            } catch (final Exception exception) {
                eventHandlingSearchCityProcessService.onFailedFindAllCities(this.process, exception);
            }
        }

        private long findAmountOfSubAreas() {
            return (long) ceil(((double) this.process.getTotalPoints()) / amountHandledPointsToSaveState);
        }
    }

    private static final class AreaIterator implements Iterator<Coordinate> {
        private final AreaCoordinate areaCoordinate;
        private final double searchStep;
        private Coordinate current;

        public AreaIterator(AreaCoordinate areaCoordinate, double searchStep) {
            this.areaCoordinate = areaCoordinate;
            this.searchStep = searchStep;
            this.current = new Coordinate(
                    areaCoordinate.getLeftBottom().getLatitude() - searchStep,
                    areaCoordinate.getLeftBottom().getLongitude()
            );
        }

        @Override
        public boolean hasNext() {
            return this.hasNextLatitude() || this.hasNextLongitude();
        }

        @Override
        public Coordinate next() {
            if (this.hasNextLatitude()) {
                return this.nextLatitude();
            } else if (this.hasNextLongitude()) {
                return this.nextLongitude();
            }
            throw new NoSuchElementException();
        }

        private boolean hasNextLatitude() {
            return compare(
                    this.current.getLatitude() + this.searchStep,
                    this.areaCoordinate.getRightUpper().getLatitude()) <= 0;
        }

        private boolean hasNextLongitude() {
            return compare(
                    this.current.getLongitude() + this.searchStep,
                    this.areaCoordinate.getRightUpper().getLongitude()) <= 0;
        }

        private Coordinate nextLatitude() {
            this.current = new Coordinate(
                    this.current.getLatitude() + this.searchStep,
                    this.current.getLongitude());
            return this.current;
        }

        private Coordinate nextLongitude() {
            this.current = new Coordinate(
                    this.areaCoordinate.getLeftBottom().getLatitude(),
                    this.current.getLongitude() + this.searchStep);
            return this.current;
        }
    }

    private final class SubAreaIterator implements Iterator<Coordinate> {
        private final AreaIterator areaIterator;
        private int amountOfPassedPoints;

        public SubAreaIterator(AreaIterator areaIterator) {
            this.areaIterator = areaIterator;
            this.amountOfPassedPoints = 0;
        }

        @Override
        public boolean hasNext() {
            return this.amountOfPassedPoints < amountHandledPointsToSaveState && this.areaIterator.hasNext();
        }

        @Override
        public Coordinate next() {
            this.amountOfPassedPoints++;
            return this.areaIterator.next();
        }
    }

    private final class SubtaskSearchingCities {
        private final List<Coordinate> coordinates;
        private final SearchingCitiesProcess process;

        public SubtaskSearchingCities(List<Coordinate> coordinates, SearchingCitiesProcess process) {
            this.coordinates = coordinates;
            this.process = process;
        }

        public Collection<City> execute() {
            try {
                final Collection<City> foundCities = searchCityService.findByCoordinates(this.coordinates);
                eventHandlingSearchCityProcessService.onSuccessFindCitiesBySubtask(
                        this.process, this.coordinates.size());
                return foundCities;
            } catch (final Exception exception) {
                eventHandlingSearchCityProcessService.onFailedFindCitiesBySubtask(exception);
                throw new FindingCitiesException(exception);
            }
        }
    }
}
