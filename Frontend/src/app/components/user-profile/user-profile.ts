import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { UserService } from '../../service/user';
import { PostService } from '../../service/post';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.css'
})
export class UserProfile implements OnInit {
  
  userId!: number;
  profileData: any = null;
  userPosts: any[] = [];
  isLoadingPosts: boolean = true;
  // cover and photo .. profile and cover variables
  isUploadingProfile: boolean = false;
  isUploadingCover: boolean = false;
  // for bio update
  isEditModalOpen: boolean = false;
    isUpdatingProfile: boolean = false;
    editFormData: any = {
      fullName: '',
      bio: '',
      birthDate: ''
    };

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private postService: PostService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    // Get the ID from the URL (e.g., /profile/5)
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        this.userId = +idParam;
        this.loadProfile();
        this.loadUserPosts();
      }
    });
  }

  // Fetch user details and friendship status
  loadProfile() {
    this.userService.getUserProfile(this.userId).subscribe({
      next: (res) => {
        this.profileData = res;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error fetching profile:', err)
    });
  }

  // Fetch only this user's posts
  loadUserPosts() {
    this.isLoadingPosts = true;
    this.postService.getUserPosts(this.userId, 0, 10).subscribe({
      next: (res: any) => {
        this.userPosts = res.content ? res.content : (Array.isArray(res) ? res : []);
        this.isLoadingPosts = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Error fetching user posts:', err);
        this.isLoadingPosts = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Handle Add Friend Action
  sendRequest() {
    this.profileData.friendshipStatus = 'PENDING_SENT';
    this.cdr.detectChanges();
    this.userService.sendFriendRequest(this.userId).subscribe({
      error: () => this.loadProfile() // Revert on error
    });
  }

  // Handle Cancel Request Action
  cancelRequest() {
    this.profileData.friendshipStatus = 'NONE';
    this.cdr.detectChanges();
    this.userService.cancelFriendRequest(this.userId).subscribe({
      error: () => this.loadProfile() // Revert on error
    });
  }

  // Basic like toggle for the profile feed
  toggleLike(post: any) {
    post.isLiked = !post.isLiked;
    post.likeCount = post.isLiked ? (post.likeCount || 0) + 1 : Math.max(0, (post.likeCount || 1) - 1);
    this.cdr.detectChanges();

    this.postService.likePost(post.id).subscribe({
      error: () => {
        // Revert UI if backend fails
        post.isLiked = !post.isLiked;
        post.likeCount = post.isLiked ? (post.likeCount || 0) + 1 : Math.max(0, (post.likeCount || 1) - 1);
        this.cdr.detectChanges();
      }
    });
  }

  onCoverSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.isUploadingCover = true;
      this.cdr.detectChanges();
      
      this.userService.uploadCoverPicture(file).subscribe({
        next: (url: string) => {
          this.profileData.coverPictureUrl = url;
          this.isUploadingCover = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error uploading cover:', err);
          this.isUploadingCover = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  // Triggered when user selects a profile picture
  onProfilePicSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.isUploadingProfile = true;
      this.cdr.detectChanges();

      this.userService.uploadProfilePicture(file).subscribe({
        next: (url: string) => {
          this.profileData.profilePictureUrl = url;
          this.isUploadingProfile = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error uploading profile pic:', err);
          this.isUploadingProfile = false;
          this.cdr.detectChanges();
        }
      });
    }
  }


  // ===================== EDIT PROFILE METHODS =====================
  openEditModal() {
    this.editFormData = {
      fullName: this.profileData.fullName,
      bio: this.profileData.bio || '',
      birthDate: this.profileData.birthDate || ''
    };
    this.isEditModalOpen = true;
  }

  closeEditModal() {
    this.isEditModalOpen = false;
  }

  submitProfileUpdate() {
    if (!this.editFormData.fullName.trim()) return; // the name should not be empty

    this.isUpdatingProfile = true;
    this.userService.updateUserProfile(this.editFormData).subscribe({
      next: (res) => {
        this.profileData = res; // update info 
        localStorage.setItem('fullName', res.fullName); // update the name 
        this.isUpdatingProfile = false;
        this.closeEditModal();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error updating profile:', err);
        this.isUpdatingProfile = false;
        this.cdr.detectChanges();
      }
    });
  }
}