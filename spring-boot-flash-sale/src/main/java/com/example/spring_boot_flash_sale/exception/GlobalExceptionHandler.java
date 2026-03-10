package com.example.spring_boot_flash_sale.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice //bắt tất cả các exception được ném ra trong ứng dụng và xử lý chúng ở đây
public class GlobalExceptionHandler {
    @ExceptionHandler(InsufficientInventoryException.class) // Xử lý InsufficientInventoryException
    //ResponseEntity<?> kiểu dữ liệu trả về có thể là bất kỳ đối tượng nào, ở đây chúng ta sẽ trả về một Map chứa thông tin lỗi
    public ResponseEntity<?> handleInsufficientInventory(InsufficientInventoryException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateBookingException.class) // Xử lý DuplicateBookingException
    public ResponseEntity<?> handleDuplicateBooking(DuplicateBookingException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class) // Xử lý tất cả các exception khác
    public ResponseEntity<?> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error",ex.getMessage()));
    }
}
