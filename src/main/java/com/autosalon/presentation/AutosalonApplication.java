package com.autosalon.presentation;

import com.autosalon.domain.enums.BodyType;
import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.enums.DriveType;
import com.autosalon.domain.enums.FuelType;
import com.autosalon.domain.enums.Role;
import com.autosalon.domain.enums.TransmissionType;
import com.autosalon.domain.model.Car;
import com.autosalon.domain.model.CarModel;
import com.autosalon.domain.model.ComponentOption;
import com.autosalon.domain.model.Configuration;
import com.autosalon.domain.model.CustomCarOrder;
import com.autosalon.domain.model.InStockCarOrder;
import com.autosalon.domain.model.Part;
import com.autosalon.domain.model.TestDriveRequest;
import com.autosalon.domain.model.User;
import com.autosalon.repository.CrudRepository;
import com.autosalon.repository.InMemoryCrudRepository;
import com.autosalon.service.CarCatalogService;
import com.autosalon.service.ConfiguratorService;
import com.autosalon.service.OrderService;
import com.autosalon.service.TestDriveService;
import com.autosalon.service.UserService;
import com.autosalon.service.filter.CarFilter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class AutosalonApplication {
    private AutosalonApplication() {
    }

    public static void main(String[] args) {
        CrudRepository<User> userRepository = new InMemoryCrudRepository<>();
        CrudRepository<CarModel> modelRepository = new InMemoryCrudRepository<>();
        CrudRepository<ComponentOption> componentRepository = new InMemoryCrudRepository<>();
        CrudRepository<Car> carRepository = new InMemoryCrudRepository<>();
        CrudRepository<Part> partRepository = new InMemoryCrudRepository<>();
        CrudRepository<InStockCarOrder> inStockOrderRepository = new InMemoryCrudRepository<>();
        CrudRepository<CustomCarOrder> customOrderRepository = new InMemoryCrudRepository<>();
        CrudRepository<TestDriveRequest> testDriveRepository = new InMemoryCrudRepository<>();

        UserService userService = new UserService(userRepository);
        CarCatalogService catalogService = new CarCatalogService(modelRepository, carRepository, partRepository);
        ConfiguratorService configuratorService = new ConfiguratorService(modelRepository, componentRepository);
        OrderService orderService = new OrderService(
                userRepository,
                carRepository,
                inStockOrderRepository,
                customOrderRepository
        );
        TestDriveService testDriveService = new TestDriveService(userRepository, carRepository, testDriveRepository);

        User client = userService.createUser("Ivan Petrov", Role.CLIENT);
        User manager = userService.createUser("Maria Sokolova", Role.DEALERSHIP_MANAGER);
        userService.createUser("Warehouse Admin", Role.WAREHOUSE_ADMIN);
        userService.createUser("System Admin", Role.SYSTEM_ADMIN);

        CarModel bmw320 = catalogService.createModel(
                "BMW",
                "320i",
                BodyType.SEDAN,
                FuelType.GASOLINE,
                184,
                BigDecimal.valueOf(2.0),
                TransmissionType.AUTOMATIC,
                DriveType.REAR,
                BigDecimal.valueOf(4_000_000),
                EnumSet.allOf(ComponentType.class)
        );
        CarModel bmw330 = catalogService.createModel(
                "BMW",
                "330i",
                BodyType.SEDAN,
                FuelType.GASOLINE,
                258,
                BigDecimal.valueOf(2.0),
                TransmissionType.AUTOMATIC,
                DriveType.REAR,
                BigDecimal.valueOf(4_800_000),
                EnumSet.allOf(ComponentType.class)
        );

        ComponentOption wheels17 = configuratorService.registerComponentOption(
                ComponentType.WHEELS,
                "17'' Standard",
                BigDecimal.ZERO,
                Set.of(bmw320.getId())
        );
        ComponentOption wheels19 = configuratorService.registerComponentOption(
                ComponentType.WHEELS,
                "19'' M-Sport",
                BigDecimal.valueOf(95_000),
                Set.of(bmw320.getId(), bmw330.getId())
        );
        ComponentOption automatic8 = configuratorService.registerComponentOption(
                ComponentType.TRANSMISSION,
                "Automatic 8AT",
                BigDecimal.ZERO,
                Set.of(bmw320.getId(), bmw330.getId())
        );
        ComponentOption steeringStandard = configuratorService.registerComponentOption(
                ComponentType.STEERING_WHEEL,
                "Sport leather standard",
                BigDecimal.ZERO,
                Set.of(bmw320.getId(), bmw330.getId())
        );
        ComponentOption steeringHeated = configuratorService.registerComponentOption(
                ComponentType.STEERING_WHEEL,
                "M-Sport heated",
                BigDecimal.valueOf(25_000),
                Set.of(bmw320.getId(), bmw330.getId())
        );
        ComponentOption graphiteInterior = configuratorService.registerComponentOption(
                ComponentType.INTERIOR,
                "Graphite fabric",
                BigDecimal.ZERO,
                Set.of(bmw320.getId())
        );
        ComponentOption dakotaInterior = configuratorService.registerComponentOption(
                ComponentType.INTERIOR,
                "Dakota leather",
                BigDecimal.valueOf(110_000),
                Set.of(bmw320.getId(), bmw330.getId())
        );

        configuratorService.assignBaseComponent(bmw320.getId(), ComponentType.WHEELS, wheels17.getId());
        configuratorService.assignBaseComponent(bmw320.getId(), ComponentType.TRANSMISSION, automatic8.getId());
        configuratorService.assignBaseComponent(bmw320.getId(), ComponentType.STEERING_WHEEL, steeringStandard.getId());
        configuratorService.assignBaseComponent(bmw320.getId(), ComponentType.INTERIOR, graphiteInterior.getId());

        Car inStockCar = catalogService.addCar(bmw320.getId(), "Black", BigDecimal.valueOf(4_050_000));
        Car testDriveCar = catalogService.addCar(bmw330.getId(), "Blue", BigDecimal.valueOf(4_900_000));
        catalogService.addPart("BMW-OIL-001", "Original oil filter", BigDecimal.valueOf(4_900), Set.of(bmw320.getId()));

        Configuration configuration = configuratorService.buildConfiguration(
                bmw320.getId(),
                Map.of(
                        ComponentType.WHEELS, wheels19.getId(),
                        ComponentType.TRANSMISSION, automatic8.getId(),
                        ComponentType.STEERING_WHEEL, steeringHeated.getId(),
                        ComponentType.INTERIOR, dakotaInterior.getId()
                )
        );

        InStockCarOrder inStockOrder = orderService.createInStockOrder(client.getId(), inStockCar.getId());
        CustomCarOrder customOrder = orderService.createCustomOrder(client.getId(), configuration);
        testDriveService.addCarToTestDriveList(testDriveCar.getId());
        TestDriveRequest testDriveRequest = testDriveService.requestTestDrive(
                client.getId(),
                testDriveCar.getId(),
                LocalDateTime.now().plusDays(2)
        );

        int availableBmwCount = catalogService.listAvailableCars(
                CarFilter.builder()
                        .brand("BMW")
                        .bodyType(BodyType.SEDAN)
                        .build()
        ).size();

        System.out.println("Autosalon demo");
        System.out.println("Manager assigned: " + manager.getFullName());
        System.out.println("Configured model: " + configuration.getModel().getDisplayName());
        System.out.println("Configuration price: " + configuration.getTotalPrice());
        System.out.println("In-stock order status: " + inStockOrder.getStatus());
        System.out.println("Custom order status: " + customOrder.getStatus());
        System.out.println("Test-drive starts at: " + testDriveRequest.getStartsAt());
        System.out.println("Available BMW sedans after purchase: " + availableBmwCount);
    }
}
