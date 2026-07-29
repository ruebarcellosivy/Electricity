import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth.service';
import { CustomValidators } from '../../../shared/validators/custom-validators';
import { CUSTOMER_TYPES, ELECTRICAL_SECTIONS } from '../../../core/models/enums';
import { RegisterResponse } from '../../../core/models/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatSelectModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly customerTypes = CUSTOMER_TYPES;
  readonly electricalSections = ELECTRICAL_SECTIONS;
  readonly hidePassword = signal(true);
  readonly hideConfirmPassword = signal(true);
  readonly submitting = signal(false);
  readonly successResult = signal<RegisterResponse | null>(null);

  readonly form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(50), CustomValidators.nameOnly()]],
    address: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(200)]],
    email: ['', [Validators.required, Validators.email]],
    mobileNumber: ['', [Validators.required, CustomValidators.mobileNumber()]],
    customerType: ['', [Validators.required]],
    electricalSection: ['', [Validators.required]],
    password: ['', [Validators.required, CustomValidators.strongPassword()]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: CustomValidators.matchFields('password', 'confirmPassword') });

  togglePassword(field: 'password' | 'confirm'): void {
    if (field === 'password') this.hidePassword.update((v) => !v);
    else this.hideConfirmPassword.update((v) => !v);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.authService.register(this.form.getRawValue()).subscribe({
      next: (response) => {
        this.submitting.set(false);
        this.successResult.set(response);
        this.snackBar.open('Registration successful!', 'Close', { duration: 4000 });
      },
      error: (err) => {
        this.submitting.set(false);
        let errorMessage = err.error?.message || 'Registration failed. Please try again.';
        
        if (err.error?.fieldErrors && Object.keys(err.error.fieldErrors).length > 0) {
          errorMessage = Object.values(err.error.fieldErrors).join(' | ');
        }
        
        this.snackBar.open(errorMessage, 'Close', { duration: 6000 });
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
