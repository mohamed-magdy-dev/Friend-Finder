package com.project._5.Friend_Finder.service;

import com.project._5.Friend_Finder.dto.PostRequestDto;
import com.project._5.Friend_Finder.dto.PostResponseDto;
import com.project._5.Friend_Finder.entity.Post;
import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.CommentsRepository;
import com.project._5.Friend_Finder.repository.PostLikesRepository;
import com.project._5.Friend_Finder.repository.PostRepository;
import com.project._5.Friend_Finder.repository.UserRepository;
import jakarta.transaction.Transactional;
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

    /**
     * Creates a new post for the currently authenticated user.
     *
     * @param request   the post payload containing content and media details
     * @param userEmail the email of the author
     * @return The created Post wrapped in a Response DTO
     */
    public PostResponseDto createPost(PostRequestDto request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post();
        post.setContent(request.getContent());
        post.setMediaUrl(request.getMediaUrl());
        post.setMediaType(request.getMediaType());
        post.setUser(user);

        Post savedPost = postRepository.save(post);
        return mapToDto(savedPost, user);
    }

    /**
     * Retrieves a paginated list of all posts for the global feed.
     *
     * @param page      the page number
     * @param size      the number of records per page
     * @param userEmail the email of the current viewer (used for calculating 'isLiked' status)
     * @return Paginated list of posts
     */
    public Page<PostResponseDto> getAllPosts(int page, int size, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        return postsPage.map(post -> mapToDto(post, currentUser));
    }

    /**
     * Retrieves a paginated list of posts authored by a specific user.
     *
     * @param userId           the ID of the post author
     * @param page             the page number
     * @param size             the number of records per page
     * @param currentUserEmail the email of the viewer
     * @return Paginated list of user-specific posts
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

    /**
     * Deletes a specific post. Requires the user to be the original author.
     *
     * @param postId    the ID of the post to delete
     * @param userEmail the email of the user attempting deletion
     * @return Success message
     */
    @Transactional //added transaction so that it can delete without giving us the "No EntityManager" error
    public String deletePost(Long postId, String userEmail) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Make sure the current user owns the post
        if (!post.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized: You can only delete your own posts.");
        }

        // Delete child records first
        commentsRepository.deleteByPost(post);
        postLikesRepository.deleteByPost(post);

        // Delete the parent post
        postRepository.delete(post);

        return "Post deleted successfully.";
    }

    /**
     * Helper method to map a Post entity to a PostResponseDto.
     * Dynamically calculates likes, comments, and the current user's interaction state.
     *
     * @param post        the Post entity
     * @param currentUser the user currently viewing the post
     * @return Mapped DTO
     */
    private PostResponseDto mapToDto(Post post, User currentUser) {
        long likeCount = postLikesRepository.countByPost(post);
        boolean isLiked = postLikesRepository.findByPostAndUser(post, currentUser).isPresent();
        Integer commentsCount = commentsRepository.countByPostId(post.getId());

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