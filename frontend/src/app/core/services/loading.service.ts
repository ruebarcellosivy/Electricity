import { Injectable, signal } from '@angular/core';

/** Tracks the number of in-flight HTTP requests so a single global spinner can be shown. */
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private requestCount = 0;
  readonly isLoading = signal(false);

  show(): void {
    this.requestCount++;
    this.isLoading.set(true);
  }

  hide(): void {
    this.requestCount = Math.max(0, this.requestCount - 1);
    this.isLoading.set(this.requestCount > 0);
  }
}
