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

  isSubmitting = false;

  // show/hide toggles for the eye icon on each password field
  showLoginPassword = false;
  showRegisterPassword = false;
  showConfirmPassword = false;

  loginData = { email: '', password: '' };
  // confirmPassword only exists on the frontend, never sent to the backend
  registerData = { fullName: '', email: '', password: '', confirmPassword: '' };

  // drives the rules checklist under the password field
  passwordFocused = false;

  // these mirror the backend rules exactly (RegisterRequest.java),
  // keep them in sync if the password policy ever changes there
  get passwordHasMinLength(): boolean {
    return this.registerData.password.length >= 8;
  }

  get passwordHasUppercase(): boolean {
    return /[A-Z]/.test(this.registerData.password);
  }

  get passwordHasSpecialChar(): boolean {
    return /[@#$%^&+=!]/.test(this.registerData.password);
  }

  get isPasswordValid(): boolean {
    return this.passwordHasMinLength && this.passwordHasUppercase && this.passwordHasSpecialChar;
  }

  constructor(private authService: AuthService,
              private router: Router) {}

  switchTab(isSignUp: boolean) {
    this.isSignUpActive = isSignUp;
    this.clearMessage();
  }

  toggleLoginPassword() {
    this.showLoginPassword = !this.showLoginPassword;
  }

  toggleRegisterPassword() {
    this.showRegisterPassword = !this.showRegisterPassword;
  }

  toggleConfirmPassword() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  onLogin() {
    // avoid double submit if the user clicks fast
    if (this.isSubmitting) return;
    this.clearMessage();
    this.isSubmitting = true;

    this.authService.login(this.loginData).subscribe({
      next: (res: any) => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('fullName', res.fullName);
        localStorage.setItem('userId', res.id);

        this.messageType = 'success';
        this.message = 'Login successful. Redirecting...';

        setTimeout(() => {
          this.router.navigate(['/home']);
        }, 1500);
      },
      error: (err: any) => {
        this.isSubmitting = false;
        this.messageType = 'error';
        this.message = err.error?.error || 'Invalid email or password.';
      }
    });
  }

  onRegister() {
    if (this.isSubmitting) return;

    // this is also blocked by the disabled state on the button,
    // but we check again here just in case
    if (this.registerData.password !== this.registerData.confirmPassword) {
      this.messageType = 'error';
      this.message = 'Passwords do not match.';
      return;
    }

    this.clearMessage();
    this.isSubmitting = true;

    // backend DTO doesn't know about confirmPassword, so strip it before sending
    const payload = {
      fullName: this.registerData.fullName,
      email: this.registerData.email,
      password: this.registerData.password
    };

    this.authService.register(payload).subscribe({
      next: (res: any) => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('fullName', res.fullName);
        localStorage.setItem('userId', res.id);

        this.messageType = 'success';
        this.message = 'Account created successfully. Redirecting...';

        setTimeout(() => {
          this.router.navigate(['/home']);
        }, 1500);
      },
      error: (err: any) => {
        this.isSubmitting = false;
        this.messageType = 'error';
        this.message = err.error?.error || 'Registration failed.';
      }
    });
  }

  private clearMessage() {
    this.message = '';
    this.messageType = null;
  }
}