import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { BillService } from '../../../core/services/bill.service';
import { PaymentService } from '../../../core/services/payment.service';
import { BillSelectionService } from '../../../core/services/bill-selection.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CustomValidators } from '../../../shared/validators/custom-validators';
import { PaymentResponse } from '../../../core/models/payment.model';

@Component({
  selector: 'app-pay-bill',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './pay-bill.component.html',
  styleUrl: './pay-bill.component.scss'
})
export class PayBillComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly billService = inject(BillService);
  private readonly paymentService = inject(PaymentService);
  private readonly billSelectionService = inject(BillSelectionService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  readonly totalAmount = signal(0);
  readonly submitting = signal(false);
  readonly results = signal<PaymentResponse[] | null>(null);

  readonly form = this.fb.nonNullable.group({
    cardHolderName: ['', [Validators.required, Validators.maxLength(50)]],
    cardNumber: ['', [Validators.required, CustomValidators.cardNumber()]],
    expiryDate: ['', [Validators.required, CustomValidators.expiryDate()]],
    cvv: ['', [Validators.required, CustomValidators.cvv()]]
  });

  ngOnInit(): void {
    const ids = this.billSelectionService.selectedBillIds();
    if (ids.length === 0) {
      this.router.navigate(['/customer/bills']);
      return;
    }
    this.billService.selectionSummary(ids).subscribe((summary) => this.totalAmount.set(summary.totalAmount));
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirm Payment',
        message: `You are about to pay ₹${this.totalAmount().toFixed(2)}. Do you want to proceed?`,
        confirmLabel: 'Pay Now'
      }
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) this.processPayment();
    });
  }

  private processPayment(): void {
    this.submitting.set(true);
    const value = this.form.getRawValue();
    this.paymentService.pay({
      billIds: this.billSelectionService.selectedBillIds(),
      cardHolderName: value.cardHolderName,
      cardNumber: value.cardNumber,
      expiryDate: value.expiryDate,
      cvv: value.cvv,
      paymentMethod: this.billSelectionService.paymentMethod()
    }).subscribe({
      next: (responses) => {
        this.submitting.set(false);
        this.billSelectionService.setPaymentResults(responses);
        this.results.set(responses);
      },
      error: () => this.submitting.set(false)
    });
  }

  downloadReceipt(paymentId: string): void {
    this.paymentService.downloadReceipt(paymentId).subscribe((blob) => this.triggerDownload(blob, `receipt-${paymentId}.pdf`));
  }

  downloadInvoice(transactionId: string): void {
    this.paymentService.downloadInvoice(transactionId).subscribe((blob) => this.triggerDownload(blob, `invoice-${transactionId}.pdf`));
  }

  viewInvoice(transactionId: string): void {
    this.router.navigate(['/customer/bills/invoice', transactionId]);
  }

  private triggerDownload(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
    window.URL.revokeObjectURL(url);
  }
}
