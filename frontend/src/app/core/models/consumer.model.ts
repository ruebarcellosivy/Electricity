import { ConnectionStatus, CustomerType } from './enums';

export interface Consumer {
  id: number;
  consumerNumber: string;
  connectionStatus: ConnectionStatus;
  customerId: number;
  customerCode: string;
  customerName: string;
  customerType: CustomerType;
  createdAt: string;
}

export interface AddConsumerRequest {
  customerId: number;
}

export interface ConnectionStatusUpdateRequest {
  action: 'DISCONNECT' | 'RECONNECT';
}
