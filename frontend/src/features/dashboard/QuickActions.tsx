import React from 'react';
import { useTranslation } from 'react-i18next';
import { CalendarPlus, UserPlus, Users, Scissors } from 'lucide-react';

interface QuickActionsProps {
  onNewAppointment: () => void;
  onAddCustomer: () => void;
  onAddStaff: () => void;
  onAddService: () => void;
}

export const QuickActions: React.FC<QuickActionsProps> = ({
  onNewAppointment,
  onAddCustomer,
  onAddStaff,
  onAddService,
}) => {
  const { t } = useTranslation();

  const actions = [
    {
      label: t('dashQuickNewAppt'),
      icon: CalendarPlus,
      onClick: onNewAppointment,
      primary: true,
    },
    { label: t('dashQuickAddCustomer'), icon: UserPlus, onClick: onAddCustomer },
    { label: t('dashQuickAddStaff'), icon: Users, onClick: onAddStaff },
    { label: t('dashQuickAddService'), icon: Scissors, onClick: onAddService },
  ];

  return (
    <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
      {actions.map(({ label, icon: Icon, onClick, primary }) => (
        <button
          key={label}
          onClick={onClick}
          className={`flex flex-col items-center gap-2 p-4 rounded border text-sm font-medium transition-all
            ${primary
              ? 'bg-neutral-900 text-white border-neutral-900 hover:bg-neutral-800'
              : 'bg-white text-neutral-700 border-neutral-200 hover:bg-neutral-50 hover:border-neutral-300'
            }`}
        >
          <Icon className="w-4 h-4 flex-shrink-0" />
          <span className="text-center leading-tight">{label}</span>
        </button>
      ))}
    </div>
  );
};
