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
import com.autosalon.repository.CrudRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

public final class OrderService {
    private final CrudRepository<User> userRepository;
    private final CrudRepository<Car> carRepository;
    private final CrudRepository<InStockCarOrder> inStockOrderRepository;
    private final CrudRepository<CustomCarOrder> customOrderRepository;

    public OrderService(
            CrudRepository<User> userRepository,
            CrudRepository<Car> carRepository,
            CrudRepository<InStockCarOrder> inStockOrderRepository,
            CrudRepository<CustomCarOrder> customOrderRepository
    ) {
        this.userRepository = requireNonNull(userRepository, "user repository");
        this.carRepository = requireNonNull(carRepository, "car repository");
        this.inStockOrderRepository = requireNonNull(inStockOrderRepository, "in-stock order repository");
        this.customOrderRepository = requireNonNull(customOrderRepository, "custom order repository");
    }

    public InStockCarOrder createInStockOrder(UUID clientId, UUID carId) {
        User client = findUserWithRole(clientId, Role.CLIENT, "client");
        User manager = assignManager();
        Car car = ServiceSupport.findOrThrow(carRepository, carId, "Car");
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

    public InStockCarOrder findInStockOrder(UUID id) {
        return ServiceSupport.findOrThrow(inStockOrderRepository, id, "InStockCarOrder");
    }

    public CustomCarOrder findCustomOrder(UUID id) {
        return ServiceSupport.findOrThrow(customOrderRepository, id, "CustomCarOrder");
    }

    public List<InStockCarOrder> listInStockOrders() {
        return inStockOrderRepository.findAll();
    }

    public List<CustomCarOrder> listCustomOrders() {
        return customOrderRepository.findAll();
    }

    public InStockCarOrder updateInStockOrderStatus(UUID orderId, InStockOrderStatus status) {
        InStockCarOrder order = findInStockOrder(orderId);
        order.setStatus(status);
        return inStockOrderRepository.save(order);
    }

    public CustomCarOrder updateCustomOrderStatus(UUID orderId, CustomOrderStatus status) {
        CustomCarOrder order = findCustomOrder(orderId);
        order.setStatus(status);
        return customOrderRepository.save(order);
    }

    private User findUserWithRole(UUID userId, Role role, String fieldName) {
        User user = ServiceSupport.findOrThrow(userRepository, userId, "User");
        return ServiceSupport.requireRole(user, role, fieldName);
    }

    private User assignManager() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.DEALERSHIP_MANAGER)
                .min(Comparator.comparing(user -> user.getId().toString()))
                .orElseThrow(() -> new DomainValidationException("no dealership manager registered"));
    }
}
