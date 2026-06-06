package com.autosalon.api;

import com.autosalon.api.dto.Dtos.CarDto;
import com.autosalon.service.StorageGrpcClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cars")
public class CarInventoryController {
    private final StorageGrpcClient storageGrpcClient;

    public CarInventoryController(StorageGrpcClient storageGrpcClient) {
        this.storageGrpcClient = storageGrpcClient;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public List<CarDto> listAvailableCars() {
        return storageGrpcClient.listAvailableCars();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public CarDto getAvailableCar(@PathVariable UUID id) {
        return storageGrpcClient.getAvailableCar(id);
    }
}
