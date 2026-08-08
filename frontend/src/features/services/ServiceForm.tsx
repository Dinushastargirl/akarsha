import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import type { Service } from '../../types';
import { DURATION_OPTIONS } from './ServicesList';

interface ServiceFormProps {
  isEdit: boolean;
  initialData: Partial<Service>;
  onSave: (payload: Omit<Service, 'id' | 'tenantId' | 'createdAt' | 'updatedAt'>) => void;
  onCancel: () => void;
  loading: boolean;
}

export const ServiceForm: React.FC<ServiceFormProps> = ({
  isEdit,
  initialData,
  onSave,
  onCancel,
  loading
}) => {
  const { t } = useTranslation();

  const [name, setName] = useState(initialData.name || '');
  const [price, setPrice] = useState(initialData.price !== undefined ? String(initialData.price) : '');
  const [durationMinutes, setDurationMinutes] = useState(
    initialData.durationMinutes || DURATION_OPTIONS[1]
  );
  const [active, setActive] = useState(initialData.active !== undefined ? initialData.active : true);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    setName(initialData.name || '');
    setPrice(initialData.price !== undefined ? String(initialData.price) : '');
    setDurationMinutes(initialData.durationMinutes || DURATION_OPTIONS[1]);
    setActive(initialData.active !== undefined ? initialData.active : true);
    setErrors({});
  }, [initialData]);

  const validate = (): boolean => {
    const errs: Record<string, string> = {};
    if (!name.trim()) errs.name = t('fieldRequired', 'This field is required.');
    const priceNum = parseFloat(price);
    if (price === '' || isNaN(priceNum) || priceNum < 0)
      errs.price = t('invalidPrice', 'Enter a valid price (zero or greater).');
    if (!durationMinutes || durationMinutes <= 0)
      errs.durationMinutes = t('invalidDuration', 'Duration must be greater than zero.');
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    onSave({
      name: name.trim(),
      price: parseFloat(price),
      durationMinutes,
      active
    });
  };

  return (
    <div>
      {/* Back header */}
      <div className="flex items-center gap-3 mb-8">
        <button onClick={onCancel} className="text-sm text-neutral-500 hover:text-neutral-800 transition-colors">
          ← {t('backToList')}
        </button>
      </div>

      <h1 className="text-xl font-semibold text-neutral-900 mb-1">
        {isEdit ? t('editService') : t('addServiceBtn')}
      </h1>
      <p className="text-sm text-neutral-500 mb-8">
        {isEdit ? t('editServiceSubtitle') : t('addServiceSubtitle')}
      </p>

      <form onSubmit={handleSubmit} className="max-w-md space-y-5" noValidate>
        {/* Service Name */}
        <div>
          <label className="form-label">{t('serviceName')}</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className={`form-input ${errors.name ? 'border-red-400' : ''}`}
            placeholder={t('serviceNamePlaceholder')}
            autoFocus
          />
          {errors.name && <p className="text-xs text-red-600 mt-1">{errors.name}</p>}
        </div>

        {/* Price */}
        <div>
          <label className="form-label">{t('servicePrice')}</label>
          <div className="flex items-center">
            <span className="text-sm text-neutral-500 mr-2 whitespace-nowrap">LKR</span>
            <input
              type="number"
              min="0"
              step="0.01"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              className={`form-input flex-1 ${errors.price ? 'border-red-400' : ''}`}
              placeholder="0.00"
            />
          </div>
          {errors.price && <p className="text-xs text-red-600 mt-1">{errors.price}</p>}
        </div>

        {/* Duration */}
        <div>
          <label className="form-label">{t('serviceDuration')}</label>
          <select
            value={durationMinutes}
            onChange={(e) => setDurationMinutes(Number(e.target.value))}
            className={`form-input ${errors.durationMinutes ? 'border-red-400' : ''}`}
          >
            {DURATION_OPTIONS.map((opt) => (
              <option key={opt} value={opt}>
                {opt < 60 ? `${opt} min` : opt === 60 ? '1 hour' : `${Math.floor(opt / 60)}h ${opt % 60 > 0 ? `${opt % 60}m` : ''}`}
              </option>
            ))}
          </select>
          {errors.durationMinutes && <p className="text-xs text-red-600 mt-1">{errors.durationMinutes}</p>}
        </div>

        {/* Active toggle — only show on edit */}
        {isEdit && (
          <div className="flex items-center gap-3 pt-1">
            <input
              type="checkbox"
              id="serviceActive"
              checked={active}
              onChange={(e) => setActive(e.target.checked)}
              className="h-4 w-4 accent-neutral-800"
            />
            <label htmlFor="serviceActive" className="text-sm text-neutral-700">
              {t('serviceActiveLabel')}
            </label>
          </div>
        )}

        {/* Actions */}
        <div className="flex gap-3 pt-2">
          <button
            type="submit"
            disabled={loading}
            className="btn-primary text-sm"
          >
            {loading ? t('saving', 'Saving…') : t('saveService')}
          </button>
          <button
            type="button"
            onClick={onCancel}
            className="btn-secondary text-sm"
          >
            {t('cancelBtn')}
          </button>
        </div>
      </form>
    </div>
  );
};
