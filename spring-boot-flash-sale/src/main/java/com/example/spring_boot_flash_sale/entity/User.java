package com.example.spring_boot_flash_sale.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity //annotate của JPA để đánh dấu đây là một thực thể (entity) trong cơ sở dữ liệu
@Table(name = "users")
//Tự động tạo ngầm các phương thức get (lấy giá trị) và set (gán giá trị) cho tất cả các thuộc tính bên trong lớp.
@Getter @Setter
//Tự động tạo một constructor không tham số (no-args constructor) và một constructor có tất cả các tham số (all-args constructor) cho lớp.
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class User {
    @Id //đánh dấu đây là thuộc tính khóa chính (primary key) của thực thể.
    @GeneratedValue(strategy = GenerationType.IDENTITY) //tự động sinh giá trị cho trường
    private Long id;

    @Column(unique = true,nullable = false) //đánh dấu đây là một cột trong bảng cơ sở dữ liệu, với các thuộc tính unique (giá trị phải duy nhất) và nullable (không được phép null).
    private String username;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
