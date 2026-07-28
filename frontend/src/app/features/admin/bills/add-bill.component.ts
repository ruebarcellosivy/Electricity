import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { CustomValidators } from '../../../shared/validators/custom-validators';
import { BillService } from '../../../core/services/bill.service';
import { Bill } from '../../../core/models/bill.model';

@Component({
  selector: 'app-add-bill',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatDatepickerModule, MatNativeDateModule, MatButtonModule],
  templateUrl: './add-bill.component.html',
  styleUrl: './add-bill.component.scss'
})
export class AddBillComponent {
  private readonly fb = inject(FormBuilder);
  private readonly billService = inject(BillService);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly created = signal<Bill | null>(null);

  readonly form = this.fb.nonNullable.group({
    consumerNumber: ['', [Validators.required, CustomValidators.consumerNumber()]],
    billingPeriod: ['', [Validators.required]],
    billDate: [null as Date | null, [Validators.required]],
    dueDate: [null as Date | null, [Validators.required]],
    disconnectionDate: [null as Date | null],
    billAmount: [null as number | null, [Validators.required, Validators.min(0)]],
    lateFee: [0, [Validators.min(0)]],
    status: ['UNPAID']
  }, { validators: CustomValidators.dateNotBefore('billDate', 'dueDate') });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    const value = this.form.getRawValue();
    this.billService.addBill({
      consumerNumber: value.consumerNumber,
      billingPeriod: value.billingPeriod,
      billDate: this.toIso(value.billDate!),
      dueDate: this.toIso(value.dueDate!),
      disconnectionDate: value.disconnectionDate ? this.toIso(value.disconnectionDate) : null,
      billAmount: value.billAmount!,
      lateFee: value.lateFee ?? 0,
      status: value.status as 'PAID' | 'UNPAID'
    }).subscribe({
      next: (bill) => {
        this.submitting.set(false);
        this.created.set(bill);
      },
      error: () => this.submitting.set(false)
    });
  }

  private toIso(date: Date): string {
    return date.toISOString().substring(0, 10);
  }

  addAnother(): void {
    this.created.set(null);
    this.form.reset({ lateFee: 0, status: 'UNPAID' });
  }
}
