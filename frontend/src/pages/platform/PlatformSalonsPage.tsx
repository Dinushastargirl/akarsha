import { useState, useEffect } from 'react';
import { ShieldAlert, CheckCircle, Power, PowerOff } from 'lucide-react';
import { apiFetch } from '../../services/api';
import type { SalonData } from '../../types';

export function PlatformSalonsPage({ onError }: { onError: (msg: string) => void }) {
  const [salons, setSalons] = useState<(SalonData & { id: number, status: string, createdAt: string })[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchSalons = async () => {
    setLoading(true);
    try {
      const res = await apiFetch('/platform/salons');
      if (!res.ok) throw new Error('Failed to fetch salons');
      const data = await res.json();
      setSalons(data);
    } catch (err: any) {
      onError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSalons();
  }, [onError]);

  const toggleStatus = async (id: number, currentStatus: string) => {
    try {
      const endpoint = currentStatus === 'ACTIVE' ? 'suspend' : 'reactivate';
      const res = await apiFetch(`/platform/salons/${id}/${endpoint}`, { method: 'POST' });
      if (!res.ok) throw new Error(`Failed to ${endpoint} salon`);
      await fetchSalons();
    } catch (err: any) {
      onError(err.message);
    }
  };

  if (loading) {
    return <div className="flex items-center justify-center h-64 text-brand-800">Loading Salons...</div>;
  }

  return (
    <div className="space-y-6 animate-subtle-fade max-w-6xl mx-auto w-full">
      <div className="flex justify-between items-center mb-6 border-b border-brand-200 pb-4">
        <div>
          <h1 className="text-2xl font-bold text-brand-900">Salons Management</h1>
          <p className="text-sm text-neutral-500">Manage tenants and platform subscriptions</p>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-neutral-200 overflow-hidden">
        <table className="min-w-full divide-y divide-neutral-200">
          <thead className="bg-neutral-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">ID</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">Name & Subdomain</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">Created</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">Status</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-neutral-500 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-neutral-200">
            {salons.map((salon) => (
              <tr key={salon.id} className="hover:bg-neutral-50 transition-colors">
                <td className="px-6 py-4 whitespace-nowrap text-sm text-neutral-500">{salon.id}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-neutral-900">{salon.name}</div>
                  <div className="text-sm text-neutral-500">{salon.subdomain}.akarsha.com</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-neutral-500">
                  {new Date(salon.createdAt).toLocaleDateString()}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {salon.status === 'ACTIVE' ? (
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                      <CheckCircle className="w-3 h-3 mr-1" /> Active
                    </span>
                  ) : (
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                      <ShieldAlert className="w-3 h-3 mr-1" /> Suspended
                    </span>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <button
                    onClick={() => toggleStatus(salon.id, salon.status)}
                    className={`inline-flex items-center space-x-1 px-3 py-1.5 rounded text-xs font-medium transition-colors ${
                      salon.status === 'ACTIVE' 
                        ? 'bg-red-50 text-red-700 hover:bg-red-100'
                        : 'bg-green-50 text-green-700 hover:bg-green-100'
                    }`}
                  >
                    {salon.status === 'ACTIVE' ? (
                      <><PowerOff className="w-3.5 h-3.5" /> <span>Suspend</span></>
                    ) : (
                      <><Power className="w-3.5 h-3.5" /> <span>Reactivate</span></>
                    )}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {salons.length === 0 && (
          <div className="p-8 text-center text-neutral-500">No salons found.</div>
        )}
      </div>
    </div>
  );
}
