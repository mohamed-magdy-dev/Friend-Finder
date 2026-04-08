package com.project._5.Friend_Finder.service;

import com.project._5.Friend_Finder.dto.PostRequestDto;
import com.project._5.Friend_Finder.dto.PostResponseDto;
import com.project._5.Friend_Finder.entity.Post;
import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.CommentsRepository;
import com.project._5.Friend_Finder.repository.PostLikesRepository;
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
    private final CommentsRepository commentsRepository;

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

    /**
     * Retrieves a paginated list of posts authored by a specific user.
     */
    public Page<PostResponseDto> getUserPosts(Long userId, int page, int size, String currentUserEmail) {

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        Pageable pageable = PageRequest.of(page, size);

        Page<Post> userPosts = postRepository.findByUserOrderByCreatedAtDesc(targetUser, pageable);

        return userPosts.map(post -> mapToDto(post, currentUser));
    }

    // deleting posts
    public String deletePost(Long postId, String userEmail) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized: You can only delete your own posts.");
        }

        postRepository.delete(post);
        return "Post deleted successfully.";
    }

    // method of smart switching (dto stuff!)
    private PostResponseDto mapToDto(Post post, User currentUser) {
        // 1. We count the likes from the database
        long likeCount = postLikesRepository.countByPost(post);

        // 2. Asking the DB if user currently liked the video?
        boolean isLiked = postLikesRepository.findByPostAndUser(post, currentUser).isPresent();

        // 3. We count the comments for this post
        Integer commentsCount = commentsRepository.countByPostId(post.getId());

        // 4. Return everything, including the new commentCount
        return new PostResponseDto(
                post.getId(),
                post.getContent(),
                post.getMediaUrl(),
                post.getMediaType(),
                post.getCreatedAt(),
                post.getUser().getId(),
                post.getUser().getFullName(),
                post.getUser().getEmail(),
                post.getUser().getProfilePictureUrl(),
                likeCount,
                isLiked,
                commentsCount
        );
    }
}