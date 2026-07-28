import { Component } from '@angular/core';
import { ComplaintManagementComponent } from '../../../shared/components/complaint-management/complaint-management.component';

@Component({
  selector: 'app-admin-complaint-list',
  standalone: true,
  imports: [ComplaintManagementComponent],
  template: `
    <app-complaint-management
      [allowAssign]="true"
      title="Complaints"
      subtitle="Search complaints, update their status and assign them to an SME">
    </app-complaint-management>
  `
})
export class AdminComplaintListComponent {}
