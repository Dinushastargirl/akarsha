import React from 'react';
import { useTranslation } from 'react-i18next';
import type { Service } from '../../types';

interface ServicesListProps {
  services: Service[];
  searchQuery: string;
  onSearchChange: (q: string) => void;
  activeFilter: string;
  onActiveFilterChange: (v: string) => void;
  onAddService: () => void;
  onSelectService: (svc: Service) => void;
  loading: boolean;
}

const DURATION_OPTIONS = [15, 30, 45, 60, 90, 120];

function formatDuration(mins: number): string {
  if (mins < 60) return `${mins} min`;
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  return m > 0 ? `${h}h ${m}m` : `${h}h`;
}

function formatPrice(price: number): string {
  return `LKR ${price.toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

export const ServicesList: React.FC<ServicesListProps> = ({
  services,
  searchQuery,
  onSearchChange,
  activeFilter,
  onActiveFilterChange,
  onAddService,
  onSelectService,
  loading
}) => {
  const { t } = useTranslation();

  return (
    <div>
      {/* Page Header */}
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-xl font-semibold text-neutral-900">{t('servicesTitle')}</h1>
          <p className="text-sm text-neutral-500 mt-1">{t('servicesSubtitle')}</p>
        </div>
        <button
          onClick={onAddService}
          className="btn-primary text-sm"
        >
          {t('addServiceBtn')}
        </button>
      </div>

      {/* Filters row */}
      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <input
          type="text"
          placeholder={t('searchServicesPlaceholder')}
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="form-input flex-1"
        />
        <select
          value={activeFilter}
          onChange={(e) => onActiveFilterChange(e.target.value)}
          className="form-input sm:w-40"
        >
          <option value="">{t('allStatuses')}</option>
          <option value="true">{t('activeOnly')}</option>
          <option value="false">{t('inactiveOnly')}</option>
        </select>
      </div>

      {/* Service table */}
      {loading && <p className="text-sm text-neutral-500 py-8 text-center">{t('loading', 'Loading…')}</p>}

      {!loading && services.length === 0 && (
        <div className="py-16 text-center">
          <p className="text-neutral-500 text-sm">{t('noServices')}</p>
          <p className="text-neutral-400 text-xs mt-1">{t('noServicesDesc')}</p>
        </div>
      )}

      {!loading && services.length > 0 && (
        <div className="border border-neutral-200 rounded-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-neutral-50 border-b border-neutral-200 text-left">
                <th className="px-4 py-3 font-medium text-neutral-500 text-xs uppercase tracking-wide">{t('serviceName')}</th>
                <th className="px-4 py-3 font-medium text-neutral-500 text-xs uppercase tracking-wide hidden sm:table-cell">{t('serviceDuration')}</th>
                <th className="px-4 py-3 font-medium text-neutral-500 text-xs uppercase tracking-wide">{t('servicePrice')}</th>
                <th className="px-4 py-3 font-medium text-neutral-500 text-xs uppercase tracking-wide">{t('status')}</th>
              </tr>
            </thead>
            <tbody>
              {services.map((svc, i) => (
                <tr
                  key={svc.id}
                  onClick={() => onSelectService(svc)}
                  className={`cursor-pointer border-b border-neutral-100 hover:bg-neutral-50 transition-colors ${
                    i === services.length - 1 ? 'border-b-0' : ''
                  }`}
                >
                  <td className="px-4 py-3 text-neutral-900 font-medium">{svc.name}</td>
                  <td className="px-4 py-3 text-neutral-600 hidden sm:table-cell">{formatDuration(svc.durationMinutes)}</td>
                  <td className="px-4 py-3 text-neutral-700">{formatPrice(svc.price)}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-block text-xs font-medium px-2 py-0.5 rounded-full ${
                        svc.active
                          ? 'bg-emerald-50 text-emerald-700'
                          : 'bg-neutral-100 text-neutral-500'
                      }`}
                    >
                      {svc.active ? t('activatedStatus') : t('deactivatedStatus')}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export { DURATION_OPTIONS, formatDuration, formatPrice };
