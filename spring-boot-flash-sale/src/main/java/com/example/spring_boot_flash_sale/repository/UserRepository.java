package com.example.spring_boot_flash_sale.repository;

import com.example.spring_boot_flash_sale.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;


public interface UserRepository extends JpaRepository<User,Long> {
    // thay vì trả về List<User>, chúng ta trả về Optional<User> để xử lý trường hợp không tìm thấy người dùng
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);//kiểm tra xem đã tồn tại người dùng với username này chưa
    boolean existsByEmail(String email);//kiểm tra xem đã tồn tại người dùng với email
}
