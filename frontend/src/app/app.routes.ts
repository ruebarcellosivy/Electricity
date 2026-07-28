import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./shared/layout/main-layout/main-layout.component').then((m) => m.MainLayoutComponent),
    children: [
      {
        path: 'customer',
        canActivate: [roleGuard],
        data: { roles: ['CUSTOMER'] },
        children: [
          { path: 'dashboard', loadComponent: () => import('./features/customer/dashboard/dashboard.component').then((m) => m.DashboardComponent) },
          { path: 'bills', loadComponent: () => import('./features/customer/bills/view-bills.component').then((m) => m.ViewBillsComponent) },
          { path: 'bills/summary', loadComponent: () => import('./features/customer/bills/bill-summary.component').then((m) => m.BillSummaryComponent) },
          { path: 'bills/pay', loadComponent: () => import('./features/customer/bills/pay-bill.component').then((m) => m.PayBillComponent) },
          { path: 'bills/invoice/:transactionId', loadComponent: () => import('./features/customer/bills/invoice.component').then((m) => m.InvoiceComponent) },
          { path: 'bills/history', loadComponent: () => import('./features/customer/bills/bill-history.component').then((m) => m.BillHistoryComponent) },
          { path: 'complaints/new', loadComponent: () => import('./features/customer/complaints/register-complaint.component').then((m) => m.RegisterComplaintComponent) },
          { path: 'complaints/status', loadComponent: () => import('./features/customer/complaints/complaint-status.component').then((m) => m.ComplaintStatusComponent) },
          { path: 'complaints/history', loadComponent: () => import('./features/customer/complaints/complaint-history.component').then((m) => m.ComplaintHistoryComponent) },
          { path: '', pathMatch: 'full', redirectTo: 'dashboard' }
        ]
      },
      {
        path: 'admin',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        children: [
          { path: 'customers', loadComponent: () => import('./features/admin/customers/customer-list.component').then((m) => m.CustomerListComponent) },
          { path: 'customers/new', loadComponent: () => import('./features/admin/customers/customer-form.component').then((m) => m.CustomerFormComponent) },
          { path: 'customers/:id/edit', loadComponent: () => import('./features/admin/customers/customer-form.component').then((m) => m.CustomerFormComponent) },
          { path: 'consumers', loadComponent: () => import('./features/admin/customers/consumer-management.component').then((m) => m.ConsumerManagementComponent) },
          { path: 'bills', loadComponent: () => import('./features/admin/bills/admin-bill-list.component').then((m) => m.AdminBillListComponent) },
          { path: 'bills/new', loadComponent: () => import('./features/admin/bills/add-bill.component').then((m) => m.AddBillComponent) },
          { path: 'bills/bulk-upload', loadComponent: () => import('./features/admin/bills/bulk-upload.component').then((m) => m.BulkUploadComponent) },
          { path: 'complaints', loadComponent: () => import('./features/admin/complaints/admin-complaint-list.component').then((m) => m.AdminComplaintListComponent) },
          { path: '', pathMatch: 'full', redirectTo: 'customers' }
        ]
      },
      {
        path: 'sme',
        canActivate: [roleGuard],
        data: { roles: ['SME'] },
        children: [
          { path: 'complaints', loadComponent: () => import('./features/sme/complaints/sme-complaint-list.component').then((m) => m.SmeComplaintListComponent) },
          { path: '', pathMatch: 'full', redirectTo: 'complaints' }
        ]
      },
      { path: '', pathMatch: 'full', redirectTo: 'customer/dashboard' }
    ]
  },
  {
    path: '',
    loadComponent: () => import('./shared/layout/auth-layout/auth-layout.component').then((m) => m.AuthLayoutComponent),
    children: [
      { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent) },
      { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then((m) => m.RegisterComponent) },
      {
        path: 'change-password',
        canActivate: [authGuard],
        loadComponent: () => import('./features/auth/change-password/change-password.component').then((m) => m.ChangePasswordComponent)
      }
    ]
  },
  { path: 'unauthorized', loadComponent: () => import('./shared/components/unauthorized/unauthorized.component').then((m) => m.UnauthorizedComponent) },
  { path: '**', loadComponent: () => import('./shared/components/not-found/not-found.component').then((m) => m.NotFoundComponent) }
];
