import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomerService } from '../../../core/services/customer.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { Customer } from '../../../core/models/customer.model';
import { CUSTOMER_TYPES, ELECTRICAL_SECTIONS } from '../../../core/models/enums';

const COLUMNS = ['customerCode', 'userId', 'fullName', 'email', 'mobileNumber', 'customerType', 'electricalSection',
  'consumerNumbers', 'status', 'actions'];

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatCardModule, MatTableModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatChipsModule, MatTooltipModule, MatPaginatorModule],
  templateUrl: './customer-list.component.html',
  styleUrl: './customer-list.component.scss'
})
export class CustomerListComponent implements OnInit {
  private readonly customerService = inject(CustomerService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  readonly columns = COLUMNS;
  readonly customerTypes = CUSTOMER_TYPES;
  readonly electricalSections = ELECTRICAL_SECTIONS;

  readonly customers = signal<Customer[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly search = signal('');
  readonly sectionFilter = signal<string>('');
  readonly typeFilter = signal<string>('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.customerService.list(this.search(), this.sectionFilter() || null, this.typeFilter() || null,
      this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.customers.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  onFilterChange(): void {
    this.pageIndex.set(0);
    this.load();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load();
  }

  editCustomer(customer: Customer): void {
    this.router.navigate(['/admin/customers', customer.id, 'edit']);
  }

  addConsumer(customer: Customer): void {
    this.router.navigate(['/admin/consumers'], { queryParams: { customerId: customer.id, customerCode: customer.customerCode } });
  }

  deactivate(customer: Customer): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Deactivate Customer',
        message: `Are you sure you want to deactivate ${customer.fullName} (${customer.customerCode})? All linked connections will be disconnected.`,
        confirmLabel: 'Deactivate'
      }
    });
    dialogRef.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;
      this.customerService.deactivate(customer.id).subscribe(() => {
        this.snackBar.open('Customer deactivated successfully.', 'Close', { duration: 4000 });
        this.load();
      });
    });
  }
}
