import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Complaint, ComplaintRequest, ComplaintStatusUpdateRequest } from '../models/complaint.model';
import { PageResponse } from '../models/page-response.model';
import { ComplaintType } from '../models/enums';

export interface ComplaintSearchCriteria {
  customerCode?: string;
  consumerNumber?: string;
  complaintNumber?: string;
  complaintType?: string;
  status?: string;
  fromDate?: string;
  toDate?: string;
}

@Injectable({ providedIn: 'root' })
export class ComplaintService {
  private readonly baseUrl = '/api/complaints';

  constructor(private readonly http: HttpClient) {}

  categories(): Observable<Record<ComplaintType, string[]>> {
    return this.http.get<Record<ComplaintType, string[]>>(`${this.baseUrl}/categories`);
  }

  register(request: ComplaintRequest): Observable<Complaint> {
    return this.http.post<Complaint>(this.baseUrl, request);
  }

  track(complaintNumber: string): Observable<Complaint> {
    return this.http.get<Complaint>(`${this.baseUrl}/track/${complaintNumber}`);
  }

  myHistory(page: number, size: number): Observable<PageResponse<Complaint>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Complaint>>(`${this.baseUrl}/me`, { params });
  }

  search(criteria: ComplaintSearchCriteria, page: number, size: number): Observable<PageResponse<Complaint>> {
    let params = new HttpParams().set('page', page).set('size', size);
    Object.entries(criteria).forEach(([key, value]) => {
      if (value) params = params.set(key, value);
    });
    return this.http.get<PageResponse<Complaint>>(this.baseUrl, { params });
  }

  updateStatus(id: number, request: ComplaintStatusUpdateRequest): Observable<Complaint> {
    return this.http.put<Complaint>(`${this.baseUrl}/${id}/status`, request);
  }

  export(criteria: ComplaintSearchCriteria, format: 'csv' | 'pdf'): Observable<Blob> {
    let params = new HttpParams().set('format', format);
    Object.entries(criteria).forEach(([key, value]) => {
      if (value) params = params.set(key, value);
    });
    return this.http.get(`${this.baseUrl}/export`, { params, responseType: 'blob' });
  }
}
