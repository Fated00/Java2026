package com.autosalon.api;

import com.autosalon.api.dto.Dtos.CreateCustomOrderRequest;
import com.autosalon.api.dto.Dtos.CreateInStockOrderRequest;
import com.autosalon.api.dto.Dtos.CustomOrderDto;
import com.autosalon.api.dto.Dtos.InStockOrderDto;
import com.autosalon.domain.model.Configuration;
import com.autosalon.security.CurrentUserService;
import com.autosalon.service.ConfiguratorService;
import com.autosalon.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final ConfiguratorService configuratorService;
    private final ApiMapper mapper;
    private final CurrentUserService currentUserService;

    public OrderController(
            OrderService orderService,
            ConfiguratorService configuratorService,
            ApiMapper mapper,
            CurrentUserService currentUserService
    ) {
        this.orderService = orderService;
        this.configuratorService = configuratorService;
        this.mapper = mapper;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/in-stock")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public List<InStockOrderDto> listInStockOrders() {
        return orderService.listInStockOrders().stream().map(mapper::toDto).toList();
    }

    @GetMapping("/in-stock/{id}")
    @PreAuthorize("@orderSecurity.canReadInStockOrder(#id)")
    public InStockOrderDto getInStockOrder(@PathVariable UUID id) {
        return mapper.toDto(orderService.findInStockOrder(id));
    }

    @PostMapping("/in-stock")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public InStockOrderDto createInStockOrder(@Valid @RequestBody CreateInStockOrderRequest request) {
        return mapper.toDto(orderService.createInStockOrder(currentUserService.currentAppUserId(), request.carId()));
    }

    @PostMapping("/in-stock/{id}/cancel")
    @PreAuthorize("@orderSecurity.canCancelInStockOrder(#id)")
    public InStockOrderDto cancelInStockOrder(@PathVariable UUID id) {
        return mapper.toDto(orderService.cancelInStockOrder(id));
    }

    @GetMapping("/custom")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public List<CustomOrderDto> listCustomOrders() {
        return orderService.listCustomOrders().stream().map(mapper::toDto).toList();
    }

    @GetMapping("/custom/{id}")
    @PreAuthorize("@orderSecurity.canReadCustomOrder(#id)")
    public CustomOrderDto getCustomOrder(@PathVariable UUID id) {
        return mapper.toDto(orderService.findCustomOrder(id));
    }

    @PostMapping("/custom")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public CustomOrderDto createCustomOrder(@Valid @RequestBody CreateCustomOrderRequest request) {
        Configuration configuration = configuratorService.buildConfiguration(request.modelId(), request.selectedOptionIds());
        return mapper.toDto(orderService.createCustomOrder(currentUserService.currentAppUserId(), configuration));
    }

    @PostMapping("/custom/{id}/cancel")
    @PreAuthorize("@orderSecurity.canCancelCustomOrder(#id)")
    public CustomOrderDto cancelCustomOrder(@PathVariable UUID id) {
        return mapper.toDto(orderService.cancelCustomOrder(id));
    }
}
