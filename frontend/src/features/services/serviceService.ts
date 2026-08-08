import { apiFetch } from '../../services/api';
import type { Service } from '../../types';

export const serviceService = {
  async getServices(query?: string, active?: boolean, page: number = 0) {
    let url = `/services?page=${page}&size=20`;
    if (query) url += `&query=${encodeURIComponent(query)}`;
    if (active !== undefined) url += `&active=${active}`;
    return apiFetch(url);
  },

  async getServiceById(id: number) {
    return apiFetch(`/services/${id}`);
  },

  async createService(payload: Omit<Service, 'id' | 'tenantId' | 'createdAt' | 'updatedAt'>) {
    return apiFetch('/services', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async updateService(id: number, payload: Omit<Service, 'id' | 'tenantId' | 'createdAt' | 'updatedAt'>) {
    return apiFetch(`/services/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
  },

  async patchStatus(id: number, active: boolean) {
    return apiFetch(`/services/${id}/status?active=${active}`, {
      method: 'PATCH'
    });
  },

  async deleteService(id: number) {
    return apiFetch(`/services/${id}`, {
      method: 'DELETE'
    });
  }
};
