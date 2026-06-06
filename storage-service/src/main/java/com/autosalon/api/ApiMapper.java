package com.autosalon.api;

import com.autosalon.api.dto.Dtos.CarDto;
import com.autosalon.api.dto.Dtos.CarModelDto;
import com.autosalon.api.dto.Dtos.ComponentOptionDto;
import com.autosalon.api.dto.Dtos.ConfigurationDto;
import com.autosalon.api.dto.Dtos.PartDto;
import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.model.Car;
import com.autosalon.domain.model.CarModel;
import com.autosalon.domain.model.ComponentOption;
import com.autosalon.domain.model.Configuration;
import com.autosalon.domain.model.Part;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class ApiMapper {
    private final ModelMapper modelMapper;

    public ApiMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public CarModelDto toDto(CarModel model) {
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        return new CarModelDto(
                model.getId(),
                model.getBrand(),
                model.getName(),
                model.getDisplayName(),
                model.getBodyType(),
                model.getFuelType(),
                model.getEnginePowerHp(),
                model.getEngineVolumeLiters(),
                model.getTransmissionType(),
                model.getDriveType(),
                model.getBasePrice(),
                model.getRequiredComponentTypes(),
                model.getBaseComponentOptionIds()
        );
    }

    public CarDto toDto(Car car) {
        return new CarDto(
                car.getId(),
                car.getModel().getId(),
                car.getModel().getDisplayName(),
                car.getColor(),
                car.getPrice(),
                car.isAvailable(),
                car.isTestDriveAvailable()
        );
    }

    public PartDto toDto(Part part) {
        return new PartDto(part.getId(), part.getSku(), part.getName(), part.getPrice(), part.getCompatibleModelIds());
    }

    public ComponentOptionDto toDto(ComponentOption option) {
        return new ComponentOptionDto(
                option.getId(),
                option.getType(),
                option.getName(),
                option.getPriceDelta(),
                option.getCompatibleModelIds()
        );
    }

    public ConfigurationDto toDto(Configuration configuration) {
        Map<ComponentType, ComponentOptionDto> components = new EnumMap<>(ComponentType.class);
        configuration.getSelectedComponents().forEach((type, option) -> components.put(type, toDto(option)));
        return new ConfigurationDto(
                configuration.getModel().getId(),
                configuration.getModel().getDisplayName(),
                components,
                configuration.getTotalPrice()
        );
    }
}
