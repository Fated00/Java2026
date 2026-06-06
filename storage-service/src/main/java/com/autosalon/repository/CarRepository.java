package com.autosalon.repository;

import com.autosalon.domain.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {
    Optional<Car> findByIdAndRemovedFalse(UUID id);

    List<Car> findByRemovedFalse();

    List<Car> findByAvailableTrueAndRemovedFalse();
}
