import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { BillService } from '../../../core/services/bill.service';
import { Bill } from '../../../core/models/bill.model';

const COLUMNS = ['billNumber', 'consumerNumber', 'customerName', 'billingPeriod', 'billDate', 'dueDate',
  'billAmount', 'lateFee', 'status', 'paymentDate'];

@Component({
  selector: 'app-admin-bill-list',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatIconModule, MatTableModule, MatChipsModule, MatPaginatorModule],
  templateUrl: './admin-bill-list.component.html',
  styleUrl: './admin-bill-list.component.scss'
})
export class AdminBillListComponent implements OnInit {
  private readonly billService = inject(BillService);

  readonly columns = COLUMNS;
  readonly bills = signal<Bill[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly consumerNumber = signal('');
  readonly customerCode = signal('');
  readonly statusFilter = signal('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.billService.adminSearch(this.consumerNumber(), this.customerCode(), this.statusFilter() || null,
      this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.bills.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  onSearch(): void {
    this.pageIndex.set(0);
    this.load();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load();
  }

  export(format: 'csv' | 'pdf'): void {
    if (!this.consumerNumber()) return;
    this.billService.exportBillHistory(this.consumerNumber(), format).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = `bill-history-${this.consumerNumber()}.${format}`;
      anchor.click();
      window.URL.revokeObjectURL(url);
    });
  }
}
