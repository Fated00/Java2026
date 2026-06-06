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
import com.autosalon.repository.CarModelRepository;
import com.autosalon.repository.CarRepository;
import com.autosalon.repository.PartRepository;
import com.autosalon.service.filter.CarFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

@Service
@Transactional
public class CarCatalogService {
    private final CarModelRepository modelRepository;
    private final CarRepository carRepository;
    private final PartRepository partRepository;

    public CarCatalogService(
            CarModelRepository modelRepository,
            CarRepository carRepository,
            PartRepository partRepository
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

    @Transactional(readOnly = true)
    public CarModel findModel(UUID id) {
        return ServiceSupport.findActiveOrThrow(modelRepository, id, "CarModel");
    }

    @Transactional(readOnly = true)
    public List<CarModel> listModels() {
        return modelRepository.findByRemovedFalse();
    }

    public Car addCar(UUID modelId, String color, BigDecimal price) {
        CarModel model = findModel(modelId);
        return carRepository.save(Car.create(model, color, price));
    }

    @Transactional(readOnly = true)
    public Car findCar(UUID id) {
        return ServiceSupport.findActiveOrThrow(carRepository, id, "Car");
    }

    @Transactional(readOnly = true)
    public List<Car> listCars() {
        return carRepository.findByRemovedFalse();
    }

    @Transactional(readOnly = true)
    public List<Car> listAvailableCars(CarFilter filter) {
        CarFilter carFilter = requireNonNull(filter, "car filter");
        validateFilter(carFilter);
        return carRepository.findByAvailableTrueAndRemovedFalse().stream()
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
        Car car = findCar(id);
        car.markRemoved();
        carRepository.save(car);
    }

    public Part addPart(String sku, String name, BigDecimal price, Set<UUID> compatibleModelIds) {
        Set<CarModel> compatibleModels = findModels(compatibleModelIds);
        return partRepository.save(Part.create(sku, name, price, compatibleModels));
    }

    @Transactional(readOnly = true)
    public Part findPart(UUID id) {
        return ServiceSupport.findActiveOrThrow(partRepository, id, "Part");
    }

    @Transactional(readOnly = true)
    public List<Part> listParts() {
        return partRepository.findByRemovedFalse();
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
            part.setCompatibleModels(findModels(compatibleModelIds));
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

    private Set<CarModel> findModels(Set<UUID> modelIds) {
        Set<CarModel> models = new HashSet<>();
        requireNonNull(modelIds, "model ids").forEach(modelId -> {
            CarModel model = modelRepository.findByIdAndRemovedFalse(modelId)
                    .orElseThrow(() -> new EntityNotFoundException("CarModel", modelId));
            models.add(model);
        });
        return models;
    }
}
