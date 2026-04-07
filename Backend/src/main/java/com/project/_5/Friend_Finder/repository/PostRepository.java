package com.project._5.Friend_Finder.repository;

import com.project._5.Friend_Finder.entity.Post;
import com.project._5.Friend_Finder.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    //post count
    Integer countByUser(User user);
    // Fetches posts authored by a specific user, ordered by newest first
    Page<Post> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

}