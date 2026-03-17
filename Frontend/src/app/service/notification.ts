import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private apiUrl = 'http://localhost:8080/api/notifications';

  constructor(private http: HttpClient) { }

  /**
   * Fetches all notifications for the current logged-in user.
   */
  getNotifications(): Observable<any[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    
    return this.http.get<any[]>(this.apiUrl, { headers });
  }

  /**
   * Marks a specific notification as read.
   */
  markAsRead(notificationId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    
    return this.http.put(`${this.apiUrl}/${notificationId}/read`, {}, { headers, responseType: 'text' as 'json' });
  }
}