package com.java_springboot_3rd.blog_cms;

import com.java_springboot_3rd.user_core.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate; // thêm RabbitMQ vào

    // 1. Lấy Comment của 1 bài viết
    public List<Comment> getCommentsByPostId(Integer postId) {
        return commentRepository.findByPostId(postId);
    }

    // 2. Viết Comment MỚI + ĐẨY VÀO RABBITMQ
    public Comment createComment(Integer postId, Comment comment, String username) {
        // Tìm bài viết
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Bài viết không tồn tại!"));

        // Lưu comment
        comment.setPost(post);
        comment.setAuthor(username); // Gắn tên người đang đăng nhập vào
        Comment savedComment = commentRepository.save(comment);

        // 🚀 BẮN SỰ KIỆN VÀO RABBITMQ
        String notification = " Bài viết '" + post.getTitle() + "' vừa có bình luận mới từ " + username + "!";
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "blog.comment.key", notification);
        System.out.println(" Đã đẩy sự kiện thông báo bình luận vào Queue!");

        return savedComment;
    }

    //3. sửa comment
    public Comment updateComment(Integer id, Comment req) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(" Bình luận không tồn tại!"));
        verifyOwnership(comment.getAuthor());
        comment.setContent(req.getContent());
        return commentRepository.save(comment);
    }

    // 4. Xóa Comment
    public void deleteComment(Integer id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(" Bình luận không tồn tại!"));
        verifyOwnership(comment.getAuthor());
        commentRepository.deleteById(id);
    }

    private void verifyOwnership(String resourceAuthor) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && (resourceAuthor == null || !resourceAuthor.equals(currentUsername))) {
            throw new org.springframework.security.access.AccessDeniedException("Lỗi: Không có quyền thao tác trên dữ liệu của người khác!");
        }
    }
}