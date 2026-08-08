import React, { useState, useEffect } from 'react';
import { ServicesList, formatDuration, formatPrice } from '../features/services/ServicesList';
import { ServiceForm } from '../features/services/ServiceForm';
import { serviceService } from '../features/services/serviceService';
import { useTranslation } from 'react-i18next';
import type { Service } from '../types';

interface ServicesPageProps {
  onError: (msg: string) => void;
}

type SubScreen = 'LIST' | 'FORM' | 'PROFILE';

export const ServicesPage: React.FC<ServicesPageProps> = ({ onError }) => {
  const { t } = useTranslation();
  const [subScreen, setSubScreen] = useState<SubScreen>('LIST');
  const [services, setServices] = useState<Service[]>([]);
  const [selectedService, setSelectedService] = useState<Service | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilter, setActiveFilter] = useState('');
  const [loading, setLoading] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);

  const fetchServices = async () => {
    setLoading(true);
    try {
      const activeParam = activeFilter === 'true' ? true : activeFilter === 'false' ? false : undefined;
      const res = await serviceService.getServices(searchQuery || undefined, activeParam);
      if (res.status === 200) {
        const data = await res.json();
        setServices(data.content);
      } else {
        onError(t('servicesFetchFailed', 'Failed to load services.'));
      }
    } catch {
      onError(t('serverError', 'Server connection failed.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchServices();
  }, [searchQuery, activeFilter]);

  const handleSave = async (payload: Omit<Service, 'id' | 'tenantId' | 'createdAt' | 'updatedAt'>) => {
    setLoading(true);
    try {
      let res;
      if (selectedService) {
        res = await serviceService.updateService(selectedService.id, payload);
      } else {
        res = await serviceService.createService(payload);
      }

      if (res.status === 200 || res.status === 201) {
        const saved = await res.json();
        setSelectedService(saved);
        setSubScreen('PROFILE');
        fetchServices();
      } else {
        const msg = await res.text();
        onError(msg || t('serviceSaveFailed', 'Failed to save service.'));
      }
    } catch {
      onError(t('serverError', 'Server connection failed.'));
    } finally {
      setLoading(false);
    }
  };

  const handleStatusToggle = async (active: boolean) => {
    if (!selectedService) return;
    setLoading(true);
    try {
      const res = await serviceService.patchStatus(selectedService.id, active);
      if (res.status === 200) {
        const updated = await res.json();
        setSelectedService(updated);
        fetchServices();
      } else {
        onError(t('serviceStatusFailed', 'Failed to update service status.'));
      }
    } catch {
      onError(t('serverError', 'Server connection failed.'));
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!selectedService) return;
    setLoading(true);
    try {
      const res = await serviceService.deleteService(selectedService.id);
      if (res.status === 200) {
        setSubScreen('LIST');
        setSelectedService(null);
        fetchServices();
      } else {
        const msg = await res.text();
        onError(msg || t('serviceDeleteFailed', 'Failed to delete service.'));
      }
    } catch {
      onError(t('serverError', 'Server connection failed.'));
    } finally {
      setLoading(false);
      setConfirmDelete(false);
    }
  };

  /* ── PROFILE SUB-SCREEN ── */
  const renderProfile = (svc: Service) => (
    <div>
      <div className="flex items-center gap-3 mb-8">
        <button onClick={() => { setSubScreen('LIST'); setSelectedService(null); }} className="text-sm text-neutral-500 hover:text-neutral-800 transition-colors">
          ← {t('backToList')}
        </button>
      </div>

      <div className="flex items-start justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold text-neutral-900">{svc.name}</h1>
          <p className="text-sm text-neutral-500 mt-1">
            {formatDuration(svc.durationMinutes)} · {formatPrice(svc.price)}
          </p>
        </div>
        <span
          className={`text-xs font-medium px-2 py-0.5 rounded-full mt-1 ${
            svc.active ? 'bg-emerald-50 text-emerald-700' : 'bg-neutral-100 text-neutral-500'
          }`}
        >
          {svc.active ? t('activatedStatus') : t('deactivatedStatus')}
        </span>
      </div>

      {/* Action row */}
      <div className="flex flex-wrap gap-3 mb-8">
        <button
          onClick={() => setSubScreen('FORM')}
          className="btn-secondary text-sm"
        >
          {t('editService')}
        </button>
        <button
          onClick={() => handleStatusToggle(!svc.active)}
          disabled={loading}
          className="btn-secondary text-sm"
        >
          {svc.active ? t('deactivateService') : t('activateService')}
        </button>
        <button
          onClick={() => setConfirmDelete(true)}
          disabled={loading}
          className="text-sm text-red-600 hover:text-red-800 transition-colors border border-red-200 px-3 py-1.5 rounded-sm hover:bg-red-50"
        >
          {t('deleteService')}
        </button>
      </div>

      {/* Details */}
      <div className="border border-neutral-200 rounded-sm divide-y divide-neutral-100">
        <div className="px-4 py-3 flex justify-between text-sm">
          <span className="text-neutral-500">{t('serviceName')}</span>
          <span className="text-neutral-900 font-medium">{svc.name}</span>
        </div>
        <div className="px-4 py-3 flex justify-between text-sm">
          <span className="text-neutral-500">{t('servicePrice')}</span>
          <span className="text-neutral-900">{formatPrice(svc.price)}</span>
        </div>
        <div className="px-4 py-3 flex justify-between text-sm">
          <span className="text-neutral-500">{t('serviceDuration')}</span>
          <span className="text-neutral-900">{formatDuration(svc.durationMinutes)}</span>
        </div>
        {svc.createdAt && (
          <div className="px-4 py-3 flex justify-between text-sm">
            <span className="text-neutral-500">{t('createdDate', 'Added on')}</span>
            <span className="text-neutral-600">{new Date(svc.createdAt).toLocaleDateString('en-LK')}</span>
          </div>
        )}
      </div>

      {/* Delete confirmation */}
      {confirmDelete && (
        <div className="mt-6 border border-red-200 bg-red-50 rounded-sm p-4">
          <p className="text-sm text-red-800 mb-4">{t('deleteServiceConfirm')}</p>
          <div className="flex gap-3">
            <button
              onClick={handleDelete}
              disabled={loading}
              className="text-sm text-white bg-red-600 hover:bg-red-700 px-4 py-1.5 rounded-sm transition-colors"
            >
              {loading ? t('deleting', 'Deleting…') : t('confirmDeleteBtn', 'Yes, delete')}
            </button>
            <button
              onClick={() => setConfirmDelete(false)}
              className="text-sm text-neutral-700 hover:text-neutral-900 border border-neutral-200 px-4 py-1.5 rounded-sm transition-colors"
            >
              {t('cancelBtn')}
            </button>
          </div>
        </div>
      )}
    </div>
  );

  return (
    <div>
      {subScreen === 'LIST' && (
        <ServicesList
          services={services}
          searchQuery={searchQuery}
          onSearchChange={setSearchQuery}
          activeFilter={activeFilter}
          onActiveFilterChange={setActiveFilter}
          onAddService={() => { setSelectedService(null); setSubScreen('FORM'); }}
          onSelectService={(svc) => { setSelectedService(svc); setSubScreen('PROFILE'); }}
          loading={loading}
        />
      )}

      {subScreen === 'FORM' && (
        <ServiceForm
          isEdit={selectedService !== null}
          initialData={selectedService || {}}
          onSave={handleSave}
          onCancel={() => setSubScreen(selectedService ? 'PROFILE' : 'LIST')}
          loading={loading}
        />
      )}

      {subScreen === 'PROFILE' && selectedService && renderProfile(selectedService)}
    </div>
  );
};
