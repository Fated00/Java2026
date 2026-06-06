package com.autosalon.service;

import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.model.TestDriveRequest;
import com.autosalon.domain.model.User;
import com.autosalon.repository.TestDriveRequestRepository;
import com.autosalon.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

@Service
@Transactional
public class TestDriveService {
    private final TestDriveRequestRepository testDriveRequestRepository;
    private final UserRepository userRepository;

    public TestDriveService(TestDriveRequestRepository testDriveRequestRepository, UserRepository userRepository) {
        this.testDriveRequestRepository = requireNonNull(testDriveRequestRepository, "test-drive repository");
        this.userRepository = requireNonNull(userRepository, "user repository");
    }

    public TestDriveRequest requestTestDrive(UUID clientId, UUID carId, LocalDateTime startsAt) {
        if (!startsAt.isAfter(LocalDateTime.now())) {
            throw new DomainValidationException("test-drive start time must be in the future");
        }
        User client = ServiceSupport.requireRole(
                ServiceSupport.findActiveOrThrow(userRepository, clientId, "User"),
                Role.CLIENT,
                "client"
        );
        return testDriveRequestRepository.save(TestDriveRequest.create(client, carId, startsAt));
    }

    @Transactional(readOnly = true)
    public TestDriveRequest findRequest(UUID id) {
        return ServiceSupport.findActiveOrThrow(testDriveRequestRepository, id, "TestDriveRequest");
    }

    @Transactional(readOnly = true)
    public List<TestDriveRequest> listRequests() {
        return testDriveRequestRepository.findByRemovedFalse();
    }
}
