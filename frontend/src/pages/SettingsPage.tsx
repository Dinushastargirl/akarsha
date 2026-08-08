import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Store, Clock, MapPin, Loader2, Info } from 'lucide-react';
import { settingsService } from '../features/settings/settingsService';
import type { SalonData } from '../types';

interface SettingsPageProps {
  onError: (msg: string) => void;
  onSuccess: (msg: string) => void;
}

export const SettingsPage: React.FC<SettingsPageProps> = ({ onError, onSuccess }) => {
  const { t } = useTranslation();
  
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState<SalonData>({
    name: '',
    subdomain: '',
    phone: '',
    address: '',
    city: '',
    businessType: '',
    openingTime: '',
    closingTime: ''
  });

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      setLoading(true);
      const res = await settingsService.getSettings();
      if (res.ok) {
        const data = await res.json();
        setFormData({
          name: data.name || '',
          subdomain: data.subdomain || '',
          phone: data.phone || '',
          address: data.address || '',
          city: data.city || '',
          businessType: data.businessType || '',
          openingTime: data.openingTime || '',
          closingTime: data.closingTime || ''
        });
      } else {
        onError(t('settingsFetchFailed'));
      }
    } catch (err) {
      onError(t('settingsFetchFailed'));
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name) {
      onError(t('allFieldsRequired'));
      return;
    }
    
    try {
      setSaving(true);
      const res = await settingsService.updateSettings(formData);
      if (res.ok) {
        onSuccess(t('settingsUpdated'));
      } else {
        onError(t('settingsUpdateFailed'));
      }
    } catch (err) {
      onError(t('settingsUpdateFailed'));
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <Loader2 className="w-8 h-8 animate-spin text-neutral-400" />
      </div>
    );
  }

  return (
    <div className="animate-subtle-fade pb-12">
      <div className="mb-8">
        <h1 className="text-3xl font-serif text-neutral-900">{t('settingsTitle')}</h1>
        <p className="text-neutral-500 mt-2">{t('settingsSubtitle')}</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-12">
        
        {/* Basic Information */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="md:col-span-1">
            <h2 className="text-lg font-medium text-neutral-900 flex items-center space-x-2">
              <Store className="w-5 h-5 text-neutral-400" />
              <span>{t('basicInfo')}</span>
            </h2>
          </div>
          <div className="md:col-span-2 space-y-6 bg-white p-6 md:p-8 rounded-xl border border-neutral-100 shadow-sm">
            <div>
              <label className="block text-sm font-medium text-neutral-700 mb-1.5">{t('salonName')} *</label>
              <input
                type="text"
                required
                value={formData.name}
                onChange={e => setFormData({ ...formData, name: e.target.value })}
                className="w-full px-4 py-2.5 rounded-lg border border-neutral-200 focus:border-neutral-400 focus:ring-1 focus:ring-neutral-400 outline-none transition-all"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700 mb-1.5">{t('salonPhone')}</label>
              <input
                type="tel"
                value={formData.phone}
                onChange={e => setFormData({ ...formData, phone: e.target.value })}
                className="w-full px-4 py-2.5 rounded-lg border border-neutral-200 focus:border-neutral-400 focus:ring-1 focus:ring-neutral-400 outline-none transition-all"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700 mb-1.5">{t('businessType')}</label>
              <select
                value={formData.businessType}
                onChange={e => setFormData({ ...formData, businessType: e.target.value })}
                className="w-full px-4 py-2.5 rounded-lg border border-neutral-200 focus:border-neutral-400 focus:ring-1 focus:ring-neutral-400 outline-none transition-all bg-white"
              >
                <option value="">{t('salonType')}</option>
                <option value="Beauty Studio">{t('beautyStudio')}</option>
                <option value="Barber">{t('barber')}</option>
                <option value="Spa">{t('spa')}</option>
                <option value="Nail Studio">{t('nailStudio')}</option>
                <option value="Other">{t('other')}</option>
              </select>
            </div>
          </div>
        </div>

        {/* Location Information */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="md:col-span-1">
            <h2 className="text-lg font-medium text-neutral-900 flex items-center space-x-2">
              <MapPin className="w-5 h-5 text-neutral-400" />
              <span>Location</span>
            </h2>
          </div>
          <div className="md:col-span-2 space-y-6 bg-white p-6 md:p-8 rounded-xl border border-neutral-100 shadow-sm">
            <div>
              <label className="block text-sm font-medium text-neutral-700 mb-1.5">{t('salonAddress')}</label>
              <input
                type="text"
                value={formData.address}
                onChange={e => setFormData({ ...formData, address: e.target.value })}
                className="w-full px-4 py-2.5 rounded-lg border border-neutral-200 focus:border-neutral-400 focus:ring-1 focus:ring-neutral-400 outline-none transition-all"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700 mb-1.5">{t('salonCity')}</label>
              <input
                type="text"
                value={formData.city}
                onChange={e => setFormData({ ...formData, city: e.target.value })}
                className="w-full px-4 py-2.5 rounded-lg border border-neutral-200 focus:border-neutral-400 focus:ring-1 focus:ring-neutral-400 outline-none transition-all"
              />
            </div>
          </div>
        </div>

        {/* Operating Hours */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="md:col-span-1">
            <h2 className="text-lg font-medium text-neutral-900 flex items-center space-x-2">
              <Clock className="w-5 h-5 text-neutral-400" />
              <span>{t('operatingHours')}</span>
            </h2>
          </div>
          <div className="md:col-span-2 space-y-6 bg-white p-6 md:p-8 rounded-xl border border-neutral-100 shadow-sm">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-1.5">{t('openingTime')}</label>
                <input
                  type="time"
                  value={formData.openingTime}
                  onChange={e => setFormData({ ...formData, openingTime: e.target.value })}
                  className="w-full px-4 py-2.5 rounded-lg border border-neutral-200 focus:border-neutral-400 focus:ring-1 focus:ring-neutral-400 outline-none transition-all"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-1.5">{t('closingTime')}</label>
                <input
                  type="time"
                  value={formData.closingTime}
                  onChange={e => setFormData({ ...formData, closingTime: e.target.value })}
                  className="w-full px-4 py-2.5 rounded-lg border border-neutral-200 focus:border-neutral-400 focus:ring-1 focus:ring-neutral-400 outline-none transition-all"
                />
              </div>
            </div>
          </div>
        </div>

        {/* System Identity (Read Only) */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="md:col-span-1">
            <h2 className="text-lg font-medium text-neutral-900 flex items-center space-x-2">
              <Info className="w-5 h-5 text-neutral-400" />
              <span>{t('systemIdentity')}</span>
            </h2>
          </div>
          <div className="md:col-span-2 space-y-6 bg-white p-6 md:p-8 rounded-xl border border-neutral-100 shadow-sm">
            <div>
              <label className="block text-sm font-medium text-neutral-700 mb-1.5">{t('subdomain')}</label>
              <div className="flex items-center space-x-2">
                <input
                  type="text"
                  value={formData.subdomain}
                  disabled
                  className="w-full px-4 py-2.5 rounded-lg border border-neutral-200 bg-neutral-50 text-neutral-500 outline-none cursor-not-allowed"
                />
              </div>
              <p className="mt-2 text-xs text-neutral-500">{t('subdomainReadonly')}</p>
            </div>
          </div>
        </div>

        {/* Form Actions */}
        <div className="flex justify-end pt-4 border-t border-neutral-200">
          <button
            type="submit"
            disabled={saving}
            className="px-6 py-2.5 bg-neutral-900 text-white rounded-lg font-medium hover:bg-neutral-800 focus:outline-none focus:ring-2 focus:ring-neutral-900 focus:ring-offset-2 disabled:opacity-50 transition-all flex items-center space-x-2"
          >
            {saving ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : null}
            <span>{t('updateSettingsBtn')}</span>
          </button>
        </div>

      </form>
    </div>
  );
};
