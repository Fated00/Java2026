package com.autosalon.service;

import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.enums.CustomOrderStatus;
import com.autosalon.domain.enums.InStockOrderStatus;
import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.model.CustomCarOrder;
import com.autosalon.domain.model.InStockCarOrder;
import com.autosalon.domain.model.OutboxEvent;
import com.autosalon.domain.model.User;
import com.autosalon.messaging.OrderKind;
import com.autosalon.messaging.OrderSentForApprovalEvent;
import com.autosalon.repository.CustomCarOrderRepository;
import com.autosalon.repository.InStockCarOrderRepository;
import com.autosalon.repository.OutboxEventRepository;
import com.autosalon.repository.UserRepository;
import com.autosalon.security.CurrentUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

@Service
@Transactional
public class OrderService {
    private final UserRepository userRepository;
    private final InStockCarOrderRepository inStockOrderRepository;
    private final CustomCarOrderRepository customOrderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    public OrderService(
            UserRepository userRepository,
            InStockCarOrderRepository inStockOrderRepository,
            CustomCarOrderRepository customOrderRepository,
            OutboxEventRepository outboxEventRepository,
            CurrentUserService currentUserService,
            ObjectMapper objectMapper
    ) {
        this.userRepository = requireNonNull(userRepository, "user repository");
        this.inStockOrderRepository = requireNonNull(inStockOrderRepository, "in-stock order repository");
        this.customOrderRepository = requireNonNull(customOrderRepository, "custom order repository");
        this.outboxEventRepository = requireNonNull(outboxEventRepository, "outbox event repository");
        this.currentUserService = requireNonNull(currentUserService, "current user service");
        this.objectMapper = requireNonNull(objectMapper, "object mapper");
    }

    public InStockCarOrder createInStockOrder(UUID clientId, UUID carId) {
        User client = findUserWithRole(clientId, Role.CLIENT, "client");
        User manager = assignManager();
        return inStockOrderRepository.save(InStockCarOrder.create(client, manager, carId));
    }

    public CustomCarOrder createCustomOrder(UUID clientId, UUID modelId, Map<ComponentType, UUID> selectedOptionIds) {
        User client = findUserWithRole(clientId, Role.CLIENT, "client");
        User manager = assignManager();
        return customOrderRepository.save(CustomCarOrder.create(client, manager, modelId, selectedOptionIds));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@orderSecurity.canReadInStockOrder(#id)")
    public InStockCarOrder findInStockOrder(UUID id) {
        return ServiceSupport.findActiveOrThrow(inStockOrderRepository, id, "InStockCarOrder");
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@orderSecurity.canReadCustomOrder(#id)")
    public CustomCarOrder findCustomOrder(UUID id) {
        return ServiceSupport.findActiveOrThrow(customOrderRepository, id, "CustomCarOrder");
    }

    @Transactional(readOnly = true)
    public List<InStockCarOrder> listInStockOrders() {
        List<InStockCarOrder> orders = inStockOrderRepository.findByRemovedFalse();
        if (currentUserService.hasRole("USER") && !currentUserService.hasAnyRole("MANAGER", "ADMIN")) {
            UUID currentUserId = currentUserService.currentAppUserId();
            return orders.stream()
                    .filter(order -> order.getClient().getId().equals(currentUserId))
                    .toList();
        }
        return orders;
    }

    @Transactional(readOnly = true)
    public List<CustomCarOrder> listCustomOrders() {
        List<CustomCarOrder> orders = customOrderRepository.findByRemovedFalse();
        if (currentUserService.hasRole("USER") && !currentUserService.hasAnyRole("MANAGER", "ADMIN")) {
            UUID currentUserId = currentUserService.currentAppUserId();
            return orders.stream()
                    .filter(order -> order.getClient().getId().equals(currentUserId))
                    .toList();
        }
        return orders;
    }

    @PreAuthorize("@orderSecurity.canManageInStockOrder(#orderId)")
    public InStockCarOrder cancelInStockOrder(UUID orderId) {
        InStockCarOrder order = ServiceSupport.findActiveOrThrow(inStockOrderRepository, orderId, "InStockCarOrder");
        order.setStatus(InStockOrderStatus.CANCELLED);
        return inStockOrderRepository.save(order);
    }

    @PreAuthorize("@orderSecurity.canManageCustomOrder(#orderId)")
    public CustomCarOrder cancelCustomOrder(UUID orderId) {
        CustomCarOrder order = ServiceSupport.findActiveOrThrow(customOrderRepository, orderId, "CustomCarOrder");
        order.setStatus(CustomOrderStatus.CANCELLED);
        return customOrderRepository.save(order);
    }

    @PreAuthorize("@orderSecurity.canManageInStockOrder(#orderId)")
    public InStockCarOrder payInStockOrder(UUID orderId) {
        InStockCarOrder order = ServiceSupport.findActiveOrThrow(inStockOrderRepository, orderId, "InStockCarOrder");
        ensureCanBePaid(order.getStatus());
        order.setStatus(InStockOrderStatus.PAID);
        InStockCarOrder saved = inStockOrderRepository.save(order);
        appendOutboxEvent(new OrderSentForApprovalEvent(order.getId(), OrderKind.IN_STOCK, order.getCarId(), null, Map.of()));
        return saved;
    }

    @PreAuthorize("@orderSecurity.canManageCustomOrder(#orderId)")
    public CustomCarOrder payCustomOrder(UUID orderId) {
        CustomCarOrder order = ServiceSupport.findActiveOrThrow(customOrderRepository, orderId, "CustomCarOrder");
        ensureCanBePaid(order.getStatus());
        order.setStatus(CustomOrderStatus.PAID);
        CustomCarOrder saved = customOrderRepository.save(order);
        appendOutboxEvent(new OrderSentForApprovalEvent(
                order.getId(),
                OrderKind.CUSTOM,
                null,
                order.getModelId(),
                order.getSelectedOptionIds()
        ));
        return saved;
    }

    public void applyApprovalResult(UUID orderId, OrderKind orderKind, boolean approved) {
        if (orderKind == OrderKind.IN_STOCK) {
            InStockCarOrder order = ServiceSupport.findActiveOrThrow(inStockOrderRepository, orderId, "InStockCarOrder");
            order.setStatus(approved ? InStockOrderStatus.CAR_READY_FOR_PICKUP : InStockOrderStatus.CANCELLED);
            inStockOrderRepository.save(order);
            return;
        }
        CustomCarOrder order = ServiceSupport.findActiveOrThrow(customOrderRepository, orderId, "CustomCarOrder");
        order.setStatus(approved ? CustomOrderStatus.WAITING_FOR_DELIVERY : CustomOrderStatus.CANCELLED);
        customOrderRepository.save(order);
    }

    private void appendOutboxEvent(OrderSentForApprovalEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxEventRepository.save(OutboxEvent.create(event.orderId(), event.getClass().getName(), payload));
        } catch (JsonProcessingException exception) {
            throw new DomainValidationException("failed to serialize outbox event");
        }
    }

    private void ensureCanBePaid(Enum<?> status) {
        if (status.name().equals("CANCELLED") || status.name().equals("COMPLETED") || status.name().equals("PAID")) {
            throw new DomainValidationException("order cannot be paid in status " + status);
        }
    }

    private User findUserWithRole(UUID userId, Role role, String fieldName) {
        User user = ServiceSupport.findActiveOrThrow(userRepository, userId, "User");
        return ServiceSupport.requireRole(user, role, fieldName);
    }

    private User assignManager() {
        return userRepository.findByRoleAndRemovedFalse(Role.DEALERSHIP_MANAGER).stream()
                .filter(user -> user.getRole() == Role.DEALERSHIP_MANAGER)
                .min(Comparator.comparing(user -> user.getId().toString()))
                .orElseThrow(() -> new DomainValidationException("no dealership manager registered"));
    }
}
