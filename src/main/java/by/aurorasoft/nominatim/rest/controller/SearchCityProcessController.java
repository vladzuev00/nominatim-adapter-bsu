package by.aurorasoft.nominatim.rest.controller;

import by.aurorasoft.nominatim.crud.model.dto.SearchingCitiesProcess;
import by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity.Status;
import by.aurorasoft.nominatim.crud.service.SearchingCitiesProcessService;
import by.aurorasoft.nominatim.rest.controller.exception.NoSuchEntityException;
import by.aurorasoft.nominatim.rest.mapper.SearchingCitiesProcessControllerMapper;
import by.aurorasoft.nominatim.rest.model.SearchingCitiesProcessPageResponse;
import by.aurorasoft.nominatim.rest.model.SearchingCitiesProcessResponse;
import by.aurorasoft.nominatim.rest.model.StartSearchingCitiesRequest;
import by.aurorasoft.nominatim.rest.validator.StartSearchingCitiesRequestValidator;
import by.aurorasoft.nominatim.service.searchcity.StartingSearchingCitiesProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/searchCity")
@RequiredArgsConstructor
@Validated
public class SearchCityProcessController {
    private static final String MESSAGE_EXCEPTION_OF_NO_SUCH_PROCESS = "Process with id '%d' doesn't exist.";

    private final StartSearchingCitiesRequestValidator validator;
    private final StartingSearchingCitiesProcessService startingProcessService;
    private final SearchingCitiesProcessService processService;
    private final SearchingCitiesProcessControllerMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<SearchingCitiesProcessResponse> findById(@PathVariable Long id) {
        final Optional<SearchingCitiesProcess> optionalFoundProcess = this.processService.getByIdOptional(id);
        return optionalFoundProcess.map(this.mapper::mapToResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NoSuchEntityException(format(MESSAGE_EXCEPTION_OF_NO_SUCH_PROCESS, id)));
    }

    @GetMapping
    public ResponseEntity<SearchingCitiesProcessPageResponse> findByStatus(
            @RequestParam(name = "status") Status status,
            @RequestParam(name = "pageNumber") @Min(0) @Max(10000) int pageNumber,
            @RequestParam(name = "pageSize") @Min(1) @Max(10000) int pageSize) {
        final List<SearchingCitiesProcess> foundProcesses = this.processService.findByStatus(
                status, pageNumber, pageSize);
        return ok(this.mapper.mapToResponse(pageNumber, pageSize, foundProcesses));
    }

    @PostMapping
    public ResponseEntity<SearchingCitiesProcessResponse> start(
            @Valid @RequestBody StartSearchingCitiesRequest request) {
        this.validator.validate(request);
        final SearchingCitiesProcess createdProcess = this.startingProcessService.start(
                request.getBbox(), request.getSearchStep());
        return ok(this.mapper.mapToResponse(createdProcess));
    }
}
