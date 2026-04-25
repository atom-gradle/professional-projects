package com.qian.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // 队列名称
    public static final String TASK_QUEUE = "analysis.task_queue";
    public static final String RESULT_QUEUE = "analysis.result_queue";

    // 死信队列
    public static final String DEAD_LETTER_QUEUE = "analysis.dead_letter_queue";
    // 死信交换机
    public static final String DEAD_LETTER_EXCHANGE = "analysis.dead_letter_exchange";
    // 死信路由键
    public static final String DEAD_LETTER_ROUTING_KEY = "analysis.dead_letter";

    // 交换机
    public static final String DIRECT_EXCHANGE = "analysis.direct_exchange";
    public static final String TASK_ROUTING_KEY = "analysis.task";
    public static final String RESULT_ROUTING_KEY = "analysis.result";

    /**
     * 声明死信交换机
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    /**
     * 声明死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE)
                .build();
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    /**
     * 声明任务队列（配置死信）
     */
    @Bean
    public Queue taskQueue() {
        Map<String, Object> args = new HashMap<>();
        // 设置死信交换机
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        // 设置死信路由键
        args.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);
        // 设置消息过期时间（30秒，可选，如果需要整体超时）
        // args.put("x-message-ttl", 30000);
        // 设置队列最大长度（可选，防止消息堆积）
        // args.put("x-max-length", 1000);

        return QueueBuilder.durable(TASK_QUEUE)
                .withArguments(args)
                .build();
    }

    /**
     * 声明结果队列
     */
    @Bean
    public Queue resultQueue() {
        return QueueBuilder.durable(RESULT_QUEUE).build();
    }

    /**
     * 声明直连交换机
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE);
    }

    /**
     * 绑定任务队列到交换机
     */
    @Bean
    public Binding taskBinding() {
        return BindingBuilder
                .bind(taskQueue())
                .to(directExchange())
                .with(TASK_ROUTING_KEY);
    }

    /**
     * 绑定结果队列到交换机
     */
    @Bean
    public Binding resultBinding() {
        return BindingBuilder
                .bind(resultQueue())
                .to(directExchange())
                .with(RESULT_ROUTING_KEY);
    }

    /**
     * JSON 消息转换器
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        // 设置消息持久化
        rabbitTemplate.setMandatory(true);
        // 设置消息确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("消息发送失败: " + cause);
            }
        });
        // 设置消息返回回调
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("消息路由失败: " + returned.getMessage());
        });
        return rabbitTemplate;
    }

    /**
     * 消费者工厂配置（手动确认模式）
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 手动确认
        factory.setPrefetchCount(1); // 每次只取一条消息
        // 设置重试策略
        factory.setDefaultRequeueRejected(false); // 拒绝后不重新入队，进入死信
        return factory;
    }
}