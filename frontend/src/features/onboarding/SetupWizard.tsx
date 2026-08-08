import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Clock, ChevronRight } from 'lucide-react';
import { apiFetch } from '../../services/api';
import type { ServiceData, StaffData } from '../../types';

interface SetupWizardProps {
  onSetupFinished: () => void;
  onError: (msg: string) => void;
}

type WizardStep = 'HOURS' | 'SERVICE' | 'STAFF';

export const SetupWizard: React.FC<SetupWizardProps> = ({ onSetupFinished, onError }) => {
  const { t } = useTranslation();
  const [step, setStep] = useState<WizardStep>('HOURS');
  const [hours, setHours] = useState({ opening: '09:00', closing: '18:00' });
  const [service, setService] = useState<ServiceData>({ name: '', price: '', duration: '30' });
  const [staff, setStaff] = useState<StaffData>({ name: '', email: '' });
  const [loading, setLoading] = useState(false);

  const handleFinishSetup = async () => {
    setLoading(true);
    try {
      const payload = {
        openingTime: hours.opening,
        closingTime: hours.closing,
        firstServiceName: service.name || null,
        firstServicePrice: service.price ? new Number(service.price) : null,
        firstServiceDuration: service.duration ? new Number(service.duration) : null,
        firstStaffName: staff.name || null,
        firstStaffEmail: staff.email || null
      };

      const res = await apiFetch('/onboarding/setup', {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      if (res.status === 200) {
        onSetupFinished();
      } else {
        const msg = await res.text();
        onError(msg || 'Setup configuration failed.');
      }
    } catch (err) {
      onError('Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md w-full mx-auto">
      {/* 1. Hours step */}
      {step === 'HOURS' && (
        <div className="space-y-6">
          <div className="flex items-center justify-between border-b border-neutral-200 pb-3">
            <span className="text-xs uppercase tracking-widest text-neutral-400 font-medium">{t('step1')}</span>
            <span className="text-xs text-neutral-500 font-medium">1 / 3</span>
          </div>
          <div>
            <h1 className="akarsha-heading text-2xl mb-2">{t('setupTitle')}</h1>
            <p className="akarsha-body text-sm">{t('setupSubtitle')}</p>
          </div>

          <div className="grid grid-cols-2 gap-4 bg-white border border-neutral-200 rounded p-6 shadow-sm-flat">
            <div className="akarsha-form-group">
              <label className="akarsha-label flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-neutral-400" />
                {t('openingTime')}
              </label>
              <input 
                type="time" 
                value={hours.opening} 
                onChange={(e) => setHours({ ...hours, opening: e.target.value })}
                className="akarsha-input"
              />
            </div>
            <div className="akarsha-form-group">
              <label className="akarsha-label flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-neutral-400" />
                {t('closingTime')}
              </label>
              <input 
                type="time" 
                value={hours.closing} 
                onChange={(e) => setHours({ ...hours, closing: e.target.value })}
                className="akarsha-input"
              />
            </div>
          </div>

          <button 
            onClick={() => setStep('SERVICE')}
            className="w-full akarsha-btn-primary flex items-center justify-center gap-1"
          >
            <span>{t('nextBtn')}</span>
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* 2. Service step */}
      {step === 'SERVICE' && (
        <div className="space-y-6">
          <div className="flex items-center justify-between border-b border-neutral-200 pb-3">
            <span className="text-xs uppercase tracking-widest text-neutral-400 font-medium">{t('step2')}</span>
            <span className="text-xs text-neutral-500 font-medium">2 / 3</span>
          </div>
          <div>
            <h1 className="akarsha-heading text-2xl mb-2">{t('serviceName')}</h1>
            <p className="akarsha-body text-sm">{t('setupSubtitle')}</p>
          </div>

          <div className="space-y-4 bg-white border border-neutral-200 rounded p-6 shadow-sm-flat">
            <div className="akarsha-form-group">
              <label className="akarsha-label">{t('serviceName')}</label>
              <input 
                type="text" 
                value={service.name} 
                onChange={(e) => setService({ ...service, name: e.target.value })}
                className="akarsha-input"
                placeholder="Haircut & Styling"
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="akarsha-form-group">
                <label className="akarsha-label">{t('servicePrice')}</label>
                <input 
                  type="number" 
                  value={service.price} 
                  onChange={(e) => setService({ ...service, price: e.target.value })}
                  className="akarsha-input"
                  placeholder="2500"
                />
              </div>
              <div className="akarsha-form-group">
                <label className="akarsha-label">{t('serviceDuration')}</label>
                <input 
                  type="number" 
                  value={service.duration} 
                  onChange={(e) => setService({ ...service, duration: e.target.value })}
                  className="akarsha-input"
                  placeholder="30"
                />
              </div>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <button 
              onClick={() => setStep('HOURS')}
              className="flex-1 akarsha-btn-secondary"
            >
              {t('prevBtn')}
            </button>
            <button 
              onClick={() => setStep('STAFF')}
              className="flex-1 akarsha-btn-primary"
            >
              {t('nextBtn')}
            </button>
          </div>
          
          <div className="flex justify-center pt-2">
            <button 
              onClick={() => { setService({ name: '', price: '', duration: '' }); setStep('STAFF'); }}
              className="akarsha-btn-tertiary"
            >
              {t('skipBtn')}
            </button>
          </div>
        </div>
      )}

      {/* 3. Staff step */}
      {step === 'STAFF' && (
        <div className="space-y-6">
          <div className="flex items-center justify-between border-b border-neutral-200 pb-3">
            <span className="text-xs uppercase tracking-widest text-neutral-400 font-medium">{t('step3')}</span>
            <span className="text-xs text-neutral-500 font-medium">3 / 3</span>
          </div>
          <div>
            <h1 className="akarsha-heading text-2xl mb-2">{t('step3')}</h1>
            <p className="akarsha-body text-sm">{t('setupSubtitle')}</p>
          </div>

          <div className="space-y-4 bg-white border border-neutral-200 rounded p-6 shadow-sm-flat">
            <div className="akarsha-form-group">
              <label className="akarsha-label">{t('staffName')}</label>
              <input 
                type="text" 
                value={staff.name} 
                onChange={(e) => setStaff({ ...staff, name: e.target.value })}
                className="akarsha-input"
                placeholder="Amara"
              />
            </div>
            <div className="akarsha-form-group">
              <label className="akarsha-label">{t('staffEmail')}</label>
              <input 
                type="email" 
                value={staff.email} 
                onChange={(e) => setStaff({ ...staff, email: e.target.value })}
                className="akarsha-input"
                placeholder="amara@salon.com"
              />
            </div>
          </div>

          <div className="flex items-center gap-4">
            <button 
              onClick={() => setStep('SERVICE')}
              className="flex-1 akarsha-btn-secondary"
            >
              {t('prevBtn')}
            </button>
            <button 
              className="flex-1 akarsha-btn-primary"
              disabled={loading}
              onClick={handleFinishSetup}
            >
              {loading ? '...' : t('finishBtn')}
            </button>
          </div>

          <div className="flex justify-center pt-2">
            <button 
              onClick={() => { setStaff({ name: '', email: '' }); handleFinishSetup(); }}
              className="akarsha-btn-tertiary"
            >
              {t('skipBtn')}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
