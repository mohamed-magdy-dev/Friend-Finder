import { Component, OnInit, ChangeDetectorRef } from '@angular/core'; 
import { CommonModule } from '@angular/common'; 
import { Router } from '@angular/router'; 
import { PostService } from '../../service/post'; 
import { FormsModule } from '@angular/forms';
import { CommentsService } from '../../service/comments';
import { UserService } from '../../service/user';

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
  suggestedUsers: any[] = [];
  constructor(
    private router: Router,
    private postService: PostService,
    private cdr: ChangeDetectorRef, // 2. حقنّا الأداة هنا عشان نستخدمها
    private commentsService: CommentsService,
    private userService: UserService
  ) {
    const storedName = localStorage.getItem('fullName');
    if (storedName) {
      this.userName = storedName;
    }
  }

  ngOnInit() {
    this.loadPosts();
    this.loadSuggestedUsers();
  }

  loadSuggestedUsers() {
    this.userService.getSuggestedUsers().subscribe({
      next: (res: any) => {
        this.suggestedUsers = res;
        this.cdr.detectChanges(); // السطر السحري عشان نعرضهم فوراً
      },
      error: (err: any) => console.error('Error fetching suggested users:', err)
    });
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

// 1️⃣ دالة اللايك (النسخة الحاسمة والمحمية)
  toggleLike(post: any) {
    // 1. نحدث الشاشة فوراً والزرار ينور ويفضل منور
    post.isLiked = !post.isLiked;
    post.likeCount = post.isLiked ? (post.likeCount || 0) + 1 : Math.max(0, (post.likeCount || 1) - 1);
    this.cdr.detectChanges();

    // 2. نبعت الطلب للباك إند في صمت
    this.postService.likePost(post.id).subscribe({
      next: (res: any) => {
        // السيرفر رد بنجاح، مش هنعدل الشاشة تاني عشان الزرار مايطفيش
        console.log('Liked successfully on backend');
      },
      error: (err: any) => {
        // لو الإيرور ده بسبب إن الباك إند رد بـ Text مش JSON، بس الستاتس 200 (نجاح)
        // يبقى مفيش مشكلة حقيقية، ومش هنلغي اللايك.
        if (err.status === 200 || err.status === 201) {
           console.log('Backend success but parse error (Ignored)');
           return; // اخرج وماتعملش حاجة
        }

        // إنما لو السيرفر ضرب إيرور حقيقي (400 أو 500)، هنا بس نلغي اللايك
        console.error('Real Error liking post:', err);
        post.isLiked = !post.isLiked;
        post.likeCount = post.isLiked ? (post.likeCount || 0) + 1 : Math.max(0, (post.likeCount || 1) - 1);
        this.cdr.detectChanges();
      }
    });
  }

  // 2️⃣ دالة إظهار التعليقات (بتفتح فوراً وتجيب الداتا في الخلفية)
  toggleComments(post: any) {
    // 1. نفتح أو نقفل المربع فوراً
    post.showComments = !post.showComments;
    this.cdr.detectChanges(); // تحديث فوري للشاشة

    // 2. لو فتحنا المربع والتعليقات لسه متحملتش، نروح نجيبها
    if (post.showComments && !post.commentsList) {
      this.commentsService.getCommentsByPostId(post.id).subscribe({
        next: (res) => {
          post.commentsList = res;
          this.cdr.detectChanges(); // تحديث الشاشة بعد وصول الداتا
        },
        error: (err) => console.error('Error fetching comments', err)
      });
    }
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