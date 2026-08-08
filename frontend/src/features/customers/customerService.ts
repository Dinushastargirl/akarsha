import { apiFetch } from '../../services/api';
import type { Customer } from '../../types';

export const customerService = {
  async getCustomers(page: number, size: number = 10) {
    return apiFetch(`/customers?page=${page}&size=${size}`);
  },

  async searchCustomers(query: string, page: number, size: number = 10) {
    return apiFetch(`/customers/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`);
  },

  async getCustomerById(id: number) {
    return apiFetch(`/customers/${id}`);
  },

  async getCustomerStats(id: number) {
    return apiFetch(`/customers/${id}/stats`);
  },

  async getCustomerAppointments(id: number, page: number, size: number = 10) {
    return apiFetch(`/appointments?customerId=${id}&page=${page}&size=${size}`);
  },

  async getCustomerInvoices(id: number, page: number, size: number = 10) {
    return apiFetch(`/customers/${id}/invoices?page=${page}&size=${size}`);
  },

  async createCustomer(customer: Omit<Customer, 'id' | 'createdAt'>) {
    return apiFetch('/customers', {
      method: 'POST',
      body: JSON.stringify(customer)
    });
  },

  async updateCustomer(id: number, customer: Omit<Customer, 'id' | 'createdAt'>) {
    return apiFetch(`/customers/${id}`, {
      method: 'PUT',
      body: JSON.stringify(customer)
    });
  },

  async deleteCustomer(id: number) {
    return apiFetch(`/customers/${id}`, {
      method: 'DELETE'
    });
  }
};
