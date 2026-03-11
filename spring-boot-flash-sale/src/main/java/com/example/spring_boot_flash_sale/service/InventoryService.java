package com.example.spring_boot_flash_sale.service;

import com.example.spring_boot_flash_sale.entity.Event;
import com.example.spring_boot_flash_sale.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor // Lệnh của Lombok, tự động tạo một hàm khởi tạo (constructor) cho các biến
                         // final ở dưới
@Slf4j
public class InventoryService {
    //StringRedisTemplate là công cụ để Java nói chuyện với Redis
    private final StringRedisTemplate redisTemplate;
    private final EventRepository eventRepository;
    private static final String INVENTORY_KEY = "inventory:event:";
    private static final String BOOKED_USERS_KEY = "booked:event:";

    // Hàm gọi để load dữ liệu vé vào Redis trước khi Flash Sale diễn ra
    public void loadInventoryToCache(Long eventId) {
        // tạo 1 biến event kiẻu Event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        String key = INVENTORY_KEY + eventId;
        redisTemplate.opsForValue().set(key,
                String.valueOf(event.getAvailableQuantity()),
                Duration.ofHours(24)); // Đặt thời gian tồn tại của cache là 1 giờ
        log.info("Loaded inventory for event {}: {} tickets", eventId, event.getAvailableQuantity());
    }

    // Hàm kiểm tra và trừ inventory khi có booking mới
    public int deductInventory(Long eventId, Long userId, int quantity) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/inventory_deduct.lua")));
        script.setResultType(Long.class);
        List<String> keys = Arrays.asList(
                INVENTORY_KEY + eventId,
                BOOKED_USERS_KEY + eventId);
        Long result = redisTemplate.execute(script, keys,
                String.valueOf(quantity),
                String.valueOf(userId));
        return result != null ? result.intValue() : 0;
    }

    // Trả lại vé vào Event và gỡ User khỏi danh sách đã khoá nếu đơn huỷ (Sau 15
    // phút chưa thanh toán)
    public void rollbackInventory(Long eventId, Long userId, int quantity) {
        String inventoryKey = INVENTORY_KEY + eventId;
        String bookedKey = BOOKED_USERS_KEY + eventId;
        //opsForValue() là phương thức của RedisTemplate trong Spring Data Redis dùng để thao tác với Redis kiểu dữ liệu String (Value)
        redisTemplate.opsForValue().increment(inventoryKey, quantity);
        redisTemplate.opsForSet().remove(bookedKey, String.valueOf(userId));
        log.info("Rolled back {} tickets for event {}, user {}", quantity, eventId, userId);
    }

    public int getCurrentInventory(Long eventId) {
        String val = redisTemplate.opsForValue().get(INVENTORY_KEY + eventId);
        return val == null ? 0 : Integer.parseInt(val);
    }

    public void forceSetInventory(Long eventId, int quantity) {
        redisTemplate.opsForValue().set(INVENTORY_KEY + eventId, String.valueOf(quantity));
    }
}
