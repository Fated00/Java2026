package com.autosalon.service;

import com.autosalon.domain.enums.BodyType;
import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.enums.DriveType;
import com.autosalon.domain.enums.FuelType;
import com.autosalon.domain.enums.TransmissionType;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.exception.EntityNotFoundException;
import com.autosalon.domain.model.Car;
import com.autosalon.domain.model.CarModel;
import com.autosalon.domain.model.Part;
import com.autosalon.repository.CrudRepository;
import com.autosalon.service.filter.CarFilter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

public final class CarCatalogService {
    private final CrudRepository<CarModel> modelRepository;
    private final CrudRepository<Car> carRepository;
    private final CrudRepository<Part> partRepository;

    public CarCatalogService(
            CrudRepository<CarModel> modelRepository,
            CrudRepository<Car> carRepository,
            CrudRepository<Part> partRepository
    ) {
        this.modelRepository = requireNonNull(modelRepository, "model repository");
        this.carRepository = requireNonNull(carRepository, "car repository");
        this.partRepository = requireNonNull(partRepository, "part repository");
    }

    public CarModel createModel(
            String brand,
            String name,
            BodyType bodyType,
            FuelType fuelType,
            int enginePowerHp,
            BigDecimal engineVolumeLiters,
            TransmissionType transmissionType,
            DriveType driveType,
            BigDecimal basePrice,
            Set<ComponentType> requiredComponentTypes
    ) {
        CarModel model = CarModel.create(
                brand,
                name,
                bodyType,
                fuelType,
                enginePowerHp,
                engineVolumeLiters,
                transmissionType,
                driveType,
                basePrice,
                requiredComponentTypes
        );
        return modelRepository.save(model);
    }

    public CarModel findModel(UUID id) {
        return ServiceSupport.findOrThrow(modelRepository, id, "CarModel");
    }

    public List<CarModel> listModels() {
        return modelRepository.findAll();
    }

    public Car addCar(UUID modelId, String color, BigDecimal price) {
        CarModel model = findModel(modelId);
        return carRepository.save(Car.create(model, color, price));
    }

    public Car findCar(UUID id) {
        return ServiceSupport.findOrThrow(carRepository, id, "Car");
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public List<Car> listAvailableCars(CarFilter filter) {
        CarFilter carFilter = requireNonNull(filter, "car filter");
        validateFilter(carFilter);
        return carRepository.findAll().stream()
                .filter(Car::isAvailable)
                .filter(matches(carFilter))
                .toList();
    }

    public Car updateCar(UUID id, String color, BigDecimal price, Boolean available) {
        Car car = findCar(id);
        if (color != null) {
            car.setColor(color);
        }
        if (price != null) {
            car.setPrice(price);
        }
        if (available != null) {
            car.setAvailable(available);
        }
        return carRepository.save(car);
    }

    public void deleteCar(UUID id) {
        if (!carRepository.existsById(id)) {
            findCar(id);
        }
        carRepository.deleteById(id);
    }

    public Part addPart(String sku, String name, BigDecimal price, Set<UUID> compatibleModelIds) {
        ensureModelsExist(compatibleModelIds);
        return partRepository.save(Part.create(sku, name, price, compatibleModelIds));
    }

    public Part findPart(UUID id) {
        return ServiceSupport.findOrThrow(partRepository, id, "Part");
    }

    public List<Part> listParts() {
        return partRepository.findAll();
    }

    public Part updatePart(UUID id, String name, BigDecimal price, Set<UUID> compatibleModelIds) {
        Part part = findPart(id);
        if (name != null) {
            part.setName(name);
        }
        if (price != null) {
            part.setPrice(price);
        }
        if (compatibleModelIds != null) {
            ensureModelsExist(compatibleModelIds);
            part.setCompatibleModelIds(compatibleModelIds);
        }
        return partRepository.save(part);
    }

    private void validateFilter(CarFilter filter) {
        if (filter.hasModelFilterWithoutBrand()) {
            throw new DomainValidationException("model filter can be used only when brand filter is selected");
        }
    }

    private Predicate<Car> matches(CarFilter filter) {
        return car -> matchesPrice(car, filter)
                && matchesText(car.getModel().getBrand(), filter.getBrand())
                && matchesText(car.getModel().getName(), filter.getModel())
                && matchesEnum(car.getModel().getBodyType(), filter.getBodyType())
                && matchesEnum(car.getModel().getFuelType(), filter.getFuelType())
                && matchesRange(car.getModel().getEnginePowerHp(), filter.getMinPowerHp(), filter.getMaxPowerHp())
                && matchesDecimalRange(
                        car.getModel().getEngineVolumeLiters(),
                        filter.getMinEngineVolume(),
                        filter.getMaxEngineVolume()
                )
                && matchesEnum(car.getModel().getTransmissionType(), filter.getTransmissionType())
                && matchesEnum(car.getModel().getDriveType(), filter.getDriveType())
                && matchesText(car.getColor(), filter.getColor());
    }

    private boolean matchesPrice(Car car, CarFilter filter) {
        return matchesDecimalRange(car.getPrice(), filter.getMinPrice(), filter.getMaxPrice());
    }

    private boolean matchesText(String actual, String expected) {
        return expected == null || expected.isBlank() || actual.equalsIgnoreCase(expected.trim());
    }

    private <T extends Enum<T>> boolean matchesEnum(T actual, T expected) {
        return expected == null || actual == expected;
    }

    private boolean matchesRange(int actual, Integer min, Integer max) {
        return (min == null || actual >= min) && (max == null || actual <= max);
    }

    private boolean matchesDecimalRange(BigDecimal actual, BigDecimal min, BigDecimal max) {
        return (min == null || actual.compareTo(min) >= 0) && (max == null || actual.compareTo(max) <= 0);
    }

    private void ensureModelsExist(Set<UUID> modelIds) {
        requireNonNull(modelIds, "model ids").stream()
                .filter(modelId -> !modelRepository.existsById(modelId))
                .findFirst()
                .ifPresent(modelId -> {
                    throw new EntityNotFoundException("CarModel", modelId);
                });
    }
}
