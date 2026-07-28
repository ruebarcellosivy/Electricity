import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InvoiceResponse, PayBillRequest, PaymentResponse } from '../models/payment.model';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly baseUrl = '/api/payments';

  constructor(private readonly http: HttpClient) {}

  pay(request: PayBillRequest): Observable<PaymentResponse[]> {
    return this.http.post<PaymentResponse[]>(`${this.baseUrl}/pay`, request);
  }

  getInvoice(transactionId: string): Observable<InvoiceResponse> {
    return this.http.get<InvoiceResponse>(`${this.baseUrl}/invoice/${transactionId}`);
  }

  downloadReceipt(paymentId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/receipt/${paymentId}/download`, { responseType: 'blob' });
  }

  downloadInvoice(transactionId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/invoice/${transactionId}/download`, { responseType: 'blob' });
  }
}
