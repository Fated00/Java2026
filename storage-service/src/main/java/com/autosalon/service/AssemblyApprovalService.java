package com.autosalon.service;

import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.model.AssemblyOrder;
import com.autosalon.domain.model.Car;
import com.autosalon.messaging.MessagingTopology;
import com.autosalon.messaging.OrderApprovalResultEvent;
import com.autosalon.messaging.OrderKind;
import com.autosalon.messaging.OrderSentForApprovalEvent;
import com.autosalon.repository.AssemblyOrderRepository;
import com.autosalon.repository.CarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssemblyApprovalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssemblyApprovalService.class);

    private final CarRepository carRepository;
    private final ConfiguratorService configuratorService;
    private final AssemblyOrderRepository assemblyOrderRepository;
    private final RabbitTemplate rabbitTemplate;

    public AssemblyApprovalService(
            CarRepository carRepository,
            ConfiguratorService configuratorService,
            AssemblyOrderRepository assemblyOrderRepository,
            RabbitTemplate rabbitTemplate
    ) {
        this.carRepository = carRepository;
        this.configuratorService = configuratorService;
        this.assemblyOrderRepository = assemblyOrderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = MessagingTopology.ORDER_APPROVAL_REQUEST_QUEUE)
    @Transactional
    public void approve(OrderSentForApprovalEvent event) {
        LOGGER.info("Received storage approval request for {} order {}", event.orderKind(), event.orderId());
        try {
            if (event.orderKind() == OrderKind.IN_STOCK) {
                approveInStockOrder(event);
            } else {
                approveCustomOrder(event);
            }
            assemblyOrderRepository.save(AssemblyOrder.approved(event.orderId(), event.orderKind()));
            publishResult(new OrderApprovalResultEvent(event.orderId(), event.orderKind(), true, null));
        } catch (RuntimeException exception) {
            String reason = exception.getMessage() == null ? "storage approval failed" : exception.getMessage();
            assemblyOrderRepository.save(AssemblyOrder.rejected(event.orderId(), event.orderKind(), reason));
            publishResult(new OrderApprovalResultEvent(event.orderId(), event.orderKind(), false, reason));
        }
    }

    private void approveInStockOrder(OrderSentForApprovalEvent event) {
        if (event.carId() == null) {
            throw new DomainValidationException("car id is required for in-stock order approval");
        }
        Car car = ServiceSupport.findActiveOrThrow(carRepository, event.carId(), "Car");
        if (!car.isAvailable()) {
            throw new DomainValidationException("car is not available for purchase");
        }
        car.setAvailable(false);
        carRepository.save(car);
    }

    private void approveCustomOrder(OrderSentForApprovalEvent event) {
        if (event.modelId() == null || event.selectedOptionIds() == null || event.selectedOptionIds().isEmpty()) {
            throw new DomainValidationException("model and selected options are required for custom order approval");
        }
        configuratorService.buildConfiguration(event.modelId(), event.selectedOptionIds());
    }

    private void publishResult(OrderApprovalResultEvent result) {
        rabbitTemplate.convertAndSend(
                MessagingTopology.EXCHANGE,
                MessagingTopology.ORDER_APPROVAL_RESULT_ROUTING_KEY,
                result
        );
        LOGGER.info("Published storage approval result for {} order {}: {}",
                result.orderKind(), result.orderId(), result.approved());
    }
}
