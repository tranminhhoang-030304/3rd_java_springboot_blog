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

    // 3. Tạo bài viết (nhận DTO và author chuẩn chỉ)
    @CacheEvict(value = "posts", allEntries = true) // Xóa cache danh sách khi có bài mới
    public Post createPost(PostRequestDTO postDTO, String author) {
        // 1. Kiểm tra DTO (Validate)
        if (postDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("Bài viết phải thuộc về một Chủ đề (CategoryId)!");
        }

        // 2. Đi tìm Chủ đề (Category) thật trong Database dựa vào ID Frontend gửi lên
        Category category = categoryRepository.findById(postDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Chủ đề có ID: " + postDTO.getCategoryId()));

        // 3. Mapping: Chuyển đổi DTO -> Entity
        Post post = new Post();
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setStatus(postDTO.getStatus());
        post.setAuthor(author);
        post.setCategory(category); // Lắp Object Category vào Entity

        // 4. Lưu xuống Database
        return postRepository.save(post);
    }

    // 4. Cập nhật bài viết
    @CacheEvict(value = "posts", key = "#id")
    public Post updatePost(Integer id, Post req) {
        Post post = getPostById(id);
        verifyOwnership(post.getAuthor()); // 🛡️ Bảo vệ: Chỉ chủ bài hoặc Admin mới được sửa

        if (req.getTitle() != null) post.setTitle(req.getTitle());
        if (req.getContent() != null) post.setContent(req.getContent());
        if (req.getStatus() != null) post.setStatus(req.getStatus());

        return postRepository.save(post);
    }

    // 5. Xóa bài viết
    @CacheEvict(value = "posts", key = "#id")
    public void deletePost(Integer id) {
        Post post = getPostById(id);
        verifyOwnership(post.getAuthor()); // 🛡️ Bảo vệ: Chỉ chủ bài hoặc Admin mới được xóa
        postRepository.deleteById(id);
    }

    // 🛡️ HÀM BẢO VỆ LÕI (Xác minh chính chủ)
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