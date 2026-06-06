package com.autosalon.service;

import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.exception.EntityNotFoundException;
import com.autosalon.domain.exception.IncompatibleComponentException;
import com.autosalon.domain.model.CarModel;
import com.autosalon.domain.model.ComponentOption;
import com.autosalon.domain.model.Configuration;
import com.autosalon.repository.CrudRepository;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

public final class ConfiguratorService {
    private final CrudRepository<CarModel> modelRepository;
    private final CrudRepository<ComponentOption> componentOptionRepository;

    public ConfiguratorService(
            CrudRepository<CarModel> modelRepository,
            CrudRepository<ComponentOption> componentOptionRepository
    ) {
        this.modelRepository = requireNonNull(modelRepository, "model repository");
        this.componentOptionRepository = requireNonNull(componentOptionRepository, "component option repository");
    }

    public ComponentOption registerComponentOption(
            ComponentType type,
            String name,
            BigDecimal priceDelta,
            Set<UUID> compatibleModelIds
    ) {
        ensureModelsExist(compatibleModelIds);
        ComponentOption option = ComponentOption.create(type, name, priceDelta, compatibleModelIds);
        return componentOptionRepository.save(option);
    }

    public CarModel assignBaseComponent(UUID modelId, ComponentType type, UUID componentOptionId) {
        CarModel model = findModel(modelId);
        ComponentOption option = findOption(componentOptionId);
        ensureOptionFits(model, type, option);
        model.assignBaseComponent(type, componentOptionId);
        return modelRepository.save(model);
    }

    public Configuration buildBaseConfiguration(UUID modelId) {
        CarModel model = findModel(modelId);
        return buildConfiguration(modelId, model.getBaseComponentOptionIds());
    }

    public Configuration buildConfiguration(UUID modelId, Map<ComponentType, UUID> selectedOptionIds) {
        CarModel model = findModel(modelId);
        Map<ComponentType, UUID> selectedIds = requireNonNull(selectedOptionIds, "selected option ids");
        validateRequiredComponents(model, selectedIds);

        Map<ComponentType, ComponentOption> selectedComponents = new EnumMap<>(ComponentType.class);
        BigDecimal totalPrice = model.getBasePrice();

        for (ComponentType type : model.getRequiredComponentTypes()) {
            ComponentOption option = findOption(selectedIds.get(type));
            ensureOptionFits(model, type, option);
            selectedComponents.put(type, option);
            totalPrice = totalPrice.add(option.getPriceDelta());
        }

        return new Configuration(model, selectedComponents, totalPrice);
    }

    public List<ComponentOption> listOptionsForModel(UUID modelId, ComponentType type) {
        CarModel model = findModel(modelId);
        requireNonNull(type, "component type");
        return componentOptionRepository.findAll().stream()
                .filter(option -> option.getType() == type)
                .filter(option -> option.isCompatibleWith(model))
                .toList();
    }

    private void validateRequiredComponents(CarModel model, Map<ComponentType, UUID> selectedOptionIds) {
        selectedOptionIds.keySet().stream()
                .filter(type -> !model.getRequiredComponentTypes().contains(type))
                .findFirst()
                .ifPresent(type -> {
                    throw new DomainValidationException(type + " is not configurable for " + model.getDisplayName());
                });

        model.getRequiredComponentTypes().stream()
                .filter(type -> !selectedOptionIds.containsKey(type))
                .findFirst()
                .ifPresent(type -> {
                    throw new DomainValidationException(
                            "missing required component \"" + type.getDisplayName() + "\""
                    );
                });
    }

    private void ensureOptionFits(CarModel model, ComponentType requestedType, ComponentOption option) {
        if (option.getType() != requestedType) {
            throw new DomainValidationException(
                    "component option " + option.getName() + " has type " + option.getType()
                            + ", expected " + requestedType
            );
        }
        if (!option.isCompatibleWith(model)) {
            throw new IncompatibleComponentException(
                    "selected " + requestedType.getDisplayName() + " \"" + option.getName()
                            + "\" is not available for model " + model.getDisplayName()
            );
        }
    }

    private CarModel findModel(UUID modelId) {
        return ServiceSupport.findOrThrow(modelRepository, modelId, "CarModel");
    }

    private ComponentOption findOption(UUID optionId) {
        return ServiceSupport.findOrThrow(componentOptionRepository, optionId, "ComponentOption");
    }

    private void ensureModelsExist(Set<UUID> modelIds) {
        requireNonNull(modelIds, "model ids").stream()
                .filter(modelId -> !modelRepository.existsById(modelId))
                .findFirst()
                .ifPresent(modelId -> {
                    throw new EntityNotFoundException("CarModel", modelId);
                });
    }
}
