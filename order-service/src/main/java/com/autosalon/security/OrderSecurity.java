package com.autosalon.security;

import com.autosalon.repository.CustomCarOrderRepository;
import com.autosalon.repository.InStockCarOrderRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("orderSecurity")
public class OrderSecurity {
    private final InStockCarOrderRepository inStockOrderRepository;
    private final CustomCarOrderRepository customOrderRepository;
    private final CurrentUserService currentUserService;

    public OrderSecurity(
            InStockCarOrderRepository inStockOrderRepository,
            CustomCarOrderRepository customOrderRepository,
            CurrentUserService currentUserService
    ) {
        this.inStockOrderRepository = inStockOrderRepository;
        this.customOrderRepository = customOrderRepository;
        this.currentUserService = currentUserService;
    }

    public boolean canReadInStockOrder(UUID orderId) {
        if (currentUserService.hasAnyRole("MANAGER", "ADMIN")) {
            return true;
        }
        UUID currentUserId = currentUserService.currentAppUserId();
        return inStockOrderRepository.findByIdAndRemovedFalse(orderId)
                .map(order -> order.getClient().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean canReadCustomOrder(UUID orderId) {
        if (currentUserService.hasAnyRole("MANAGER", "ADMIN")) {
            return true;
        }
        UUID currentUserId = currentUserService.currentAppUserId();
        return customOrderRepository.findByIdAndRemovedFalse(orderId)
                .map(order -> order.getClient().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean canManageInStockOrder(UUID orderId) {
        return canReadInStockOrder(orderId) || currentUserService.hasRole("ADMIN");
    }

    public boolean canManageCustomOrder(UUID orderId) {
        return canReadCustomOrder(orderId) || currentUserService.hasRole("ADMIN");
    }
}
