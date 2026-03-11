package com.example.spring_boot_flash_sale.service;

import com.example.spring_boot_flash_sale.config.RabbitMQConfig;
import com.example.spring_boot_flash_sale.dto.request.BookingRequest;
import com.example.spring_boot_flash_sale.dto.response.BookingResponse;
import com.example.spring_boot_flash_sale.entity.*;
import com.example.spring_boot_flash_sale.exception.*;
import com.example.spring_boot_flash_sale.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor // Lombok tự tạo constructor chứa các biến private final
@Slf4j
public class BookingService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional // Biến toàn bộ hàm bên dưới thành 1 block Transaction, thành công hết hoặc
                   // rollback (lùi lại) hết
    public BookingResponse createBooking(BookingRequest request, String idempotencyKey) {

        // 1. Kiểm tra idempotency key
        if (idempotencyKey != null) {
            // .ifPresent(): Nếu tìm thấy đơn hàng tồn tại với idempotencyKey này -> ném
            // Exception
            orderRepository.findByIdempotencyKey(idempotencyKey).ifPresent(existing -> {
                throw new DuplicateBookingException("Duplicate request: " + idempotencyKey);
            }); // Dấu ngoặc đã được sửa cho hợp lệ
        }

        // 2. Lấy thông tin Event (Sự kiện giảm giá)
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // 3. Kiểm tra inventory (số lượng hàng còn lại)
        if (event.getAvailableQuantity() < request.getQuantity()) {
            throw new InsufficientInventoryException("Not enough inventory");
        }

        // 4. Trừ inventory
        event.setAvailableQuantity(event.getAvailableQuantity() - request.getQuantity());
        eventRepository.save(event);

        // 5. Lấy user và tạo order
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal totalAmount = event.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .orderCode(UUID.randomUUID().toString())
                .user(user)
                .event(event)
                .quantity(request.getQuantity())
                .unitPrice(event.getPrice())
                .totalAmount(totalAmount)
                .status("PENDING")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .idempotencyKey(idempotencyKey)
                .build();

        orderRepository.save(order);

        // 6. Đẩy message vào RabbitMQ để xử lý bất đồng bộ sau
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                order.getOrderCode());

        log.info("Booking created: {}", order.getOrderCode());

        // 7. Trả về response
        return BookingResponse.builder()
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .expiresAt(order.getExpiresAt())
                .message("Booking successful! Please pay within 15 minutes.")
                .build();
    }
}
