import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { UserService } from '../../service/user';

@Component({
  selector: 'app-search-results',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './search-results.html',
  styleUrl: './search-results.css'
})
export class SearchResults implements OnInit {
  
  searchQuery: string = '';
  users: any[] = [];
  currentPage: number = 0;
  totalPages: number = 0;
  isLoading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    // Listen to changes in the URL (e.g., /search/ahmed)
    this.route.paramMap.subscribe(params => {
      const query = params.get('query');
      if (query) {
        this.searchQuery = query;
        this.currentPage = 0;
        this.fetchResults();
      }
    });
  }

  // Fetch paginated results from the backend
  fetchResults() {
    this.isLoading = true;
    this.userService.getFullSearchResults(this.searchQuery, this.currentPage, 10).subscribe({
      next: (res: any) => {
        this.users = res.content ? res.content : [];
        this.totalPages = res.totalPages || 0;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Error fetching search results:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Go to next page
  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.fetchResults();
    }
  }

  // Go to previous page
  prevPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.fetchResults();
    }
  }
}