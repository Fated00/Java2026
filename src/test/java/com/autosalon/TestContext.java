package com.autosalon;

import com.autosalon.domain.enums.BodyType;
import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.enums.DriveType;
import com.autosalon.domain.enums.FuelType;
import com.autosalon.domain.enums.TransmissionType;
import com.autosalon.domain.model.Car;
import com.autosalon.domain.model.CarModel;
import com.autosalon.domain.model.ComponentOption;
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
import com.autosalon.service.UserService;

import java.math.BigDecimal;
import java.util.EnumSet;

public final class TestContext {
    public final CrudRepository<User> userRepository = new InMemoryCrudRepository<>();
    public final CrudRepository<CarModel> modelRepository = new InMemoryCrudRepository<>();
    public final CrudRepository<ComponentOption> componentRepository = new InMemoryCrudRepository<>();
    public final CrudRepository<Car> carRepository = new InMemoryCrudRepository<>();
    public final CrudRepository<Part> partRepository = new InMemoryCrudRepository<>();
    public final CrudRepository<InStockCarOrder> inStockOrderRepository = new InMemoryCrudRepository<>();
    public final CrudRepository<CustomCarOrder> customOrderRepository = new InMemoryCrudRepository<>();
    public final CrudRepository<TestDriveRequest> testDriveRepository = new InMemoryCrudRepository<>();

    public final UserService userService = new UserService(userRepository);
    public final CarCatalogService catalogService = new CarCatalogService(modelRepository, carRepository, partRepository);
    public final ConfiguratorService configuratorService = new ConfiguratorService(modelRepository, componentRepository);
    public final OrderService orderService = new OrderService(
            userRepository,
            carRepository,
            inStockOrderRepository,
            customOrderRepository
    );

    public CarModel createBmw320() {
        return catalogService.createModel(
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
    }

    public CarModel createBmw330() {
        return catalogService.createModel(
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
    }
}
