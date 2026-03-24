package com.java_springboot_3rd.blog_cms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // Spring Data JPA tự viết hết các lệnh CRUD cho Categories
}