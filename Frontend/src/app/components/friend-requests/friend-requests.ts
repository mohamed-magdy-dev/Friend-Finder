import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UserService } from '../../service/user';

@Component({
  selector: 'app-friend-requests',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './friend-requests.html',
  styleUrl: './friend-requests.css'
})
export class FriendRequests implements OnInit {

  pendingRequests: any[] = [];

  constructor(
    private userService: UserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadPendingRequests();
  }

  
   // Fetches pending friend requests from the server.
   
  loadPendingRequests() {
    this.userService.getPendingFriendRequests().subscribe({
      next: (res: any[]) => {
        this.pendingRequests = res;
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error('Error fetching pending requests:', err)
    });
  }

  
   // Accepts a request and removes it from the UI.
  acceptRequest(requestId: number) {
    this.pendingRequests = this.pendingRequests.filter(req => req.requestId !== requestId);
    this.cdr.detectChanges();

    this.userService.acceptFriendRequest(requestId).subscribe({
      error: () => this.loadPendingRequests() // Revert on failure
    });
  }

  
    // Rejects a request and removes it from the UI.
   
  rejectRequest(requestId: number) {
    this.pendingRequests = this.pendingRequests.filter(req => req.requestId !== requestId);
    this.cdr.detectChanges();

    this.userService.rejectFriendRequest(requestId).subscribe({
      error: () => this.loadPendingRequests() // Revert on failure
    });
  }
}