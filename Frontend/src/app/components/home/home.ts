import { Component, OnInit, ChangeDetectorRef } from '@angular/core'; 
import { CommonModule } from '@angular/common'; 
import { Router } from '@angular/router'; 
import { PostService } from '../../service/post'; 
import { FormsModule } from '@angular/forms';
import { CommentsService } from '../../service/comments';

@Component({
  selector: 'app-home', 
  standalone: true,     
  imports: [CommonModule, FormsModule], 
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
  constructor(
    private router: Router,
    private postService: PostService,
    private cdr: ChangeDetectorRef, // 2. حقنّا الأداة هنا عشان نستخدمها
    private commentsService: CommentsService
  ) {
    const storedName = localStorage.getItem('fullName');
    if (storedName) {
      this.userName = storedName;
    }
  }

  ngOnInit() {
    this.loadPosts();
  }


createPost() {
    // لو المربع فاضي، متعملش حاجة
    if (!this.newPostContent.trim()) return; 

    this.isPosting = true; // عشان نقفل الزرار واليوزر ميكررش الطلب

    const request = {
      content: this.newPostContent,
      mediaType: 'TEXT' // مؤقتاً لحد ما نعمل رفع الصور
    };

    this.postService.createPost(request).subscribe({
      next: (res: any) => {
        // السطر ده سحري: بيحط البوست الجديد في "أول" المصفوفة عشان يظهر فوق خالص
        this.posts.unshift(res); 
        
        this.newPostContent = ''; // بنفضي المربع تاني
        this.isPosting = false;
        
        this.cdr.detectChanges(); // بنصحي الحارس عشان يحدّث الشاشة
      },
      error: (err: any) => {
        console.error('Error creating post:', err);
        this.isPosting = false;
        this.cdr.detectChanges();
      }
    });}

    toggleLike(post: any) {
    this.postService.toggleLike(post.id).subscribe({
      next: (res: any) => {
        // التريكة هنا: إحنا بنغير حالة البوست في الفرونت إند فوراً عشان اليوزر يحس بسرعة الموقع
        if (res.message === 'Liked') {
          post.isLiked = true; // بنعلم إنه معموله لايك
          post.likeCount = (post.likeCount || 0) + 1;
        } else if (res.message === 'Unliked') {
          post.isLiked = false; // بنشيل العلامة
          post.likeCount = Math.max(0, (post.likeCount || 1) - 1);
        }
        
        // بنصحي الحارس عشان يغير لون الزرار في الـ HTML
        this.cdr.detectChanges(); 
      },
      error: (err: any) => {
        console.error('Error toggling like:', err);
      }
    });
  }

  loadPosts() {
    this.isLoading = true;
    this.postService.getAllPosts(0, 10).subscribe({
      next: (res: any) => {
        this.posts = res.content ? res.content : (Array.isArray(res) ? res : []); 
        this.isLoading = false; 
        
        // 3. السطر السحري: "يا أنجولار، أنا غيرت الداتا، حدث الـ HTML فوراً دلوقتي!"
        this.cdr.detectChanges(); 
      },
      error: (err: any) => {
        console.error('Error fetching posts:', err);
        this.errorMessage = 'Failed to load posts. Check console.';
        this.isLoading = false;
        
        // إجبار التحديث حتى لو في حالة الإيرور
        this.cdr.detectChanges(); 

        if (err.status === 403) {
          this.logout();
        }
      }
    });
  }

  // 1️⃣ إظهار وإخفاء مربع التعليقات
  toggleComments(post: any) {
    post.showComments = !post.showComments; // بنعكس الحالة (فتح/قفل)
    
    // لو فتحنا الكومنتات، ومفيش كومنتات متحملة قبل كده، بنروح نجيبها من الباك إند
    if (post.showComments && !post.commentsList) {
      this.commentsService.getCommentsByPostId(post.id).subscribe({
        next: (res) => {
          post.commentsList = res; // بنحفظ الكومنتات جوه البوست نفسه
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Error fetching comments', err)
      });
    }
  }

  // 2️⃣ إرسال تعليق جديد
  submitComment(post: any) {
    if (!post.newCommentText?.trim()) return; // لو المربع فاضي متعملش حاجة

    this.commentsService.addComment(post.id, post.newCommentText).subscribe({
      next: (res) => {
        if (!post.commentsList) post.commentsList = [];
        post.commentsList.push(res); // بنضيف الكومنت الجديد للستة عشان يظهر فوراً
        post.newCommentText = ''; // بنفضي مربع الكتابة
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
}