package com.autosalon;

import com.autosalon.messaging.OrderKind;
import com.autosalon.messaging.OrderSentForApprovalEvent;
import com.autosalon.repository.AssemblyOrderRepository;
import com.autosalon.repository.CarRepository;
import com.autosalon.service.AssemblyApprovalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StorageApprovalIntegrationTest extends StorageIntegrationTestBase {
    @Autowired
    private AssemblyApprovalService assemblyApprovalService;

    @Autowired
    private AssemblyOrderRepository assemblyOrderRepository;

    @Autowired
    private CarRepository carRepository;

    @Test
    @Transactional
    void approvesInStockOrderAndReservesCar() {
        UUID orderId = UUID.randomUUID();
        UUID carId = UUID.fromString("40000000-0000-0000-0000-000000000001");

        assemblyApprovalService.approve(new OrderSentForApprovalEvent(orderId, OrderKind.IN_STOCK, carId, null, Map.of()));

        boolean available = carRepository.findByIdAndRemovedFalse(carId).orElseThrow().isAvailable();
        String status = assemblyOrderRepository.findByRemovedFalse().stream()
                .filter(assemblyOrder -> assemblyOrder.getSourceOrderId().equals(orderId))
                .findFirst()
                .orElseThrow()
                .getStatus()
                .name();

        assertThat(available).isFalse();
        assertThat(status).isEqualTo("APPROVED");
    }

    @Test
    @Transactional
    void rejectsInvalidCustomConfiguration() {
        UUID orderId = UUID.randomUUID();

        assemblyApprovalService.approve(new OrderSentForApprovalEvent(
                orderId,
                OrderKind.CUSTOM,
                null,
                UUID.fromString("20000000-0000-0000-0000-000000000001"),
                Map.of()
        ));

        String status = assemblyOrderRepository.findByRemovedFalse().stream()
                .filter(assemblyOrder -> assemblyOrder.getSourceOrderId().equals(orderId))
                .findFirst()
                .orElseThrow()
                .getStatus()
                .name();

        assertThat(status).isEqualTo("REJECTED");
    }
}
