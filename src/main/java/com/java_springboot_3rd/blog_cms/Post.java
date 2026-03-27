package com.java_springboot_3rd.blog_cms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "posts")
public class Post implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT") // Để chứa nội dung dài
    private String content;

    private String author;

    private String status = "draft"; // Mặc định là bản nháp ('draft' hoặc 'published')

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)

    @CreationTimestamp
    private LocalDateTime createdAt;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Nhiều Post thuộc về 1 Category
    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("posts")
    private Category category;

    // 1 Post có nhiều Comments
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Comment> comments;

    public java.util.List<Comment> getComments(){
        if (this.comments == null) return null;
        return new java.util.ArrayList<>(this.comments);
    }
}