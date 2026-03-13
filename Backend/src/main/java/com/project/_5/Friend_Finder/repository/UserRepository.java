package com.project._5.Friend_Finder.repository;

import com.project._5.Friend_Finder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // for login and email searching
    Optional<User> findByFullName(String fullName); // to find by username (for login later!)

    // added this for login to login with either email or user name
    @Query("SELECT u FROM User u WHERE u.email = :input OR u.fullName = :input")
    Optional<User> findByEmailOrFullName(String input);

    // this is for the first 5 users (except the one user is currently using)
    // note the name is like that since spring translates this into sql query
    // findTop5ByEmailNot = spring dataJpa translates it and understands it.
    List<User> findTop5ByEmailNot(String email);
}
