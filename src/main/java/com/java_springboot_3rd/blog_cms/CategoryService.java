package com.java_springboot_3rd.blog_cms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Service
@Slf4j
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // 1. Lấy danh sách (Có Redis Cache)
    @Cacheable(value = "categories")
    public List<Category> getAllCategories() {
        log.info("🐌 [Redis Miss] Đang lội xuống MySQL lấy toàn bộ Chủ đề...");
        return categoryRepository.findAll();
    }

    // 2. Lấy 1 Chủ đề
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("❌ Không tìm thấy Chủ đề có ID: " + id));
    }

    // 3. Thêm mới (Xóa Cache cũ đi để user thấy cái mới)
    @CacheEvict(value = "categories", allEntries = true)
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    // 4. Cập nhật (Xóa Cache cũ)
    @CacheEvict(value = "categories", allEntries = true)
    public Category updateCategory(Integer id, Category req) {
        Category cat = getCategoryById(id);
        cat.setName(req.getName());
        cat.setDescription(req.getDescription());
        return categoryRepository.save(cat);
    }

    // 5. Xóa (Xóa Cache cũ)
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Integer id) {
        categoryRepository.deleteById(id);
    }
}