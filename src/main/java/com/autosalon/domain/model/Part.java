package com.autosalon.domain.model;

import com.autosalon.domain.Identifiable;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireId;
import static com.autosalon.domain.validation.DomainValidator.requireNotEmpty;
import static com.autosalon.domain.validation.DomainValidator.requirePositive;
import static com.autosalon.domain.validation.DomainValidator.requireText;

public final class Part implements Identifiable {
    private final UUID id;
    private final String sku;
    private String name;
    private BigDecimal price;
    private Set<UUID> compatibleModelIds;

    public Part(UUID id, String sku, String name, BigDecimal price, Set<UUID> compatibleModelIds) {
        this.id = requireId(id, "part id");
        this.sku = requireText(sku, "sku");
        this.name = requireText(name, "part name");
        this.price = requirePositive(price, "part price");
        this.compatibleModelIds = Set.copyOf(requireNotEmpty(compatibleModelIds, "compatible model ids"));
    }

    public static Part create(String sku, String name, BigDecimal price, Set<UUID> compatibleModelIds) {
        return new Part(UUID.randomUUID(), sku, name, price, compatibleModelIds);
    }

    @Override
    public UUID getId() {
        return id;
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
        return compatibleModelIds;
    }

    public void setCompatibleModelIds(Set<UUID> compatibleModelIds) {
        this.compatibleModelIds = Set.copyOf(requireNotEmpty(compatibleModelIds, "compatible model ids"));
    }
}
