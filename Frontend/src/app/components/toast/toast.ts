import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../service/toast'; 

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (toastService.toast$ | async; as toast) {
      <div class="position-fixed bottom-0 end-0 p-4" style="z-index: 2000;">
        <div class="d-flex align-items-center text-white border-0 shadow-lg ff-toast-anim p-3" 
             [ngClass]="toast.type === 'success' ? 'bg-success' : 'bg-danger'" 
             style="border-radius: 12px; min-width: 300px;">
          
          <div class="fw-bold d-flex flex-grow-1 align-items-center">
            <i class="bi me-2 fs-5" [ngClass]="toast.type === 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill'"></i>
            {{ toast.message }}
          </div>
          
          <button type="button" class="btn-close btn-close-white ms-3 shadow-none" (click)="close()"></button>
        </div>
      </div>
    }
  `,
  styles: [`
    .ff-toast-anim { animation: slideUp 0.3s ease-out; } 
    @keyframes slideUp { 
      from { transform: translateY(100%); opacity: 0; } 
      to { transform: translateY(0); opacity: 1; } 
    }
  `]
})
export class Toast {
  constructor(public toastService: ToastService) {} //i made this public so that html file sees it

  close() {
    this.toastService.clear();
  }
}