package com.project._5.Friend_Finder.service;
import com.project._5.Friend_Finder.dto.UserResponseDto; // استخدمنا الاسم الجديد
import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
        // calling the first 5 users from database (without the current user ofc!)
        List<User> suggestedUsers = userRepository.findTop5ByEmailNot(currentUserEmail);

        // we transfer them into safe DTOs
        return suggestedUsers.stream()
                .map(user -> new UserResponseDto(user.getId(), user.getFullName(), user.getEmail()))
                .collect(Collectors.toList());
    }
}