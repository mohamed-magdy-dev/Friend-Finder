package com.project._5.Friend_Finder.controller;

import com.project._5.Friend_Finder.dto.PostRequestDto;
import com.project._5.Friend_Finder.dto.PostResponseDto;
import com.project._5.Friend_Finder.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // cors for frontend
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@RequestBody PostRequestDto request, Principal principal) {

        String userEmail = principal.getName();
        return ResponseEntity.ok(postService.createPost(request, userEmail));
    }

    // bringing all posts (with pagination)
    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        String userEmail = principal.getName();
        return ResponseEntity.ok(postService.getAllPosts(page, size, userEmail));    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponseDto>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        Page<PostResponseDto> posts = postService.getUserPosts(userId, page, size, principal.getName());
        return ResponseEntity.ok(posts);
    }
}