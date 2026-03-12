package com.example.spring_boot_flash_sale.consumer;

import com.example.spring_boot_flash_sale.entity.Order;
import com.example.spring_boot_flash_sale.repository.OrderRepository;
import com.example.spring_boot_flash_sale.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DelayedOrderConsumer {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    // Lắng nghe trên Queue huỷ (nơi messages rơi xuống sau 15p delay)
    @RabbitListener(queues = "order.cancel.queue")
    public void processCancelOrder(String orderCode) {
        log.info("Auto-cancel check for order: {}", orderCode);

        orderRepository.findByOrderCode(orderCode).ifPresent(order -> {
            // Nếu sau 15p trạng thái vẫn là PENDING -> Huỷ đơn
            if ("PENDING".equals(order.getStatus())) {
                order.setStatus("EXPIRED");
                orderRepository.save(order);

                // Nhả vé lại vào kho Redis và xoá User khóa để họ mua lại đc
                inventoryService.rollbackInventory(
                        order.getEvent().getId(),
                        order.getUser().getId(),
                        order.getQuantity());

                log.info("Order {} expired and rolled back", orderCode);
            } else {
                // QUAN TRỌNG: Nếu đã thanh toán (PAID) thì bỏ qua
                log.info("Order {} already processed (status: {}), skip auto-cancel", orderCode, order.getStatus());
            }
        });
    }
}
