import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  
  // رابط الباك إند اللي عملناه
  private apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) { }

  getAllUsers(): Observable<any[]> {
    // 1. بنجيب التوكن من جيبنا (LocalStorage)
    const token = localStorage.getItem('token');

    // 2. بنحطه في الهيدر عشان السكيورتي يدخلنا
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    // 3. بنبعت الطلب (GET) ومعاه التوكن
    return this.http.get<any[]>(this.apiUrl, { headers });
  }
}