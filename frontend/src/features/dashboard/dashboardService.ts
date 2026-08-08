import { apiFetch } from '../../services/api';
import type { DashboardStats } from '../../types/dashboard';

export const dashboardService = {
  async getStats(): Promise<DashboardStats> {
    const res = await apiFetch('/dashboard');
    if (!res.ok) throw new Error('Failed to load dashboard');
    return res.json();
  },
};
