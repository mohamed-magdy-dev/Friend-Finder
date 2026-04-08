package com.project._5.Friend_Finder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private LocalDateTime createdAt;

    // post author information (صاحب البوست يعني)
    private Long authorId; // added this for profile page
    private String authorName;
    private String authorEmail;
    private String authorProfilePictureUrl;
    // for "likes" to solve the "like post" problem:
    // added JsonProperty since Lombok doesn't want to send the name as it should!
    @JsonProperty("likeCount")
    private long likeCount;
    private Boolean isLiked;
    // fixing the comments bug
    private Integer commentsCount; // i will make it count the comments from here first
}