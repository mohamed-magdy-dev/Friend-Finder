import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { Router } from '@angular/router'; 
import { HttpClientModule } from '@angular/common/http';
import { UserService } from '../../service/user';

@Component({
  selector: 'app-home', 
  standalone: true,     
  imports: [CommonModule, HttpClientModule], 
  templateUrl: './home.html', 
  styleUrl: './home.css'     
})
export class Home implements OnInit { 
  
  userName: string = 'Friend'; 
  users: any[] = []; // دي المصفوفة اللي هنخزن فيها الأصدقاء
  errorMessage: string = ''; // عشان لو فيه خطأ

  constructor(
    private router: Router,
    private userService: UserService // حقن السيرفس هنا
  ) {
    const storedName = localStorage.getItem('fullName');
    if (storedName) {
      this.userName = storedName;
    }
  }

  ngOnInit() {
    this.loadUsers(); // whenever page loades .. get the users first!
  }

  // users method: (the one that brings the users)
  loadUsers() {
    this.userService.getAllUsers().subscribe({
      next: (data: any) => {
        this.users = data; 
        console.log('Users loaded:', data);
      },
      error: (err: any) => {
        console.error('Error fetching users:', err);
        this.errorMessage = 'Failed to load users.';
        // if token expires or any problems --> kick 
        if (err.status === 403) {
          this.logout();
        }
      }
    });
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('fullName');
    this.router.navigate(['/login']);
  }
}