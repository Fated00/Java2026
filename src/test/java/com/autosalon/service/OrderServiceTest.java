package com.autosalon.service;

import com.autosalon.TestContext;
import com.autosalon.domain.enums.CustomOrderStatus;
import com.autosalon.domain.enums.InStockOrderStatus;
import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.model.Car;
import com.autosalon.domain.model.CarModel;
import com.autosalon.domain.model.Configuration;
import com.autosalon.domain.model.CustomCarOrder;
import com.autosalon.domain.model.InStockCarOrder;
import com.autosalon.domain.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {
    @Test
    void createsInStockOrderAssignsManagerAndReservesCar() {
        TestContext context = new TestContext();
        User client = context.userService.createUser("Client", Role.CLIENT);
        User manager = context.userService.createUser("Manager", Role.DEALERSHIP_MANAGER);
        CarModel model = context.createBmw320();
        Car car = context.catalogService.addCar(model.getId(), "Black", BigDecimal.valueOf(4_050_000));

        InStockCarOrder order = context.orderService.createInStockOrder(client.getId(), car.getId());

        assertEquals(client.getId(), order.getClient().getId());
        assertEquals(manager.getId(), order.getManager().getId());
        assertEquals(InStockOrderStatus.CREATED, order.getStatus());
        assertNotNull(order.getCreatedAt());
        assertFalse(context.catalogService.findCar(car.getId()).isAvailable());
    }

    @Test
    void rejectsPurchaseOfUnavailableCar() {
        TestContext context = new TestContext();
        User client = context.userService.createUser("Client", Role.CLIENT);
        context.userService.createUser("Manager", Role.DEALERSHIP_MANAGER);
        CarModel model = context.createBmw320();
        Car car = context.catalogService.addCar(model.getId(), "Black", BigDecimal.valueOf(4_050_000));
        car.setAvailable(false);

        assertThrows(
                DomainValidationException.class,
                () -> context.orderService.createInStockOrder(client.getId(), car.getId())
        );
    }

    @Test
    void createsCustomOrderAndUpdatesStatuses() {
        TestContext context = new TestContext();
        User client = context.userService.createUser("Client", Role.CLIENT);
        context.userService.createUser("Manager", Role.DEALERSHIP_MANAGER);
        CarModel model = context.createBmw320();
        Configuration configuration = new Configuration(model, Map.of(), model.getBasePrice());

        CustomCarOrder customOrder = context.orderService.createCustomOrder(client.getId(), configuration);
        CustomCarOrder updatedCustom = context.orderService.updateCustomOrderStatus(
                customOrder.getId(),
                CustomOrderStatus.WAITING_FOR_PAYMENT
        );

        assertEquals(CustomOrderStatus.WAITING_FOR_PAYMENT, updatedCustom.getStatus());
        assertEquals(configuration, context.orderService.findCustomOrder(customOrder.getId()).getConfiguration());

        Car car = context.catalogService.addCar(model.getId(), "Black", BigDecimal.valueOf(4_050_000));
        InStockCarOrder inStockOrder = context.orderService.createInStockOrder(client.getId(), car.getId());
        InStockCarOrder updatedInStock = context.orderService.updateInStockOrderStatus(
                inStockOrder.getId(),
                InStockOrderStatus.PAID
        );

        assertEquals(InStockOrderStatus.PAID, updatedInStock.getStatus());
        assertEquals(1, context.orderService.listCustomOrders().size());
        assertEquals(1, context.orderService.listInStockOrders().size());
    }

    @Test
    void rejectsOrderWhenManagerIsMissing() {
        TestContext context = new TestContext();
        User client = context.userService.createUser("Client", Role.CLIENT);
        CarModel model = context.createBmw320();
        Car car = context.catalogService.addCar(model.getId(), "Black", BigDecimal.valueOf(4_050_000));

        assertThrows(
                DomainValidationException.class,
                () -> context.orderService.createInStockOrder(client.getId(), car.getId())
        );
    }
}
