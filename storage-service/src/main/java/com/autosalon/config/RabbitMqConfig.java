package com.autosalon.config;

import com.autosalon.messaging.MessagingTopology;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Bean
    public DirectExchange autosalonExchange() {
        return new DirectExchange(MessagingTopology.EXCHANGE);
    }

    @Bean
    public Queue orderApprovalRequestQueue() {
        return new Queue(MessagingTopology.ORDER_APPROVAL_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue orderApprovalResultQueue() {
        return new Queue(MessagingTopology.ORDER_APPROVAL_RESULT_QUEUE, true);
    }

    @Bean
    public Binding orderApprovalRequestBinding(Queue orderApprovalRequestQueue, DirectExchange autosalonExchange) {
        return BindingBuilder.bind(orderApprovalRequestQueue)
                .to(autosalonExchange)
                .with(MessagingTopology.ORDER_APPROVAL_REQUEST_ROUTING_KEY);
    }

    @Bean
    public Binding orderApprovalResultBinding(Queue orderApprovalResultQueue, DirectExchange autosalonExchange) {
        return BindingBuilder.bind(orderApprovalResultQueue)
                .to(autosalonExchange)
                .with(MessagingTopology.ORDER_APPROVAL_RESULT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
