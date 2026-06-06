package com.autosalon.repository;

import com.autosalon.domain.model.TestDriveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestDriveRequestRepository extends JpaRepository<TestDriveRequest, UUID> {
    Optional<TestDriveRequest> findByIdAndRemovedFalse(UUID id);

    List<TestDriveRequest> findByRemovedFalse();

    boolean existsByCarIdAndStartsAtAndRemovedFalse(UUID carId, LocalDateTime startsAt);
}
