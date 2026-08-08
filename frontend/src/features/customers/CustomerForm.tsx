import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { ArrowLeft } from 'lucide-react';
import type { Customer } from '../../types';

interface CustomerFormProps {
  isEdit: boolean;
  initialData: Partial<Customer>;
  onSave: (data: Omit<Customer, 'id' | 'createdAt'>) => Promise<void>;
  onCancel: () => void;
  loading: boolean;
}

export const CustomerForm: React.FC<CustomerFormProps> = ({
  isEdit,
  initialData,
  onSave,
  onCancel,
  loading
}) => {
  const { t } = useTranslation();
  const [cName, setCName] = useState(initialData.fullName || '');
  const [cPhone, setCPhone] = useState(initialData.phone || '');
  const [cEmail, setCEmail] = useState(initialData.email || '');
  const [cBirthday, setCBirthday] = useState(initialData.birthday || '');
  const [cNotes, setCNotes] = useState(initialData.notes || '');

  useEffect(() => {
    setCName(initialData.fullName || '');
    setCPhone(initialData.phone || '');
    setCEmail(initialData.email || '');
    setCBirthday(initialData.birthday || '');
    setCNotes(initialData.notes || '');
  }, [initialData]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave({
      fullName: cName,
      phone: cPhone,
      email: cEmail || undefined,
      birthday: cBirthday || undefined,
      notes: cNotes || undefined
    });
  };

  return (
    <div className="max-w-md w-full mx-auto space-y-6">
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
          {isEdit ? t('editCustomer') : t('addCustomerBtn')}
        </span>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="space-y-4 bg-white border border-neutral-200 rounded p-6 shadow-sm-flat">
          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('fullNameLabel')} *</label>
            <input 
              type="text" 
              value={cName}
              onChange={(e) => setCName(e.target.value)}
              className="akarsha-input"
              placeholder="Sunil Fernando"
              required
            />
          </div>
          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('phoneLabel')} *</label>
            <input 
              type="text" 
              value={cPhone}
              onChange={(e) => setCPhone(e.target.value)}
              className="akarsha-input"
              placeholder="0777123456"
              required
            />
          </div>
          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('emailLabel')} (Optional)</label>
            <input 
              type="email" 
              value={cEmail}
              onChange={(e) => setCEmail(e.target.value)}
              className="akarsha-input"
              placeholder="sunil@outlook.com"
            />
          </div>
          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('birthdayLabel')} (Optional)</label>
            <input 
              type="date" 
              value={cBirthday}
              onChange={(e) => setCBirthday(e.target.value)}
              className="akarsha-input"
            />
          </div>
          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('notesLabel')} (Optional)</label>
            <textarea 
              value={cNotes}
              onChange={(e) => setCNotes(e.target.value)}
              className="akarsha-input min-h-[80px]"
              placeholder="Prefers hot water wash, tea without sugar."
            />
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
            {loading ? '...' : t('saveCustomer')}
          </button>
        </div>
      </form>
    </div>
  );
};
