package com.project._5.Friend_Finder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "POSTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // نص البوست (Oracle compatible)
    @Lob
    @Column (name = "content") //(nullable = false)
    private String content;

    // لينك صورة أو فيديو (اختياري)
    private String mediaUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // علاقة Many-to-One مع User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}