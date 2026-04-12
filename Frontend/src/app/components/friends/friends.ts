import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UserService } from '../../service/user'; 

@Component({
  selector: 'app-friends',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './friends.html',
  styleUrl: './friends.css'
})
export class FriendsComponent implements OnInit {
  friends: any[] = [];
  isLoading: boolean = true;
  currentUserId: number = 0;

  constructor(
    private userService: UserService, 
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.currentUserId = Number(localStorage.getItem('userId')) || 0;
    this.loadMyFriends();
  }

  loadMyFriends() {
    this.isLoading = true;
    this.userService.getMyFriends().subscribe({
      next: (res: any[]) => {
        this.friends = res;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Error fetching friends:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
}