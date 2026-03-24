package com.java_springboot_3rd.blog_cms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // 1. Lấy danh sách có Filter (Lọc)
    public List<Post> getAllPosts(Integer categoryId, String status) {
        if (categoryId != null && status != null) {
            return postRepository.findByCategoryIdAndStatus(categoryId, status);
        } else if (categoryId != null) {
            return postRepository.findByCategoryId(categoryId);
        } else if (status != null) {
            return postRepository.findByStatus(status);
        }
        return postRepository.findAll();
    }

    // 2. Lấy chi tiết 1 bài (Có Redis Cache)
    @Cacheable(value = "posts", key = "#id")
    public Post getPostById(Integer id) {
        System.out.println(" Đang lấy chi tiết Bài viết ID: " + id);
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(" Không tìm thấy Bài viết có ID: " + id));
    }

    // 3. Tạo bài viết
    public Post createPost(Post post) {
        // Kiểm tra xem Category có tồn tại không
        if (post.getCategory() == null || post.getCategory().getId() == null) {
            throw new IllegalArgumentException(" Bài viết phải thuộc về một Chủ đề (CategoryId)!");
        }
        Category cat = categoryRepository.findById(post.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException(" Chủ đề không tồn tại!"));

        post.setCategory(cat);
        return postRepository.save(post);
    }

    // 4. Cập nhật bài viết
    @CacheEvict(value = "posts", key = "#id")
    public Post updatePost(Integer id, Post req) {
        Post post = getPostById(id);
        verifyOwnership(post.getAuthor());
        if (req.getTitle() != null) post.setTitle(req.getTitle());
        if (req.getContent() != null) post.setContent(req.getContent());
        if (req.getStatus() != null) post.setStatus(req.getStatus());
        return postRepository.save(post);
    }

    // 5. Xóa bài viết
    @CacheEvict(value = "posts", key = "#id")
    public void deletePost(Integer id) {
        Post post = getPostById(id);
        verifyOwnership(post.getAuthor());
        postRepository.deleteById(id);
    }

    private void verifyOwnership(String resourceAuthor) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        String currentUsername = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && (resourceAuthor == null || !resourceAuthor.equals(currentUsername))) {
            throw new org.springframework.security.access.AccessDeniedException("Lỗi: không có quyền thao tác trên dữ liệu của người khác!");
        }
    }
}