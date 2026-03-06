package com.project._5.Friend_Finder.service;
import com.project._5.Friend_Finder.entity.Post;
import com.project._5.Friend_Finder.entity.PostLike;
import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.PostLikesRepository;
import com.project._5.Friend_Finder.repository.PostRepository;
import com.project._5.Friend_Finder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikesService {

    private final PostLikesRepository postLikesRepository; // ده الريبو بتاعك بالـ S
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // دالة التبديل (Toggle): بتعمل لايك لو مفيش، وبتشيله لو موجود
    public String toggleLike(Long postId, String userEmail) {
        // 1. نجيب اليوزر اللي داس على الزرار
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. نجيب البوست اللي انداس عليه
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 3. نسأل الداتا بيز: هل الراجل ده عمل لايك للبوست ده قبل كده؟
        Optional<PostLike> existingLike = postLikesRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            // لو لقيناه عامل لايك -> نمسح اللايك (Unlike)
            postLikesRepository.delete(existingLike.get());
            return "Unliked";
        } else {
            // لو ملقيناش لايك -> نكريت لايك جديد (Like)
            PostLike newLike = new PostLike();
            newLike.setPost(post);
            newLike.setUser(user);
            postLikesRepository.save(newLike);
            return "Liked";
        }
    }
}
