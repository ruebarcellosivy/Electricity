import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ComplaintService } from '../../../core/services/complaint.service';
import { COMPLAINT_STATUSES, COMPLAINT_TYPES } from '../../../core/models/enums';
import { Complaint } from '../../../core/models/complaint.model';
import { UpdateComplaintDialogComponent } from './update-complaint-dialog.component';

const COLUMNS = ['complaintNumber', 'consumerNumber', 'customerName', 'complaintType', 'dateSubmitted',
  'status', 'lastUpdated', 'actions'];

@Component({
  selector: 'app-complaint-management',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatIconModule, MatTableModule, MatChipsModule, MatDatepickerModule, MatNativeDateModule,
    MatPaginatorModule, MatDialogModule],
  templateUrl: './complaint-management.component.html',
  styleUrl: './complaint-management.component.scss'
})
export class ComplaintManagementComponent implements OnInit {
  /** Whether the current role (ADMIN) may assign complaints to an SME user id. */
  @Input() allowAssign = false;
  @Input() title = 'Complaints';
  @Input() subtitle = 'Search and manage customer complaints';

  private readonly complaintService = inject(ComplaintService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly columns = COLUMNS;
  readonly complaintTypes = COMPLAINT_TYPES;
  readonly complaintStatuses = COMPLAINT_STATUSES;

  readonly complaints = signal<Complaint[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);

  readonly customerCode = signal('');
  readonly consumerNumber = signal('');
  readonly complaintNumber = signal('');
  readonly typeFilter = signal('');
  readonly statusFilter = signal('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.complaintService.search({
      customerCode: this.customerCode(),
      consumerNumber: this.consumerNumber(),
      complaintNumber: this.complaintNumber(),
      complaintType: this.typeFilter(),
      status: this.statusFilter()
    }, this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.complaints.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  onSearch(): void {
    this.pageIndex.set(0);
    this.load();
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

  openUpdateDialog(complaint: Complaint): void {
    const dialogRef = this.dialog.open(UpdateComplaintDialogComponent, {
      width: '480px',
      data: { complaint, allowAssign: this.allowAssign }
    });
    dialogRef.afterClosed().subscribe((updated) => {
      if (updated) {
        this.snackBar.open('Complaint status updated successfully.', 'Close', { duration: 4000 });
        this.load();
      }
    });
  }

  export(format: 'csv' | 'pdf'): void {
    this.complaintService.export({
      customerCode: this.customerCode(),
      consumerNumber: this.consumerNumber(),
      complaintType: this.typeFilter(),
      status: this.statusFilter()
    }, format).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = `complaints.${format}`;
      anchor.click();
      window.URL.revokeObjectURL(url);
    });
  }
}
