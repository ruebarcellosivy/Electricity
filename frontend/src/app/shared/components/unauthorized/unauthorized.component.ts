import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  template: `
    <div class="wrapper">
      <mat-card class="message-card">
        <mat-icon class="icon">block</mat-icon>
        <h2>Access Denied</h2>
        <p>You do not have permission to view this page.</p>
        <button mat-flat-button color="primary" routerLink="/login">Back to Login</button>
      </mat-card>
    </div>
  `,
  styles: [`
    .wrapper { display: flex; align-items: center; justify-content: center; min-height: 60vh; }
    .message-card { text-align: center; padding: 32px; max-width: 360px; }
    .icon { font-size: 48px; width: 48px; height: 48px; color: #c62828; }
  `]
})
export class UnauthorizedComponent {}
