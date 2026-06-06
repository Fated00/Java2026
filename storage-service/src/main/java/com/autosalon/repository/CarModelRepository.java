package com.autosalon.repository;

import com.autosalon.domain.model.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarModelRepository extends JpaRepository<CarModel, UUID>, JpaSpecificationExecutor<CarModel> {
    Optional<CarModel> findByIdAndRemovedFalse(UUID id);

    List<CarModel> findByRemovedFalse();

    Optional<CarModel> findByBrandIgnoreCaseAndNameIgnoreCaseAndRemovedFalse(String brand, String name);
}
