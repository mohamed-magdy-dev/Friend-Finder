package com.project._5.Friend_Finder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// one row in the "recent activity" list on the profile sidebar
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDto {
    private String type;        // "POST", "COMMENT" or "LIKE" - lets the frontend pick an icon
    private String description; // human readable line, e.g. "Commented: \"nice post!\""
    private LocalDateTime createdAt;
}