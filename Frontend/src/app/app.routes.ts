import { Routes } from '@angular/router';
import { AuthContainerComponent } from './auth/auth-container/auth-container';
import { Home } from './components/home/home';
import { FriendRequests } from './components/friend-requests/friend-requests';
import { UserProfile } from './components/user-profile/user-profile'; 
import { SearchResults } from './components/search-results/search-results'; // <-- ADD THIS

export const routes: Routes = [
  { path: 'login', component: AuthContainerComponent },
  { path: 'home', component: Home },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'friend-requests', component: FriendRequests },
  // Dynamic route for user profile ... 
  { path: 'profile/:id', component: UserProfile } ,
  { path: 'search/:query', component: SearchResults }
];