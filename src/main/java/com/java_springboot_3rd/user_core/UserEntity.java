package com.java_springboot_3rd.user_core;

import jakarta.persistence.*;
import lombok.Data; // Thu gọn code
import java.io.Serializable;

@Data // Tự động sinh ngầm toàn bộ Getter, Setter, toString...
@Entity
@Table(name = "users")
public class UserEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    @Column(nullable = false)
    private String role = "ROLE_USER"; // Spring Security yêu cầu chữ ROLE_ đứng trước

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}