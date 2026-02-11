import { Routes } from '@angular/router';
import { AuthContainerComponent } from './auth/auth-container/auth-container';
import { Home } from './components/home/home';
export const routes: Routes = [
  { path: 'login', component: AuthContainerComponent },
  { path: 'home', component: Home },
  { path: '', redirectTo: 'login', pathMatch: 'full' }
 
];