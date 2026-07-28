import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from '../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

interface NavItem {
  label: string;
  path: string;
  icon: string;
}

const CUSTOMER_NAV: NavItem[] = [
  { label: 'Dashboard', path: '/customer/dashboard', icon: 'space_dashboard' },
  { label: 'View / Pay Bills', path: '/customer/bills', icon: 'receipt_long' },
  { label: 'Bill History', path: '/customer/bills/history', icon: 'history' },
  { label: 'Register Complaint', path: '/customer/complaints/new', icon: 'report_problem' },
  { label: 'Complaint Status', path: '/customer/complaints/status', icon: 'search' },
  { label: 'Complaint History', path: '/customer/complaints/history', icon: 'list_alt' }
];

const ADMIN_NAV: NavItem[] = [
  { label: 'Customers', path: '/admin/customers', icon: 'group' },
  { label: 'Add Customer', path: '/admin/customers/new', icon: 'person_add' },
  { label: 'Consumers', path: '/admin/consumers', icon: 'electrical_services' },
  { label: 'Add Bill', path: '/admin/bills/new', icon: 'note_add' },
  { label: 'Bulk Upload Bills', path: '/admin/bills/bulk-upload', icon: 'upload_file' },
  { label: 'View Bills', path: '/admin/bills', icon: 'receipt_long' },
  { label: 'Complaints', path: '/admin/complaints', icon: 'report' }
];

const SME_NAV: NavItem[] = [
  { label: 'Complaints', path: '/sme/complaints', icon: 'report' }
];

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatToolbarModule, MatSidenavModule,
    MatListModule, MatIconModule, MatButtonModule, MatMenuModule],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly currentUser = this.authService.currentUser;

  readonly navItems = computed<NavItem[]>(() => {
    switch (this.currentUser()?.role) {
      case 'ADMIN':
        return ADMIN_NAV;
      case 'SME':
        return SME_NAV;
      default:
        return CUSTOMER_NAV;
    }
  });

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: () => {
        this.authService.clearSession();
        this.router.navigate(['/login']);
      }
    });
  }
}
