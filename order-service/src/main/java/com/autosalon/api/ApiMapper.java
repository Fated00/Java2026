package com.autosalon.api;

import com.autosalon.api.dto.Dtos.ComponentOptionDto;
import com.autosalon.api.dto.Dtos.ConfigurationDto;
import com.autosalon.api.dto.Dtos.CustomOrderDto;
import com.autosalon.api.dto.Dtos.InStockOrderDto;
import com.autosalon.api.dto.Dtos.TestDriveRequestDto;
import com.autosalon.api.dto.Dtos.UserDto;
import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.model.CustomCarOrder;
import com.autosalon.domain.model.InStockCarOrder;
import com.autosalon.domain.model.TestDriveRequest;
import com.autosalon.domain.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class ApiMapper {
    private final ModelMapper modelMapper;

    public ApiMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public UserDto toDto(User user) {
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        return new UserDto(user.getId(), user.getFullName(), user.getRole());
    }

    public InStockOrderDto toDto(InStockCarOrder order) {
        return new InStockOrderDto(
                order.getId(),
                order.getClient().getId(),
                order.getManager().getId(),
                order.getCarId(),
                order.getStatus(),
                order.getOrderedAt()
        );
    }

    public CustomOrderDto toDto(CustomCarOrder order) {
        return new CustomOrderDto(
                order.getId(),
                order.getClient().getId(),
                order.getManager().getId(),
                toConfigurationDto(order),
                order.getStatus(),
                order.getOrderedAt()
        );
    }

    public TestDriveRequestDto toDto(TestDriveRequest request) {
        return new TestDriveRequestDto(
                request.getId(),
                request.getClient().getId(),
                request.getCarId(),
                request.getStartsAt()
        );
    }

    private ConfigurationDto toConfigurationDto(CustomCarOrder order) {
        Map<ComponentType, ComponentOptionDto> components = new EnumMap<>(ComponentType.class);
        order.getSelectedOptionIds().forEach((type, optionId) -> components.put(type, toOptionDto(type, optionId)));
        return new ConfigurationDto(order.getModelId(), order.getModelId().toString(), components, order.getTotalPrice());
    }

    private ComponentOptionDto toOptionDto(ComponentType type, UUID optionId) {
        return new ComponentOptionDto(optionId, type, type.getDisplayName(), BigDecimal.ZERO, Set.of());
    }
}
