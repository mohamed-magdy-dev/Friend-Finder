import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { Router } from '@angular/router'; 
// import { HttpClientModule } from '@angular/common/http';
// import { UserService } from '../../service/user';
import { PostService } from '../../service/post';
@Component({
  selector: 'app-home', 
  standalone: true,     
  imports: [CommonModule], 
  templateUrl: './home.html', 
  styleUrl: './home.css'     
})
export class Home implements OnInit { 
  
  userName: string = 'Friend'; 
  posts: any[] = []; // 🌟 دي المصفوفة اللي هتشيل البوستات
  isLoading: boolean = true; // عشان رسالة التحميل
  errorMessage: string = ''; 

  constructor(
    private router: Router,
    private postService: PostService // 🌟 حقن السيرفس هنا
  ) {
    const storedName = localStorage.getItem('fullName');
    if (storedName) {
      this.userName = storedName;
    }
  }

  ngOnInit() {
    this.loadPosts();
  }

  loadPosts() {
    this.isLoading = true;
    this.postService.getAllPosts(0, 10).subscribe({
      next: (res: any) => {
        // 🌟 التريكة هنا: البوستات بتيجي من سبرينج جوه حاجة اسمها content
        this.posts = res.content; 
        this.isLoading = false;
        console.log('Posts loaded:', this.posts);
      },
      error: (err: any) => {
        console.error('Error fetching posts:', err);
        this.errorMessage = 'Failed to load posts.';
        this.isLoading = false;
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