package com.autosalon.api;

import com.autosalon.api.dto.Dtos.CarDto;
import com.autosalon.api.dto.Dtos.CarModelDto;
import com.autosalon.api.dto.Dtos.CreateCarModelRequest;
import com.autosalon.api.dto.Dtos.CreateCarRequest;
import com.autosalon.api.dto.Dtos.CreatePartRequest;
import com.autosalon.api.dto.Dtos.PartDto;
import com.autosalon.api.dto.Dtos.UpdateCarRequest;
import com.autosalon.domain.enums.BodyType;
import com.autosalon.domain.enums.DriveType;
import com.autosalon.domain.enums.FuelType;
import com.autosalon.domain.enums.TransmissionType;
import com.autosalon.service.CarCatalogService;
import com.autosalon.service.filter.CarFilter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalog")
public final class CatalogController {
    private final CarCatalogService catalogService;
    private final ApiMapper mapper;

    public CatalogController(CarCatalogService catalogService, ApiMapper mapper) {
        this.catalogService = catalogService;
        this.mapper = mapper;
    }

    @GetMapping("/models")
    public List<CarModelDto> listModels() {
        return catalogService.listModels().stream().map(mapper::toDto).toList();
    }

    @GetMapping("/models/{id}")
    public CarModelDto getModel(@PathVariable UUID id) {
        return mapper.toDto(catalogService.findModel(id));
    }

    @PostMapping("/models")
    @ResponseStatus(HttpStatus.CREATED)
    public CarModelDto createModel(@Valid @RequestBody CreateCarModelRequest request) {
        return mapper.toDto(catalogService.createModel(
                request.brand(),
                request.name(),
                request.bodyType(),
                request.fuelType(),
                request.enginePowerHp(),
                request.engineVolumeLiters(),
                request.transmissionType(),
                request.driveType(),
                request.basePrice(),
                request.requiredComponentTypes()
        ));
    }

    @GetMapping("/cars")
    public List<CarDto> listCars() {
        return catalogService.listCars().stream().map(mapper::toDto).toList();
    }

    @GetMapping("/cars/available")
    public List<CarDto> listAvailableCars(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) BodyType bodyType,
            @RequestParam(required = false) FuelType fuelType,
            @RequestParam(required = false) Integer minPowerHp,
            @RequestParam(required = false) Integer maxPowerHp,
            @RequestParam(required = false) BigDecimal minEngineVolume,
            @RequestParam(required = false) BigDecimal maxEngineVolume,
            @RequestParam(required = false) TransmissionType transmissionType,
            @RequestParam(required = false) DriveType driveType,
            @RequestParam(required = false) String color
    ) {
        CarFilter.Builder builder = CarFilter.builder()
                .priceFrom(minPrice)
                .priceTo(maxPrice)
                .brand(brand)
                .model(model)
                .bodyType(bodyType)
                .fuelType(fuelType)
                .engineVolumeFrom(minEngineVolume)
                .engineVolumeTo(maxEngineVolume)
                .transmissionType(transmissionType)
                .driveType(driveType)
                .color(color);
        if (minPowerHp != null) {
            builder.powerFrom(minPowerHp);
        }
        if (maxPowerHp != null) {
            builder.powerTo(maxPowerHp);
        }
        return catalogService.listAvailableCars(builder.build()).stream().map(mapper::toDto).toList();
    }

    @GetMapping("/cars/{id}")
    public CarDto getCar(@PathVariable UUID id) {
        return mapper.toDto(catalogService.findCar(id));
    }

    @PostMapping("/cars")
    @ResponseStatus(HttpStatus.CREATED)
    public CarDto createCar(@Valid @RequestBody CreateCarRequest request) {
        return mapper.toDto(catalogService.addCar(request.modelId(), request.color(), request.price()));
    }

    @PatchMapping("/cars/{id}")
    public CarDto updateCar(@PathVariable UUID id, @Valid @RequestBody UpdateCarRequest request) {
        return mapper.toDto(catalogService.updateCar(id, request.color(), request.price(), request.available()));
    }

    @DeleteMapping("/cars/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCar(@PathVariable UUID id) {
        catalogService.deleteCar(id);
    }

    @GetMapping("/parts")
    public List<PartDto> listParts() {
        return catalogService.listParts().stream().map(mapper::toDto).toList();
    }

    @PostMapping("/parts")
    @ResponseStatus(HttpStatus.CREATED)
    public PartDto createPart(@Valid @RequestBody CreatePartRequest request) {
        return mapper.toDto(catalogService.addPart(
                request.sku(),
                request.name(),
                request.price(),
                request.compatibleModelIds()
        ));
    }
}
