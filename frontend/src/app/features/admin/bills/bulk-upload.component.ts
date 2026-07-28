import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { BillService } from '../../../core/services/bill.service';
import { BulkUploadResult } from '../../../core/models/bill.model';

@Component({
  selector: 'app-bulk-upload',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatListModule],
  templateUrl: './bulk-upload.component.html',
  styleUrl: './bulk-upload.component.scss'
})
export class BulkUploadComponent {
  private readonly billService = inject(BillService);

  readonly selectedFile = signal<File | null>(null);
  readonly uploading = signal(false);
  readonly result = signal<BulkUploadResult | null>(null);

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files?.[0] ?? null);
    this.result.set(null);
  }

  upload(): void {
    const file = this.selectedFile();
    if (!file) return;
    this.uploading.set(true);
    this.billService.bulkUpload(file).subscribe({
      next: (result) => {
        this.uploading.set(false);
        this.result.set(result);
      },
      error: () => this.uploading.set(false)
    });
  }
}
