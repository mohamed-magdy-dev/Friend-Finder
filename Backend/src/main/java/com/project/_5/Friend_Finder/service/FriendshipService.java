package com.project._5.Friend_Finder.service;

import com.project._5.Friend_Finder.dto.FriendRequestsDto;
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

    // ok what if the user sent friend request by accident?
    // we will have to make another method to recover that disaster

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
     * Accepts a pending friend request and creates a notification for the sender.
     */
    public String acceptFriendRequest(Long requestId, String receiverEmail) {
        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        // Security check: Ensure the current user is the actual receiver of the request
        if (!friendship.getReceiver().getId().equals(receiver.getId())) {
            throw new RuntimeException("Unauthorized action");
        }

        // Update status to accepted
        friendship.setStatus("ACCEPTED");
        friendshipRepository.save(friendship);

        // Notify the sender that their request was accepted
        Notification notification = new Notification();
        notification.setUser(friendship.getSender());
        notification.setMessage(receiver.getFullName() + " accepted your friend request.");
        notificationRepository.save(notification);

        return "Friend request accepted successfully!";
    }

    /**
     * Rejects (deletes) a pending friend request.
     */
    public String rejectFriendRequest(Long requestId, String receiverEmail) {
        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        // Security check: Ensure the current user is the actual receiver
        if (!friendship.getReceiver().getId().equals(receiver.getId())) {
            throw new RuntimeException("Unauthorized action");
        }

        // Delete the request entirely
        friendshipRepository.delete(friendship);

        return "Friend request rejected successfully!";
    }

    /**
     * Retrieves a list of pending friend requests for the current logged-in user.
     */
    public List<FriendRequestsDto> getPendingRequests(String receiverEmail) {
        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch pending requests from the database
        List<Friendship> pendingRequests = friendshipRepository.findByReceiverAndStatus(receiver, "PENDING");

        // Map the entities to DTOs
        return pendingRequests.stream()
                .map(request -> new FriendRequestsDto(
                        request.getId(),
                        request.getSender().getId(),
                        request.getSender().getFullName(),
                        request.getSender().getEmail()
                ))
                .collect(Collectors.toList());
    }
}
