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
 likePost(postId: number): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    });
    // اتأكد إن اللينك ده نفس اللي إنت عامله في الـ Backend
    return this.http.post<any>(`${this.apiUrl}/${postId}/like`, {}, { headers });
  }

  // Fetches posts authored by a specific user only
  getUserPosts(userId: number, page: number = 0, size: number = 10): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    
    return this.http.get<any>(`${this.apiUrl}/user/${userId}?page=${page}&size=${size}`, { headers });
  }

  deletePost(postId: number) {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    
   
    return this.http.delete(`${this.apiUrl}/${postId}`, { 
      headers: headers, 
      responseType: 'text' 
    });
  }
}