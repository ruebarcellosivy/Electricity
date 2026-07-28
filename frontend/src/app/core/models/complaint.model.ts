import { ComplaintStatus, ComplaintType, ContactMethod } from './enums';

export interface Remark {
  remark: string;
  statusAtTime: ComplaintStatus;
  updatedBy: string;
  createdAt: string;
}

export interface Complaint {
  id: number;
  complaintNumber: string;
  consumerNumber: string;
  customerName: string;
  complaintType: ComplaintType;
  category: string;
  description: string;
  preferredContactMethod: ContactMethod;
  contactDetails: string;
  status: ComplaintStatus;
  assignedTo: string | null;
  resolutionDueAt: string | null;
  createdAt: string;
  updatedAt: string;
  remarks: Remark[];
}

export interface ComplaintRequest {
  consumerNumber: string;
  complaintType: ComplaintType;
  category: string;
  description: string;
  preferredContactMethod: ContactMethod;
  contactDetails: string;
}

export interface ComplaintStatusUpdateRequest {
  status: ComplaintStatus;
  remark: string;
  assignedTo?: string;
}
