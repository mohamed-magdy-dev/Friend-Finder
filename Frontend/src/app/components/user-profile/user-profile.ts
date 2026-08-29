import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { UserService } from '../../service/user';
import { PostService } from '../../service/post';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../service/toast';

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

  // recent activity sidebar - last 5 posts/comments/likes merged together,
  // computed on the backend, we just display whatever it sends back
  activities: any[] = [];
  isLoadingActivity: boolean = true;

  // limits used across the edit form - keep these in sync with
  // whatever the backend ends up enforcing (right now it doesn't enforce any of this!)
  readonly BIO_MAX_LENGTH = 300;
  readonly FULL_NAME_MAX_LENGTH = 60;
  private readonly MIN_AGE = 13;
  private readonly MAX_AGE = 120;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private postService: PostService,
    private cdr: ChangeDetectorRef,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    // Get the ID from the URL (e.g., /profile/5)
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        this.userId = +idParam;
        this.loadProfile();
        this.loadUserPosts();
        this.loadUserActivity();
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

  // Fetch the merged post/comment/like activity feed for the sidebar
  loadUserActivity() {
    this.isLoadingActivity = true;
    this.userService.getUserActivity(this.userId).subscribe({
      next: (res: any[]) => {
        this.activities = res || [];
        this.isLoadingActivity = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Error fetching user activity:', err);
        this.isLoadingActivity = false;
        this.cdr.detectChanges();
      }
    });
  }

  // small lookup so the template doesn't need a big @switch for icons
  private readonly activityIcons: Record<string, string> = {
    POST: 'bi-file-earmark-text-fill',
    COMMENT: 'bi-chat-dots-fill',
    LIKE: 'bi-hand-thumbs-up-fill'
  };

  getActivityIcon(type: string): string {
    return this.activityIcons[type] || 'bi-clock-history';
  }

  // Turns a full name into a slug for the @handle under the name.
  // Using a regex here (not .replace(' ', '')) so ALL spaces get removed,
  // not just the first one - "Ahmed Ali Hassan" -> "ahmedalihassan"
  get profileHandle(): string {
    if (!this.profileData?.fullName) return '';
    return this.profileData.fullName.toLowerCase().replace(/\s+/g, '');
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

  // ---- birth date bounds, used for the date input's min/max and for validation ----
  // earliest allowed date = MAX_AGE years ago (blocks "born in 1850" jokes)
  get minBirthDate(): string {
    return this.dateYearsAgo(this.MAX_AGE);
  }

  // latest allowed date = MIN_AGE years ago (blocks future dates and "0 years old")
  get maxBirthDate(): string {
    return this.dateYearsAgo(this.MIN_AGE);
  }

  private dateYearsAgo(years: number): string {
    const d = new Date();
    d.setFullYear(d.getFullYear() - years);
    return d.toISOString().split('T')[0]; // yyyy-MM-dd, matches <input type="date">
  }

  // birth date is optional, so empty is fine - only check the range once something is typed
  get isBirthDateValid(): boolean {
    if (!this.editFormData.birthDate) return true;
    const selected = this.editFormData.birthDate;
    return selected >= this.minBirthDate && selected <= this.maxBirthDate;
  }

  get isBioTooLong(): boolean {
    return (this.editFormData.bio || '').length > this.BIO_MAX_LENGTH;
  }

  submitProfileUpdate() {
    if (!this.editFormData.fullName.trim()) return;

    // same rules the min/max on the date input already enforce, just double
    // checked here in case someone gets past the input somehow
    if (!this.isBirthDateValid || this.isBioTooLong) return;

    this.isUpdatingProfile = true;
    this.cdr.detectChanges();

    this.userService.updateUserProfile(this.editFormData).subscribe({
      next: (res) => {
        this.profileData = res;
        localStorage.setItem('fullName', res.fullName);
        this.isUpdatingProfile = false;
        this.closeEditModal();
        this.cdr.detectChanges();
        this.toastService.show('Profile updated successfully!', 'success');
      },
      error: (err) => {
        console.error('Error updating profile:', err);
        this.isUpdatingProfile = false;
        this.cdr.detectChanges();
        this.toastService.show('Failed to update profile. Please try again.', 'error');
      }
    });
  }

  unfriend() {
    this.userService.unfriendUser(this.userId).subscribe({
      next: () => {
        // set the status to "NONE" so that the button becomes "Add Friend" again
        this.profileData.friendshipStatus = 'NONE';
        // If toast isworking .. show it here
        if (this.toastService) {
          this.toastService.show('Removed from friends', 'success');
        }
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error unfriending user:', err)
    });
  }
}