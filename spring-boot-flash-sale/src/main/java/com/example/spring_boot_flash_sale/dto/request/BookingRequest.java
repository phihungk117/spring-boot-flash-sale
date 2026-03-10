package com.example.spring_boot_flash_sale.dto.request;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class BookingRequest {
    private Long eventId;
    private Long userId;
    private Integer quantity;
}
