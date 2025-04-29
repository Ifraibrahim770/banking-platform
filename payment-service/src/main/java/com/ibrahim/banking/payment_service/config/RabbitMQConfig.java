package com.ibrahim.banking.payment_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String PAYMENT_DEPOSIT_QUEUE = "payment.deposit.queue";
    public static final String PAYMENT_WITHDRAWAL_QUEUE = "payment.withdrawal.queue";
    public static final String PAYMENT_TRANSFER_QUEUE = "payment.transfer.queue";
    
    // Dead Letter Queues
    public static final String PAYMENT_DEPOSIT_DLQ = "payment.deposit.dlq";
    public static final String PAYMENT_WITHDRAWAL_DLQ = "payment.withdrawal.dlq";
    public static final String PAYMENT_TRANSFER_DLQ = "payment.transfer.dlq";
    
    // Notification Queues
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_DLQ = "notification.dlq";
    
    // Exchange names
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_DLX = "payment.dlx";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_DLX = "notification.dlx";
    
    // Routing keys
    public static final String DEPOSIT_ROUTING_KEY = "payment.deposit";
    public static final String WITHDRAWAL_ROUTING_KEY = "payment.withdrawal";
    public static final String TRANSFER_ROUTING_KEY = "payment.transfer";
    public static final String NOTIFICATION_ROUTING_KEY = "notification";

    // Message converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Configure RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // Payment Exchange
    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE);
    }

    // Dead Letter Exchange for Payment
    @Bean
    public DirectExchange paymentDLX() {
        return new DirectExchange(PAYMENT_DLX);
    }

    // Notification Exchange
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    // Dead Letter Exchange for Notification
    @Bean
    public DirectExchange notificationDLX() {
        return new DirectExchange(NOTIFICATION_DLX);
    }

    // Payment Queues
    @Bean
    public Queue depositQueue() {
        return QueueBuilder.durable(PAYMENT_DEPOSIT_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
                .withArgument("x-dead-letter-routing-key", DEPOSIT_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue withdrawalQueue() {
        return QueueBuilder.durable(PAYMENT_WITHDRAWAL_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
                .withArgument("x-dead-letter-routing-key", WITHDRAWAL_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue transferQueue() {
        return QueueBuilder.durable(PAYMENT_TRANSFER_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
                .withArgument("x-dead-letter-routing-key", TRANSFER_ROUTING_KEY)
                .build();
    }

    // Dead Letter Queues
    @Bean
    public Queue depositDLQ() {
        return QueueBuilder.durable(PAYMENT_DEPOSIT_DLQ).build();
    }

    @Bean
    public Queue withdrawalDLQ() {
        return QueueBuilder.durable(PAYMENT_WITHDRAWAL_DLQ).build();
    }

    @Bean
    public Queue transferDLQ() {
        return QueueBuilder.durable(PAYMENT_TRANSFER_DLQ).build();
    }

    // Notification Queues
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue notificationDLQ() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    // Bindings for Payment Exchange
    @Bean
    public Binding depositBinding() {
        return BindingBuilder.bind(depositQueue()).to(paymentExchange()).with(DEPOSIT_ROUTING_KEY);
    }

    @Bean
    public Binding withdrawalBinding() {
        return BindingBuilder.bind(withdrawalQueue()).to(paymentExchange()).with(WITHDRAWAL_ROUTING_KEY);
    }

    @Bean
    public Binding transferBinding() {
        return BindingBuilder.bind(transferQueue()).to(paymentExchange()).with(TRANSFER_ROUTING_KEY);
    }

    // Bindings for Payment DLX
    @Bean
    public Binding depositDLQBinding() {
        return BindingBuilder.bind(depositDLQ()).to(paymentDLX()).with(DEPOSIT_ROUTING_KEY);
    }

    @Bean
    public Binding withdrawalDLQBinding() {
        return BindingBuilder.bind(withdrawalDLQ()).to(paymentDLX()).with(WITHDRAWAL_ROUTING_KEY);
    }

    @Bean
    public Binding transferDLQBinding() {
        return BindingBuilder.bind(transferDLQ()).to(paymentDLX()).with(TRANSFER_ROUTING_KEY);
    }

    // Bindings for Notification Exchange
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(notificationExchange()).with(NOTIFICATION_ROUTING_KEY);
    }

    // Binding for Notification DLX
    @Bean
    public Binding notificationDLQBinding() {
        return BindingBuilder.bind(notificationDLQ()).to(notificationDLX()).with(NOTIFICATION_ROUTING_KEY);
    }
} 