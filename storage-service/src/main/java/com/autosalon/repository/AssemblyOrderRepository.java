package com.autosalon.repository;

import com.autosalon.domain.model.AssemblyOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssemblyOrderRepository extends JpaRepository<AssemblyOrder, UUID> {
    List<AssemblyOrder> findByRemovedFalse();
}
