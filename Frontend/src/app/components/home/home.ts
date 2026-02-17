import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { Router } from '@angular/router'; 
@Component({
  selector: 'app-home', 
  standalone: true,     
  imports: [CommonModule], 
  templateUrl: './home.html', 
  styleUrl: './home.css'     
})
export class Home { 
  
  // 1. المتغير اللي هيظهر في الـ HTML
  userName: string = 'Friend'; 

  // 2. بنحضر الـ Router عشان نستخدمه
  constructor(private router: Router) {
    // أول ما الصفحة تفتح، بنشوف هل فيه اسم متخزن ولا لأ
    const storedName = localStorage.getItem('fullName');
    if (storedName) {
      this.userName = storedName;
    }
  }

  // 3. دالة الخروج
  logout() {
    // امسح التوكن والبيانات
    localStorage.removeItem('token');
    localStorage.removeItem('fullName');
    
    // ارجع لصفحة الدخول
    this.router.navigate(['/login']);
  }
}