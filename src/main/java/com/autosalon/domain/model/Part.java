package com.autosalon.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import static com.autosalon.domain.validation.DomainValidator.requireNotEmpty;
import static com.autosalon.domain.validation.DomainValidator.requirePositive;
import static com.autosalon.domain.validation.DomainValidator.requireText;

@Entity
@Table(name = "parts")
public class Part extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "part_compatible_models",
            joinColumns = @JoinColumn(name = "part_id"),
            inverseJoinColumns = @JoinColumn(name = "model_id")
    )
    private Set<CarModel> compatibleModels = new HashSet<>();

    protected Part() {
    }

    public Part(UUID id, String sku, String name, BigDecimal price, Set<CarModel> compatibleModels) {
        super(requireId(id, "part id"));
        this.sku = requireText(sku, "sku");
        this.name = requireText(name, "part name");
        this.price = requirePositive(price, "part price");
        this.compatibleModels = new HashSet<>(requireNotEmpty(compatibleModels, "compatible models"));
    }

    public static Part create(String sku, String name, BigDecimal price, Set<CarModel> compatibleModels) {
        return new Part(UUID.randomUUID(), sku, name, price, compatibleModels);
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireText(name, "part name");
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = requirePositive(price, "part price");
    }

    public Set<UUID> getCompatibleModelIds() {
        return compatibleModels.stream()
                .map(CarModel::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<CarModel> getCompatibleModels() {
        return Set.copyOf(compatibleModels);
    }

    public void setCompatibleModels(Set<CarModel> compatibleModels) {
        this.compatibleModels = new HashSet<>(requireNotEmpty(compatibleModels, "compatible models"));
    }
}
