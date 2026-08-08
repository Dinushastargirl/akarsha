import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { ArrowLeft } from 'lucide-react';
import type { User as StaffUser } from '../../types';

interface StaffFormProps {
  isEdit: boolean;
  initialData: Partial<StaffUser>;
  onSave: (payload: {
    fullName: string;
    email: string;
    phone?: string;
    role: string;
    active: boolean;
  }) => Promise<void>;
  onCancel: () => void;
  loading: boolean;
}

export const StaffForm: React.FC<StaffFormProps> = ({
  isEdit,
  initialData,
  onSave,
  onCancel,
  loading
}) => {
  const { t } = useTranslation();
  
  const [fullName, setFullName] = useState(initialData.fullName || '');
  const [email, setEmail] = useState(initialData.email || '');
  const [phone, setPhone] = useState(initialData.phone || '');
  const [role, setRole] = useState(initialData.role || 'STAFF');
  const [active, setActive] = useState(initialData.active !== undefined ? initialData.active : true);

  useEffect(() => {
    setFullName(initialData.fullName || '');
    setEmail(initialData.email || '');
    setPhone(initialData.phone || '');
    setRole(initialData.role || 'STAFF');
    setActive(initialData.active !== undefined ? initialData.active : true);
  }, [initialData]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName.trim() || !email.trim()) return;
    onSave({
      fullName: fullName.trim(),
      email: email.trim(),
      phone: phone.trim() || undefined,
      role: role.trim(),
      active
    });
  };

  return (
    <div className="max-w-md w-full mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-neutral-200 pb-3">
        <button 
          type="button"
          onClick={onCancel}
          className="flex items-center gap-1.5 text-xs text-neutral-500 hover:text-neutral-800 font-medium"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>{t('backToList')}</span>
        </button>
        <span className="text-xs uppercase tracking-widest text-neutral-400 font-medium">
          {isEdit ? t('editStaff') : t('addStaffBtn')}
        </span>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6 text-left">
        <div className="space-y-4 bg-white border border-neutral-200 rounded p-6 shadow-sm-flat">
          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('fullNameLabel')} *</label>
            <input 
              type="text" 
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              className="akarsha-input"
              placeholder="Sunil Perera"
              required
            />
          </div>

          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('emailLabel')} *</label>
            <input 
              type="email" 
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="akarsha-input"
              placeholder="sunil@salon.com"
              required
              disabled={isEdit} // Email cannot be modified once set for unique identity reasons
            />
          </div>

          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('phoneLabel')}</label>
            <input 
              type="text" 
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              className="akarsha-input"
              placeholder="0771234567"
            />
          </div>

          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('businessType')}</label>
            <select 
              value={role}
              onChange={(e) => setRole(e.target.value)}
              className="akarsha-input cursor-pointer"
            >
              <option value="STAFF">{t('roleStaff')}</option>
              <option value="MANAGER">{t('roleManager')}</option>
              <option value="SALON_OWNER">{t('roleOwner')}</option>
            </select>
          </div>

          <div className="flex items-center space-x-2 pt-2">
            <input 
              type="checkbox" 
              id="activeStatus"
              checked={active}
              onChange={(e) => setActive(e.target.checked)}
              className="rounded border-neutral-300 text-neutral-900 focus:ring-0 focus:ring-offset-0 cursor-pointer h-4 w-4"
            />
            <label htmlFor="activeStatus" className="text-sm font-semibold text-neutral-700 cursor-pointer">
              {t('activatedStatus')}
            </label>
          </div>
        </div>

        <div className="flex gap-4">
          <button 
            type="button" 
            onClick={onCancel}
            className="flex-1 akarsha-btn-secondary"
          >
            {t('cancelBtn')}
          </button>
          <button 
            type="submit" 
            disabled={loading}
            className="flex-1 akarsha-btn-primary disabled:opacity-50"
          >
            {loading ? '...' : t('saveStaff')}
          </button>
        </div>
      </form>
    </div>
  );
};
