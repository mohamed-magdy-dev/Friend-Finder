import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { UserService } from '../../service/user';
import { PostService } from '../../service/post';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.css'
})
export class UserProfile implements OnInit {
  
  userId!: number;
  profileData: any = null;
  userPosts: any[] = [];
  isLoadingPosts: boolean = true;

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
}