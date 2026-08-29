package com.project._5.Friend_Finder.service;

import com.project._5.Friend_Finder.dto.AuthResponse;
import com.project._5.Friend_Finder.dto.LoginRequest;
import com.project._5.Friend_Finder.dto.RegisterRequest;
import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.UserRepository;
import com.project._5.Friend_Finder.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    // registering function (Register)
    // The first user to register automatically becomes ADMIN, rest are USER
    // Password is hashed (BCrypt) before saving - never stored as plain text
    public AuthResponse register(RegisterRequest request) {
        // making sure the email is not repeated or there before
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.findByFullName(request.getFullName()).isPresent()) {
            throw new RuntimeException("this name already exists! please try a different name");
        }
        // prepare user and hashing passwords
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // hashing .. and it is importantً
        user.setRole("USER");


        if (userRepository.count() == 0) {
            user.setRole("ADMIN");
        } else {
            user.setRole("USER");
        }
        // save in DB
        userRepository.save(user);

        //  extracting a token and returning it.
        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponse(token, user.getFullName(),user.getId());
    }

    // login method
    public AuthResponse login(LoginRequest request) {
        String input = request.getEmail();

        User user = userRepository.findByEmailOrFullName(input)
                .orElseThrow(() -> new RuntimeException("login information are incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password Incorrect");
        }
        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponse(token, user.getFullName(),user.getId());
    }
}