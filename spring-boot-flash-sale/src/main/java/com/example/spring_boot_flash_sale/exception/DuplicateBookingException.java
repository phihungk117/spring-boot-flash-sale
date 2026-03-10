package com.example.spring_boot_flash_sale.exception;

// Exception này sẽ được ném ra khi người dùng cố gắng đặt hàng nhiều lần cho cùng một sự kiện
public class DuplicateBookingException extends RuntimeException {
    public DuplicateBookingException(String message) {
        super(message);
    }
}
