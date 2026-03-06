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
    private final PostLikesRepository postLikesRepository; // 🌟 حقنّا ريبو اللايكات هنا

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

        // 🌟 بنبعت اليوزر الحالي لدالة التحويل
        return mapToDto(savedPost, user);
    }

    // 2️⃣ دالة جلب البوستات (اتعدلت عشان تاخد الإيميل)
    public Page<PostResponseDto> getAllPosts(int page, int size, String userEmail) {
        // بنجيب اليوزر اللي فاتح الصفحة دلوقتي
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        // 🌟 بنبعت اليوزر الحالي لدالة التحويل عشان نعرف هو داس لايك ولا لأ
        return postsPage.map(post -> mapToDto(post, currentUser));
    }

    // 3️⃣ دالة التحويل الذكية (اللي بتعبي العلبة)
    private PostResponseDto mapToDto(Post post, User currentUser) {
        // بنعد اللايكات من الداتا بيز
        long likeCount = postLikesRepository.countByPost(post);

        // بنسأل الداتا بيز: هل اليوزر الحالي عامل لايك للبوست ده؟
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