import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PostService {
  
  // العنوان بتاع البوستات اللي جربته في Postman
  private apiUrl = 'http://localhost:8080/api/posts';

  constructor(private http: HttpClient) { }

  // الدالة اللي بتجيب البوستات، ولاحظ إننا باعتين رقم الصفحة والحجم
  getAllPosts(page: number = 0, size: number = 10): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    // بنبعت الطلب ومعاه رقم الصفحة في الـ URL
    return this.http.get<any>(`${this.apiUrl}?page=${page}&size=${size}`, { headers });
  }
  createPost(postData: any): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    // بنبعت الطلب كـ POST للباك إند ومعاه الداتا (اللي هي النص)
    return this.http.post<any>(this.apiUrl, postData, { headers });
  }
  toggleLike(postId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    // لاحظ إننا ضفنا الـ postId في اللينك زي ما عملنا في سبرينج بوت
    return this.http.post<any>(`${this.apiUrl}/${postId}/like`, {}, { headers });
  }
}