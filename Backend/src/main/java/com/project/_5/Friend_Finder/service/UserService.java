package com.project._5.Friend_Finder.service;

import com.project._5.Friend_Finder.dto.ActivityDto;
import com.project._5.Friend_Finder.dto.ProfileUpdateRequestDto;
import com.project._5.Friend_Finder.dto.UserProfileDto;
import com.project._5.Friend_Finder.dto.UserResponseDto;
import com.project._5.Friend_Finder.entity.Friendship;
import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.*;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final PostRepository postRepository;
    private final String UPLOAD_DIR = "uploads/images/";
    // user activity
    private final CommentsRepository commentsRepository;
    private final PostLikesRepository postLikesRepository;
    /**
     * Retrieves all users registered in the system.
     *
     * @return List of all users
     */
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserResponseDto(user.getId(), user.getFullName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    /**
     * Generates a list of suggested users to follow or add as friends.
     * Excludes the current user and users they are already friends with.
     *
     * @param currentUserEmail the email of the current user
     * @return List of suggested users
     */
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

    /**
     * Retrieves the profile information of a target user, including friendship status and statistics.
     *
     * @param targetUserId     the ID of the profile owner
     * @param currentUserEmail the email of the user viewing the profile
     * @return Complete UserProfileDto
     */
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

        Integer postsCount = postRepository.countByUser(targetUser);
        Integer friendsCount = friendshipRepository.countBySenderAndStatus(targetUser, "ACCEPTED") +
                friendshipRepository.countByReceiverAndStatus(targetUser, "ACCEPTED");

        return new UserProfileDto(
                targetUser.getId(),
                targetUser.getFullName(),
                targetUser.getEmail(),
                targetUser.getBio(),
                targetUser.getProfilePictureUrl(),
                targetUser.getCoverPictureUrl(),
                targetUser.getBirthDate(),
                status,
                postsCount,
                friendsCount
        );
    }

    /**
     * Searches for users by their full name (case-insensitive).
     *
     * @param name             the search query
     * @param currentUserEmail the email of the user performing the search
     * @return List of matching users
     */
    public List<UserResponseDto> searchUsersByName(String name, String currentUserEmail) {
        List<User> matchedUsers = userRepository.findTop10ByFullNameContainingIgnoreCase(name);
        return matchedUsers.stream()
                .filter(user -> !user.getEmail().equals(currentUserEmail))
                .map(user -> new UserResponseDto(user.getId(), user.getFullName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves full paginated search results for users.
     */
    public Page<UserResponseDto> getFullSearchResults(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findByFullNameContainingIgnoreCase(name, pageable);
        return usersPage.map(user -> new UserResponseDto(user.getId(), user.getFullName(), user.getEmail()));
    }

    /**
     * Uploads and updates the user's profile picture.
     */
    public String uploadProfilePicture(MultipartFile file, String email) {
        return saveFileAndUpdateUser(file, email, true);
    }

    /**
     * Uploads and updates the user's cover picture.
     */
    public String uploadCoverPicture(MultipartFile file, String email) {
        return saveFileAndUpdateUser(file, email, false);
    }

    /**
     * Helper method to save files locally and update the corresponding URL in the database.
     */
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

    /**
     * Updates general profile details (Name, Bio, BirthDate).
     */
    public UserProfileDto updateUserProfile(String email, ProfileUpdateRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setBio(request.getBio());
        user.setBirthDate(request.getBirthDate());

        userRepository.save(user);

        return getUserProfile(user.getId(), email);
    }

    public List<ActivityDto> getUserActivity(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ActivityDto> activity = new ArrayList<>();

        // last 5 posts
        postRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, 5))
                .forEach(post -> activity.add(new ActivityDto(
                        "POST",
                        "Posted: " + truncate(post.getContent()),
                        post.getCreatedAt()
                )));

        // last 5 comments
        commentsRepository.findTop5ByUserOrderByCreatedAtDesc(user)
                .forEach(comment -> activity.add(new ActivityDto(
                        "COMMENT",
                        "Commented: \"" + truncate(comment.getContent()) + "\"",
                        comment.getCreatedAt()
                )));

        // last 5 likes
        postLikesRepository.findTop5ByUserOrderByCreatedAtDesc(user)
                .forEach(like -> activity.add(new ActivityDto(
                        "LIKE",
                        "Liked a post",
                        like.getCreatedAt()
                )));

        // we now have up to 15 items (5+5+5) from three different sources -
        // sort them all together by date and just keep the newest 5
        return activity.stream()
                .sorted(Comparator.comparing(ActivityDto::getCreatedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    // keeps long [post/comment] text from blowing up the sidebar layout
    private String truncate(String text) {
        if (text == null) return "";
        return text.length() > 40 ? text.substring(0, 40) + "..." : text;
    }
}