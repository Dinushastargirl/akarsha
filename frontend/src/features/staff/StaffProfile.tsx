import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, Edit2, Calendar, ShieldCheck, Mail, Phone } from 'lucide-react';
import { StaffScheduleEditor } from './StaffSchedule';
import { StaffServices } from './StaffServices';
import type { User as StaffUser, StaffSchedule } from '../../types';

interface StaffProfileProps {
  staff: StaffUser;
  scheduleList: StaffSchedule[];
  servicesList: any[];
  assignedServices: any[];
  onBack: () => void;
  onEdit: () => void;
  onSaveSchedule: (payload: any[]) => Promise<void>;
  onSaveServices: (serviceIds: number[]) => Promise<void>;
  onStatusToggle: (active: boolean) => Promise<void>;
  loading: boolean;
}

type TabState = 'OVERVIEW' | 'SCHEDULE' | 'SERVICES';

export const StaffProfile: React.FC<StaffProfileProps> = ({
  staff,
  scheduleList,
  servicesList,
  assignedServices,
  onBack,
  onEdit,
  onSaveSchedule,
  onSaveServices,
  onStatusToggle,
  loading
}) => {
  const { t } = useTranslation();
  const [tab, setTab] = useState<TabState>('OVERVIEW');

  const getRoleLabel = (role: string) => {
    switch (role) {
      case 'SALON_OWNER':
      case 'OWNER':
        return t('roleOwner');
      case 'MANAGER':
        return t('roleManager');
      default:
        return t('roleStaff');
    }
  };

  return (
    <div className="space-y-6 text-left">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-neutral-200 pb-3">
        <button 
          onClick={onBack}
          className="flex items-center gap-1.5 text-xs text-neutral-500 hover:text-neutral-800 font-medium"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>{t('backToList')}</span>
        </button>
        <div className="flex items-center space-x-2">
          <button 
            onClick={onEdit}
            className="text-xs font-medium text-neutral-600 hover:text-neutral-900 border border-neutral-200 rounded px-2.5 py-1 bg-white flex items-center gap-1 shadow-sm-flat"
          >
            <Edit2 className="w-3 h-3" />
            <span>{t('editCustomer')}</span>
          </button>
          <button 
            onClick={() => onStatusToggle(!staff.active)}
            disabled={loading}
            className={`text-xs font-semibold border rounded px-2.5 py-1 transition-all ${staff.active ? 'border-red-200 text-red-600 hover:bg-red-50' : 'border-green-200 text-green-600 hover:bg-green-50'}`}
          >
            {staff.active ? t('deactivateStaff') : t('activateStaff')}
          </button>
        </div>
      </div>

      {/* Tabs Selection */}
      <div className="flex border-b border-neutral-200 space-x-6">
        <button 
          onClick={() => setTab('OVERVIEW')}
          className={`py-2 text-sm font-semibold border-b-2 transition-colors ${tab === 'OVERVIEW' ? 'border-neutral-900 text-neutral-900' : 'border-transparent text-neutral-400 hover:text-neutral-700'}`}
        >
          Overview
        </button>
        <button 
          onClick={() => setTab('SCHEDULE')}
          className={`py-2 text-sm font-semibold border-b-2 transition-colors ${tab === 'SCHEDULE' ? 'border-neutral-900 text-neutral-900' : 'border-transparent text-neutral-400 hover:text-neutral-700'}`}
        >
          {t('staffSchedule')}
        </button>
        <button 
          onClick={() => setTab('SERVICES')}
          className={`py-2 text-sm font-semibold border-b-2 transition-colors ${tab === 'SERVICES' ? 'border-neutral-900 text-neutral-900' : 'border-transparent text-neutral-400 hover:text-neutral-700'}`}
        >
          {t('staffServices')}
        </button>
      </div>

      {/* Tab Contents */}
      {tab === 'OVERVIEW' && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Card Info */}
          <div className="md:col-span-1 bg-white border border-neutral-200 rounded p-6 space-y-4 shadow-sm-flat self-start">
            <div>
              <div className="flex items-center space-x-2">
                <h2 className="akarsha-heading text-xl font-medium">{staff.fullName}</h2>
                <span className={`text-[9px] font-bold uppercase tracking-wider px-1.5 py-0.5 rounded ${staff.active ? 'bg-green-50 text-green-700 border border-green-200' : 'bg-neutral-100 text-neutral-500 border border-neutral-200'}`}>
                  {staff.active ? t('activatedStatus') : t('deactivatedStatus')}
                </span>
              </div>
              <span className="text-xs text-neutral-400 block mt-0.5">{getRoleLabel(staff.role)}</span>
            </div>

            <div className="space-y-3 pt-3 border-t border-neutral-100 text-sm">
              <div className="flex items-center gap-2 text-neutral-700">
                <Mail className="w-4 h-4 text-neutral-400" />
                <span>{staff.email}</span>
              </div>
              {staff.phone && (
                <div className="flex items-center gap-2 text-neutral-700">
                  <Phone className="w-4 h-4 text-neutral-400" />
                  <span>{staff.phone}</span>
                </div>
              )}
            </div>
          </div>

          {/* Allocation Summaries */}
          <div className="md:col-span-2 space-y-6">
            <div className="bg-white border border-neutral-200 rounded p-6 shadow-sm-flat">
              <h3 className="akarsha-heading text-lg mb-3 flex items-center gap-2">
                <ShieldCheck className="w-4 h-4 text-neutral-400" />
                <span>Karana Services ({assignedServices.length})</span>
              </h3>
              {assignedServices.length === 0 ? (
                <p className="text-xs text-neutral-400 italic">No services assigned yet.</p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {assignedServices.map(s => (
                    <span key={s.id} className="bg-neutral-100 border border-neutral-200 rounded px-2.5 py-1 text-xs font-semibold text-neutral-800">
                      {s.name}
                    </span>
                  ))}
                </div>
              )}
            </div>

            <div className="bg-white border border-neutral-200 rounded p-6 shadow-sm-flat">
              <h3 className="akarsha-heading text-lg mb-3 flex items-center gap-2">
                <Calendar className="w-4 h-4 text-neutral-400" />
                <span>Weda Karana Dawas</span>
              </h3>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                {scheduleList.filter(s => s.working).map(s => {
                  const days = ["", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
                  return (
                    <div key={s.dayOfWeek} className="border border-neutral-100 rounded p-2 bg-neutral-50/50 text-center">
                      <span className="block text-xs font-bold text-neutral-700">{days[s.dayOfWeek]}</span>
                      <span className="block text-[10px] text-neutral-500 mt-0.5">{s.startTime.substring(0, 5)} - {s.endTime.substring(0, 5)}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      )}

      {tab === 'SCHEDULE' && (
        <StaffScheduleEditor 
          scheduleList={scheduleList}
          onSave={onSaveSchedule}
          loading={loading}
        />
      )}

      {tab === 'SERVICES' && (
        <StaffServices 
          servicesList={servicesList}
          assignedServices={assignedServices}
          onSave={onSaveServices}
          loading={loading}
        />
      )}
    </div>
  );
};
