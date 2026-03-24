package com.java_springboot_3rd.user_core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> { // Kế thừa JpaRepository<Tên Entity, Kiểu dữ liệu của Khóa chính>
    boolean existsByUsername(String username); //Spring tự động sinh code SQL dựa vào tên hàm
    Optional<UserEntity> findByUsername(String username); //Để trống vì Spring Boot đã tự động cung cấp sẵn các hàm: save(), findAll(), findById(), delete()
}