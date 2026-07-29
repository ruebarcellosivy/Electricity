import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatRadioModule } from '@angular/material/radio';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ConsumerService } from '../../../core/services/consumer.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CustomValidators } from '../../../shared/validators/custom-validators';
import { CUSTOMER_TYPES, ELECTRICAL_SECTIONS } from '../../../core/models/enums';
import { Consumer } from '../../../core/models/consumer.model';

const COLUMNS = ['consumerNumber', 'customerCode', 'customerName', 'customerType', 'connectionStatus', 'action'];

@Component({
  selector: 'app-consumer-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule, MatTableModule, MatChipsModule, MatRadioModule, MatPaginatorModule],
  templateUrl: './consumer-management.component.html',
  styleUrl: './consumer-management.component.scss'
})
export class ConsumerManagementComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly consumerService = inject(ConsumerService);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly columns = COLUMNS;
  readonly customerTypes = CUSTOMER_TYPES;
  readonly electricalSections = ELECTRICAL_SECTIONS;

  readonly consumers = signal<Consumer[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly sectionFilter = signal<string>('');
  readonly typeFilter = signal<string>('');
  readonly prefillCustomerCode = signal<string>('');
  readonly submitting = signal(false);

  readonly form = this.fb.nonNullable.group({
    customerId: [0, [Validators.required, Validators.min(1)]]
  });

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const customerId = params.get('customerId');
      const customerCode = params.get('customerCode');
      if (customerId) this.form.controls.customerId.setValue(Number(customerId));
      if (customerCode) this.prefillCustomerCode.set(customerCode);
    });
    this.load();
  }

  load(): void {
    this.consumerService.list(this.sectionFilter() || null, this.typeFilter() || null,
      this.pageIndex(), this.pageSize()).subscribe((page) => {
      this.consumers.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  onFilterChange(): void {
    this.pageIndex.set(0);
    this.load();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.consumerService.add(this.form.getRawValue()).subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open('Consumer Number auto-generated and linked successfully.', 'Close', { duration: 4000 });
        this.load();
      },
      error: () => this.submitting.set(false)
    });
  }

  onConnectionStatusChangeAction(action: 'DISCONNECT' | 'RECONNECT', consumer: Consumer): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: action === 'DISCONNECT' ? 'Disconnect Consumer' : 'Reconnect Consumer',
        message: `Are you sure you want to ${action === 'DISCONNECT' ? 'disconnect' : 'reconnect'} consumer number ${consumer.consumerNumber}?`,
        confirmLabel: action === 'DISCONNECT' ? 'Disconnect' : 'Reconnect'
      }
    });
    dialogRef.afterClosed().subscribe((confirmed) => {
      if (!confirmed) {
        return;
      }
      this.consumerService.updateConnectionStatus(consumer.consumerNumber, { action }).subscribe({
        next: () => {
          this.snackBar.open(`Consumer ${action === 'DISCONNECT' ? 'deactivated' : 'activated'} successfully.`, 'Close', { duration: 4000 });
          this.load();
        },
        error: () => this.load()
      });
    });
  }
}
