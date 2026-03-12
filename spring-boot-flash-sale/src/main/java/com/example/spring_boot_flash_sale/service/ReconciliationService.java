package com.example.spring_boot_flash_sale.service;

import com.example.spring_boot_flash_sale.entity.Event;
import com.example.spring_boot_flash_sale.repository.EventRepository;
import com.example.spring_boot_flash_sale.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationService {

    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    // Chạy mỗi 5 phút (300,000 miliseconds)
    @Scheduled(fixedDelay = 300_000)
    public void reconcileInventory() {
        log.info("Starting inventory reconciliation...");

        List<Event> activeEvents = eventRepository.findByStatus("ACTIVE");

        for (Event event : activeEvents) {
            int redisInventory = inventoryService.getCurrentInventory(event.getId());

            // Tính inventory thực tế: Tổng số vé - (Số vé đang PENDING hoặc PAID)
            int pendingOrPaidCount = orderRepository
                    .findByStatus("PENDING").size()
                    + orderRepository.findByStatus("PAID").size(); // Đây là demo gọn lại

            int dbInventory = event.getTotalQuantity() - pendingOrPaidCount;

            if (redisInventory != dbInventory) {
                log.warn("Inventory mismatch for event {}: Redis={}, DB={}. Fixing...",
                        event.getId(), redisInventory, dbInventory);

                // Lấy CSDL chuẩn DB để đè lên Redis
                inventoryService.forceSetInventory(event.getId(), dbInventory);
            }
        }

        log.info("Reconciliation completed.");
    }
}
