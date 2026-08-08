import { useState, useEffect } from 'react';
import { Users, Activity, PauseCircle, Building } from 'lucide-react';
import { apiFetch } from '../../services/api';

export function PlatformDashboardPage({ onError }: { onError: (msg: string) => void }) {
  const [metrics, setMetrics] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const res = await apiFetch('/platform/dashboard');
        if (!res.ok) throw new Error('Failed to load platform metrics');
        const data = await res.json();
        setMetrics(data);
      } catch (err: any) {
        onError(err.message);
      } finally {
        setLoading(false);
      }
    };
    fetchMetrics();
  }, [onError]);

  if (loading) {
    return <div className="flex items-center justify-center h-64 text-brand-800">Loading Platform Dashboard...</div>;
  }

  if (!metrics) {
    return <div className="text-center p-8 bg-red-50 text-red-600 rounded-lg">Error loading metrics.</div>;
  }

  return (
    <div className="space-y-6 animate-subtle-fade max-w-6xl mx-auto w-full">
      <div className="flex justify-between items-center mb-6 border-b border-brand-200 pb-4">
        <div>
          <h1 className="text-2xl font-bold text-brand-900">Akarsha Super Admin</h1>
          <p className="text-sm text-neutral-500">Platform-wide operations overview</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-6 rounded-lg shadow-sm border border-neutral-200 flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-neutral-500">Total Salons</p>
            <p className="text-2xl font-bold text-neutral-900">{metrics.totalSalons}</p>
          </div>
          <div className="p-3 bg-brand-100 rounded-full"><Building className="w-5 h-5 text-brand-700" /></div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-neutral-200 flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-neutral-500">Active Salons</p>
            <p className="text-2xl font-bold text-green-600">{metrics.activeSalons}</p>
          </div>
          <div className="p-3 bg-green-100 rounded-full"><Activity className="w-5 h-5 text-green-700" /></div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-neutral-200 flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-neutral-500">Suspended Salons</p>
            <p className="text-2xl font-bold text-red-600">{metrics.suspendedSalons}</p>
          </div>
          <div className="p-3 bg-red-100 rounded-full"><PauseCircle className="w-5 h-5 text-red-700" /></div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-neutral-200 flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-neutral-500">Total Users</p>
            <p className="text-2xl font-bold text-neutral-900">{metrics.totalUsers}</p>
          </div>
          <div className="p-3 bg-blue-100 rounded-full"><Users className="w-5 h-5 text-blue-700" /></div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-neutral-200 p-6 text-center text-neutral-500">
        <h3 className="font-semibold text-neutral-700 mb-2">Platform Revenue</h3>
        <p className="text-sm">Revenue reporting requires active payment gateway synchronization.</p>
      </div>
    </div>
  );
}
