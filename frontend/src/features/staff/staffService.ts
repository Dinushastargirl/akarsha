import { apiFetch } from '../../services/api';
import type { User } from '../../types';

export const staffService = {
  async getStaff(query?: string, active?: boolean, page: number = 0) {
    let url = `/staff?page=${page}&size=15`;
    if (query) url += `&query=${encodeURIComponent(query)}`;
    if (active !== undefined) url += `&active=${active}`;
    return apiFetch(url);
  },

  async getStaffById(id: number) {
    return apiFetch(`/staff/${id}`);
  },

  async createStaff(payload: Omit<User, 'id' | 'services'>) {
    return apiFetch('/staff', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async updateStaff(id: number, payload: Omit<User, 'id' | 'services'>) {
    return apiFetch(`/staff/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
  },

  async patchStatus(id: number, active: boolean) {
    return apiFetch(`/staff/${id}/status?active=${active}`, {
      method: 'PATCH'
    });
  },

  async deleteStaff(id: number) {
    return apiFetch(`/staff/${id}`, {
      method: 'DELETE'
    });
  },

  async getSchedule(id: number) {
    return apiFetch(`/staff/${id}/schedule`);
  },

  async updateSchedule(id: number, payload: Array<{ dayOfWeek: number; working: boolean; startTime: string; endTime: string }>) {
    return apiFetch(`/staff/${id}/schedule`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
  },

  async getServices(id: number) {
    return apiFetch(`/staff/${id}/services`);
  },

  async updateServices(id: number, serviceIds: number[]) {
    return apiFetch(`/staff/${id}/services`, {
      method: 'PUT',
      body: JSON.stringify(serviceIds)
    });
  }
};
