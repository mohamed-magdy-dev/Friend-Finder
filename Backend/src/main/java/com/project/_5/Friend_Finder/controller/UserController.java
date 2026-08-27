package com.project._5.Friend_Finder.controller;

import com.project._5.Friend_Finder.dto.UserProfileDto;
import com.project._5.Friend_Finder.dto.UserResponseDto;
import com.project._5.Friend_Finder.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // عشان الفرونت ميضربش CORS
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @GetMapping("/suggestions")
    public ResponseEntity<List<UserResponseDto>> getSuggestedUsers(Principal principal) {
        // getting the email of the current user ( the one who logged in)
        // principal.getName()
        List<UserResponseDto> suggestions = userService.getSuggestedUsers(principal.getName());
        return ResponseEntity.ok(suggestions);
    }

                           // user profile
    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable Long userId, Principal principal) {
        UserProfileDto profile = userService.getUserProfile(userId, principal.getName());
        return ResponseEntity.ok(profile);
    }

    // Search endpoint
    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDto>> searchUsers(@RequestParam String name, Principal principal) {
        List<UserResponseDto> results = userService.searchUsersByName(name, principal.getName());
        return ResponseEntity.ok(results);
    }

    // Endpoint for the full search results page
    @GetMapping("/search/full")
    public ResponseEntity<Page<UserResponseDto>> getFullSearchResults(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserResponseDto> results = userService.getFullSearchResults(name, page, size);
        return ResponseEntity.ok(results);
    }

    // Endpoint to upload a profile picture
    @PostMapping("/profile-picture")
    public ResponseEntity<String> uploadProfilePicture(@RequestParam("file") MultipartFile file, Principal principal) {

        String fileUrl = userService.uploadProfilePicture(file, principal.getName());
        return ResponseEntity.ok(fileUrl);
    }

    // -----------
    // Endpoint to upload a cover banner----------
    @PostMapping("/cover-picture")
    public ResponseEntity<String> uploadCoverPicture(@RequestParam("file") MultipartFile file, Principal principal) {
        String fileUrl = userService.uploadCoverPicture(file, principal.getName());
        return ResponseEntity.ok(fileUrl);
    }

    // Endpoint to serve (display) the images to the frontend
    @GetMapping("/images/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {
        try {
            Path imagePath = Paths.get("uploads/images/").resolve(imageName);
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                // Try to determine the content type dynamically (e.g., image/png, image/jpeg)
                String contentType = Files.probeContentType(imagePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===================== UPDATE PROFILE ENDPOINT =====================
    @PutMapping("/profile/update")
    public ResponseEntity<UserProfileDto> updateProfile(@Valid @RequestBody com.project._5.Friend_Finder.dto.ProfileUpdateRequestDto request, Principal principal) {
        UserProfileDto updatedProfile = userService.updateUserProfile(principal.getName(), request);
        return ResponseEntity.ok(updatedProfile);
    }
}