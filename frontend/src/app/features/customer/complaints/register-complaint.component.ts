import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ComplaintService } from '../../../core/services/complaint.service';
import { ConsumerService } from '../../../core/services/consumer.service';
import { CustomerService } from '../../../core/services/customer.service';
import { COMPLAINT_TYPES, CONTACT_METHODS, ComplaintType, ContactMethod } from '../../../core/models/enums';
import { Consumer } from '../../../core/models/consumer.model';
import { Complaint } from '../../../core/models/complaint.model';

@Component({
  selector: 'app-register-complaint',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatSelectModule,
    MatInputModule, MatRadioModule, MatButtonModule, MatIconModule],
  templateUrl: './register-complaint.component.html',
  styleUrl: './register-complaint.component.scss'
})
export class RegisterComplaintComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly complaintService = inject(ComplaintService);
  private readonly consumerService = inject(ConsumerService);
  private readonly customerService = inject(CustomerService);
  private readonly router = inject(Router);

  readonly complaintTypes = COMPLAINT_TYPES;
  readonly contactMethods = CONTACT_METHODS;
  readonly consumers = signal<Consumer[]>([]);
  readonly categoriesMap = signal<Record<string, string[]>>({});
  readonly submitting = signal(false);
  readonly result = signal<Complaint | null>(null);

  readonly selectedComplaintType = signal<string>('');

  readonly categoryOptions = computed(() => {
    const type = this.selectedComplaintType();
    return type ? this.categoriesMap()[type] || [] : [];
  });

  readonly form = this.fb.nonNullable.group({
    consumerNumber: ['', [Validators.required]],
    complaintType: ['' as ComplaintType, [Validators.required]],
    category: ['', [Validators.required]],
    description: ['', [Validators.required, Validators.maxLength(1000)]],
    preferredContactMethod: ['EMAIL' as ContactMethod, [Validators.required]],
    contactDetails: ['', [Validators.required, Validators.maxLength(100)]]
  });

  ngOnInit(): void {
    this.consumerService.myConsumers().subscribe((consumers) => {
      this.consumers.set(consumers);
      if (consumers.length === 1) {
        this.form.controls.consumerNumber.setValue(consumers[0].consumerNumber);
      }
    });
    this.complaintService.categories().subscribe((map) => this.categoriesMap.set(map));
    this.customerService.myProfile().subscribe((profile) => {
      this.form.controls.contactDetails.setValue(profile.email);
    });

    this.form.controls.complaintType.valueChanges.subscribe((type) => {
      this.selectedComplaintType.set(type);
      this.form.controls.category.setValue('');
    });
    this.form.controls.preferredContactMethod.valueChanges.subscribe((method) => {
      this.customerService.myProfile().subscribe((profile) => {
        this.form.controls.contactDetails.setValue(method === 'EMAIL' ? profile.email : profile.mobileNumber);
      });
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.complaintService.register(this.form.getRawValue()).subscribe({
      next: (complaint) => {
        this.submitting.set(false);
        this.result.set(complaint);
      },
      error: () => this.submitting.set(false)
    });
  }

  reset(): void {
    this.form.reset({ preferredContactMethod: 'EMAIL' });
  }
}
