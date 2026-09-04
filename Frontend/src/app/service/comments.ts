import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CommentsService {
  private apiUrl = 'http://localhost:8080/api/posts'; // same as posts link

  constructor(private http: HttpClient) {}

  // bringing comments to a certain post
  getCommentsByPostId(postId: number): Observable<any> {
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${localStorage.getItem('token')}` });
    return this.http.get<any>(`${this.apiUrl}/${postId}/comments`, { headers });
  }

  // adding a new comment 
  addComment(postId: number, content: string): Observable<any> {
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${localStorage.getItem('token')}` });
    return this.http.post<any>(`${this.apiUrl}/${postId}/comments`, { content }, { headers });
  }

  // deleting the comment
  deleteComment(commentId: number): Observable<any> {
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${localStorage.getItem('token')}` });
    return this.http.delete<any>(`${this.apiUrl}/comments/${commentId}`, { headers, responseType: 'text' as 'json' });
    // I added responseType 'text' - backend returns a plain String, not JSON...
   }
// editing the comment
updateComment(commentId: number, content: string): Observable<any> {
  const headers = new HttpHeaders({'Authorization': `Bearer ${localStorage.getItem('token')}`});
  return this.http.put<any>(`${this.apiUrl}/comments/${commentId}`,{ content },{ headers }); 
  }

}