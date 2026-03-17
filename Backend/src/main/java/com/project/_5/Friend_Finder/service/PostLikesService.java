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

    private final PostLikesRepository postLikesRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;


    public String toggleLike(Long postId, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));


        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));


        Optional<PostLike> existingLike = postLikesRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {

            postLikesRepository.delete(existingLike.get());
            return "Unliked";
        } else {

            PostLike newLike = new PostLike();
            newLike.setPost(post);
            newLike.setUser(user);
            postLikesRepository.save(newLike);
            return "Liked";
        }
    }
}
