export type Role = 'CUSTOMER' | 'ADMIN' | 'SME';

export type CustomerType = 'RESIDENTIAL' | 'COMMERCIAL';
export type ElectricalSection = 'OFFICE' | 'REGION';
export type CustomerStatus = 'ACTIVE' | 'INACTIVE';
export type ConnectionStatus = 'CONNECTED' | 'DISCONNECTED';
export type BillStatus = 'UNPAID' | 'PAID';
export type ComplaintType = 'BILLING_ISSUE' | 'POWER_OUTAGE' | 'METER_ISSUE' | 'OTHER';
export type ComplaintStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
export type ContactMethod = 'EMAIL' | 'PHONE';
export type TransactionType = 'CREDIT' | 'DEBIT';
export type TransactionStatus = 'SUCCESS' | 'FAILED';
export type PaymentMethod = 'CREDIT_CARD' | 'DEBIT_CARD' | 'NET_BANKING';

export const CUSTOMER_TYPES: CustomerType[] = ['RESIDENTIAL', 'COMMERCIAL'];
export const ELECTRICAL_SECTIONS: ElectricalSection[] = ['OFFICE', 'REGION'];
export const COMPLAINT_TYPES: ComplaintType[] = ['BILLING_ISSUE', 'POWER_OUTAGE', 'METER_ISSUE', 'OTHER'];
export const COMPLAINT_STATUSES: ComplaintStatus[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
export const CONTACT_METHODS: ContactMethod[] = ['EMAIL', 'PHONE'];
export const PAYMENT_METHODS: PaymentMethod[] = ['CREDIT_CARD', 'DEBIT_CARD', 'NET_BANKING'];
