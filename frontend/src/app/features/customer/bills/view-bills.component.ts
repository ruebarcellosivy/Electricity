import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SelectionModel } from '@angular/cdk/collections';
import { MatTableModule } from '@angular/material/table';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { BillService } from '../../../core/services/bill.service';
import { BillSelectionService } from '../../../core/services/bill-selection.service';
import { Bill } from '../../../core/models/bill.model';

const COLUMNS = ['select', 'consumerNumber', 'billNumber', 'status', 'connectionType', 'connectionStatus',
  'mobileNumber', 'billingPeriod', 'billDate', 'dueDate', 'disconnectionDate', 'payableAmount'];

@Component({
  selector: 'app-view-bills',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatCheckboxModule, MatButtonModule, MatChipsModule,
    MatCardModule, MatFormFieldModule, MatSelectModule, MatPaginatorModule],
  templateUrl: './view-bills.component.html',
  styleUrl: './view-bills.component.scss'
})
export class ViewBillsComponent implements OnInit {
  private readonly billService = inject(BillService);
  private readonly billSelectionService = inject(BillSelectionService);
  private readonly router = inject(Router);

  readonly columns = COLUMNS;
  readonly bills = signal<Bill[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(5);
  readonly statusFilter = signal<string>('');
  readonly selection = new SelectionModel<number>(true, []);

  readonly totalPayable = computed(() => {
    return this.bills()
      .filter((b) => this.selection.isSelected(b.id))
      .reduce((sum, b) => sum + b.payableAmount, 0);
  });

  ngOnInit(): void {
    this.loadBills();
  }

  loadBills(): void {
    const status = this.statusFilter() || null;
    this.billService.myBills(status, this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.bills.set(page.content);
      this.totalElements.set(page.totalElements);
      this.selection.clear();
    });
  }

  onFilterChange(value: string): void {
    this.statusFilter.set(value);
    this.pageIndex.set(0);
    this.loadBills();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadBills();
  }

  toggleAll(): void {
    const payable = this.bills().filter((b) => b.status === 'UNPAID');
    if (this.selection.selected.length === payable.length) {
      this.selection.clear();
    } else {
      this.selection.clear();
      this.selection.select(...payable.map((b) => b.id));
    }
  }

  proceedToPay(): void {
    this.billSelectionService.setSelection(this.selection.selected);
    this.router.navigate(['/customer/bills/summary']);
  }
}
