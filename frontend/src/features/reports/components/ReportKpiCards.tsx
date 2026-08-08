import { useTranslation } from 'react-i18next';
import { DollarSign, FileText, CalendarCheck, Users, Scissors, Target } from 'lucide-react';
import type { ReportOverview } from '../types';

export function ReportKpiCards({ data }: { data: ReportOverview | null }) {
  const { t } = useTranslation();

  if (!data) return null;

  const cards = [
    {
      title: t('grossRevenue'),
      value: `$${data.grossRevenue.toFixed(2)}`,
      icon: <DollarSign className="w-5 h-5 text-emerald-600" />,
      bg: 'bg-emerald-50',
      border: 'border-emerald-100'
    },
    {
      title: t('totalCollected'),
      value: `$${data.totalPaid.toFixed(2)}`,
      icon: <FileText className="w-5 h-5 text-blue-600" />,
      bg: 'bg-blue-50',
      border: 'border-blue-100'
    },
    {
      title: t('avgTransaction'),
      value: `$${data.averageTransactionValue.toFixed(2)}`,
      icon: <Target className="w-5 h-5 text-purple-600" />,
      bg: 'bg-purple-50',
      border: 'border-purple-100'
    },
    {
      title: t('completedAppts'),
      value: data.completedAppointments,
      icon: <CalendarCheck className="w-5 h-5 text-indigo-600" />,
      bg: 'bg-indigo-50',
      border: 'border-indigo-100'
    },
    {
      title: t('noShows'),
      value: data.noShows,
      icon: <Scissors className="w-5 h-5 text-red-600" />,
      bg: 'bg-red-50',
      border: 'border-red-100'
    },
    {
      title: t('newCustomers'),
      value: data.newCustomers,
      icon: <Users className="w-5 h-5 text-amber-600" />,
      bg: 'bg-amber-50',
      border: 'border-amber-100'
    }
  ];

  return (
    <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
      {cards.map((c, i) => (
        <div key={i} className={`p-4 rounded-xl border ${c.border} bg-white shadow-sm flex items-center space-x-4`}>
          <div className={`p-3 rounded-lg ${c.bg}`}>
            {c.icon}
          </div>
          <div>
            <p className="text-xs text-neutral-500 font-medium uppercase tracking-wider">{c.title}</p>
            <p className="text-2xl font-serif text-neutral-900 mt-1">{c.value}</p>
          </div>
        </div>
      ))}
    </div>
  );
}
