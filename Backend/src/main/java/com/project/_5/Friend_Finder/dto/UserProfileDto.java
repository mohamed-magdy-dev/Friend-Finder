package com.project._5.Friend_Finder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private Long id;
    private String fullName;
    private String email;
    // profile stuff
    private String bio;
    private String profilePictureUrl;
    private String coverPictureUrl;
    private LocalDate birthDate;

    // status can be: "SELF", "FRIENDS", "PENDING_SENT", "PENDING_RECEIVED", "NONE"
    private String friendshipStatus;


}