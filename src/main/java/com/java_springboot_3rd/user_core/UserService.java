package com.java_springboot_3rd.user_core;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    // 1. LẤY 1 USER (DÙNG CACHE)
    @Cacheable(value = "users", key = "#id")
    public UserEntity getUserById(Integer id) {
        System.out.println("Không tìm thấy trong Redis! Tìm dưới DB id: " + id);
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user có id: " + id));
    }

    // 2. LẤY TOÀN BỘ USER (Hàm bị thất lạc đã trở về)
    public List<UserEntity> getAllUsers() {
        System.out.println("Đang lấy toàn bộ danh sách User...");
        return userRepository.findAll();
    }

    // 3. CẬP NHẬT USER (ÉP CẬP NHẬT CACHE)
    @CachePut(value = "users", key = "#id")
    public UserEntity updateUser(Integer id, UserEntity updatedInfo) {
        System.out.println("Đang xử lý cập nhật cho ID: " + id);

        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user có ID: " + id));

        // Băm pass nếu có pass mới
        if (updatedInfo.getPassword() != null && !updatedInfo.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedInfo.getPassword()));
        }

        if (updatedInfo.getEmail() != null) {
            existingUser.setEmail(updatedInfo.getEmail());
        }
        return userRepository.save(existingUser);
    }

    // 4. XÓA USER (XÓA SẠCH CACHE CỦA USER ĐÓ)
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Integer id) {
        System.out.println("Đang xóa ID: " + id);

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Không thể xóa! Không tìm thấy user ID: " + id);
        }

        userRepository.deleteById(id);
    }

    // 5. ĐĂNG KÝ
    public UserEntity registerUser(UserEntity newUser) {
        System.out.println("Đang đăng ký cho user: " + newUser.getUsername());

        if (newUser.getUsername() == null || newUser.getUsername().trim().length() < 3) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống và phải từ 3 ký tự trở lên!");
        }

        if (newUser.getPassword() == null || newUser.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu quá ngắn, ít nhất 6 ký tự!");
        }

        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + newUser.getUsername() + "' đã có người sử dụng! Vui lòng chọn tên khác.");
        }

        String rawPassword = newUser.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        newUser.setPassword(encodedPassword);

        UserEntity savedUser = userRepository.save(newUser);
        System.out.println(" [Database] Đã lưu THẬT user " + savedUser.getUsername() + " với ID là: " + savedUser.getId());

        return savedUser;
    }

}