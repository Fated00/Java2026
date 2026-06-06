package com.autosalon.service;

import com.autosalon.domain.model.OutboxEvent;
import com.autosalon.domain.model.OutboxEventStatus;
import com.autosalon.messaging.MessagingTopology;
import com.autosalon.messaging.OrderSentForApprovalEvent;
import com.autosalon.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            RabbitTemplate rabbitTemplate
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(
            initialDelayString = "${autosalon.outbox.initial-delay-ms:1000}",
            fixedDelayString = "${autosalon.outbox.fixed-delay-ms:1000}"
    )
    @Transactional
    public void publishNewEvents() {
        for (OutboxEvent event : outboxEventRepository.findTop20ByStatusOrderByCreatedAtAsc(OutboxEventStatus.NEW)) {
            publish(event);
        }
    }

    private void publish(OutboxEvent event) {
        try {
            OrderSentForApprovalEvent payload = objectMapper.readValue(event.getPayload(), OrderSentForApprovalEvent.class);
            rabbitTemplate.convertAndSend(
                    MessagingTopology.EXCHANGE,
                    MessagingTopology.ORDER_APPROVAL_REQUEST_ROUTING_KEY,
                    payload
            );
            event.markPublished();
            outboxEventRepository.save(event);
            LOGGER.info("Published outbox event {} for order {}", event.getId(), event.getAggregateId());
        } catch (JsonProcessingException | AmqpException exception) {
            LOGGER.warn("Outbox event {} was not published: {}", event.getId(), exception.getMessage());
        }
    }
}
