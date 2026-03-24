package com.java_springboot_3rd.blog_cms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    // Tìm toàn bộ bài viết thuộc 1 Chủ đề cụ thể
    List<Post> findByCategoryId(Integer categoryId);

    // Tìm toàn bộ bài viết theo Trạng thái (draft hoặc published)
    List<Post> findByStatus(String status);

    // Tìm bài viết vừa theo Chủ đề vừa theo Trạng thái
    List<Post> findByCategoryIdAndStatus(Integer categoryId, String status);
}