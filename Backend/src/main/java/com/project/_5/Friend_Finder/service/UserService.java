package com.project._5.Friend_Finder.service;

import com.project._5.Friend_Finder.dto.UserResponseDto;
import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.FriendshipRepository;
import com.project._5.Friend_Finder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    // method for calling all users and convert them to DTO
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> new UserResponseDto(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail()
                ))
                .collect(Collectors.toList());
    }

    // this for suggested friends function --->
    public List<UserResponseDto> getSuggestedUsers(String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<User> suggestedUsers = userRepository.findTop5ByEmailNot(currentUserEmail);

        return suggestedUsers.stream()
                .map(user -> {
                    boolean isSent = friendshipRepository.findBySenderAndReceiver(currentUser, user).isPresent();
                    return new UserResponseDto(user.getId(), user.getFullName(), user.getEmail(), isSent);
                })
                .collect(Collectors.toList());
    }
}