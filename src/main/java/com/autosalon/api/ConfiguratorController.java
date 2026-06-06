package com.autosalon.api;

import com.autosalon.api.dto.Dtos.AssignBaseComponentRequest;
import com.autosalon.api.dto.Dtos.BuildConfigurationRequest;
import com.autosalon.api.dto.Dtos.CarModelDto;
import com.autosalon.api.dto.Dtos.ComponentOptionDto;
import com.autosalon.api.dto.Dtos.ConfigurationDto;
import com.autosalon.api.dto.Dtos.RegisterComponentOptionRequest;
import com.autosalon.domain.enums.ComponentType;
import com.autosalon.service.ConfiguratorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/configurator")
public final class ConfiguratorController {
    private final ConfiguratorService configuratorService;
    private final ApiMapper mapper;

    public ConfiguratorController(ConfiguratorService configuratorService, ApiMapper mapper) {
        this.configuratorService = configuratorService;
        this.mapper = mapper;
    }

    @PostMapping("/options")
    @ResponseStatus(HttpStatus.CREATED)
    public ComponentOptionDto registerOption(@Valid @RequestBody RegisterComponentOptionRequest request) {
        return mapper.toDto(configuratorService.registerComponentOption(
                request.type(),
                request.name(),
                request.priceDelta(),
                request.compatibleModelIds()
        ));
    }

    @GetMapping("/options")
    public List<ComponentOptionDto> listOptionsForModel(@RequestParam UUID modelId, @RequestParam ComponentType type) {
        return configuratorService.listOptionsForModel(modelId, type).stream().map(mapper::toDto).toList();
    }

    @PostMapping("/base-components")
    public CarModelDto assignBaseComponent(@Valid @RequestBody AssignBaseComponentRequest request) {
        return mapper.toDto(configuratorService.assignBaseComponent(
                request.modelId(),
                request.type(),
                request.componentOptionId()
        ));
    }

    @GetMapping("/base-configurations")
    public List<ConfigurationDto> listBaseConfigurations(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Set<ComponentType> componentTypes,
            @RequestParam(required = false) Set<UUID> componentOptionIds
    ) {
        return configuratorService.listBaseConfigurations(brand, componentTypes, componentOptionIds).stream()
                .map(mapper::toDto)
                .toList();
    }

    @PostMapping("/configurations")
    public ConfigurationDto buildConfiguration(@Valid @RequestBody BuildConfigurationRequest request) {
        return mapper.toDto(configuratorService.buildConfiguration(request.modelId(), request.selectedOptionIds()));
    }
}
