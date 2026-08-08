import React from 'react';
import { useTranslation } from 'react-i18next';
import { Plus, User, Clock } from 'lucide-react';
import type { Appointment, User as StaffUser } from '../../types';

interface AppointmentListProps {
  appointments: Appointment[];
  selectedDate: string;
  onDateChange: (date: string) => void;
  staffList: StaffUser[];
  selectedStaffId: string;
  onStaffChange: (id: string) => void;
  selectedStatus: string;
  onStatusChange: (status: string) => void;
  onBookAppointment: () => void;
  onSelectAppointment: (a: Appointment) => void;
}

export const AppointmentList: React.FC<AppointmentListProps> = ({
  appointments,
  selectedDate,
  onDateChange,
  staffList,
  selectedStaffId,
  onStaffChange,
  selectedStatus,
  onStatusChange,
  onBookAppointment,
  onSelectAppointment
}) => {
  const { t } = useTranslation();

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'BOOKED': return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'CONFIRMED': return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'COMPLETED': return 'bg-green-50 text-green-700 border-green-200';
      case 'CANCELLED': return 'bg-red-50 text-red-700 border-red-200';
      case 'NO_SHOW': return 'bg-neutral-100 text-neutral-600 border-neutral-300';
      default: return 'bg-neutral-50 text-neutral-800';
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Heading */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="akarsha-heading text-3xl mb-1">{t('appointmentsTitle')}</h1>
          <p className="akarsha-body text-sm">{t('appointmentsSubtitle')}</p>
        </div>
        <button 
          onClick={onBookAppointment}
          className="akarsha-btn-primary flex items-center gap-2 self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>{t('bookAppointmentBtn')}</span>
        </button>
      </div>

      {/* Date & Filter Controls */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 bg-white border border-neutral-200 rounded p-4 shadow-sm-flat">
        {/* Date Selector */}
        <div className="akarsha-form-group mb-0">
          <label className="akarsha-label">{t('selectDate')}</label>
          <div className="relative">
            <input 
              type="date" 
              value={selectedDate}
              onChange={(e) => onDateChange(e.target.value)}
              className="akarsha-input"
            />
          </div>
        </div>

        {/* Staff Filter */}
        <div className="akarsha-form-group mb-0">
          <label className="akarsha-label">{t('selectStaff')}</label>
          <select 
            value={selectedStaffId}
            onChange={(e) => onStaffChange(e.target.value)}
            className="akarsha-input cursor-pointer"
          >
            <option value="">All Staff</option>
            {staffList.map((s) => (
              <option key={s.id} value={s.id}>{s.fullName}</option>
            ))}
          </select>
        </div>

        {/* Status Filter */}
        <div className="akarsha-form-group mb-0">
          <label className="akarsha-label">{t('appStatus')}</label>
          <select 
            value={selectedStatus}
            onChange={(e) => onStatusChange(e.target.value)}
            className="akarsha-input cursor-pointer"
          >
            <option value="">All Statuses</option>
            <option value="BOOKED">BOOKED</option>
            <option value="CONFIRMED">CONFIRMED</option>
            <option value="COMPLETED">COMPLETED</option>
            <option value="CANCELLED">CANCELLED</option>
            <option value="NO_SHOW">NO_SHOW</option>
          </select>
        </div>
      </div>

      {/* Appointment Timeline List */}
      {appointments.length === 0 ? (
        <div className="text-center py-16 bg-white border border-neutral-200 rounded shadow-sm-flat">
          <p className="font-medium text-neutral-700 text-lg mb-1">{t('noAppointments')}</p>
          <p className="text-sm text-neutral-400 mb-6">{t('noAppointmentsDesc')}</p>
          <button 
            onClick={onBookAppointment}
            className="akarsha-btn-secondary inline-flex items-center gap-2"
          >
            <Plus className="w-4 h-4" />
            <span>{t('bookAppointmentBtn')}</span>
          </button>
        </div>
      ) : (
        <div className="space-y-3">
          {appointments.map((a) => (
            <div 
              key={a.id} 
              onClick={() => onSelectAppointment(a)}
              className="bg-white border border-neutral-200 hover:border-neutral-400 rounded p-4 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 shadow-sm-flat transition-all cursor-pointer text-left"
            >
              <div className="flex items-center space-x-4">
                {/* Time Indicator */}
                <div className="flex items-center space-x-1.5 bg-neutral-100 text-neutral-800 rounded px-2.5 py-1.5 text-xs font-semibold">
                  <Clock className="w-3.5 h-3.5 text-neutral-500" />
                  <span>{a.startTime.substring(0, 5)} - {a.endTime.substring(0, 5)}</span>
                </div>

                <div>
                  <span className="block text-sm font-semibold text-neutral-900">{a.customer.fullName}</span>
                  <div className="flex items-center space-x-3 mt-0.5 text-xs text-neutral-500">
                    <span className="font-medium">{a.service.name} ({a.service.durationMinutes} min)</span>
                    <span>•</span>
                    <span className="flex items-center gap-1">
                      <User className="w-3 h-3 text-neutral-400" />
                      {a.staff.fullName}
                    </span>
                  </div>
                </div>
              </div>

              {/* Status and Action */}
              <div className="flex items-center space-x-3 self-end sm:self-auto">
                <span className={`text-[10px] tracking-wide uppercase px-2 py-0.5 rounded border ${getStatusColor(a.status)} font-semibold`}>
                  {a.status}
                </span>
                <span className="text-xs font-semibold text-neutral-900">
                  LKR {a.service.price}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
