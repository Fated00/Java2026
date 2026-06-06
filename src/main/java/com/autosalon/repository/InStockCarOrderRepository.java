package com.autosalon.repository;

import com.autosalon.domain.model.InStockCarOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InStockCarOrderRepository extends JpaRepository<InStockCarOrder, UUID> {
    Optional<InStockCarOrder> findByIdAndRemovedFalse(UUID id);

    List<InStockCarOrder> findByRemovedFalse();
}
