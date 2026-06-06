package com.autosalon;

import com.autosalon.domain.enums.ComponentType;
import com.autosalon.repository.CarModelRepository;
import com.autosalon.repository.CarRepository;
import com.autosalon.repository.ComponentOptionRepository;
import com.autosalon.repository.specification.CarModelSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryIntegrationTest extends IntegrationTestBase {
    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private ComponentOptionRepository componentOptionRepository;

    @Autowired
    private CarRepository carRepository;

    @Test
    void repositoriesLoadSeededEntitiesWithRelations() {
        var bmw320 = carModelRepository
                .findByBrandIgnoreCaseAndNameIgnoreCaseAndRemovedFalse("bmw", "320i")
                .orElseThrow();

        assertThat(bmw320.getRequiredComponentTypes()).containsExactlyInAnyOrder(
                ComponentType.WHEELS,
                ComponentType.TRANSMISSION,
                ComponentType.STEERING_WHEEL,
                ComponentType.INTERIOR
        );
        assertThat(bmw320.getBaseComponentOptions()).containsKeys(ComponentType.WHEELS, ComponentType.INTERIOR);
        assertThat(carRepository.findByAvailableTrueAndRemovedFalse()).hasSize(2);
    }

    @Test
    void specificationsFilterBaseConfigurationsInDatabaseQuery() {
        var standardWheels = componentOptionRepository
                .findByNameIgnoreCaseAndRemovedFalse("17 Standard")
                .orElseThrow();

        Specification<com.autosalon.domain.model.CarModel> specification = Specification
                .where(CarModelSpecifications.notRemoved())
                .and(CarModelSpecifications.brandEquals("BMW"))
                .and(CarModelSpecifications.hasBaseComponentTypes(Set.of(ComponentType.WHEELS)))
                .and(CarModelSpecifications.hasBaseComponentOptionIds(Set.of(standardWheels.getId())));

        var result = carModelRepository.findAll(specification);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDisplayName()).isEqualTo("BMW 320i");
    }
}
