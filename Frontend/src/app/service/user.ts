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

  /**
   * Fetches the list of pending friend requests for the current user.
   */
  getPendingFriendRequests(): Observable<any[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    
    return this.http.get<any[]>(`${this.friendsApiUrl}/pending`, { headers });
  }

  /**
   * Accepts a specific friend request by its ID.
   */
  acceptFriendRequest(requestId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    
    // Using PUT because we defined it as @PutMapping in Spring Boot
    return this.http.put(`${this.friendsApiUrl}/accept/${requestId}`, {}, { headers, responseType: 'text' as 'json' });
  }

  /**
   * Rejects (deletes) a specific friend request by its ID.
   */
  rejectFriendRequest(requestId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    
    // Using DELETE because we defined it as @DeleteMapping in Spring Boot
    return this.http.delete(`${this.friendsApiUrl}/reject/${requestId}`, { headers, responseType: 'text' as 'json' });
  }
}