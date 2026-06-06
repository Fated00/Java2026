package com.autosalon.api;

import com.autosalon.api.dto.Dtos.CreateTestDriveRequest;
import com.autosalon.api.dto.Dtos.TestDriveRequestDto;
import com.autosalon.security.CurrentUserService;
import com.autosalon.service.TestDriveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
public class TestDriveController {
    private final TestDriveService testDriveService;
    private final ApiMapper mapper;
    private final CurrentUserService currentUserService;

    public TestDriveController(TestDriveService testDriveService, ApiMapper mapper, CurrentUserService currentUserService) {
        this.testDriveService = testDriveService;
        this.mapper = mapper;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public List<TestDriveRequestDto> listRequests() {
        return testDriveService.listRequests().stream().map(mapper::toDto).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public TestDriveRequestDto getRequest(@PathVariable UUID id) {
        return mapper.toDto(testDriveService.findRequest(id));
    }

    @PostMapping("/cars/{carId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public void addCarToTestDriveList(@PathVariable UUID carId) {
        testDriveService.addCarToTestDriveList(carId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public TestDriveRequestDto createRequest(@Valid @RequestBody CreateTestDriveRequest request) {
        return mapper.toDto(testDriveService.requestTestDrive(
                currentUserService.currentAppUserId(),
                request.carId(),
                request.startsAt()
        ));
    }
}
