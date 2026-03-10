package com.example.spring_boot_flash_sale.repository;

import com.example.spring_boot_flash_sale.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(String status);//tìm kiếm các sự kiện theo trạng thái (ACTIVE, INACTIVE, EXPIRED)
}
