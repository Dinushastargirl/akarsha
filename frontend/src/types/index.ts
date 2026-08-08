export interface SalonData {
  name: string;
  subdomain: string;
  phone: string;
  address: string;
  city: string;
  businessType: string;
  openingTime?: string;
  closingTime?: string;
}

export interface ServiceData {
  name: string;
  price: string;
  duration: string;
}

export interface StaffData {
  name: string;
  email: string;
}

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: string;
  phone?: string;
  active: boolean;
  services?: Array<{ id: number; name: string; price: number; durationMinutes: number }>;
}

export interface StaffSchedule {
  id: number;
  dayOfWeek: number;
  working: boolean;
  startTime: string;
  endTime: string;
}

export interface Customer {
  id: number;
  fullName: string;
  phone: string;
  email?: string;
  birthday?: string;
  notes?: string;
  createdAt: string;
}

export interface CustomerStats {
  totalVisits: number;
  completedVisits: number;
  noShowCount: number;
  totalRevenue: number;
  lastVisitDate?: string;
}

export interface Service {
  id: number;
  name: string;
  price: number;
  durationMinutes: number;
  active: boolean;
  tenantId?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Appointment {
  id: number;
  customer: Customer;
  service: {
    id: number;
    name: string;
    price: number;
    durationMinutes: number;
  };
  staff: User;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  status: 'BOOKED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';
  notes?: string;
  createdAt: string;
}
