package com.autosalon.api;

import com.autosalon.api.dto.Dtos.CreateTestDriveRequest;
import com.autosalon.api.dto.Dtos.TestDriveRequestDto;
import com.autosalon.service.TestDriveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/test-drives")
public final class TestDriveController {
    private final TestDriveService testDriveService;
    private final ApiMapper mapper;

    public TestDriveController(TestDriveService testDriveService, ApiMapper mapper) {
        this.testDriveService = testDriveService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<TestDriveRequestDto> listRequests() {
        return testDriveService.listRequests().stream().map(mapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public TestDriveRequestDto getRequest(@PathVariable UUID id) {
        return mapper.toDto(testDriveService.findRequest(id));
    }

    @PostMapping("/cars/{carId}")
    public void addCarToTestDriveList(@PathVariable UUID carId) {
        testDriveService.addCarToTestDriveList(carId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TestDriveRequestDto createRequest(@Valid @RequestBody CreateTestDriveRequest request) {
        return mapper.toDto(testDriveService.requestTestDrive(
                request.clientId(),
                request.carId(),
                request.startsAt()
        ));
    }
}
