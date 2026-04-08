import { Component, OnInit, ChangeDetectorRef } from '@angular/core'; 
import { CommonModule } from '@angular/common'; 
import { Router } from '@angular/router'; 
import { PostService } from '../../service/post'; 
import { FormsModule } from '@angular/forms';
import { CommentsService } from '../../service/comments';
import { UserService } from '../../service/user';
import { NotificationService } from '../../service/notification';
import { RouterLink } from '@angular/router';
@Component({
  selector: 'app-home', 
  standalone: true,     
  imports: [CommonModule, FormsModule, RouterLink], 
  templateUrl: './home.html', 
  styleUrl: './home.css'     
})
export class Home implements OnInit { 
  
  userName: string = 'Friend'; 
  posts: any[] = []; 
  isLoading: boolean = true; 
  errorMessage: string = ''; 
  newPostContent: string = ''; 
  isPosting: boolean = false;
  suggestedUsers: any[] = [];
  pendingRequests: any[] = [];
  //counters :
  postsCount: number = 0;
  friendsCount: number = 0;
  // notification:
  notifications: any[] = [];
  unreadNotificationsCount: number = 0;
 isNotificationOpen: boolean = false;
  isFriendRequestOpen: boolean = false; // for angular vs bootstrap thing
  // Search variables
  searchQuery: string = '';
  searchResults: any[] = [];
  isSearchDropdownOpen: boolean = false;
  // profile dropdown thingy
  isProfileMenuOpen: boolean = false;
currentUserId: number = 0;
// for profile picture
currentUserPic: string | null = null;
  constructor(
    private router: Router,
    private postService: PostService,
    private cdr: ChangeDetectorRef, 
    private commentsService: CommentsService,
    private userService: UserService,
    private notificationService: NotificationService,
    
    
  ) {
    const storedName = localStorage.getItem('fullName');
    if (storedName) {
      this.userName = storedName;
    }
  }

  ngOnInit() {
    this.currentUserId = Number(localStorage.getItem('userId')) || 0;
    this.loadCurrentUserProfile();
    this.loadPosts();
    this.loadSuggestedUsers();
    this.loadPendingRequests();
    this.loadNotifications();
    
  }
toggleNotifications() {
    this.isNotificationOpen = !this.isNotificationOpen;
    this.isFriendRequestOpen = false; // Close the other
  }
  toggleFriendRequests() {
    this.isFriendRequestOpen = !this.isFriendRequestOpen;
    this.isNotificationOpen = false; // Close the other
  }
 loadSuggestedUsers() {
    this.userService.getSuggestedUsers().subscribe({
      next: (res: any[]) => {
        // Map the backend property 'requestSent' to our frontend property 'isRequestSent'
        this.suggestedUsers = res.map(user => {
          user.isRequestSent = user.requestSent || false; 
          return user;
        });
        
        this.cdr.detectChanges(); 
      },
      error: (err: any) => console.error('Error fetching suggested users:', err)
    });
  }

  // profile img 
  loadCurrentUserProfile() {
    if (this.currentUserId > 0) {
      this.userService.getUserProfile(this.currentUserId).subscribe({
        next: (res: any) => {
          this.currentUserPic = res.profilePictureUrl;

              this.postsCount = res.postsCount || 0;
              this.friendsCount = res.friendsCount || 0;

          this.cdr.detectChanges();
        },
        error: (err: any) => console.error('Error fetching current user profile:', err)
      });
    }
  }

createPost() {
    if (!this.newPostContent.trim()) return; 

    this.isPosting = true; 

    const request = {
      content: this.newPostContent,
      mediaType: 'TEXT' 
    };

    this.postService.createPost(request).subscribe({
      next: (res: any) => {
        this.posts.unshift(res); 
        
        this.newPostContent = ''; 
        this.isPosting = false;
        
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Error creating post:', err);
        this.isPosting = false;
        this.cdr.detectChanges();
      }
    });}

  toggleLike(post: any) {
    post.isLiked = !post.isLiked;
    post.likeCount = post.isLiked ? (post.likeCount || 0) + 1 : Math.max(0, (post.likeCount || 1) - 1);
    this.cdr.detectChanges();

    this.postService.likePost(post.id).subscribe({
      next: (res: any) => {
        console.log('Liked successfully on backend');
      },
      error: (err: any) => { 
        if (err.status === 200 || err.status === 201) {
           console.log('Backend success but parse error (Ignored)');
           return; 
        }

        console.error('Real Error liking post:', err);
        post.isLiked = !post.isLiked;
        post.likeCount = post.isLiked ? (post.likeCount || 0) + 1 : Math.max(0, (post.likeCount || 1) - 1);
        this.cdr.detectChanges();
      }
    });
  }

 
  toggleComments(post: any) {
    post.showComments = !post.showComments;

    if (post.showComments && !post.commentsList) {
      post.commentsList = []; 

      // التعديل هنا: استخدمنا commentsService
      // (ملاحظة: لو الدالة عندك في السيرفس مسمهاش getComments، غيرها للاسم الصح بتاعك)
      this.commentsService.getCommentsByPostId(post.id).subscribe({
        next: (comments: any) => {
          post.commentsList = comments;
          this.cdr.detectChanges(); 
        },
        error: (err: any) => {
          console.error('Error fetching comments:', err);
        }
      });
    } else {
      this.cdr.detectChanges();
    }
  }

  loadPosts() {
    this.isLoading = true;
    this.postService.getAllPosts(0, 10).subscribe({
      next: (res: any) => {
        this.posts = res.content ? res.content : (Array.isArray(res) ? res : []); 
        this.isLoading = false; 
        
        this.cdr.detectChanges(); 
      },
      error: (err: any) => {
        console.error('Error fetching posts:', err);
        this.errorMessage = 'Failed to load posts. Check console.';
        this.isLoading = false;
        
        
        this.cdr.detectChanges(); 

        if (err.status === 403) {
          this.logout();
        }
      }
    });
  }

 
  
  submitComment(post: any) {
    if (!post.newCommentText?.trim()) return; 

    this.commentsService.addComment(post.id, post.newCommentText).subscribe({
      next: (res) => {
        if (!post.commentsList) post.commentsList = [];
        post.commentsList.push(res); 
        post.newCommentText = ''; 
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error adding comment', err)
    });
  }
  
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('fullName');
    this.router.navigate(['/login']);
  }


  toggleFriendRequest(user: any) {
    if (user.isRequestSent) {
      
      user.isRequestSent = false; 
      this.cdr.detectChanges();
      
      this.userService.cancelFriendRequest(user.id).subscribe({
        next: () => console.log('Request cancelled successfully'),
        error: (err: any) => {
          console.error('Error cancelling request', err);
          user.isRequestSent = true; 
          this.cdr.detectChanges();
        }
      });
      
    } else {
      user.isRequestSent = true; 
      this.cdr.detectChanges();

      this.userService.sendFriendRequest(user.id).subscribe({
        next: () => console.log('Request sent successfully'),
        error: (err: any) => {
          console.error('Error sending request:', err);
          user.isRequestSent = false; 
          this.cdr.detectChanges();
        }
      });
    }
  }


  
   // Loads all pending friend requests from the backend.
   
  loadPendingRequests() {
    this.userService.getPendingFriendRequests().subscribe({
      next: (res: any[]) => {
        this.pendingRequests = res;
        this.cdr.detectChanges(); // Update the UI
      },
      error: (err: any) => console.error('Error fetching pending requests:', err)
    });
  }


   // Accepts a friend request and removes it from the UI instantly.
   
  acceptRequest(requestId: number) {
    // Optimistic UI update: Remove the request from the array immediately
    this.pendingRequests = this.pendingRequests.filter(req => req.requestId !== requestId);
    this.cdr.detectChanges();

    // Send the actual request to the backend
    this.userService.acceptFriendRequest(requestId).subscribe({
      next: () => console.log('Friend request accepted successfully'),
      error: (err: any) => {
        console.error('Error accepting friend request:', err);
        // If it fails, reload the list to restore the removed request
        this.loadPendingRequests();
      }
    });
  }

 
   // Rejects a friend request and removes it from the UI instantly.
   
  rejectRequest(requestId: number) {
    // Optimistic UI update: Remove the request from the array immediately
    this.pendingRequests = this.pendingRequests.filter(req => req.requestId !== requestId);
    this.cdr.detectChanges();

    // Send the actual request to the backend
    this.userService.rejectFriendRequest(requestId).subscribe({
      next: () => console.log('Friend request rejected successfully'),
      error: (err: any) => {
        console.error('Error rejecting friend request:', err);
        // If it fails, reload the list to restore the removed request
        this.loadPendingRequests();
      }
    });
  }

 
    //Loads notifications and calculates the unread count:
 
  loadNotifications() {
    this.notificationService.getNotifications().subscribe({
      next: (res: any[]) => {
        this.notifications = res;
        // Count how many notifications have isRead === false
        this.unreadNotificationsCount = this.notifications.filter(n => !n.read).length;
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error('Error fetching notifications:', err)
    });
  }

  
    // Marks a notification as read and updates the UI instantly.
  
  markNotificationAsRead(notification: any) {
    if (notification.read) return; // Already read, do nothing

    // Optimistic UI update
    notification.read = true;
    this.unreadNotificationsCount = Math.max(0, this.unreadNotificationsCount - 1);
    this.cdr.detectChanges();

    // Send update to backend
    this.notificationService.markAsRead(notification.id).subscribe({
      error: (err: any) => console.error('Error marking notification as read:', err)
    });
  }

  // Triggered when user types in the search bar
  onSearch() {
    if (!this.searchQuery.trim()) {
      this.searchResults = [];
      this.isSearchDropdownOpen = false;
      return;
    }
    this.userService.searchUsers(this.searchQuery).subscribe({
      next: (res: any[]) => {
        this.searchResults = res;
        this.isSearchDropdownOpen = true;
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error('Error searching users:', err)
    });
  }
    // Triggered when the user presses Enter in the search bar
       onSearchEnter() {
        if (this.searchQuery.trim()) {
        this.isSearchDropdownOpen = false;
        this.router.navigate(['/search', this.searchQuery.trim()]);
      }
    }   
  // Closes the dropdown (used when clicking outside or losing focus)
  closeSearch() {
    // Timeout allows the click event on the link to fire before hiding the dropdown
    setTimeout(() => {
      this.isSearchDropdownOpen = false;
      this.cdr.detectChanges();
    }, 200);
  }

  postToDelete: any = null;
  isDeleting: boolean = false;

  
  openDeleteModal(post: any) {
    this.postToDelete = post;
  }

  
  closeDeleteModal() {
    this.postToDelete = null;
  }

 
  confirmDelete() {
    if (!this.postToDelete) return;

    this.isDeleting = true; 

    this.postService.deletePost(this.postToDelete.id).subscribe({
      next: () => {
        
        this.posts = this.posts.filter(p => p.id !== this.postToDelete.id);
        
        
        this.isDeleting = false;
        this.postToDelete = null;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Error deleting post:', err);
        this.isDeleting = false;
        this.cdr.detectChanges();
      }
    });
  }
}