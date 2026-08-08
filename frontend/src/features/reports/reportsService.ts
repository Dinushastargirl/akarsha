import type { 
  ReportOverview, RevenueReport, AppointmentReport, 
  ServicePerformance, StaffPerformance, CustomerInsights, NoShowReport 
} from './types';

const API_BASE_URL = '/api/v1/reports';

async function fetchWithAuth(url: string) {
  const token = localStorage.getItem('akarsha_token');
  const response = await fetch(url, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    if (response.status === 401 || response.status === 403) {
      throw new Error('Unauthorized');
    }
    throw new Error('Failed to fetch report data');
  }

  return response.json();
}

export const reportsService = {
  getOverview: (startDate: string, endDate: string, tz: string = Intl.DateTimeFormat().resolvedOptions().timeZone): Promise<ReportOverview> => {
    return fetchWithAuth(`${API_BASE_URL}/overview?startDate=${startDate}&endDate=${endDate}&tz=${encodeURIComponent(tz)}`);
  },

  getRevenue: (startDate: string, endDate: string, tz: string = Intl.DateTimeFormat().resolvedOptions().timeZone): Promise<RevenueReport> => {
    return fetchWithAuth(`${API_BASE_URL}/revenue?startDate=${startDate}&endDate=${endDate}&tz=${encodeURIComponent(tz)}`);
  },

  getAppointments: (startDate: string, endDate: string): Promise<AppointmentReport> => {
    return fetchWithAuth(`${API_BASE_URL}/appointments?startDate=${startDate}&endDate=${endDate}`);
  },

  getServices: (startDate: string, endDate: string): Promise<ServicePerformance[]> => {
    return fetchWithAuth(`${API_BASE_URL}/services?startDate=${startDate}&endDate=${endDate}`);
  },

  getStaff: (startDate: string, endDate: string): Promise<StaffPerformance[]> => {
    return fetchWithAuth(`${API_BASE_URL}/staff?startDate=${startDate}&endDate=${endDate}`);
  },

  getCustomers: (startDate: string, endDate: string, tz: string = Intl.DateTimeFormat().resolvedOptions().timeZone): Promise<CustomerInsights> => {
    return fetchWithAuth(`${API_BASE_URL}/customers?startDate=${startDate}&endDate=${endDate}&tz=${encodeURIComponent(tz)}`);
  },

  getNoShows: (startDate: string, endDate: string): Promise<NoShowReport> => {
    return fetchWithAuth(`${API_BASE_URL}/no-shows?startDate=${startDate}&endDate=${endDate}`);
  }
};
