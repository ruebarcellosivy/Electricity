import { BillStatus, ConnectionStatus, CustomerType } from './enums';

export interface Bill {
  id: number;
  billNumber: string;
  consumerNumber: string;
  customerName: string;
  mobileNumber: string;
  connectionType: CustomerType;
  connectionStatus: ConnectionStatus;
  billingPeriod: string;
  billDate: string;
  dueDate: string;
  disconnectionDate: string | null;
  billAmount: number;
  lateFee: number;
  payableAmount: number;
  status: BillStatus;
  paymentDate: string | null;
}

export interface AddBillRequest {
  consumerNumber: string;
  billingPeriod: string;
  billDate: string;
  dueDate: string;
  disconnectionDate?: string | null;
  billAmount: number;
  lateFee: number;
  status?: BillStatus;
}

export interface BillSelectionSummary {
  bills: Bill[];
  totalAmount: number;
}

export interface BulkUploadResult {
  totalRows: number;
  successCount: number;
  failureCount: number;
  errors: string[];
}
