import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { ComplaintService } from '../../../core/services/complaint.service';
import { Complaint } from '../../../core/models/complaint.model';

@Component({
  selector: 'app-complaint-history',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatExpansionModule, MatChipsModule, MatPaginatorModule],
  templateUrl: './complaint-history.component.html',
  styleUrl: './complaint-history.component.scss'
})
export class ComplaintHistoryComponent implements OnInit {
  private readonly complaintService = inject(ComplaintService);

  readonly complaints = signal<Complaint[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.complaintService.myHistory(this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.complaints.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load();
  }

  statusColor(status: string): string {
    switch (status) {
      case 'RESOLVED':
      case 'CLOSED':
        return 'primary';
      case 'IN_PROGRESS':
        return 'accent';
      default:
        return 'warn';
    }
  }
}
