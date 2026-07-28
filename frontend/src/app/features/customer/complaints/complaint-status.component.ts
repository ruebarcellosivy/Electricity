import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { ComplaintService } from '../../../core/services/complaint.service';
import { Complaint } from '../../../core/models/complaint.model';

@Component({
  selector: 'app-complaint-status',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatChipsModule],
  templateUrl: './complaint-status.component.html',
  styleUrl: './complaint-status.component.scss'
})
export class ComplaintStatusComponent {
  private readonly fb = inject(FormBuilder);
  private readonly complaintService = inject(ComplaintService);

  readonly complaint = signal<Complaint | null>(null);
  readonly searched = signal(false);

  readonly form = this.fb.nonNullable.group({
    complaintNumber: ['', [Validators.required]]
  });

  search(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.searched.set(true);
    this.complaintService.track(this.form.getRawValue().complaintNumber).subscribe({
      next: (complaint) => this.complaint.set(complaint),
      error: () => this.complaint.set(null)
    });
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
