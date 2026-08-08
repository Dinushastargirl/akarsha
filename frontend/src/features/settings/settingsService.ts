import { apiFetch } from '../../services/api';
import type { SalonData } from '../../types';

export const settingsService = {
  async getSettings() {
    return apiFetch('/settings');
  },

  async updateSettings(payload: Partial<SalonData>) {
    return apiFetch('/settings', {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
  }
};
