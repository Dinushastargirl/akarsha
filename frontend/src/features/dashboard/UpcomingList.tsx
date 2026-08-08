import React from 'react';
import { useTranslation } from 'react-i18next';
import { CalendarDays } from 'lucide-react';
import type { AppointmentSummary } from '../../types/dashboard';

interface UpcomingListProps {
  appointments: AppointmentSummary[];
}

function formatDate(dateStr: string) {
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' });
}

function fmt(time: string) {
  return time.slice(0, 5);
}

export const UpcomingList: React.FC<UpcomingListProps> = ({ appointments }) => {
  const { t } = useTranslation();

  if (appointments.length === 0) {
    return (
      <div className="bg-white border border-neutral-200 rounded p-6 shadow-sm-flat text-center">
        <CalendarDays className="w-8 h-8 text-neutral-200 mx-auto mb-3" />
        <p className="text-sm text-neutral-400">{t('dashNoUpcoming')}</p>
      </div>
    );
  }

  // Group by date for cleaner display
  const grouped: Record<string, AppointmentSummary[]> = {};
  appointments.forEach((a) => {
    if (!grouped[a.appointmentDate]) grouped[a.appointmentDate] = [];
    grouped[a.appointmentDate].push(a);
  });

  return (
    <div className="bg-white border border-neutral-200 rounded shadow-sm-flat">
      {Object.entries(grouped).map(([date, appts]) => (
        <div key={date}>
          <div className="px-5 py-2 bg-neutral-50 border-b border-neutral-100">
            <span className="text-[10px] uppercase tracking-wider text-neutral-400 font-medium">
              {formatDate(date)}
            </span>
          </div>
          {appts.map((appt) => (
            <div
              key={appt.id}
              className="flex items-center gap-4 px-5 py-3 border-b border-neutral-100 last:border-b-0 hover:bg-neutral-50 transition-colors"
            >
              <div className="w-14 text-right flex-shrink-0">
                <span className="text-sm font-medium text-neutral-700">{fmt(appt.startTime)}</span>
              </div>
              <div className="flex-1 min-w-0">
                <span className="block text-sm font-medium text-neutral-900 truncate">{appt.customerName}</span>
                <span className="block text-xs text-neutral-500 truncate">{appt.serviceName} · {appt.staffName}</span>
              </div>
              <span className="text-xs text-neutral-400 flex-shrink-0">
                Rs. {Number(appt.price).toLocaleString()}
              </span>
            </div>
          ))}
        </div>
      ))}
    </div>
  );
};
