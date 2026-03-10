package com.example.spring_boot_flash_sale.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public class BookingResponse {
    private String orderCode;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String message;
}
