package com.autosalon.domain.model;

import com.autosalon.domain.enums.ComponentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNonNull;
import static com.autosalon.domain.validation.DomainValidator.requireNotEmpty;
import static com.autosalon.domain.validation.DomainValidator.requireText;

@Entity
@Table(name = "component_options")
public class ComponentOption extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComponentType type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal priceDelta;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "component_option_compatible_models",
            joinColumns = @JoinColumn(name = "component_option_id"),
            inverseJoinColumns = @JoinColumn(name = "model_id")
    )
    private Set<CarModel> compatibleModels = new HashSet<>();

    protected ComponentOption() {
    }

    public ComponentOption(UUID id, ComponentType type, String name, BigDecimal priceDelta, Set<CarModel> compatibleModels) {
        super(requireId(id, "component option id"));
        this.type = requireNonNull(type, "component type");
        this.name = requireText(name, "component option name");
        this.priceDelta = requireNonNull(priceDelta, "price delta");
        this.compatibleModels = new HashSet<>(requireNotEmpty(compatibleModels, "compatible models"));
    }

    public static ComponentOption create(
            ComponentType type,
            String name,
            BigDecimal priceDelta,
            Set<CarModel> compatibleModels
    ) {
        return new ComponentOption(UUID.randomUUID(), type, name, priceDelta, compatibleModels);
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

    public Set<CarModel> getCompatibleModels() {
        return Set.copyOf(compatibleModels);
    }

    public Set<UUID> getCompatibleModelIds() {
        return compatibleModels.stream()
                .map(CarModel::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isCompatibleWith(CarModel model) {
        return getCompatibleModelIds().contains(model.getId());
    }
}
