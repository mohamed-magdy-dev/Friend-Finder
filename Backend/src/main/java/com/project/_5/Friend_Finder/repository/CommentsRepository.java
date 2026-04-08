package com.project._5.Friend_Finder.repository;
import com.project._5.Friend_Finder.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    // counting comments according to the post
    Integer countByPostId(Long postId);
}
