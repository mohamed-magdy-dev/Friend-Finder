package com.project._5.Friend_Finder.service;

import com.project._5.Friend_Finder.dto.ProfileUpdateRequestDto;
import com.project._5.Friend_Finder.dto.UserProfileDto;
import com.project._5.Friend_Finder.dto.UserResponseDto;
import com.project._5.Friend_Finder.entity.Friendship;
import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.FriendshipRepository;
import com.project._5.Friend_Finder.repository.PostRepository; // <== ضفنا ده هنا
import com.project._5.Friend_Finder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final PostRepository postRepository; // <== عرفناه هنا علشان نستخدمه في العد

    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserResponseDto(user.getId(), user.getFullName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    public List<UserResponseDto> getSuggestedUsers(String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<User> allUsers = userRepository.findTop5ByEmailNot(currentUserEmail);

        return allUsers.stream()
                .filter(user -> {
                    Optional<Friendship> sentRequest = friendshipRepository.findBySenderAndReceiver(currentUser, user);
                    Optional<Friendship> receivedRequest = friendshipRepository.findBySenderAndReceiver(user, currentUser);
                    boolean isAlreadyFriend = (sentRequest.isPresent() && "ACCEPTED".equals(sentRequest.get().getStatus())) ||
                            (receivedRequest.isPresent() && "ACCEPTED".equals(receivedRequest.get().getStatus()));
                    return !isAlreadyFriend;
                })
                .map(user -> {
                    Optional<Friendship> sentRequest = friendshipRepository.findBySenderAndReceiver(currentUser, user);
                    boolean isSent = sentRequest.isPresent() && "PENDING".equals(sentRequest.get().getStatus());
                    return new UserResponseDto(user.getId(), user.getFullName(), user.getEmail(), isSent);
                })
                .collect(Collectors.toList());
    }

    // ===================== USER PROFILE (مع العدادات) =====================
    public UserProfileDto getUserProfile(Long targetUserId, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        String status = "NONE";

        if (currentUser.getId().equals(targetUser.getId())) {
            status = "SELF";
        } else {
            Optional<Friendship> sentRequest = friendshipRepository.findBySenderAndReceiver(currentUser, targetUser);
            Optional<Friendship> receivedRequest = friendshipRepository.findBySenderAndReceiver(targetUser, currentUser);

            if (sentRequest.isPresent()) {
                status = sentRequest.get().getStatus().equals("ACCEPTED") ? "FRIENDS" : "PENDING_SENT";
            } else if (receivedRequest.isPresent()) {
                status = receivedRequest.get().getStatus().equals("ACCEPTED") ? "FRIENDS" : "PENDING_RECEIVED";
            }
        }

        // 1. حساب عدد البوستات
        Integer postsCount = postRepository.countByUser(targetUser);

        // 2. حساب عدد الأصدقاء (في الاتجاهين)
        Integer friendsCount = friendshipRepository.countBySenderAndStatus(targetUser, "ACCEPTED") +
                friendshipRepository.countByReceiverAndStatus(targetUser, "ACCEPTED");

        // 3. إرجاع الـ DTO بكل البيانات الجديدة
        return new UserProfileDto(
                targetUser.getId(),
                targetUser.getFullName(),
                targetUser.getEmail(),
                targetUser.getBio(),
                targetUser.getProfilePictureUrl(),
                targetUser.getCoverPictureUrl(),
                targetUser.getBirthDate(),
                status,
                postsCount, // <== ضفنا عدد البوستات
                friendsCount // <== ضفنا عدد الأصدقاء
        );
    }

    public List<UserResponseDto> searchUsersByName(String name, String currentUserEmail) {
        List<User> matchedUsers = userRepository.findTop10ByFullNameContainingIgnoreCase(name);
        return matchedUsers.stream()
                .filter(user -> !user.getEmail().equals(currentUserEmail))
                .map(user -> new UserResponseDto(user.getId(), user.getFullName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    public Page<UserResponseDto> getFullSearchResults(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findByFullNameContainingIgnoreCase(name, pageable);
        return usersPage.map(user -> new UserResponseDto(user.getId(), user.getFullName(), user.getEmail()));
    }

    private final String UPLOAD_DIR = "uploads/images/";

    public String uploadProfilePicture(MultipartFile file, String email) {
        return saveFileAndUpdateUser(file, email, true);
    }

    public String uploadCoverPicture(MultipartFile file, String email) {
        return saveFileAndUpdateUser(file, email, false);
    }

    private String saveFileAndUpdateUser(MultipartFile file, String email, boolean isProfilePic) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                originalFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            } else {
                originalFilename = "uploaded_image.jpg";
            }

            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "http://localhost:8080/api/users/images/" + fileName;

            if (isProfilePic) {
                user.setProfilePictureUrl(fileUrl);
            } else {
                user.setCoverPictureUrl(fileUrl);
            }

            userRepository.save(user);
            return fileUrl;

        } catch (Exception e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public UserProfileDto updateUserProfile(String email, ProfileUpdateRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setBio(request.getBio());
        user.setBirthDate(request.getBirthDate());

        userRepository.save(user);

        return getUserProfile(user.getId(), email);
    }
}