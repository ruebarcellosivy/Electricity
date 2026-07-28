import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth.service';
import { CustomValidators } from '../../../shared/validators/custom-validators';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.scss'
})
export class ChangePasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly submitting = signal(false);
  readonly mustChangePassword = this.authService.currentUser()?.mustChangePassword ?? false;

  readonly form = this.fb.nonNullable.group({
    oldPassword: ['', [Validators.required]],
    password: ['', [Validators.required, CustomValidators.strongPassword()]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: CustomValidators.matchFields('password', 'confirmPassword') });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.authService.changePassword(this.form.getRawValue()).subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open('Password changed successfully.', 'Close', { duration: 4000 });
        this.router.navigate(['/customer/dashboard']);
      },
      error: () => this.submitting.set(false)
    });
  }
}
