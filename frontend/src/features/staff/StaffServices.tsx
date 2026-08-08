import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';

interface StaffServicesProps {
  servicesList: any[];
  assignedServices: any[];
  onSave: (serviceIds: number[]) => Promise<void>;
  loading: boolean;
}

export const StaffServices: React.FC<StaffServicesProps> = ({
  servicesList,
  assignedServices,
  onSave,
  loading
}) => {
  const { t } = useTranslation();
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  useEffect(() => {
    setSelectedIds(assignedServices.map(s => s.id));
  }, [assignedServices]);

  const handleToggle = (id: number) => {
    setSelectedIds(prev =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    );
  };

  return (
    <div className="space-y-6 text-left">
      <div className="space-y-2 max-h-[350px] overflow-y-auto border border-neutral-200 rounded bg-white p-4 shadow-sm-flat">
        {servicesList.length === 0 ? (
          <p className="text-xs text-neutral-400">No services registered in this salon yet.</p>
        ) : (
          servicesList.map(s => {
            const checked = selectedIds.includes(s.id);
            return (
              <div 
                key={s.id}
                onClick={() => handleToggle(s.id)}
                className={`flex items-center justify-between p-3 rounded border hover:bg-neutral-50 transition-colors cursor-pointer ${checked ? 'border-neutral-900 bg-neutral-50/20' : 'border-neutral-100 bg-white'}`}
              >
                <div className="flex items-center space-x-3">
                  <input 
                    type="checkbox" 
                    checked={checked}
                    onChange={() => {}} // toggled by row click
                    className="rounded border-neutral-300 text-neutral-900 focus:ring-0 cursor-pointer h-4 w-4"
                  />
                  <div>
                    <span className="text-sm font-semibold text-neutral-900">{s.name}</span>
                    <span className="text-[10px] text-neutral-400 block uppercase tracking-wide mt-0.5">{s.durationMinutes} min</span>
                  </div>
                </div>

                <span className="text-xs font-semibold text-neutral-900">LKR {s.price}</span>
              </div>
            );
          })
        )}
      </div>

      <button 
        type="button" 
        onClick={() => onSave(selectedIds)}
        disabled={loading}
        className="w-full sm:w-auto sm:px-8 akarsha-btn-primary disabled:opacity-50"
      >
        {loading ? '...' : t('finishBtn')}
      </button>
    </div>
  );
};
