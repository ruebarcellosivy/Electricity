import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AddConsumerRequest, ConnectionStatusUpdateRequest, Consumer } from '../models/consumer.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class ConsumerService {
  private readonly baseUrl = '/api/consumers';

  constructor(private readonly http: HttpClient) {}

  add(request: AddConsumerRequest): Observable<Consumer> {
    return this.http.post<Consumer>(this.baseUrl, request);
  }

  list(section: string | null, type: string | null, page: number, size: number): Observable<PageResponse<Consumer>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (section) params = params.set('electricalSection', section);
    if (type) params = params.set('customerType', type);
    return this.http.get<PageResponse<Consumer>>(this.baseUrl, { params });
  }

  myConsumers(): Observable<Consumer[]> {
    return this.http.get<Consumer[]>(`${this.baseUrl}/me`);
  }

  updateConnectionStatus(consumerNumber: string, request: ConnectionStatusUpdateRequest): Observable<Consumer> {
    return this.http.put<Consumer>(`${this.baseUrl}/${consumerNumber}/connection-status`, request);
  }
}
