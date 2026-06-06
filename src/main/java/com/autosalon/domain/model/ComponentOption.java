package com.autosalon.domain.model;

import com.autosalon.domain.Identifiable;
import com.autosalon.domain.enums.ComponentType;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;
import static com.autosalon.domain.validation.DomainValidator.requireNotEmpty;
import static com.autosalon.domain.validation.DomainValidator.requireText;

public final class ComponentOption implements Identifiable {
    private final UUID id;
    private final ComponentType type;
    private final String name;
    private final BigDecimal priceDelta;
    private final Set<UUID> compatibleModelIds;

    public ComponentOption(UUID id, ComponentType type, String name, BigDecimal priceDelta, Set<UUID> compatibleModelIds) {
        this.id = requireId(id, "component option id");
        this.type = requireNonNull(type, "component type");
        this.name = requireText(name, "component option name");
        this.priceDelta = requireNonNull(priceDelta, "price delta");
        this.compatibleModelIds = Set.copyOf(requireNotEmpty(compatibleModelIds, "compatible model ids"));
    }

    public static ComponentOption create(
            ComponentType type,
            String name,
            BigDecimal priceDelta,
            Set<UUID> compatibleModelIds
    ) {
        return new ComponentOption(UUID.randomUUID(), type, name, priceDelta, compatibleModelIds);
    }

    @Override
    public UUID getId() {
        return id;
    }

    public ComponentType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPriceDelta() {
        return priceDelta;
    }

    public Set<UUID> getCompatibleModelIds() {
        return compatibleModelIds;
    }

    public boolean isCompatibleWith(CarModel model) {
        return compatibleModelIds.contains(model.getId());
    }
}
