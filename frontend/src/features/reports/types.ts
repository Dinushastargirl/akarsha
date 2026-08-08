export interface ReportOverview {
  grossRevenue: number;
  totalPaid: number;
  averageTransactionValue: number;
  numberOfPaidInvoices: number;
  totalAppointments: number;
  completedAppointments: number;
  cancelledAppointments: number;
  noShows: number;
  totalCustomers: number;
  newCustomers: number;
  returningCustomers: number;
}

export interface RevenueByDate {
  date: string;
  amount: number;
}

export interface RevenueReport {
  revenueByDate: RevenueByDate[];
  revenueByMethod: Record<string, number>;
}

export interface AppointmentByDate {
  date: string;
  total: number;
  completed: number;
  cancelled: number;
  noShow: number;
}

export interface AppointmentReport {
  appointmentsByDate: AppointmentByDate[];
}

export interface ServicePerformance {
  serviceId: number;
  serviceName: string;
  appointmentCount: number;
  completedAppointments: number;
  cancelledAppointments: number;
  noShowAppointments: number;
  revenue: number;
  averagePrice: number;
}

export interface StaffPerformance {
  staffId: number;
  staffName: string;
  appointmentsAssigned: number;
  completedAppointments: number;
  cancelledAppointments: number;
  noShowAppointments: number;
  revenueGenerated: number;
  completionRate: number;
}

export interface CustomerInsights {
  totalCustomers: number;
  newCustomers: number;
  returningCustomers: number;
  repeatVisitRate: number;
  averageVisitsPerCustomer: number;
  customersWithCompletedVisits: number;
  customersWithNoShows: number;
}

export interface NoShowReport {
  totalNoShows: number;
  noShowRate: number;
  noShowsByService: { serviceId: number; serviceName: string; count: number }[];
  noShowsByStaff: { staffId: number; staffName: string; count: number }[];
  noShowsByDate: { date: string; count: number }[];
}
