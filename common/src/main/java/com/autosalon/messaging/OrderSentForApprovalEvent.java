package com.autosalon.messaging;

import com.autosalon.domain.enums.ComponentType;

import java.util.Map;
import java.util.UUID;

public record OrderSentForApprovalEvent(
        UUID orderId,
        OrderKind orderKind,
        UUID carId,
        UUID modelId,
        Map<ComponentType, UUID> selectedOptionIds
) {
}
