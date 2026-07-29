import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormControl } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomValidators } from '../../../shared/validators/custom-validators';
import { BillService } from '../../../core/services/bill.service';
import { ConsumerService } from '../../../core/services/consumer.service';
import { Bill } from '../../../core/models/bill.model';
import { Consumer } from '../../../core/models/consumer.model';

@Component({
  selector: 'app-add-bill',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatDatepickerModule, MatNativeDateModule, MatButtonModule],
  templateUrl: './add-bill.component.html',
  styleUrl: './add-bill.component.scss'
})
export class AddBillComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly billService = inject(BillService);
  private readonly consumerService = inject(ConsumerService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly submitting = signal(false);
  readonly created = signal<Bill | null>(null);
  
  readonly consumers = signal<Consumer[]>([]);
  readonly billingPeriods = signal<string[]>([]);
  readonly maxDate = new Date();

  readonly searchControl = new FormControl('');
  private readonly searchValue = toSignal(this.searchControl.valueChanges, { initialValue: '' });

  readonly filteredConsumers = computed(() => {
    const search = this.searchValue()?.toLowerCase() || '';
    const list = this.consumers();
    if (!search) return list;
    return list.filter(c => 
      c.consumerNumber.toLowerCase().includes(search) || 
      c.customerName.toLowerCase().includes(search)
    );
  });

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

  ngOnInit(): void {
    this.consumerService.list(null, null, 0, 1000).subscribe(page => {
      this.consumers.set(page.content);
    });
    this.billingPeriods.set(this.generateBillingPeriods());
  }

  private generateBillingPeriods(): string[] {
    const periods: string[] = [];
    const now = new Date();
    const months = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'];
    for (let i = -11; i <= 0; i++) {
      const d = new Date(now.getFullYear(), now.getMonth() + i, 1);
      periods.push(`${months[d.getMonth()]}-${d.getFullYear()}`);
    }
    return periods.reverse();
  }

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
        this.snackBar.open('Bill generated successfully', 'Close', { duration: 3000 });
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
