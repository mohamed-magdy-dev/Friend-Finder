package com.project._5.Friend_Finder.controller;

import com.project._5.Friend_Finder.dto.UserResponseDto;
import com.project._5.Friend_Finder.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users") // ده العنوان الجديد
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

}