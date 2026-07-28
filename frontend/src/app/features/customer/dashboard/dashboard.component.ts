import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { CustomerService } from '../../../core/services/customer.service';
import { HomeSummary } from '../../../core/models/customer.model';

interface FeatureLink {
  label: string;
  path: string;
  icon: string;
}

const FEATURES: FeatureLink[] = [
  { label: 'View Bills', path: '/customer/bills', icon: 'receipt_long' },
  { label: 'Pay Bill', path: '/customer/bills', icon: 'payment' },
  { label: 'Bill History', path: '/customer/bills/history', icon: 'history' },
  { label: 'Register Complaint', path: '/customer/complaints/new', icon: 'report_problem' },
  { label: 'Complaint Status', path: '/customer/complaints/status', icon: 'search' }
];

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatIconModule, MatButtonModule, MatChipsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private readonly customerService = inject(CustomerService);

  readonly features = FEATURES;
  readonly summary = signal<HomeSummary | null>(null);

  ngOnInit(): void {
    this.customerService.myHomeSummary().subscribe((summary) => this.summary.set(summary));
  }
}
