package com.example.spring_boot_flash_sale.repository;

import com.example.spring_boot_flash_sale.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
    List<Order> findByStatus(String status);
    List<Order> findByStatusAndExpiresAtBefore(String status, LocalDateTime time);
}
