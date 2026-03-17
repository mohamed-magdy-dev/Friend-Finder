package com.project._5.Friend_Finder.controller;

import com.project._5.Friend_Finder.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    // brings the Id of the person we sent inv to
    @PostMapping("/add/{receiverId}")
    public ResponseEntity<String> sendFriendRequest(@PathVariable Long receiverId, Principal principal) {

        String senderEmail = principal.getName();


        String resultMessage = friendshipService.sendFriendRequest(senderEmail, receiverId);

        return ResponseEntity.ok(resultMessage);
    }
    // canceling the request method
    @DeleteMapping("/cancel/{receiverId}")
    public ResponseEntity<String> cancelFriendRequest(@PathVariable Long receiverId, Principal principal) {
        String resultMessage = friendshipService.cancelFriendRequest(principal.getName(), receiverId);
        return ResponseEntity.ok(resultMessage);
    }
}