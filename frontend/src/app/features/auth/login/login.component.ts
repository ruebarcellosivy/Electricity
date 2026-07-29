import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly hidePassword = signal(true);
  readonly submitting = signal(false);

  readonly form = this.fb.nonNullable.group({
    userId: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  togglePasswordVisibility(): void {
    this.hidePassword.update((v) => !v);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.authService.login(this.form.getRawValue()).subscribe({
      next: (response) => {
        this.submitting.set(false);
        if (response.mustChangePassword) {
          this.snackBar.open('Please change your password', 'Close', { duration: 3000 });
          this.router.navigate(['/change-password']);
          return;
        }
        this.snackBar.open('Successfully signed in', 'Close', { duration: 3000 });
        switch (response.role) {
          case 'ADMIN':
            this.router.navigate(['/admin/customers']);
            break;
          case 'SME':
            this.router.navigate(['/sme/complaints']);
            break;
          default:
            this.router.navigate(['/customer/dashboard']);
        }
      },
      error: () => this.submitting.set(false)
    });
  }
}
