package com.project._5.Friend_Finder.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentsResponseDto {
    private Long id;
    private String content;
    private String authorName;
    private String authorEmail;
    private LocalDateTime createdAt;
    private String authorProfilePictureUrl;
}
