package com.project._5.Friend_Finder.controller;

import com.project._5.Friend_Finder.dto.FriendRequestsDto;
import com.project._5.Friend_Finder.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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

    /**
     * Endpoint to accept a friend request.
     */
    @PutMapping("/accept/{requestId}")
    public ResponseEntity<String> acceptRequest(@PathVariable Long requestId, Principal principal) {
        String resultMessage = friendshipService.acceptFriendRequest(requestId, principal.getName());
        return ResponseEntity.ok(resultMessage);
    }

    /**
     * Endpoint to reject a friend request.
     */
    @DeleteMapping("/reject/{requestId}")
    public ResponseEntity<String> rejectRequest(@PathVariable Long requestId, Principal principal) {
        String resultMessage = friendshipService.rejectFriendRequest(requestId, principal.getName());
        return ResponseEntity.ok(resultMessage);
    }


    @GetMapping("/pending")
    public ResponseEntity<List<FriendRequestsDto>> getPendingRequests(Principal principal) {
        List<FriendRequestsDto> requests = friendshipService.getPendingRequests(principal.getName());
        return ResponseEntity.ok(requests);
    }

    @DeleteMapping("/unfriend/{friendId}")
    public ResponseEntity<String> unfriend(Principal principal, @PathVariable Long friendId) {
        String result = friendshipService.unfriend(principal.getName(), friendId);
        return ResponseEntity.ok(result);
    }

}