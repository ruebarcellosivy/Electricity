import { Role } from './enums';

export interface LoginRequest {
  userId: string;
  password: string;
}

export interface LoginResponse {
  userId: string;
  role: Role;
  fullName: string;
  customerCode: string | null;
  mustChangePassword: boolean;
}

export interface RegisterRequest {
  fullName: string;
  address: string;
  email: string;
  mobileNumber: string;
  customerType: string;
  electricalSection: string;
  password: string;
  confirmPassword: string;
}

export interface RegisterResponse {
  userId: string;
  customerCode: string;
  consumerNumber: string;
  fullName: string;
  email: string;
  message: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  password: string;
  confirmPassword: string;
}

export interface MessageResponse {
  message: string;
}
