package com.project._5.Friend_Finder.controller;
import com.project._5.Friend_Finder.service.PostLikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostLikesController {
    private final PostLikesService postLikesService;

    // بنعمل Endpoint بياخد رقم البوست في اللينك
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long postId, Principal principal) {
        // بنبعت رقم البوست وإيميل اليوزر للسيرفس
        String result = postLikesService.toggleLike(postId, principal.getName());

        // بنرجع JSON بسيط للفرونت إند فيه النتيجة (Liked أو Unliked)
        return ResponseEntity.ok(Map.of("message", result));
    }
}
