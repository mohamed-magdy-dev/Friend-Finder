package com.project._5.Friend_Finder.repository;

import com.project._5.Friend_Finder.entity.Post;
import com.project._5.Friend_Finder.entity.PostLike;
import com.project._5.Friend_Finder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikesRepository extends JpaRepository<PostLike, Long> {
    // 1️⃣ الدالة دي بتدور: هل فيه لايك بيربط بين "البوست ده" و "اليوزر ده"؟
    // دي اللي هنستخدمها عشان نعرف اليوزر داس لايك قبل كده ولا لأ.
    Optional<PostLike> findByPostAndUser(Post post, User user);

    // 2️⃣ الدالة دي بتعد: البوست ده عنده كام لايك في الجدول؟
    // عشان نبعت الرقم للفرونت إند ونكتب (15 Likes) مثلاً.
    long countByPost(Post post);
}
