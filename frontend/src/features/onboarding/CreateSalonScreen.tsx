import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { apiFetch } from '../../services/api';
import type { SalonData } from '../../types';

interface CreateSalonScreenProps {
  salon: SalonData;
  onChange: (salon: SalonData) => void;
  onCreateSuccess: (newToken: string) => void;
  onError: (msg: string) => void;
}

export const CreateSalonScreen: React.FC<CreateSalonScreenProps> = ({ salon, onChange, onCreateSuccess, onError }) => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);

  const handleCreateSalon = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!salon.name || !salon.subdomain || !salon.phone || !salon.address || !salon.city) {
      onError(t('allFieldsRequired'));
      return;
    }
    if (!/^[a-zA-Z0-9-]+$/.test(salon.subdomain)) {
      onError('Web address must contain only letters, numbers, or hyphens.');
      return;
    }
    setLoading(true);
    try {
      const res = await apiFetch('/onboarding/create-salon', {
        method: 'POST',
        body: JSON.stringify(salon)
      });
      if (res.status === 200) {
        const data = await res.json();
        onCreateSuccess(data.token);
      } else {
        const msg = await res.text();
        onError(msg || 'Failed to create salon.');
      }
    } catch (err) {
      onError('Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md w-full mx-auto">
      <form onSubmit={handleCreateSalon} className="space-y-6">
        <div>
          <h1 className="akarsha-heading text-3xl mb-2">{t('createSalonTitle')}</h1>
          <p className="akarsha-body text-sm">{t('createSalonSubtitle')}</p>
        </div>

        <div className="space-y-4">
          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('salonName')}</label>
            <input 
              type="text" 
              value={salon.name} 
              onChange={(e) => onChange({ ...salon, name: e.target.value })}
              className="akarsha-input"
              placeholder="Nirvana Salon"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div className="akarsha-form-group">
              <label className="akarsha-label">{t('salonPhone')}</label>
              <input 
                type="text" 
                value={salon.phone} 
                onChange={(e) => onChange({ ...salon, phone: e.target.value })}
                className="akarsha-input"
                placeholder="0771234567"
              />
            </div>
            <div className="akarsha-form-group">
              <label className="akarsha-label">{t('businessType')}</label>
              <select 
                value={salon.businessType} 
                onChange={(e) => onChange({ ...salon, businessType: e.target.value })}
                className="akarsha-input cursor-pointer"
              >
                <option value="Salon">{t('salonType')}</option>
                <option value="Beauty Studio">{t('beautyStudio')}</option>
                <option value="Barber">{t('barber')}</option>
                <option value="Spa">{t('spa')}</option>
                <option value="Nail Studio">{t('nailStudio')}</option>
                <option value="Other">{t('other')}</option>
              </select>
            </div>
          </div>
          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('subdomain')}</label>
            <div className="flex rounded border border-neutral-200 bg-white focus-within:border-neutral-500 overflow-hidden">
              <input 
                type="text" 
                value={salon.subdomain} 
                onChange={(e) => onChange({ ...salon, subdomain: e.target.value.toLowerCase().replace(/\s+/g, '') })}
                className="flex-grow px-3 py-2 text-sm focus:outline-none bg-transparent"
                placeholder="nirvana"
              />
              <span className="px-3 py-2 bg-neutral-100 text-xs text-neutral-400 font-medium border-l border-neutral-200 flex items-center">
                .akarsha.lk
              </span>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div className="akarsha-form-group">
              <label className="akarsha-label">{t('salonAddress')}</label>
              <input 
                type="text" 
                value={salon.address} 
                onChange={(e) => onChange({ ...salon, address: e.target.value })}
                className="akarsha-input"
                placeholder="123 Galle Road"
              />
            </div>
            <div className="akarsha-form-group">
              <label className="akarsha-label">{t('salonCity')}</label>
              <input 
                type="text" 
                value={salon.city} 
                onChange={(e) => onChange({ ...salon, city: e.target.value })}
                className="akarsha-input"
                placeholder="Colombo 03"
              />
            </div>
          </div>
        </div>

        <button 
          type="submit" 
          disabled={loading}
          className="w-full akarsha-btn-primary disabled:opacity-50"
        >
          {loading ? '...' : t('createSalonBtn')}
        </button>
      </form>
    </div>
  );
};
