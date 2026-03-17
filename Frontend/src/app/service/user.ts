import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  
  private apiUrl = 'http://localhost:8080/api/users';
  private friendsApiUrl = 'http://localhost:8080/api/friends'; 

  constructor(private http: HttpClient) { }

  getAllUsers(): Observable<any[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    return this.http.get<any[]>(this.apiUrl, { headers });
  }

  getSuggestedUsers(): Observable<any[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    return this.http.get<any[]>(`${this.apiUrl}/suggestions`, { headers });
  }

  sendFriendRequest(receiverId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    
    return this.http.post(`${this.friendsApiUrl}/add/${receiverId}`, {}, { headers, responseType: 'text' as 'json' });
  }
  cancelFriendRequest(receiverId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    
    // بنستخدم http.delete عشان إحنا عاملينها DeleteMapping في الباك إند
    return this.http.delete(`${this.friendsApiUrl}/cancel/${receiverId}`, { headers, responseType: 'text' as 'json' });
  }
}