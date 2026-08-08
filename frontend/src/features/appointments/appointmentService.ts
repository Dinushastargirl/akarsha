import { apiFetch } from '../../services/api';

export const appointmentService = {
  async getAppointments(date?: string, staffId?: number, customerId?: number, status?: string, page: number = 0) {
    let url = `/appointments?page=${page}&size=15`;
    if (date) url += `&date=${date}`;
    if (staffId) url += `&staffId=${staffId}`;
    if (customerId) url += `&customerId=${customerId}`;
    if (status) url += `&status=${status}`;
    return apiFetch(url);
  },

  async getAppointmentById(id: number) {
    return apiFetch(`/appointments/${id}`);
  },

  async createAppointment(payload: {
    customerId: number;
    serviceId: number;
    staffId: number;
    appointmentDate: string;
    startTime: string;
    endTime: string;
    notes?: string;
  }) {
    return apiFetch('/appointments', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async updateAppointment(id: number, payload: {
    customerId: number;
    serviceId: number;
    staffId: number;
    appointmentDate: string;
    startTime: string;
    endTime: string;
    notes?: string;
  }) {
    return apiFetch(`/appointments/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
  },

  async patchStatus(id: number, status: string) {
    return apiFetch(`/appointments/${id}/status?status=${status}`, {
      method: 'PATCH'
    });
  },

  async deleteAppointment(id: number) {
    return apiFetch(`/appointments/${id}`, {
      method: 'DELETE'
    });
  },

  async checkoutAppointment(id: number, payload: {
    paymentMethod: string;
    taxAmount: number;
    discountAmount: number;
    notes?: string;
    lineItems: {
      itemType: string;
      referenceId?: number;
      description: string;
      quantity: number;
      unitPrice: number;
    }[];
  }) {
    return apiFetch(`/checkout/appointment/${id}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async getStaff() {
    return apiFetch('/appointments/staff');
  },

  async getSlots(staffId: number, date: string, durationMinutes: number) {
    return apiFetch(`/appointments/slots?staffId=${staffId}&date=${date}&durationMinutes=${durationMinutes}`);
  },

  async getServices() {
    return apiFetch('/appointments/services');
  }
};
