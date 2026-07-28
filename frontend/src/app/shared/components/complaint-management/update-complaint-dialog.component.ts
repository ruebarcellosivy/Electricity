import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { ComplaintService } from '../../../core/services/complaint.service';
import { COMPLAINT_STATUSES } from '../../../core/models/enums';
import { Complaint } from '../../../core/models/complaint.model';

export interface UpdateComplaintDialogData {
  complaint: Complaint;
  allowAssign: boolean;
}

@Component({
  selector: 'app-update-complaint-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatSelectModule,
    MatInputModule, MatButtonModule],
  templateUrl: './update-complaint-dialog.component.html',
  styleUrl: './update-complaint-dialog.component.scss'
})
export class UpdateComplaintDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly complaintService = inject(ComplaintService);
  private readonly dialogRef = inject(MatDialogRef<UpdateComplaintDialogComponent>);
  readonly data = inject<UpdateComplaintDialogData>(MAT_DIALOG_DATA);

  readonly statuses = COMPLAINT_STATUSES;
  readonly submitting = signal(false);

  readonly form = this.fb.nonNullable.group({
    status: [this.data.complaint.status, [Validators.required]],
    remark: ['', [Validators.required, Validators.maxLength(1000)]],
    assignedTo: [this.data.complaint.assignedTo || '']
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    const value = this.form.getRawValue();
    this.complaintService.updateStatus(this.data.complaint.id, {
      status: value.status,
      remark: value.remark,
      assignedTo: this.data.allowAssign ? value.assignedTo : undefined
    }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.dialogRef.close(true);
      },
      error: () => this.submitting.set(false)
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
