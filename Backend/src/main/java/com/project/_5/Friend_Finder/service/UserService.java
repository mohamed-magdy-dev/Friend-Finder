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

    // ميثود تجيب كل الناس وتحولهم لـ DTO
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
}