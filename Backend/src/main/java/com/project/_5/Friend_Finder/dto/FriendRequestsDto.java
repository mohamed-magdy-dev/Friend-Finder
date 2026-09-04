package com.project._5.Friend_Finder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestsDto {
    private Long requestId;
    private Long senderId;
    private String senderName;
    private String senderEmail;
}
