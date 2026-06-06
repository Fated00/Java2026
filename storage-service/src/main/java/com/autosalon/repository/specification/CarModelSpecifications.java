package com.autosalon.repository.specification;

import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.model.CarModel;
import com.autosalon.domain.model.ComponentOption;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.UUID;

public final class CarModelSpecifications {
    private CarModelSpecifications() {
    }

    public static Specification<CarModel> notRemoved() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("removed"));
    }

    public static Specification<CarModel> brandEquals(String brand) {
        return (root, query, criteriaBuilder) -> {
            if (brand == null || brand.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(criteriaBuilder.lower(root.get("brand")), brand.trim().toLowerCase());
        };
    }

    public static Specification<CarModel> hasBaseComponentTypes(Collection<ComponentType> componentTypes) {
        return (root, query, criteriaBuilder) -> {
            if (componentTypes == null || componentTypes.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            query.distinct(true);
            MapJoin<CarModel, ComponentType, ComponentOption> join = root.joinMap("baseComponentOptions");
            return join.key().in(componentTypes);
        };
    }

    public static Specification<CarModel> hasBaseComponentOptionIds(Collection<UUID> componentOptionIds) {
        return (root, query, criteriaBuilder) -> {
            if (componentOptionIds == null || componentOptionIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            query.distinct(true);
            MapJoin<CarModel, ComponentType, ComponentOption> join = root.joinMap("baseComponentOptions");
            Predicate idMatch = join.value().get("id").in(componentOptionIds);
            return idMatch;
        };
    }
}
