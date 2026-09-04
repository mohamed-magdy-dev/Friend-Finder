import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PostService } from '../../service/post';
import { FormsModule } from '@angular/forms';
import { CommentsService } from '../../service/comments';
import { UserService } from '../../service/user';
import { NotificationService } from '../../service/notification';
import { ToastService } from '../../service/toast';
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

  // counters
  postsCount: number = 0;
  friendsCount: number = 0;

  // pagination for the feed - lets us append older posts instead of
  // replacing the whole list, so posts from other users don't get pushed out
  currentPage: number = 0;
  hasMorePosts: boolean = true;
  isLoadingMore: boolean = false;

  // notifications
  notifications: any[] = [];
  unreadNotificationsCount: number = 0;
  isNotificationOpen: boolean = false;
  isFriendRequestOpen: boolean = false;

  // search bar
  searchQuery: string = '';
  searchResults: any[] = [];
  isSearchDropdownOpen: boolean = false;

  // profile dropdown
  isProfileMenuOpen: boolean = false;
  currentUserId: number = 0;

  // profile picture
  currentUserPic: string | null = null;

  // delete-post confirmation modal
  postToDelete: any = null;
  isDeleting: boolean = false;

  // delete-comment confirmation modal - holds {post, comment} together
  // since deleting a comment needs to know which post's commentsList to update
  commentToDelete: { post: any; comment: any } | null = null;

  constructor(
    private router: Router,
    private postService: PostService,
    private cdr: ChangeDetectorRef,
    private commentsService: CommentsService,
    private userService: UserService,
    private notificationService: NotificationService,
    private toastService: ToastService,
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
    this.isFriendRequestOpen = false; // close the other dropdown
  }

  toggleFriendRequests() {
    this.isFriendRequestOpen = !this.isFriendRequestOpen;
    this.isNotificationOpen = false; // close the other dropdown
  }

  loadSuggestedUsers() {
    this.userService.getSuggestedUsers().subscribe({
      next: (res: any[]) => {
        // map backend property 'requestSent' to frontend property 'isRequestSent'
        this.suggestedUsers = res.map(user => {
          user.isRequestSent = user.requestSent || false;
          return user;
        });
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error('Error fetching suggested users:', err)
    });
  }

  // profile picture + stats for the sidebar card
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
    });
  }

  toggleLike(post: any) {
    // optimistic UI update first, then sync with backend
    post.isLiked = !post.isLiked;
    post.likeCount = post.isLiked ? (post.likeCount || 0) + 1 : Math.max(0, (post.likeCount || 1) - 1);
    this.cdr.detectChanges();

    this.postService.likePost(post.id).subscribe({
      next: (res: any) => {
        console.log('Liked successfully on backend');
      },
      error: (err: any) => {
        if (err.status === 200 || err.status === 201) {
          console.log('Backend success but parse error (ignored)');
          return;
        }

        console.error('Real error liking post:', err);
        // revert on real failure
        post.isLiked = !post.isLiked;
        post.likeCount = post.isLiked ? (post.likeCount || 0) + 1 : Math.max(0, (post.likeCount || 1) - 1);
        this.cdr.detectChanges();
      }
    });
  }

  // Shows/hides the comment thread under a post. Comments are fetched
  // once (lazily) the first time it's opened, then cached on the post
  // object itself so re-opening doesn't re-fetch.
  toggleComments(post: any) {
    post.showComments = !post.showComments;

    if (post.showComments && !post.commentsList) {
      post.commentsList = [];

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

  // loads the first page of the feed (page 0) - called on init and refresh
  loadPosts() {
    this.isLoading = true;
    this.currentPage = 0;

    this.postService.getAllPosts(0, 10).subscribe({
      next: (res: any) => {
        this.posts = res.content ? res.content : (Array.isArray(res) ? res : []);
        // Spring's Page object tells us if this was the last page or not
        // this.hasMorePosts = res.last === false;

        const totalPages = res.page?.totalPages ?? res.totalPages ?? 1;
        this.hasMorePosts = totalPages > 1;
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

  // fetches the next page and appends it to the existing list instead of
  // replacing it - this is what keeps older posts from other users visible
  // instead of getting pushed out when someone posts a lot at once
  loadMorePosts() {
    if (this.isLoadingMore || !this.hasMorePosts) return;

    this.isLoadingMore = true;
    const nextPage = this.currentPage + 1;

    this.postService.getAllPosts(nextPage, 10).subscribe({
      next: (res: any) => {
        const newPosts = res.content ? res.content : [];
        this.posts = [...this.posts, ...newPosts];
        this.currentPage = nextPage;
        // this.hasMorePosts = res.last === false;
        // load more feature
        const totalPages = res.page?.totalPages ?? res.totalPages ?? 1;
        this.hasMorePosts = nextPage + 1 < totalPages;
        this.isLoadingMore = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Error loading more posts:', err);
        this.isLoadingMore = false;
        this.cdr.detectChanges();
      }
    });
  }

  submitComment(post: any) {
    if (!post.newCommentText?.trim()) return;

    this.commentsService.addComment(post.id, post.newCommentText).subscribe({
      next: (res: any) => {
        if (!post.commentsList) post.commentsList = [];
        post.commentsList.push(res);
        post.commentsCount = (post.commentsCount || 0) + 1;
        post.newCommentText = '';
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error adding comment', err)
    });
  }

  // Comments don't carry an authorId, only authorName - but fullName is
  // guaranteed unique at registration (AuthService checks for that), so
  // comparing names here is safe. The real check still happens server-side.
  isOwnComment(comment: any): boolean {
    return comment.authorName === localStorage.getItem('fullName');
  }

  // clicking the trash icon just opens the confirm modal - the actual
  // delete happens in confirmDeleteComment() below
  deleteComment(post: any, comment: any) {
    this.commentToDelete = { post, comment };
  }

  cancelDeleteComment() {
    this.commentToDelete = null;
  }

  confirmDeleteComment() {
    if (!this.commentToDelete) return;
    const { post, comment } = this.commentToDelete;

    this.commentsService.deleteComment(comment.id).subscribe({
      next: () => {
        post.commentsList = post.commentsList.filter((c: any) => c.id !== comment.id);
        post.commentsCount = Math.max(0, (post.commentsCount || 1) - 1);
        this.commentToDelete = null;
        this.cdr.detectChanges(); // this makes the comment disappear without a manual refresh
      },
      error: (err: any) => {
        console.error('Error deleting comment', err);
        this.commentToDelete = null;
        this.cdr.detectChanges();
      }
    });
  }

  // Inline edit for a comment - "editing" state lives on the comment object
  // itself (comment.isEditing / comment.editText), so multiple comments on
  // the same post can each be edited independently without extra bookkeeping.
  startEditComment(comment: any) {
    comment.isEditing = true;
    comment.editText = comment.content;
  }

  cancelEditComment(comment: any) {
    comment.isEditing = false;
    comment.editText = '';
  }

  saveEditComment(comment: any) {
    if (!comment.editText?.trim()) return;

    this.commentsService.updateComment(comment.id, comment.editText).subscribe({
      next: (updated: any) => {
        comment.content = updated.content;
        comment.isEditing = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error('Error updating comment', err)
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

  // loads all pending friend requests from the backend
  loadPendingRequests() {
    this.userService.getPendingFriendRequests().subscribe({
      next: (res: any[]) => {
        this.pendingRequests = res;
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error('Error fetching pending requests:', err)
    });
  }

  // accepts a friend request and removes it from the UI instantly
  acceptRequest(requestId: number) {
    // optimistic UI update: remove the request from the array immediately
    this.pendingRequests = this.pendingRequests.filter(req => req.requestId !== requestId);
    this.cdr.detectChanges();

    this.userService.acceptFriendRequest(requestId).subscribe({
      next: () => console.log('Friend request accepted successfully'),
      error: (err: any) => {
        console.error('Error accepting friend request:', err);
        // reload the list to restore the removed request if it failed
        this.loadPendingRequests();
      }
    });
  }

  // rejects a friend request and removes it from the UI instantly
  rejectRequest(requestId: number) {
    this.pendingRequests = this.pendingRequests.filter(req => req.requestId !== requestId);
    this.cdr.detectChanges();

    this.userService.rejectFriendRequest(requestId).subscribe({
      next: () => console.log('Friend request rejected successfully'),
      error: (err: any) => {
        console.error('Error rejecting friend request:', err);
        this.loadPendingRequests();
      }
    });
  }

  // loads notifications and calculates the unread count
  loadNotifications() {
    this.notificationService.getNotifications().subscribe({
      next: (res: any[]) => {
        this.notifications = res;
        this.unreadNotificationsCount = this.notifications.filter(n => !n.read).length;
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error('Error fetching notifications:', err)
    });
  }

  // marks a notification as read and updates the UI instantly
  markNotificationAsRead(notification: any) {
    if (notification.read) return; // already read, nothing to do

    notification.read = true;
    this.unreadNotificationsCount = Math.max(0, this.unreadNotificationsCount - 1);
    this.cdr.detectChanges();

    this.notificationService.markAsRead(notification.id).subscribe({
      error: (err: any) => console.error('Error marking notification as read:', err)
    });
  }

  // Called when the user clicks a notification in the dropdown.
  // If the notification is about a like/comment on a post, we scroll the
  // feed down to that post and give it a quick highlight so it's easy to spot.
  // Friend-request notifications don't have a postId, so we just mark them read.
  openNotification(notif: any) {
    this.markNotificationAsRead(notif);
    this.isNotificationOpen = false;

    if (!notif.postId) return;

    // small delay so the dropdown has time to close before we scroll
    setTimeout(() => {
      const el = document.getElementById('post-' + notif.postId);
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
        el.classList.add('ff-highlight');
        setTimeout(() => el.classList.remove('ff-highlight'), 2000);
      } else {
        // the post exists but isn't loaded in the current feed page (pagination)
        this.toastService.show('This post is not visible in your current feed.', 'error');
      }
    }, 100);
  }

  // triggered when user types in the search bar
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

  // triggered when the user presses Enter in the search bar
  onSearchEnter() {
    if (this.searchQuery.trim()) {
      this.isSearchDropdownOpen = false;
      this.router.navigate(['/search', this.searchQuery.trim()]);
    }
  }

  // closes the dropdown (used when clicking outside or losing focus)
  closeSearch() {
    // timeout allows the click event on the link to fire before hiding the dropdown
    setTimeout(() => {
      this.isSearchDropdownOpen = false;
      this.cdr.detectChanges();
    }, 200);
  }

  // ===================== DELETE POST =====================
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