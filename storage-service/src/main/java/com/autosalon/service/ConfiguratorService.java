package com.autosalon.service;

import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.exception.EntityNotFoundException;
import com.autosalon.domain.exception.IncompatibleComponentException;
import com.autosalon.domain.model.CarModel;
import com.autosalon.domain.model.ComponentOption;
import com.autosalon.domain.model.Configuration;
import com.autosalon.repository.CarModelRepository;
import com.autosalon.repository.ComponentOptionRepository;
import com.autosalon.repository.specification.CarModelSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

@Service
@Transactional
public class ConfiguratorService {
    private final CarModelRepository modelRepository;
    private final ComponentOptionRepository componentOptionRepository;

    public ConfiguratorService(
            CarModelRepository modelRepository,
            ComponentOptionRepository componentOptionRepository
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
        Set<CarModel> compatibleModels = findModels(compatibleModelIds);
        ComponentOption option = ComponentOption.create(type, name, priceDelta, compatibleModels);
        return componentOptionRepository.save(option);
    }

    public CarModel assignBaseComponent(UUID modelId, ComponentType type, UUID componentOptionId) {
        CarModel model = findModel(modelId);
        ComponentOption option = findOption(componentOptionId);
        ensureOptionFits(model, type, option);
        model.assignBaseComponent(type, option);
        return modelRepository.save(model);
    }

    @Transactional(readOnly = true)
    public Configuration buildBaseConfiguration(UUID modelId) {
        CarModel model = findModel(modelId);
        return buildConfiguration(modelId, model.getBaseComponentOptionIds());
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public List<ComponentOption> listOptionsForModel(UUID modelId, ComponentType type) {
        CarModel model = findModel(modelId);
        requireNonNull(type, "component type");
        return componentOptionRepository.findByTypeAndRemovedFalse(type).stream()
                .filter(option -> option.getType() == type)
                .filter(option -> option.isCompatibleWith(model))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Configuration> listBaseConfigurations(
            String brand,
            Set<ComponentType> componentTypes,
            Set<UUID> componentOptionIds
    ) {
        Specification<CarModel> specification = Specification
                .where(CarModelSpecifications.notRemoved())
                .and(CarModelSpecifications.brandEquals(brand))
                .and(CarModelSpecifications.hasBaseComponentTypes(componentTypes))
                .and(CarModelSpecifications.hasBaseComponentOptionIds(componentOptionIds));

        return modelRepository.findAll(specification).stream()
                .map(model -> buildConfiguration(model.getId(), model.getBaseComponentOptionIds()))
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
        return ServiceSupport.findActiveOrThrow(modelRepository, modelId, "CarModel");
    }

    private ComponentOption findOption(UUID optionId) {
        return ServiceSupport.findActiveOrThrow(componentOptionRepository, optionId, "ComponentOption");
    }

    private Set<CarModel> findModels(Set<UUID> modelIds) {
        Set<CarModel> models = new HashSet<>();
        requireNonNull(modelIds, "model ids").forEach(modelId -> {
            CarModel model = modelRepository.findByIdAndRemovedFalse(modelId)
                    .orElseThrow(() -> new EntityNotFoundException("CarModel", modelId));
            models.add(model);
        });
        return models;
    }
}
