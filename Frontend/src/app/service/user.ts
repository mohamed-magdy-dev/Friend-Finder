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

  // Fetches the user profile and friendship status
  getUserProfile(userId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    
    return this.http.get<any>(`${this.apiUrl}/profile/${userId}`, { headers });
  }

  // Search for users by name
  searchUsers(name: string): Observable<any[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    return this.http.get<any[]>(`${this.apiUrl}/search?name=${name}`, { headers });
  }


  // Fetches full search results with pagination
  getFullSearchResults(name: string, page: number = 0, size: number = 10): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    return this.http.get<any>(`${this.apiUrl}/search/full?name=${name}&page=${page}&size=${size}`, { headers });
  }
  

  // photos part :

  // Upload Profile Picture (Updated with correct response type)
  uploadProfilePicture(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    const token = localStorage.getItem('token');
    // Important: Headers DO NOT need 'Content-Type' when sending FormData
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    
    // THE FIX: Specify responseType as 'text' since backend returns a raw URL string 
    return this.http.post(`${this.apiUrl}/profile-picture`, formData, { headers, responseType: 'text' });
  }

  // Upload Cover Banner (Updated with correct response type)
  uploadCoverPicture(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    
    // THE FIX: Specify responseType as 'text' here too 
    return this.http.post(`${this.apiUrl}/cover-picture`, formData, { headers, responseType: 'text' });
  }

  // Update User Profile Details
  updateUserProfile(profileData: any): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    return this.http.put<any>(`${this.apiUrl}/profile/update`, profileData, { headers });
  }
  
  // User Activity 
getUserActivity(userId: number): Observable<any> {
  const token = localStorage.getItem('token');
  const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
  return this.http.get<any>(`${this.apiUrl}/${userId}/activity`, { headers });
}

  // unfriend user
 unfriendUser(friendId: number) {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    
    const correctUrl = `http://localhost:8080/api/friends/unfriend/${friendId}`;
    
    return this.http.delete(correctUrl, { 
      headers: headers, 
      responseType: 'text' 
    });
  }

  // Friends page:
  getMyFriends() {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.get<any[]>('http://localhost:8080/api/friends/my-friends', { headers });
  }
}