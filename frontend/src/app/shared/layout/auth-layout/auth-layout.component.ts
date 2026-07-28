import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [RouterOutlet, MatIconModule],
  template: `
    <div class="auth-shell">
      <div class="auth-brand">
        <mat-icon>bolt</mat-icon>
        <h1>Electricity Billing System</h1>
        <p>Manage your connection, bills, payments and complaints in one place.</p>
      </div>
      <div class="auth-panel">
        <router-outlet></router-outlet>
      </div>
    </div>
  `,
  styles: [`
    .auth-shell {
      min-height: 100vh;
      display: flex;
      flex-wrap: wrap;
    }

    .auth-brand {
      flex: 1 1 380px;
      background: linear-gradient(135deg, #3f51b5, #283593);
      color: white;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px 24px;
      text-align: center;

      mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        margin-bottom: 16px;
      }

      h1 {
        margin: 0 0 8px;
        font-size: 1.8rem;
      }

      p {
        max-width: 340px;
        opacity: 0.9;
      }
    }

    .auth-panel {
      flex: 1 1 420px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 32px;
      background: #f5f6fa;
    }
  `]
})
export class AuthLayoutComponent {}
