package com.project._5.Friend_Finder.service;

import com.project._5.Friend_Finder.dto.FriendRequestsDto;
import com.project._5.Friend_Finder.dto.UserResponseDto;
import com.project._5.Friend_Finder.entity.Friendship;
import com.project._5.Friend_Finder.entity.Notification;
import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.FriendshipRepository;
import com.project._5.Friend_Finder.repository.NotificationRepository;
import com.project._5.Friend_Finder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    /**
     * Sends a friend request from the current user to a target user.
     * Prevents self-requests and duplicate requests.
     *
     * @param senderEmail the email of the user sending the request
     * @param receiverId  the ID of the user receiving the request
     * @return Success message
     */
    public String sendFriendRequest(String senderEmail, Long receiverId) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("You cannot send a friend request to yourself!");
        }

        Optional<Friendship> existingRequest = friendshipRepository.findBySenderAndReceiver(sender, receiver);
        Optional<Friendship> reverseRequest = friendshipRepository.findBySenderAndReceiver(receiver, sender);

        if (existingRequest.isPresent() || reverseRequest.isPresent()) {
            throw new RuntimeException("Friend request already exists!");
        }

        Friendship friendship = new Friendship();
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus("PENDING");
        friendshipRepository.save(friendship);

        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setMessage(sender.getFullName() + " sent you a friend request.");
        notificationRepository.save(notification);

        return "Friend request sent successfully!";
    }

    /**
     * Cancels a pending friend request and removes the associated notification.
     *
     * @param senderEmail the email of the user who sent the request
     * @param receiverId  the ID of the receiver
     * @return Success message
     */
    public String cancelFriendRequest(String senderEmail, Long receiverId) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Friendship friendship = friendshipRepository.findBySenderAndReceiver(sender, receiver)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        friendshipRepository.delete(friendship);

        String expectedMessage = sender.getFullName() + " sent you a friend request.";
        List<Notification> notifications = notificationRepository.findByUserAndMessage(receiver, expectedMessage);
        notificationRepository.deleteAll(notifications);

        return "Friend request cancelled successfully!";
    }

    /**
     * Accepts a pending friend request and notifies the sender.
     *
     * @param requestId     the ID of the friendship request
     * @param receiverEmail the email of the user accepting the request
     * @return Success message
     */
    public String acceptFriendRequest(Long requestId, String receiverEmail) {
        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!friendship.getReceiver().getId().equals(receiver.getId())) {
            throw new RuntimeException("Unauthorized action");
        }

        friendship.setStatus("ACCEPTED");
        friendshipRepository.save(friendship);

        Notification notification = new Notification();
        notification.setUser(friendship.getSender());
        notification.setMessage(receiver.getFullName() + " accepted your friend request.");
        notificationRepository.save(notification);

        return "Friend request accepted successfully!";
    }

    /**
     * Rejects and deletes a pending friend request.
     *
     * @param requestId     the ID of the friendship request
     * @param receiverEmail the email of the user rejecting the request
     * @return Success message
     */
    public String rejectFriendRequest(Long requestId, String receiverEmail) {
        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!friendship.getReceiver().getId().equals(receiver.getId())) {
            throw new RuntimeException("Unauthorized action");
        }

        friendshipRepository.delete(friendship);

        return "Friend request rejected successfully!";
    }

    /**
     * Retrieves all pending friend requests for the logged-in user.
     *
     * @param receiverEmail the email of the current user
     * @return List of pending friend requests
     */
    public List<FriendRequestsDto> getPendingRequests(String receiverEmail) {
        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Friendship> pendingRequests = friendshipRepository.findByReceiverAndStatus(receiver, "PENDING");

        return pendingRequests.stream()
                .map(request -> new FriendRequestsDto(
                        request.getId(),
                        request.getSender().getId(),
                        request.getSender().getFullName(),
                        request.getSender().getEmail()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Removes an existing accepted friendship (Unfriend).
     * Checks both directions to locate the friendship record.
     *
     * @param currentUserEmail the email of the user initiating the unfriend action
     * @param friendId         the ID of the friend to be removed
     * @return Success message
     */
    public String unfriend(String currentUserEmail, Long friendId) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        Optional<Friendship> friendship = friendshipRepository.findBySenderAndReceiver(currentUser, friend);
        if (friendship.isEmpty()) {
            friendship = friendshipRepository.findBySenderAndReceiver(friend, currentUser);
        }

        if (friendship.isPresent() && "ACCEPTED".equals(friendship.get().getStatus())) {
            friendshipRepository.delete(friendship.get());
            return "Unfriended successfully.";
        } else {
            throw new RuntimeException("You are not friends with this user.");
        }
    }

    /**
     * Retrieves a list of accepted friends for the current user.
     *
     * @param currentUserEmail the email of the current user
     * @return List of friends
     */
    public List<UserResponseDto> getMyFriends(String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(currentUser);

        return friendships.stream()
                .map(f -> {
                    User friend = f.getSender().getId().equals(currentUser.getId()) ? f.getReceiver() : f.getSender();
                    UserResponseDto dto = new UserResponseDto(friend.getId(), friend.getFullName(), friend.getEmail());
                    dto.setProfilePictureUrl(friend.getProfilePictureUrl());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}