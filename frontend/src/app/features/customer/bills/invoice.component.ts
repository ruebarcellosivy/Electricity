import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PaymentService } from '../../../core/services/payment.service';
import { InvoiceResponse } from '../../../core/models/payment.model';

@Component({
  selector: 'app-invoice',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './invoice.component.html',
  styleUrl: './invoice.component.scss'
})
export class InvoiceComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly paymentService = inject(PaymentService);

  readonly invoice = signal<InvoiceResponse | null>(null);

  ngOnInit(): void {
    const transactionId = this.route.snapshot.paramMap.get('transactionId');
    if (transactionId) {
      this.paymentService.getInvoice(transactionId).subscribe((invoice) => this.invoice.set(invoice));
    }
  }

  download(): void {
    const invoice = this.invoice();
    if (!invoice) return;
    this.paymentService.downloadInvoice(invoice.transactionId).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = `invoice-${invoice.transactionId}.pdf`;
      anchor.click();
      window.URL.revokeObjectURL(url);
    });
  }
}
