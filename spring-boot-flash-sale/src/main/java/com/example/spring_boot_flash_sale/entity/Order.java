package com.example.spring_boot_flash_sale.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)//Định nghĩa mối quan hệ nhiều-đến-một giữa Order và User. Mỗi đơn hàng thuộc về một người dùng.
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Builder.Default
    private Integer quantity = 1;

    private BigDecimal unitPrice;
    private BigDecimal totalAmount;

    @Builder.Default
    private String status = "PENDING"; // PENDING, PAID, CANCELLED, EXPIRED

    private LocalDateTime expiresAt;//giờ hết hạn của đơn hàng, sau thời gian này nếu chưa thanh toán sẽ tự động hủy
    private LocalDateTime paidAt;//giờ thanh toán thành công
    private LocalDateTime cancelledAt;
    private String cancelReason;//lý do hủy đơn hàng
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}