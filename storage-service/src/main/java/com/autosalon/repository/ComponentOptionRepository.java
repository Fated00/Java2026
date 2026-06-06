package com.autosalon.repository;

import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.model.ComponentOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComponentOptionRepository extends JpaRepository<ComponentOption, UUID> {
    Optional<ComponentOption> findByIdAndRemovedFalse(UUID id);

    List<ComponentOption> findByRemovedFalse();

    List<ComponentOption> findByTypeAndRemovedFalse(ComponentType type);

    Optional<ComponentOption> findByNameIgnoreCaseAndRemovedFalse(String name);
}
