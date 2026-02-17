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
      error: (err: any) => {
  console.error('Registration Error:', err);
  // لو الباك إند باعت لنا إيرور من الـ Validation أو الـ Exception Handler
  if (err.error && typeof err.error === 'object') {
    if (err.error.error) {
      // دي عشان لو رسالة RuntimeException (زي الإيميل مكرر)
      this.errorMessage = err.error.error;
    } else {
      // دي عشان أخطاء الـ Validation (زي الباسورد ضعيف)
      // هنجيب أول خطأ في القائمة ونعرضه
      this.errorMessage = Object.values(err.error)[0] as string;
    }
  } else {
    this.errorMessage = 'An unexpected server error occurred!';
  }
}
   
    });
  }

  // دالة الريجستر
  onRegister() {
    this.authService.register(this.registerData).subscribe({
      next: () => {
        alert('Registered! Now log in.');
        // بعد التسجيل الناجح، نرجع أوتوماتيك لشاشة اللوجن
        this.isSignUpActive = false;
      },
      error: (err: any) => {
  console.error('Login Error:', err);
  if (err.error && err.error.error) {
    // هيعرض رسالة "كلمة المرور خطأ" أو "بيانات الدخول غير صحيحة" اللي جاية من الباك إند
    this.errorMessage = err.error.error; 
  } else {
    this.errorMessage = 'Make sure the entered data is correct. ';
  }
}
    });
  }
}