import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomerService } from '../../../core/services/customer.service';
import { CustomValidators } from '../../../shared/validators/custom-validators';
import { CUSTOMER_TYPES, ELECTRICAL_SECTIONS } from '../../../core/models/enums';
import { Customer } from '../../../core/models/customer.model';

@Component({
  selector: 'app-customer-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatButtonModule, MatIconModule],
  templateUrl: './customer-form.component.html',
  styleUrl: './customer-form.component.scss'
})
export class CustomerFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly customerService = inject(CustomerService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly customerTypes = CUSTOMER_TYPES;
  readonly electricalSections = ELECTRICAL_SECTIONS;
  readonly isEditMode = signal(false);
  readonly submitting = signal(false);
  readonly createdCustomer = signal<Customer | null>(null);
  readonly customerId = signal<number | null>(null);
  readonly existingCustomer = signal<Customer | null>(null);

  readonly form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(50), CustomValidators.nameOnly()]],
    address: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(200)]],
    email: ['', [Validators.required, Validators.email]],
    mobileNumber: ['', [Validators.required, CustomValidators.mobileNumber()]],
    customerType: ['RESIDENTIAL', [Validators.required]],
    electricalSection: ['REGION', [Validators.required]]
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode.set(true);
      this.customerId.set(Number(idParam));
      this.customerService.getById(Number(idParam)).subscribe((customer) => {
        this.existingCustomer.set(customer);
        this.form.patchValue(customer);
      });
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    const value = this.form.getRawValue();

    if (this.isEditMode()) {
      this.customerService.update(this.customerId()!, value).subscribe({
        next: () => {
          this.submitting.set(false);
          this.snackBar.open('Customer details updated successfully.', 'Close', { duration: 4000 });
          this.router.navigate(['/admin/customers']);
        },
        error: () => this.submitting.set(false)
      });
    } else {
      this.customerService.create(value).subscribe({
        next: (customer) => {
          this.submitting.set(false);
          this.createdCustomer.set(customer);
        },
        error: () => this.submitting.set(false)
      });
    }
  }

  goToAddConsumer(): void {
    const customer = this.createdCustomer();
    if (!customer) return;
    this.router.navigate(['/admin/consumers'], { queryParams: { customerId: customer.id, customerCode: customer.customerCode } });
  }
}
