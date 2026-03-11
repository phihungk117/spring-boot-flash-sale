package com.example.spring_boot_flash_sale.consumer;

import com.example.spring_boot_flash_sale.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderConsumer {
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void processOrder(String orderCode) {
        log.info("Processing order: {}", orderCode);
        // Sau sẽ xử lý thêm logic ở đây
    }
}
