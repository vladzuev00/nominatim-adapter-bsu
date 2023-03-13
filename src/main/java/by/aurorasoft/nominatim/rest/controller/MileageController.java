package by.aurorasoft.nominatim.rest.controller;

import by.aurorasoft.nominatim.rest.model.MileageRequest;
import by.aurorasoft.nominatim.rest.model.MileageResponse;
import by.aurorasoft.nominatim.service.mileage.MileageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/mileage")
@Validated
@RequiredArgsConstructor
public class MileageController {
    private final MileageService mileageService;

    @PostMapping
    public ResponseEntity<MileageResponse> findMileage(@Valid @RequestBody MileageRequest mileageRequest) {
        return ok(this.mileageService.findMileage(mileageRequest));
    }
}
