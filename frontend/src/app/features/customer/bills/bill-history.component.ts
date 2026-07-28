import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';
import { BillService } from '../../../core/services/bill.service';
import { ConsumerService } from '../../../core/services/consumer.service';
import { Bill } from '../../../core/models/bill.model';

const COLUMNS = ['billDate', 'billingPeriod', 'dueDate', 'billAmount', 'status', 'paymentDate', 'actions'];

@Component({
  selector: 'app-bill-history',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatTableModule, MatFormFieldModule, MatSelectModule,
    MatDatepickerModule, MatNativeDateModule, MatInputModule, MatButtonModule, MatIconModule,
    MatPaginatorModule, MatChipsModule],
  templateUrl: './bill-history.component.html',
  styleUrl: './bill-history.component.scss'
})
export class BillHistoryComponent implements OnInit {
  private readonly billService = inject(BillService);
  private readonly consumerService = inject(ConsumerService);
  private readonly router = inject(Router);

  readonly columns = COLUMNS;
  readonly bills = signal<Bill[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);

  readonly fromDate = signal<Date | null>(this.sixMonthsAgo());
  readonly toDate = signal<Date | null>(new Date());
  readonly statusFilter = signal<string>('');
  readonly sortBy = signal<string>('billDate');
  readonly primaryConsumerNumber = signal<string>('');

  private sixMonthsAgo(): Date {
    const d = new Date();
    d.setMonth(d.getMonth() - 6);
    return d;
  }

  ngOnInit(): void {
    this.consumerService.myConsumers().subscribe((consumers) => {
      if (consumers.length > 0) this.primaryConsumerNumber.set(consumers[0].consumerNumber);
    });
    this.loadHistory();
  }

  loadHistory(): void {
    const from = this.fromDate() ? this.toIsoDate(this.fromDate()!) : null;
    const to = this.toDate() ? this.toIsoDate(this.toDate()!) : null;
    this.billService.myBillHistory(from, to, this.statusFilter() || null, this.sortBy(),
      this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.bills.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  private toIsoDate(date: Date): string {
    return date.toISOString().substring(0, 10);
  }

  onFilterChange(): void {
    this.pageIndex.set(0);
    this.loadHistory();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadHistory();
  }

  viewBill(bill: Bill): void {
    this.router.navigate(['/customer/bills']);
  }

  downloadHistory(): void {
    const consumerNumber = this.primaryConsumerNumber();
    if (!consumerNumber) return;
    this.billService.exportBillHistory(consumerNumber, 'pdf').subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = `bill-history-${consumerNumber}.pdf`;
      anchor.click();
      window.URL.revokeObjectURL(url);
    });
  }
}
