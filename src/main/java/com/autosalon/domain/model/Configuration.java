package com.autosalon.domain.model;

import com.autosalon.domain.enums.ComponentType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;
import static com.autosalon.domain.validation.DomainValidator.requirePositive;

public final class Configuration {
    private final CarModel model;
    private final Map<ComponentType, ComponentOption> selectedComponents;
    private final BigDecimal totalPrice;

    public Configuration(CarModel model, Map<ComponentType, ComponentOption> selectedComponents, BigDecimal totalPrice) {
        this.model = requireNonNull(model, "car model");
        Map<ComponentType, ComponentOption> componentsCopy = new EnumMap<>(ComponentType.class);
        componentsCopy.putAll(requireNonNull(selectedComponents, "selected components"));
        this.selectedComponents = Collections.unmodifiableMap(componentsCopy);
        this.totalPrice = requirePositive(totalPrice, "total price");
    }

    public CarModel getModel() {
        return model;
    }

    public Map<ComponentType, ComponentOption> getSelectedComponents() {
        return selectedComponents;
    }

    public ComponentOption getComponent(ComponentType type) {
        return selectedComponents.get(type);
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}
