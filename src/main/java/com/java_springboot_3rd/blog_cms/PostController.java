package com.java_springboot_3rd.blog_cms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private TrackingService trackingService;

    // PUBLIC: Lọc bài viết (VD: /api/posts?categoryId=1&status=published)
    @GetMapping
    public ResponseEntity<?> getAllPosts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(postService.getAllPosts(categoryId, status));
    }

    // PUBLIC: Xem chi tiết bài viết + BẮN KAFKA
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Integer id) {
        try {
            Post post = postService.getPostById(id);
            trackingService.logPostView(id, post.getTitle());
            System.out.println(" [Kafka] Đã ghi nhận sự kiện đọc bài viết!");
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ADMIN: Quản lý bài viết
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        org.springframework.security.core.Authentication authentication = // Lấy thông tin người dùng đang đăng nhập từ Security
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();// Lấy ra username
        post.setAuthor(currentPrincipalName); // Gán tên tác giả vào Entity Post trước khi lưu
        return postService.createPost(post); // Gọi Service lưu vào DB như bình thường
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