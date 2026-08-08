import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { RefreshCw, AlertCircle } from 'lucide-react';

import { dashboardService } from '../features/dashboard/dashboardService';
import { StatCard } from '../features/dashboard/StatCard';
import { TodayTimeline } from '../features/dashboard/TodayTimeline';
import { UpcomingList } from '../features/dashboard/UpcomingList';
import { QuickActions } from '../features/dashboard/QuickActions';
import type { DashboardStats } from '../types/dashboard';
import type { SalonData } from '../types';

interface DashboardProps {
  salon: SalonData;
  emailInput: string;
  onNavigateToCustomers: () => void;
  onNavigateToAppointments: () => void;
  onNavigateToStaff: () => void;
  onNavigateToServices: () => void;
}

export const Dashboard: React.FC<DashboardProps> = ({
  onNavigateToCustomers,
  onNavigateToAppointments,
  onNavigateToStaff,
  onNavigateToServices,
}) => {
  const { t } = useTranslation();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await dashboardService.getStats();
      setStats(data);
    } catch {
      setError(t('dashLoadFailed'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  // ── Loading ──────────────────────────────────────────────────────────────────
  if (loading) {
    return (
      <div className="w-full max-w-4xl mx-auto animate-subtle-fade">
        <div className="flex items-center gap-2 text-neutral-400 mb-8">
          <RefreshCw className="w-4 h-4 animate-spin" />
          <span className="text-sm">{t('loading')}</span>
        </div>
        {/* Skeleton grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="bg-white border border-neutral-200 rounded p-5 h-24 animate-pulse" />
          ))}
        </div>
        <div className="h-48 bg-white border border-neutral-200 rounded animate-pulse" />
      </div>
    );
  }

  // ── Error ────────────────────────────────────────────────────────────────────
  if (error) {
    return (
      <div className="w-full max-w-4xl mx-auto animate-subtle-fade">
        <div className="bg-red-50 border border-red-200 rounded p-5 flex items-start gap-3">
          <AlertCircle className="w-5 h-5 text-error flex-shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="text-sm text-error font-medium">{error}</p>
            <button onClick={load} className="mt-2 text-xs underline text-neutral-500 hover:text-neutral-800 transition-colors">
              {t('retryBtn')}
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!stats) return null;

  const today = new Date().toLocaleDateString(undefined, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  return (
    <div className="w-full max-w-4xl mx-auto space-y-8 animate-subtle-fade">

      {/* Page header */}
      <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-3">
        <div>
          <h1 className="akarsha-heading text-3xl">{t('dashTitle')}</h1>
          <p className="akarsha-body text-sm mt-1">{today}</p>
        </div>
        <button
          onClick={load}
          className="flex items-center gap-1.5 text-xs text-neutral-500 hover:text-neutral-800 transition-colors"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          {t('dashRefresh')}
        </button>
      </div>

      {/* Quick actions */}
      <section>
        <h2 className="akarsha-label mb-3">{t('dashQuickActions')}</h2>
        <QuickActions
          onNewAppointment={onNavigateToAppointments}
          onAddCustomer={onNavigateToCustomers}
          onAddStaff={onNavigateToStaff}
          onAddService={onNavigateToServices}
        />
      </section>

      {/* Today's stats */}
      <section>
        <h2 className="akarsha-label mb-3">{t('dashTodayStats')}</h2>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <StatCard
            label={t('dashTodayTotal')}
            value={stats.todayTotal}
            sub={stats.todayTotal === 1 ? t('dashAppointment') : t('dashAppointments')}
          />
          <StatCard
            label={t('dashTodayCompleted')}
            value={stats.todayCompleted}
            accent="success"
          />
          <StatCard
            label={t('dashTodayCancelled')}
            value={stats.todayCancelled}
            accent={stats.todayCancelled > 0 ? 'warning' : 'muted'}
          />
          <StatCard
            label={t('dashTodayRevenue')}
            value={`Rs. ${Number(stats.todayEstimatedRevenue).toLocaleString()}`}
            sub={t('dashRevenueNote')}
          />
        </div>
      </section>

      {/* Salon overview */}
      <section>
        <h2 className="akarsha-label mb-3">{t('dashOverview')}</h2>
        <div className="grid grid-cols-3 gap-4">
          <StatCard label={t('dashTotalCustomers')} value={stats.totalCustomers} />
          <StatCard label={t('dashActiveStaff')} value={stats.activeStaff} />
          <StatCard label={t('dashActiveServices')} value={stats.activeServices} />
        </div>
      </section>

      {/* Two-column layout: timeline + upcoming */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <section>
          <h2 className="akarsha-label mb-3">{t('dashTodayTimeline')}</h2>
          <TodayTimeline appointments={stats.todayTimeline} />
        </section>
        <section>
          <h2 className="akarsha-label mb-3">{t('dashUpcoming')}</h2>
          <UpcomingList appointments={stats.upcomingAppointments} />
        </section>
      </div>

    </div>
  );
};
