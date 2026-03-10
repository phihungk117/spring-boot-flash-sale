package com.example.spring_boot_flash_sale.exception;

// Exception này sẽ được ném ra khi số lượng đặt hàng vượt quá số lượng tồn kho của sự kiện
public class InsufficientInventoryException extends RuntimeException{
    public InsufficientInventoryException(String message) {
        super(message);
    }
    
}
