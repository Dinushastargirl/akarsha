export interface AppointmentSummary {
  id: number;
  customerName: string;
  staffName: string;
  serviceName: string;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  status: 'BOOKED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';
  price: number;
}

export interface DashboardStats {
  todayTotal: number;
  todayCompleted: number;
  todayCancelled: number;
  todayEstimatedRevenue: number;
  totalCustomers: number;
  activeStaff: number;
  activeServices: number;
  todayTimeline: AppointmentSummary[];
  upcomingAppointments: AppointmentSummary[];
}
