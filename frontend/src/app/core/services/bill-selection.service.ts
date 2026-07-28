import { Injectable, signal } from '@angular/core';
import { PaymentResponse } from '../models/payment.model';
import { PaymentMethod } from '../models/enums';

/** Carries the customer's selected bill ids (and, later, the payment result) between the
 *  View Bills -> Bill Summary -> Pay Bill -> Invoice screens (US003-US006). */
@Injectable({ providedIn: 'root' })
export class BillSelectionService {
  readonly selectedBillIds = signal<number[]>([]);
  readonly paymentMethod = signal<PaymentMethod>('CREDIT_CARD');
  readonly lastPaymentResults = signal<PaymentResponse[]>([]);

  setSelection(ids: number[]): void {
    this.selectedBillIds.set(ids);
  }

  setPaymentMethod(method: PaymentMethod): void {
    this.paymentMethod.set(method);
  }

  setPaymentResults(results: PaymentResponse[]): void {
    this.lastPaymentResults.set(results);
  }
}
