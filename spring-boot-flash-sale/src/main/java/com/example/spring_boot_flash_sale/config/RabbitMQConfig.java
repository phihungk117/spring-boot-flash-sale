package com.example.spring_boot_flash_sale.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Configuration // báo Spring rằng đây là một lớp cấu hình, nơi chúng ta sẽ định nghĩa các bean
               // liên quan đến RabbitMQ
public class RabbitMQConfig {
    // final để đảm bảo rằng tên queue, exchange và routing key không bị thay đổi
    // trong quá trình chạy ứng dụng
    // thùng chứa,Consumer sẽ sử dụng những hằng số này để gửi và nhận tin nhắn
    public static final String ORDER_QUEUE = "order.queue";
    // Producer (người gửi) không gửi trực tiếp vào Queue mà gửi vào Exchange.
    // Exchange sẽ quyết định tin nhắn này đi về đâu dựa trên "luật" (Routing Key).
    public static final String ORDER_EXCHANGE = "order.exchange";
    // Nó gắn vào tin nhắn để Exchange biết đường mà ném tin nhắn đó vào đúng
    // ORDER_QUEUE
    public static final String ORDER_ROUTING_KEY = "order.routing.key";
    public static final String DELAY_QUEUE = "order.delay.queue";
    public static final String DELAY_EXCHANGE = "order.delay.exchange";
    public static final String DELAY_ROUTING_KEY = "order.delay";
    public static final String DLQ_EXCHANGE = "order.dlq.exchange";

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE).build();
    }

    // DirectExchange là một loại Exchange trong RabbitMQ, nó sẽ gửi tin nhắn đến
    // Queue dựa trên Routing Key chính xác
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    // Binding là sự liên kết giữa Exchange và Queue, nó sẽ sử dụng Routing Key để
    // xác định tin nhắn nào sẽ được gửi đến Queue nào
    @Bean
    public Binding orderBinding(Queue orderQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(ORDER_ROUTING_KEY);
    }

    // MessageConverter giúp chúng ta chuyển đổi đối tượng Java thành định dạng mà
    // RabbitMQ có thể hiểu được (ví dụ: JSON) và ngược lại
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(DELAY_EXCHANGE);
    }

    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(DLQ_EXCHANGE);
    }

    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable(DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "order.cancel")
                .withArgument("x-message-ttl", 900000)
                .build();
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable("order.cancel.queue").build();
    }

    @Bean
    public Binding dlqBinding(Queue dlqQueue, DirectExchange dlqExchange) {
        return BindingBuilder.bind(dlqQueue).to(dlqExchange).with("order.cancel");
    }

}
