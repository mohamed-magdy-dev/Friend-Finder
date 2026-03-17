package com.project._5.Friend_Finder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private boolean isRequestSent;

    // this constructor thing will solve my ever lasting problem
    // since it only needs 3 fields only so it works now
    public UserResponseDto(Long id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        // isRequestSent is false by default so...
    }
}