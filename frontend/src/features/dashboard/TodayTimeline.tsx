import React from 'react';
import { useTranslation } from 'react-i18next';
import { Clock } from 'lucide-react';
import type { AppointmentSummary } from '../../types/dashboard';

interface TodayTimelineProps {
  appointments: AppointmentSummary[];
}

const STATUS_STYLES: Record<string, string> = {
  BOOKED: 'bg-blue-50 text-blue-700 border-blue-100',
  CONFIRMED: 'bg-brand-100 text-brand-700 border-brand-200',
  COMPLETED: 'bg-green-50 text-success border-green-100',
  CANCELLED: 'bg-neutral-100 text-neutral-400 border-neutral-200',
  NO_SHOW: 'bg-red-50 text-error border-red-100',
};

const STATUS_DOT: Record<string, string> = {
  BOOKED: 'bg-blue-400',
  CONFIRMED: 'bg-brand-500',
  COMPLETED: 'bg-success',
  CANCELLED: 'bg-neutral-300',
  NO_SHOW: 'bg-error',
};

function fmt(time: string) {
  // "10:00:00" → "10:00"
  return time.slice(0, 5);
}

export const TodayTimeline: React.FC<TodayTimelineProps> = ({ appointments }) => {
  const { t } = useTranslation();

  if (appointments.length === 0) {
    return (
      <div className="bg-white border border-neutral-200 rounded p-6 shadow-sm-flat text-center">
        <Clock className="w-8 h-8 text-neutral-200 mx-auto mb-3" />
        <p className="text-sm text-neutral-400">{t('dashNoAppointmentsToday')}</p>
      </div>
    );
  }

  return (
    <div className="bg-white border border-neutral-200 rounded shadow-sm-flat divide-y divide-neutral-100">
      {appointments.map((appt) => (
        <div key={appt.id} className="flex items-center gap-4 px-5 py-3 hover:bg-neutral-50 transition-colors">
          {/* Time column */}
          <div className="w-16 text-right flex-shrink-0">
            <span className="text-sm font-medium text-neutral-700">{fmt(appt.startTime)}</span>
            <span className="block text-[10px] text-neutral-400">{fmt(appt.endTime)}</span>
          </div>

          {/* Dot */}
          <div className={`w-2 h-2 rounded-full flex-shrink-0 ${STATUS_DOT[appt.status] ?? 'bg-neutral-300'}`} />

          {/* Details */}
          <div className="flex-1 min-w-0">
            <span className="block text-sm font-medium text-neutral-900 truncate">{appt.customerName}</span>
            <span className="block text-xs text-neutral-500 truncate">{appt.serviceName} · {appt.staffName}</span>
          </div>

          {/* Status badge */}
          <span className={`text-[10px] font-medium px-2 py-0.5 rounded border flex-shrink-0 ${STATUS_STYLES[appt.status] ?? ''}`}>
            {t(`status${appt.status}` as any, appt.status)}
          </span>
        </div>
      ))}
    </div>
  );
};
