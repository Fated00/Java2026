package com.autosalon.repository;

import com.autosalon.domain.model.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartRepository extends JpaRepository<Part, UUID> {
    Optional<Part> findByIdAndRemovedFalse(UUID id);

    List<Part> findByRemovedFalse();
}
