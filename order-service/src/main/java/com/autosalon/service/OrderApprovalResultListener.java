package com.autosalon.service;

import com.autosalon.messaging.MessagingTopology;
import com.autosalon.messaging.OrderApprovalResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderApprovalResultListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderApprovalResultListener.class);

    private final OrderService orderService;

    public OrderApprovalResultListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = MessagingTopology.ORDER_APPROVAL_RESULT_QUEUE)
    @Transactional
    public void handle(OrderApprovalResultEvent event) {
        LOGGER.info("Received approval result for {} order {}: {}",
                event.orderKind(), event.orderId(), event.approved());
        orderService.applyApprovalResult(event.orderId(), event.orderKind(), event.approved());
    }
}
