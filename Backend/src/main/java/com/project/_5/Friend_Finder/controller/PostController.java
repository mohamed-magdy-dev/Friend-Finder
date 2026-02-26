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
@CrossOrigin(origins = "*") // عشان الفرونت إند
public class PostController {

    private final PostService postService;

    // 1. إنشاء بوست جديد
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@RequestBody PostRequestDto request, Principal principal) {
        // principal.getName() هنا بترجع "الإيميل" اللي متخزن في SecurityContextHolder
        String userEmail = principal.getName();
        return ResponseEntity.ok(postService.createPost(request, userEmail));
    }

    // bringing all posts (with pagination)
    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getAllPosts(page, size));
    }
}