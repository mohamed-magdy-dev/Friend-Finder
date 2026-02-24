package com.project._5.Friend_Finder.repository;

import com.project._5.Friend_Finder.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // سبرينج داتا ذكي جداً، لما بتكتب اسم الميثود بالطريقة دي، هو بيفهم لوحده إنه المفروض يجيب كل البوستات ويرتبهم بالتاريخ تنازلياً (Desc).
    // مش محتاجين نكتب كود SQL بإيدينا!
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}