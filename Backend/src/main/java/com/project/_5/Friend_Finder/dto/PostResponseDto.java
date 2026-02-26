package com.project._5.Friend_Finder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto { // ده اللي الباك إند هيبعته للفرونت عشان يعرضه في الصفحة الرئيسية
    private Long id;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private LocalDateTime createdAt;

    // post author information (صاحب البوست يعني)
    private String authorName;
    private String authorEmail;
}