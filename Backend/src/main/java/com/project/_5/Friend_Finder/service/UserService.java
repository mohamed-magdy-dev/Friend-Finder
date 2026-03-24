package com.project._5.Friend_Finder.service;

import com.project._5.Friend_Finder.dto.UserProfileDto;
import com.project._5.Friend_Finder.dto.UserResponseDto;
import com.project._5.Friend_Finder.entity.Friendship;
import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.FriendshipRepository;
import com.project._5.Friend_Finder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

        List<User> allUsers = userRepository.findTop5ByEmailNot(currentUserEmail);

        return allUsers.stream()
                .filter(user -> {
                    // Check if there is already an ACCEPTED friendship in either direction
                    Optional<Friendship> sentRequest = friendshipRepository.findBySenderAndReceiver(currentUser, user);
                    Optional<Friendship> receivedRequest = friendshipRepository.findBySenderAndReceiver(user, currentUser);

                    boolean isAlreadyFriend = (sentRequest.isPresent() && "ACCEPTED".equals(sentRequest.get().getStatus())) ||
                            (receivedRequest.isPresent() && "ACCEPTED".equals(receivedRequest.get().getStatus()));

                    // Only keep the user in the suggestion list if they are NOT already friends
                    return !isAlreadyFriend;
                })
                .map(user -> {
                    // Check if the current user has a PENDING request sent to this user
                    Optional<Friendship> sentRequest = friendshipRepository.findBySenderAndReceiver(currentUser, user);
                    boolean isSent = sentRequest.isPresent() && "PENDING".equals(sentRequest.get().getStatus());

                    return new UserResponseDto(user.getId(), user.getFullName(), user.getEmail(), isSent);
                })
                .collect(Collectors.toList());
    }

    // User profile

    public UserProfileDto getUserProfile(Long targetUserId, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        String status = "NONE";

        // Check if the user is visiting their own profile
        if (currentUser.getId().equals(targetUser.getId())) {
            status = "SELF";
        } else {
            // Check for any friendship relation
            Optional<Friendship> sentRequest = friendshipRepository.findBySenderAndReceiver(currentUser, targetUser);
            Optional<Friendship> receivedRequest = friendshipRepository.findBySenderAndReceiver(targetUser, currentUser);

            if (sentRequest.isPresent()) {
                status = sentRequest.get().getStatus().equals("ACCEPTED") ? "FRIENDS" : "PENDING_SENT";
            } else if (receivedRequest.isPresent()) {
                status = receivedRequest.get().getStatus().equals("ACCEPTED") ? "FRIENDS" : "PENDING_RECEIVED";
            }
        }

        // Return the enriched DTO with all profile details
        return new UserProfileDto(
                targetUser.getId(),
                targetUser.getFullName(),
                targetUser.getEmail(),
                targetUser.getBio(),
                targetUser.getProfilePictureUrl(),
                targetUser.getCoverPictureUrl(),
                targetUser.getBirthDate(),
                status
        );
    }

    // Search function: returns a list of users matching the search name
    public List<UserResponseDto> searchUsersByName(String name, String currentUserEmail) {
        List<User> matchedUsers = userRepository.findTop10ByFullNameContainingIgnoreCase(name);
        return matchedUsers.stream()
                // Optional: Hide the current user from their own search results
                .filter(user -> !user.getEmail().equals(currentUserEmail))
                .map(user -> new UserResponseDto(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail()
                ))
                .collect(Collectors.toList());
    }

    // returns full page instead of normal list (for search feature).
    public Page<UserResponseDto> getFullSearchResults(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findByFullNameContainingIgnoreCase(name, pageable);

        return usersPage.map(user -> new UserResponseDto(
                user.getId(),
                user.getFullName(),
                user.getEmail()
        ));
    }
}