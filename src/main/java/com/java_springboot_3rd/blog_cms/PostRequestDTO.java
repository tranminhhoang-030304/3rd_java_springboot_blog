package com.java_springboot_3rd.blog_cms;

import lombok.Data;
import java.io.Serializable;

@Data
public class PostRequestDTO implements Serializable {
    private String title;
    private String content;

    // Chỉ nhận ID của danh mục, không nhận cả Object
    private Integer categoryId;
    private String status;
}