import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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

  isSignUpActive = false;

  message = '';
  messageType: 'success' | 'error' | null = null;

  loginData = { email: '', password: '' };
  registerData = { fullName: '', email: '', password: '' };

  constructor(private authService: AuthService,
              private router: Router) {}

  switchTab(isSignUp: boolean) {
    this.isSignUpActive = isSignUp;
    this.clearMessage();
  }

  onLogin() {
    this.clearMessage();

    this.authService.login(this.loginData).subscribe({
      next: (res: any) => {

        localStorage.setItem('token', res.token);
        localStorage.setItem('fullName', res.fullName);
        localStorage.setItem('userId', res.id); // <-- ضفناه هنا في اللوجين

        this.messageType = 'success';
        this.message = 'Login successful. Redirecting...';

        setTimeout(() => {
          this.router.navigate(['/home']);
        }, 2000);
      },
      error: (err: any) => {
        this.messageType = 'error';
        if (err.error?.error) {
          this.message = err.error.error;
        } else {
          this.message = 'Invalid email or password.';
        }
      }
    });
  }

  onRegister() {
    this.clearMessage();

    this.authService.register(this.registerData).subscribe({
      next: (res: any) => {

        localStorage.setItem('token', res.token);
        localStorage.setItem('fullName', res.fullName);
        localStorage.setItem('userId', res.id); // <-- وضفناه هنا في الريجستر

        this.messageType = 'success';
        this.message = 'Account created successfully. Redirecting...';

        setTimeout(() => {
          this.router.navigate(['/home']);
        }, 2000);
      },
      error: (err: any) => {
        this.messageType = 'error';
        if (err.error?.error) {
          this.message = err.error.error;
        } else {
          this.message = 'Registration failed.';
        }
      }
    });
  }
  private clearMessage() {
    this.message = '';
    this.messageType = null;
  }
}