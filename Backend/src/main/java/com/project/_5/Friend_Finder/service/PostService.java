package com.project._5.Friend_Finder.service;

import com.project._5.Friend_Finder.dto.PostRequestDto;
import com.project._5.Friend_Finder.dto.PostResponseDto;
import com.project._5.Friend_Finder.entity.Post;
import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.PostLikesRepository; // الريبو بتاعك
import com.project._5.Friend_Finder.repository.PostRepository;
import com.project._5.Friend_Finder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikesRepository postLikesRepository;

    // create post
    public PostResponseDto createPost(PostRequestDto request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post();
        post.setContent(request.getContent());
        post.setMediaUrl(request.getMediaUrl());
        post.setMediaType(request.getMediaType());
        post.setUser(user);

        Post savedPost = postRepository.save(post);

        // sending current user to dto
        return mapToDto(savedPost, user);
    }

    // method for bringing the posts
    public Page<PostResponseDto> getAllPosts(int page, int size, String userEmail) {
      // bring the user that is opening the page now

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        // sending the current user to convert method to check if he liked (pressed like) or not
        return postsPage.map(post -> mapToDto(post, currentUser));
    }

    // method of  smart switching (dto stuff!)
    private PostResponseDto mapToDto(Post post, User currentUser) {
        // we count the likes from the database
        long likeCount = postLikesRepository.countByPost(post);

        // asking the DB if user currently liked the video?
        boolean isLiked = postLikesRepository.findByPostAndUser(post, currentUser).isPresent();

        return new PostResponseDto(
                post.getId(),
                post.getContent(),
                post.getMediaUrl(),
                post.getMediaType(),
                post.getCreatedAt(),
                post.getUser().getFullName(),
                post.getUser().getEmail(),
                likeCount, // added counts (for like)
                isLiked    // added this
        );
    }
}