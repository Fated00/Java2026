package com.autosalon.service;

import com.autosalon.domain.enums.CustomOrderStatus;
import com.autosalon.domain.enums.InStockOrderStatus;
import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.model.Car;
import com.autosalon.domain.model.Configuration;
import com.autosalon.domain.model.CustomCarOrder;
import com.autosalon.domain.model.InStockCarOrder;
import com.autosalon.domain.model.User;
import com.autosalon.repository.CarRepository;
import com.autosalon.repository.CustomCarOrderRepository;
import com.autosalon.repository.InStockCarOrderRepository;
import com.autosalon.repository.UserRepository;
import com.autosalon.security.CurrentUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

@Service
@Transactional
public class OrderService {
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final InStockCarOrderRepository inStockOrderRepository;
    private final CustomCarOrderRepository customOrderRepository;
    private final CurrentUserService currentUserService;

    public OrderService(
            UserRepository userRepository,
            CarRepository carRepository,
            InStockCarOrderRepository inStockOrderRepository,
            CustomCarOrderRepository customOrderRepository,
            CurrentUserService currentUserService
    ) {
        this.userRepository = requireNonNull(userRepository, "user repository");
        this.carRepository = requireNonNull(carRepository, "car repository");
        this.inStockOrderRepository = requireNonNull(inStockOrderRepository, "in-stock order repository");
        this.customOrderRepository = requireNonNull(customOrderRepository, "custom order repository");
        this.currentUserService = requireNonNull(currentUserService, "current user service");
    }

    public InStockCarOrder createInStockOrder(UUID clientId, UUID carId) {
        User client = findUserWithRole(clientId, Role.CLIENT, "client");
        User manager = assignManager();
        Car car = ServiceSupport.findActiveOrThrow(carRepository, carId, "Car");
        if (!car.isAvailable()) {
            throw new DomainValidationException("car is not available for purchase");
        }

        car.setAvailable(false);
        carRepository.save(car);
        return inStockOrderRepository.save(InStockCarOrder.create(client, manager, car));
    }

    public CustomCarOrder createCustomOrder(UUID clientId, Configuration configuration) {
        User client = findUserWithRole(clientId, Role.CLIENT, "client");
        User manager = assignManager();
        return customOrderRepository.save(CustomCarOrder.create(client, manager, requireNonNull(configuration, "configuration")));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@orderSecurity.canReadInStockOrder(#id)")
    public InStockCarOrder findInStockOrder(UUID id) {
        return ServiceSupport.findActiveOrThrow(inStockOrderRepository, id, "InStockCarOrder");
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@orderSecurity.canReadCustomOrder(#id)")
    public CustomCarOrder findCustomOrder(UUID id) {
        return ServiceSupport.findActiveOrThrow(customOrderRepository, id, "CustomCarOrder");
    }

    @Transactional(readOnly = true)
    public List<InStockCarOrder> listInStockOrders() {
        List<InStockCarOrder> orders = inStockOrderRepository.findByRemovedFalse();
        if (currentUserService.hasRole("USER") && !currentUserService.hasAnyRole("MANAGER", "ADMIN")) {
            UUID currentUserId = currentUserService.currentAppUserId();
            return orders.stream()
                    .filter(order -> order.getClient().getId().equals(currentUserId))
                    .toList();
        }
        return orders;
    }

    @Transactional(readOnly = true)
    public List<CustomCarOrder> listCustomOrders() {
        List<CustomCarOrder> orders = customOrderRepository.findByRemovedFalse();
        if (currentUserService.hasRole("USER") && !currentUserService.hasAnyRole("MANAGER", "ADMIN")) {
            UUID currentUserId = currentUserService.currentAppUserId();
            return orders.stream()
                    .filter(order -> order.getClient().getId().equals(currentUserId))
                    .toList();
        }
        return orders;
    }

    @PreAuthorize("@orderSecurity.canCancelInStockOrder(#orderId)")
    public InStockCarOrder cancelInStockOrder(UUID orderId) {
        InStockCarOrder order = ServiceSupport.findActiveOrThrow(inStockOrderRepository, orderId, "InStockCarOrder");
        order.setStatus(InStockOrderStatus.CANCELLED);
        return inStockOrderRepository.save(order);
    }

    @PreAuthorize("@orderSecurity.canCancelCustomOrder(#orderId)")
    public CustomCarOrder cancelCustomOrder(UUID orderId) {
        CustomCarOrder order = ServiceSupport.findActiveOrThrow(customOrderRepository, orderId, "CustomCarOrder");
        order.setStatus(CustomOrderStatus.CANCELLED);
        return customOrderRepository.save(order);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public InStockCarOrder updateInStockOrderStatus(UUID orderId, InStockOrderStatus status) {
        InStockCarOrder order = findInStockOrder(orderId);
        order.setStatus(status);
        return inStockOrderRepository.save(order);
    }

    @PreAuthorize("hasAnyRole('MANAGER','WAREHOUSE_ADMIN','ADMIN')")
    public CustomCarOrder updateCustomOrderStatus(UUID orderId, CustomOrderStatus status) {
        CustomCarOrder order = findCustomOrder(orderId);
        order.setStatus(status);
        return customOrderRepository.save(order);
    }

    private User findUserWithRole(UUID userId, Role role, String fieldName) {
        User user = ServiceSupport.findActiveOrThrow(userRepository, userId, "User");
        return ServiceSupport.requireRole(user, role, fieldName);
    }

    private User assignManager() {
        return userRepository.findByRoleAndRemovedFalse(Role.DEALERSHIP_MANAGER).stream()
                .filter(user -> user.getRole() == Role.DEALERSHIP_MANAGER)
                .min(Comparator.comparing(user -> user.getId().toString()))
                .orElseThrow(() -> new DomainValidationException("no dealership manager registered"));
    }
}
