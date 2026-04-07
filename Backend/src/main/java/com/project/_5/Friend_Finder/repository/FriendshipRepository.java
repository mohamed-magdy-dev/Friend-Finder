package com.project._5.Friend_Finder.repository;

import com.project._5.Friend_Finder.entity.Friendship;
import com.project._5.Friend_Finder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Integer countBySenderAndStatus(User sender, String status);

    Integer countByReceiverAndStatus(User receiver, String status);

    // 1- method to ask: has user A sent request to user B before?
    // nobody wants the same friend  request 50 times!
    Optional<Friendship> findBySenderAndReceiver(User sender, User receiver);

    // 2- this method should return the "pending" requests which are sent to a user
    // this what will show the pending requests (mostly) for user A
    List<Friendship> findByReceiverAndStatus(User receiver, String status);
}