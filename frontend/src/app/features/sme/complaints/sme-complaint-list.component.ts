import { Component } from '@angular/core';
import { ComplaintManagementComponent } from '../../../shared/components/complaint-management/complaint-management.component';

@Component({
  selector: 'app-sme-complaint-list',
  standalone: true,
  imports: [ComplaintManagementComponent],
  template: `
    <app-complaint-management
      [allowAssign]="false"
      title="Assigned Complaints"
      subtitle="Search complaints, update their status and add remarks">
    </app-complaint-management>
  `
})
export class SmeComplaintListComponent {}
