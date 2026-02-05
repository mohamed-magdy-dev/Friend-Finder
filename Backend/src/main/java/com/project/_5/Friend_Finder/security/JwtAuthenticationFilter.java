package com.project._5.Friend_Finder.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. بندور على التوكن في الـ Header بتاع الـ Request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // لو مفيش توكن أو مش بيبدأ بكلمة Bearer، عدي الطلب (ومش هيدخل طبعاً لأن السكيورتي هيوقفه بعدين)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. بنطلع التوكن الصافي (بنشيل كلمة Bearer والمسافة)
        jwt = authHeader.substring(7);
        // بنطلع الإيميل من التوكن
        userEmail = jwtUtils.extractUsername(jwt);

        // 3. لو الإيميل موجود واليوزر مش معمولة Login أصلاً
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 4. نتأكد إن التوكن سليم
            if (jwtUtils.validateToken(jwt, userDetails)) {
                // نسجله في النظام إنه "دخل خلاص"
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // كمل يا ريس
        filterChain.doFilter(request, response);
    }
}