package com.example.spring_boot_flash_sale.controller;

import com.example.spring_boot_flash_sale.dto.request.BookingRequest;
import com.example.spring_boot_flash_sale.dto.response.BookingResponse;
import com.example.spring_boot_flash_sale.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Đánh dấu đây là controller xử lý các yêu cầu REST API
@RequestMapping("/api/bookings") // Định nghĩa đường dẫn cơ sở
@RequiredArgsConstructor // Tự động tạo constructor cho các trường final
public class BookingController {
    private final BookingService bookingService;

    @PostMapping // Đánh dấu hàm bên dưới chỉ nhận HTTP request POST
    public ResponseEntity<BookingResponse> createBooking(
            // @RequestBody lấy JSON từ body của request, convert thành object Java
            @RequestBody BookingRequest request,
            // @RequestHeader lấy giá trị của header "Idempotency-Key", required = false
            // nghĩa là null nếu không gửi
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        BookingResponse response = bookingService.createBooking(request, idempotencyKey);

        return ResponseEntity.ok(response);
    }
}
