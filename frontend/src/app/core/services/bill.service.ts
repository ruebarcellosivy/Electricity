import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AddBillRequest, Bill, BillSelectionSummary, BulkUploadResult } from '../models/bill.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class BillService {
  private readonly baseUrl = '/api/bills';

  constructor(private readonly http: HttpClient) {}

  addBill(request: AddBillRequest): Observable<Bill> {
    return this.http.post<Bill>(this.baseUrl, request);
  }

  bulkUpload(file: File): Observable<BulkUploadResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<BulkUploadResult>(`${this.baseUrl}/bulk-upload`, formData);
  }

  myBills(status: string | null, page: number, size: number): Observable<PageResponse<Bill>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<Bill>>(`${this.baseUrl}/me`, { params });
  }

  myBillHistory(fromDate: string | null, toDate: string | null, status: string | null, sortBy: string,
                page: number, size: number): Observable<PageResponse<Bill>> {
    let params = new HttpParams().set('sortBy', sortBy).set('page', page).set('size', size);
    if (fromDate) params = params.set('fromDate', fromDate);
    if (toDate) params = params.set('toDate', toDate);
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<Bill>>(`${this.baseUrl}/me/history`, { params });
  }

  selectionSummary(billIds: number[]): Observable<BillSelectionSummary> {
    return this.http.post<BillSelectionSummary>(`${this.baseUrl}/me/selection-summary`, billIds);
  }

  adminSearch(consumerNumber: string, customerCode: string, status: string | null,
              page: number, size: number): Observable<PageResponse<Bill>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (consumerNumber) params = params.set('consumerNumber', consumerNumber);
    if (customerCode) params = params.set('customerCode', customerCode);
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<Bill>>(this.baseUrl, { params });
  }

  exportBillHistory(consumerNumber: string, format: 'csv' | 'pdf'): Observable<Blob> {
    const params = new HttpParams().set('consumerNumber', consumerNumber).set('format', format);
    return this.http.get(`${this.baseUrl}/export`, { params, responseType: 'blob' });
  }

  getById(id: number): Observable<Bill> {
    return this.http.get<Bill>(`${this.baseUrl}/${id}`);
  }
}
