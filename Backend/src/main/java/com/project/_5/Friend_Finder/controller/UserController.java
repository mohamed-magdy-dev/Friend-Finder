package com.project._5.Friend_Finder.controller;

import com.project._5.Friend_Finder.dto.UserProfileDto;
import com.project._5.Friend_Finder.dto.UserResponseDto;
import com.project._5.Friend_Finder.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // عشان الفرونت ميضربش CORS
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @GetMapping("/suggestions")
    public ResponseEntity<List<UserResponseDto>> getSuggestedUsers(Principal principal) {
        // getting the email of the current user ( the one who logged in)
        // principal.getName()
        List<UserResponseDto> suggestions = userService.getSuggestedUsers(principal.getName());
        return ResponseEntity.ok(suggestions);
    }

                           // user profile
    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable Long userId, Principal principal) {
        UserProfileDto profile = userService.getUserProfile(userId, principal.getName());
        return ResponseEntity.ok(profile);
    }

    // Search endpoint
    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDto>> searchUsers(@RequestParam String name, Principal principal) {
        List<UserResponseDto> results = userService.searchUsersByName(name, principal.getName());
        return ResponseEntity.ok(results);
    }

    // Endpoint for the full search results page
    @GetMapping("/search/full")
    public ResponseEntity<Page<UserResponseDto>> getFullSearchResults(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserResponseDto> results = userService.getFullSearchResults(name, page, size);
        return ResponseEntity.ok(results);
    }
}