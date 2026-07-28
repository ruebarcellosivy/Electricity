import { PaymentMethod, TransactionStatus, TransactionType } from './enums';

export interface PayBillRequest {
  billIds: number[];
  cardNumber: string;
  expiryDate: string;
  cvv: string;
  cardHolderName: string;
  paymentMethod: PaymentMethod;
}

export interface PaymentResponse {
  paymentId: string;
  transactionId: string;
  receiptNumber: string;
  transactionDate: string;
  transactionType: TransactionType;
  billNumber: string;
  transactionAmount: number;
  transactionStatus: TransactionStatus;
}

export interface InvoiceResponse {
  invoiceNumber: string;
  paymentId: string;
  transactionId: string;
  receiptNumber: string;
  consumerNumber: string;
  customerName: string;
  address: string;
  transactionDate: string;
  transactionType: TransactionType;
  billNumber: string;
  transactionAmount: number;
  transactionStatus: TransactionStatus;
}
