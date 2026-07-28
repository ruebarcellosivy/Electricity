import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { BillService } from '../../../core/services/bill.service';
import { BillSelectionService } from '../../../core/services/bill-selection.service';
import { Bill } from '../../../core/models/bill.model';
import { PAYMENT_METHODS, PaymentMethod } from '../../../core/models/enums';

const COLUMNS = ['consumerNumber', 'billDate', 'billingPeriod', 'billAmount', 'dueDate', 'remove'];

@Component({
  selector: 'app-bill-summary',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatTableModule, MatButtonModule, MatIconModule, MatRadioModule],
  templateUrl: './bill-summary.component.html',
  styleUrl: './bill-summary.component.scss'
})
export class BillSummaryComponent implements OnInit {
  private readonly billService = inject(BillService);
  private readonly billSelectionService = inject(BillSelectionService);
  private readonly router = inject(Router);

  readonly columns = COLUMNS;
  readonly bills = signal<Bill[]>([]);
  readonly loadFailed = signal(false);
  readonly paymentMethods = PAYMENT_METHODS;
  readonly paymentMethod = this.billSelectionService.paymentMethod;

  readonly totalAmount = computed(() => this.bills().reduce((sum, b) => sum + b.payableAmount, 0));

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
    const ids = this.billSelectionService.selectedBillIds();
    if (ids.length === 0) {
      this.router.navigate(['/customer/bills']);
      return;
    }
    this.loadFailed.set(false);
    this.billService.selectionSummary(ids).subscribe({
      next: (summary) => this.bills.set(summary.bills),
      error: () => this.loadFailed.set(true)
    });
  }

  removeBill(id: number): void {
    const remaining = this.bills().filter((b) => b.id !== id);
    this.bills.set(remaining);
    this.billSelectionService.setSelection(remaining.map((b) => b.id));
  }

  onPaymentMethodChange(method: PaymentMethod): void {
    this.billSelectionService.setPaymentMethod(method);
  }

  goBack(): void {
    this.router.navigate(['/customer/bills']);
  }

  proceedToPayment(): void {
    this.router.navigate(['/customer/bills/pay']);
  }
}
