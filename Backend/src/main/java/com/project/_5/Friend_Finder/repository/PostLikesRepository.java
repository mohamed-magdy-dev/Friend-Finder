package com.project._5.Friend_Finder.repository;

import com.project._5.Friend_Finder.entity.Post;
import com.project._5.Friend_Finder.entity.PostLike;
import com.project._5.Friend_Finder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikesRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, User user);
    long countByPost(Post post);

    // Interface to delete likes (for deleting post)
    void deleteByPost(Post post);

    // recent activity
    List<PostLike> findTop5ByUserOrderByCreatedAtDesc(User user);
}
