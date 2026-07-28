import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminCreateCustomerRequest, Customer, HomeSummary, UpdateCustomerRequest } from '../models/customer.model';
import { PageResponse } from '../models/page-response.model';
import { MessageResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly baseUrl = '/api/customers';

  constructor(private readonly http: HttpClient) {}

  create(request: AdminCreateCustomerRequest): Observable<Customer> {
    return this.http.post<Customer>(this.baseUrl, request);
  }

  list(search: string, section: string | null, type: string | null, page: number, size: number): Observable<PageResponse<Customer>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search) params = params.set('search', search);
    if (section) params = params.set('electricalSection', section);
    if (type) params = params.set('customerType', type);
    return this.http.get<PageResponse<Customer>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Customer> {
    return this.http.get<Customer>(`${this.baseUrl}/${id}`);
  }

  update(id: number, request: UpdateCustomerRequest): Observable<Customer> {
    return this.http.put<Customer>(`${this.baseUrl}/${id}`, request);
  }

  deactivate(id: number): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.baseUrl}/${id}`);
  }

  myProfile(): Observable<Customer> {
    return this.http.get<Customer>(`${this.baseUrl}/me`);
  }

  myHomeSummary(): Observable<HomeSummary> {
    return this.http.get<HomeSummary>(`${this.baseUrl}/me/home`);
  }
}
