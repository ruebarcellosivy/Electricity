import { CustomerStatus, CustomerType, ElectricalSection } from './enums';

export interface Customer {
  id: number;
  customerCode: string;
  fullName: string;
  address: string;
  email: string;
  mobileNumber: string;
  customerType: CustomerType;
  electricalSection: ElectricalSection;
  status: CustomerStatus;
  userId: string;
  consumerNumbers: string[];
  createdAt: string;
}

export interface AdminCreateCustomerRequest {
  fullName: string;
  address: string;
  email: string;
  mobileNumber: string;
  customerType: string;
  electricalSection: string;
}

export interface UpdateCustomerRequest {
  fullName: string;
  address: string;
  email: string;
  mobileNumber: string;
  customerType: string;
}

export interface HomeSummary {
  profile: Customer;
  latestBill: import('./bill.model').Bill | null;
  unpaidBillCount: number;
  openComplaintCount: number;
}
