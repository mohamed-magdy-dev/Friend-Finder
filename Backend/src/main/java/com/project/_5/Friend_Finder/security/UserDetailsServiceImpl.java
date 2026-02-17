package com.project._5.Friend_Finder.security;

import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. بندور ع اليوزر بالإيميل
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. بنحوله للشكل اللي سبرينج سكيورتي بيفهمه (org.springframework.security.core.userdetails.User)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                // هنا بنحول الرول بتاعنا لصلاحية سبرينج يفهمها
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
