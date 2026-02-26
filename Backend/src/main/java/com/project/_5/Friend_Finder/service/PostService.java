package com.project._5.Friend_Finder.service;

import com.project._5.Friend_Finder.dto.PostRequestDto;
import com.project._5.Friend_Finder.dto.PostResponseDto;
import com.project._5.Friend_Finder.entity.Post;
import com.project._5.Friend_Finder.entity.User;
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

    // 1️⃣ دالة إنشاء بوست جديد
    public PostResponseDto createPost(PostRequestDto request, String userEmail) {
        // 1. ندور على اليوزر اللي بيكتب البوست (بنجيبه بالإيميل من التوكن عشان الأمان)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. نجهز البوست الجديد عشان نحفظه
        Post post = new Post();
        post.setContent(request.getContent());
        post.setMediaUrl(request.getMediaUrl()); // حاليا هتبقي فاضية لحد ما نعمل رفع الصور
        post.setMediaType(request.getMediaType());
        post.setUser(user); // ربطنا البوست بصاحبه!

        // 3. نحفظ في الداتا بيز
        Post savedPost = postRepository.save(post);

        // 4. نرجع البوست في العلبة النضيفة (ResponseDto)
        return mapToDto(savedPost);
    }

    // 2️⃣ دالة جلب البوستات (بالباجينيشن)
    public Page<PostResponseDto> getAllPosts(int page, int size) {
        // بنجهز طلب الباجينيشن (رقم الصفحة، وحجمها)
        Pageable pageable = PageRequest.of(page, size);

        // بنجيب البوستات من الداتا بيز مترتبة
        Page<Post> postsPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        // بنحول كل Post لـ PostResponseDto عشان الفرونت إند
        return postsPage.map(this::mapToDto); // استخدام دالة التحويل اللي تحت
    }

    // 🛠️ دالة مساعدة (Helper) عشان تحول من Entity لـ DTO ومانكررش الكود
    private PostResponseDto mapToDto(Post post) {
        return new PostResponseDto(
                post.getId(),
                post.getContent(),
                post.getMediaUrl(),
                post.getMediaType(),
                post.getCreatedAt(),
                post.getUser().getFullName(), // أخدنا اسم صاحب البوست
                post.getUser().getEmail()     // أخدنا إيميل صاحب البوست
        );
    }
}