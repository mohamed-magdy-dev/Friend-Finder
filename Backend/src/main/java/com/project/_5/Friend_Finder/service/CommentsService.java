package com.project._5.Friend_Finder.service;

import com.project._5.Friend_Finder.dto.CommentsRequestDto;
import com.project._5.Friend_Finder.dto.CommentsResponseDto;
import com.project._5.Friend_Finder.entity.Comment;
import com.project._5.Friend_Finder.entity.Post;
import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.CommentsRepository;
import com.project._5.Friend_Finder.repository.PostRepository;
import com.project._5.Friend_Finder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // method for adding new comment
    public CommentsResponseDto addComment(Long postId, String userEmail, CommentsRequestDto request) {
       // first we bring user details --> to know who wrote the comment ofc
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // second, we bring the post which the comment will be under
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // we make a new comment and link it the user and the post
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPost(post);
        comment.setUser(user);


        Comment savedComment = commentsRepository.save(comment);


        return mapToDto(savedComment);
    }


    public List<CommentsResponseDto> getCommentsByPostId(Long postId) {

        List<Comment> comments = commentsRepository.findByPostIdOrderByCreatedAtAsc(postId);


        return comments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    private CommentsResponseDto mapToDto(Comment comment) {
        return new CommentsResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getFullName(),
                comment.getUser().getEmail(),
                comment.getCreatedAt()
        );
    }
}