package com.java_springboot_3rd.blog_cms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder; // 🔴 Đã bổ sung import này
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private TrackingService trackingService;

    // PUBLIC: Lọc bài viết
    @GetMapping
    public ResponseEntity<?> getAllPosts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(postService.getAllPosts(categoryId, status));
    }

    // PUBLIC: Xem chi tiết bài viết
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Integer id) {
        log.info("Có người dùng đang yêu cầu xem bài viết ID: {}", id);
        try {
            if(id < 0){
                log.warn("Warning: ID âm!!!!!");
            }
            Post post = postService.getPostById(id);
            trackingService.logPostView(id, post.getTitle());
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            log.error("Lỗi!!! Sập khi tìm bài viết: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ADMIN: Quản lý bài viết (Tạo mới bằng DTO)
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostRequestDTO postDTO) {
        try {
            String author = SecurityContextHolder.getContext().getAuthentication().getName();
            Post newPost = postService.createPost(postDTO, author);
            return ResponseEntity.status(HttpStatus.CREATED).body(newPost);

        } catch (IllegalArgumentException e) { // Bắt lỗi Validation
            log.error("Lỗi dữ liệu đầu vào: {}", e.getMessage());
            // KHÔNG in cả cục StackTrace (dùng e.getMessage() thôi) để tránh việc Logback "hoảng loạn"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) { // Bắt lỗi hệ thống
            log.error("Lỗi khi tạo bài viết: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Integer id, @RequestBody Post post) {
        return ResponseEntity.ok(postService.updatePost(id, post));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Integer id) {
        postService.deletePost(id);
        return ResponseEntity.ok("Đã xóa bài viết " + id);
    }
}