package com.java_springboot_3rd.user_core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController // Đánh dấu chuyên xử lý request
@RequestMapping("/api") // Tất cả các API sẽ bắt đầu bằng /api
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 1. API ĐĂNG KÝ
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserEntity newUser) {
        System.out.println("🌐 [Controller] Nhận được request tạo user mới!");
        try {
            UserEntity savedUser = userService.registerUser(newUser);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("❌ Có lỗi hệ thống xảy ra!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 2. API ĐĂNG NHẬP (JWT & REFRESH TOKEN)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserEntity loginRequest) {
        System.out.println("🌐 [Controller] Nhận được request đăng nhập!");
        Optional<UserEntity> userOpt = userRepository.findByUsername(loginRequest.getUsername());

        if (userOpt.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPassword())) {
            String username = userOpt.get().getUsername();
            String role = userOpt.get().getRole();

            // 1. Tạo cặp token
            String accessToken = jwtUtil.generateAccessToken(username, role);
            String refreshToken = jwtUtil.generateRefreshToken(username, role);

            // 2. Để Refresh Token vào Redis (Sống 7 ngày)
            redisTemplate.opsForValue().set("RT:" + username, refreshToken, 7, TimeUnit.DAYS);

            // 3. Đóng gói trả về
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);

            System.out.println("✅ Đăng nhập thành công! Đã cấp phát Token!");
            return ResponseEntity.ok(tokens);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tên đăng nhập hoặc mật khẩu!");
    }

    // 3. API REFRESH TOKEN
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        try {
            // 1. Lấy tên User từ Refresh Token
            String username = jwtUtil.extractUsername(refreshToken);

            // 2. kiểm tra xem có tồn tại thông tin này trong Redis không
            String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + username);

            // 3. So sánh thông tin vừa nhập và trong Redis
            if (savedRefreshToken != null && savedRefreshToken.equals(refreshToken)) {
                String role = jwtUtil.extractRole(refreshToken);
                // Hợp lệ! Cấp Access Token mới (15 phút)
                String newAccessToken = jwtUtil.generateAccessToken(username, role);

                Map<String, String> response = new HashMap<>();
                response.put("accessToken", newAccessToken);
                System.out.println(" [Auth] Đã cấp phát lại Access Token mới cho: " + username);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token không hợp lệ hoặc đã bị thu hồi!");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token đã hết hạn hoặc bị lỗi!");
        }
    }

    // 4. API LẤY THÔNG TIN 1 USER (SỬ DỤNG CACHING REDIS)
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Integer id) {
        System.out.println("🌐 [Controller] Nhận request lấy thông tin user ID: " + id);
        try {
            UserEntity user = userService.getUserById(id);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // 5. API CẬP NHẬT USER (PUT)
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody UserEntity updatedInfo) {
        System.out.println("🌐 [Controller] Nhận request CẬP NHẬT user ID: " + id);
        try {
            UserEntity user = userService.updateUser(id, updatedInfo);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 6. API XÓA USER (DELETE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        System.out.println("🌐 [Controller] Nhận request XÓA user ID: " + id);
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(" Đã xóa user " + id + " ra khỏi DB!", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // 7. API LẤY TẤT CẢ USER
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        System.out.println("🌐 [Controller] Có người đang truy cập danh sách toàn bộ User!");
        try {
            return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi lấy danh sách!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 8. API TEST RABBITMQ
    @PostMapping("/test-queue")
    public ResponseEntity<String> testRabbitMQ(@RequestBody String message) {
        System.out.println("🌐 [Controller] Nhận được yêu cầu: " + message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
        System.out.println("🚀 [Controller] Đưa vào Queue! Trả kết quả ngay lập tức!");
        return new ResponseEntity<>("Đã tiếp nhận: " + message + ". đang xử lý!", HttpStatus.OK);
    }

    // 9. API TEST ĐẶT HÀNG (truyền OBJECT VÀO RABBITMQ)
    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@RequestBody OrderEvent newOrder) {
        System.out.println("🌐 [Controller] Lễ tân vừa nhận được đơn hàng mã: " + newOrder.getOrderId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, newOrder);
        System.out.println("[Controller] Đã đưa vào Queue! Phản hồi ngay!");
        return new ResponseEntity<>("Đặt hàng thành công! Phản hồi sẽ được gửi tới email: " + newOrder.getUserEmail(), HttpStatus.OK);
    }
}

/**
 @RestController // Đánh dấu đây là nơi tiếp nhận Request từ trình duyệt Web (Chrome, Edge...)
 @RequestMapping("/api") // Đường dẫn gốc cho tất cả các API trong file này
 public class UserController {

 @Autowired // Kêu Spring tiêm tầng Database vào đây để lấy dữ liệu
 private UserRepository userRepository;

 @Autowired // Kêu Spring tiêm thêm UserService vào để dùng
 private UserService userService;

 // đuôi "/users", hàm sẽ chạy!
 @GetMapping("/users")
 public List<UserEntity> getAllUsers() {
 System.out.println("🌐 Có người đang truy cập từ Trình duyệt kìa!");

 // Dùng hàm findAll() có sẵn của Spring Data JPA để lôi TOÀN BỘ data từ MySQL lên!
 return userRepository.findAll();
 }

 // API MỚI: Đăng ký User (POST)
 @PostMapping("/users")
 public UserEntity createUser(@RequestBody UserEntity userRequest) {
 // @RequestBody sẽ tự động "dịch" cục JSON của Client thành Object UserEntity trong Java
 System.out.println("🌐 [Controller] Nhận được request tạo user mới!");

 // Giao việc cho Service xử lý
 return userService.registerUser(userRequest);
 }

 // API MỚI: Cập nhật User (PUT)
 // Đường dẫn có thêm {id} để biết cần sửa ai (Ví dụ: /api/users/5)
 @PutMapping("/users/{id}")
 public UserEntity updateUser(@PathVariable Integer id, @RequestBody UserEntity updatedInfo) {
 // @PathVariable giúp "móc" con số 5 từ trên đường dẫn URL nhét vào biến 'id'
 System.out.println("🌐 [Controller] Nhận request CẬP NHẬT user ID: " + id);
 return userService.updateUser(id, updatedInfo);
 }

 // API MỚI: Xóa User (DELETE)
 @DeleteMapping("/users/{id}")
 public String deleteUser(@PathVariable Integer id) {
 System.out.println("🌐 [Controller] Nhận request XÓA user ID: " + id);
 userService.deleteUser(id);

 return "Đã tiễn user có ID " + id + " ra khỏi DB! 🪦";
 }
 // API ĐĂNG NHẬP
 @PostMapping("/login")
 public String login(@RequestBody UserEntity loginRequest) {
 return userService.login(loginRequest);
 }

 }
 */

