import React from 'react';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, Trash2, Calendar, User, Clock } from 'lucide-react';
import type { Appointment } from '../../types';

interface AppointmentDetailsProps {
  appointment: Appointment;
  onBack: () => void;
  onStatusChange: (status: 'BOOKED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW') => Promise<void>;
  onDelete: () => void;
}

export const AppointmentDetails: React.FC<AppointmentDetailsProps> = ({
  appointment,
  onBack,
  onStatusChange,
  onDelete
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
    <div className="max-w-md w-full mx-auto space-y-6 text-left">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-neutral-200 pb-3">
        <button 
          onClick={onBack}
          className="flex items-center gap-1.5 text-xs text-neutral-500 hover:text-neutral-800 font-medium"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>{t('backToList')}</span>
        </button>
        <span className="text-xs uppercase tracking-widest text-neutral-400 font-medium">
          {t('appDetails')}
        </span>
      </div>

      <div className="space-y-6 bg-white border border-neutral-200 rounded p-6 shadow-sm-flat">
        {/* Core Info */}
        <div className="space-y-2">
          <div className="flex justify-between items-start">
            <div>
              <h2 className="akarsha-heading text-xl font-medium">{appointment.customer.fullName}</h2>
              <span className="text-xs text-neutral-500">{appointment.customer.phone}</span>
            </div>
            <span className={`text-[10px] tracking-wide uppercase px-2 py-0.5 rounded border font-semibold ${getStatusColor(appointment.status)}`}>
              {appointment.status}
            </span>
          </div>
        </div>

        <div className="border-t border-neutral-100 pt-4 space-y-3">
          <div className="flex items-center gap-2.5 text-sm text-neutral-700">
            <Calendar className="w-4 h-4 text-neutral-400" />
            <span className="font-medium">{new Date(appointment.appointmentDate).toLocaleDateString()}</span>
          </div>
          <div className="flex items-center gap-2.5 text-sm text-neutral-700">
            <Clock className="w-4 h-4 text-neutral-400" />
            <span className="font-medium">{appointment.startTime.substring(0, 5)} - {appointment.endTime.substring(0, 5)}</span>
          </div>
          <div className="flex items-center gap-2.5 text-sm text-neutral-700">
            <User className="w-4 h-4 text-neutral-400" />
            <span className="font-medium">Served by: {appointment.staff.fullName}</span>
          </div>
        </div>

        {/* Service Details Card */}
        <div className="p-4 bg-neutral-50 rounded border border-neutral-200 flex justify-between items-center text-sm font-semibold">
          <div>
            <span className="block text-neutral-900">{appointment.service.name}</span>
            <span className="block text-[10px] text-neutral-400 uppercase tracking-wide mt-0.5">{appointment.service.durationMinutes} minutes</span>
          </div>
          <span className="text-neutral-900">LKR {appointment.service.price}</span>
        </div>

        {/* Notes */}
        {appointment.notes && (
          <div className="space-y-1">
            <span className="akarsha-label">{t('notesLabel')}</span>
            <p className="text-sm text-neutral-600 whitespace-pre-wrap">{appointment.notes}</p>
          </div>
        )}

        {/* Status Actions */}
        <div className="border-t border-neutral-100 pt-4 space-y-3">
          <span className="block text-[10px] uppercase tracking-wider text-neutral-400 font-semibold mb-1">Update Status</span>
          <div className="grid grid-cols-2 gap-2">
            <button 
              onClick={() => onStatusChange('CONFIRMED')}
              disabled={appointment.status === 'CONFIRMED'}
              className="py-2 border border-amber-200 rounded text-xs font-semibold text-amber-700 bg-amber-50/30 hover:bg-amber-50 disabled:opacity-40"
            >
              Confirm Book
            </button>
            <button 
              onClick={() => onStatusChange('COMPLETED')}
              disabled={appointment.status === 'COMPLETED'}
              className="py-2 border border-green-200 rounded text-xs font-semibold text-green-700 bg-green-50/30 hover:bg-green-50 disabled:opacity-40"
            >
              Complete
            </button>
            <button 
              onClick={() => onStatusChange('CANCELLED')}
              disabled={appointment.status === 'CANCELLED'}
              className="py-2 border border-red-200 rounded text-xs font-semibold text-red-700 bg-red-50/30 hover:bg-red-50 disabled:opacity-40"
            >
              Cancel Book
            </button>
            <button 
              onClick={() => onStatusChange('NO_SHOW')}
              disabled={appointment.status === 'NO_SHOW'}
              className="py-2 border border-neutral-300 rounded text-xs font-semibold text-neutral-600 bg-neutral-100/30 hover:bg-neutral-100 disabled:opacity-40"
            >
              No Show
            </button>
          </div>
        </div>
      </div>

      {/* Delete / Cancel Action */}
      <button 
        type="button" 
        onClick={onDelete}
        className="w-full flex items-center justify-center gap-2 border border-red-200 hover:bg-red-50 text-red-600 rounded py-2 text-xs font-semibold"
      >
        <Trash2 className="w-4 h-4" />
        <span>Delete Booking</span>
      </button>
    </div>
  );
};
