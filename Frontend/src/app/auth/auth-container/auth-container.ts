import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
//import { AuthService } from '../auth.service';
import { Router } from '@angular/router';
import { AuthService } from '../auth';

@Component({
  selector: 'app-auth-container',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auth-container.html',
  styleUrl: './auth-container.css'
})
export class AuthContainerComponent {
  // المتغير السحري: لو true يبقى بنعرض التسجيل، لو false يبقى دخول
  isSignUpActive: boolean = false;

  // متغيرات الفورم (لمينا بتوع اللوجن والريجستر هنا)
  loginData = { email: '', password: '' };
  registerData = { fullName: '', email: '', password: '' };
  
  errorMessage: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  // دالة اللوجن
  onLogin() {
    this.authService.login(this.loginData).subscribe({
      next: (res: any) => {localStorage.setItem('token', res.token);
      
      // ضيف السطر ده ضروري 👇
      localStorage.setItem('fullName', res.fullName); 
      
      // التوجيه للصفحة الرئيسية
      this.router.navigate(['/home']);
      },
      error: () => this.errorMessage = 'إيميل أو باسورد غلط!'
    });
  }

  // دالة الريجستر
  onRegister() {
    this.authService.register(this.registerData).subscribe({
      next: () => {
        alert('تم التسجيل! سجل دخولك بقى.');
        // بعد التسجيل الناجح، نرجع أوتوماتيك لشاشة اللوجن
        this.isSignUpActive = false;
      },
      error: () => this.errorMessage = 'الإيميل ده مستخدم قبل كده!'
    });
  }
}