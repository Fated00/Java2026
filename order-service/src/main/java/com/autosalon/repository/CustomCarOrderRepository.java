package com.autosalon.repository;

import com.autosalon.domain.model.CustomCarOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomCarOrderRepository extends JpaRepository<CustomCarOrder, UUID> {
    Optional<CustomCarOrder> findByIdAndRemovedFalse(UUID id);

    List<CustomCarOrder> findByRemovedFalse();
}
