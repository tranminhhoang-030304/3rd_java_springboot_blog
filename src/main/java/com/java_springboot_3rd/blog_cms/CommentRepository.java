package com.java_springboot_3rd.blog_cms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // Lấy toàn bộ bình luận của 1 Bài viết cụ thể
    List<Comment> findByPostId(Integer postId);
}