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
    public AuthResponse register(RegisterRequest request) {
        // making sure the email is not repeated or there before
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // prepare user and hashing passwords
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // hashing .. and it is importantً
        user.setRole("USER");

        // save in DB
        userRepository.save(user);

        //  extracting a token and returning it.
        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponse(token, user.getFullName());
    }

    // login method
    public AuthResponse login(LoginRequest request) {
        //  security manager is the one checking from email/pass
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // if pass the authentication or class above then generate token.
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponse(token, user.getFullName());
    }
}