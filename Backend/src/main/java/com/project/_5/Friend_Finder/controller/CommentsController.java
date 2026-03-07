package com.project._5.Friend_Finder.controller;

import com.project._5.Friend_Finder.dto.CommentsRequestDto;
import com.project._5.Friend_Finder.dto.CommentsResponseDto;
import com.project._5.Friend_Finder.service.CommentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommentsController {

    private final CommentsService commentsService;

    // post for user to send a comment
    // link will be something like : POST /api/posts/1/comments
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentsResponseDto> addComment(
            @PathVariable Long postId,
            @RequestBody CommentsRequestDto request,
            Principal principal) {

        // we take data and send it to service "comments service"
        CommentsResponseDto newComment = commentsService.addComment(postId, principal.getName(), request);
        return ResponseEntity.ok(newComment);
    }

    // Get, to bring post comments
    // link will be something like GET /api/posts/1/comments
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentsResponseDto>> getComments(@PathVariable Long postId) {

        List<CommentsResponseDto> comments = commentsService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }
}