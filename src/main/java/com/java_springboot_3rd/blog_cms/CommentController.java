package com.java_springboot_3rd.blog_cms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // PUBLIC: Ai cũng xem được bình luận
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable Integer postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    // AUTHENTICATED: Phải Đăng nhập mới được bình luận
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> createComment(@PathVariable Integer postId, @RequestBody Comment comment) {
        try {
            // Lấy tên người dùng từ Hệ thống an ninh (Đã được JwtFilter xác nhận)
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            // bắt đăng nhập để bình luận
            if ("anonymousUser".equals(username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Đăng nhập để bình luận!");
            }

            Comment savedComment = commentService.createComment(postId, comment, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //sửa bình luận
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/comments/{id}")
    public ResponseEntity<?> updateComment(@PathVariable Integer id, @RequestBody Comment comment) {
        return ResponseEntity.ok(commentService.updateComment(id, comment));
    }

    // dăng nhập mới được xóa bình luận
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok("Đã xóa bình luận " + id);
    }
}