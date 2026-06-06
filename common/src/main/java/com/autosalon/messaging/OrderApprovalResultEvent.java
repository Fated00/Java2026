package com.autosalon.messaging;

import java.util.UUID;

public record OrderApprovalResultEvent(
        UUID orderId,
        OrderKind orderKind,
        boolean approved,
        String reason
) {
}
