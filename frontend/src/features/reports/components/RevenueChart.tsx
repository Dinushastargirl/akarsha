import { useTranslation } from 'react-i18next';
import type { RevenueReport } from '../types';

export function RevenueChart({ data }: { data: RevenueReport | null }) {
  const { t } = useTranslation();

  if (!data || data.revenueByDate.length === 0) {
    return (
      <div className="bg-white p-6 rounded-lg border border-neutral-100 shadow-sm-flat h-64 flex items-center justify-center text-neutral-400">
        {t('noDataAvailable')}
      </div>
    );
  }

  const maxAmount = Math.max(...data.revenueByDate.map(d => d.amount), 1); // Avoid division by zero

  return (
    <div className="bg-white p-6 rounded-lg border border-neutral-100 shadow-sm-flat">
      <h3 className="font-serif text-lg text-neutral-900 mb-6">{t('revenueOverTime')}</h3>
      
      <div className="flex items-end space-x-2 h-48 overflow-x-auto pb-2">
        {data.revenueByDate.map((item, idx) => {
          const heightPct = (item.amount / maxAmount) * 100;
          const dateObj = new Date(item.date);
          const label = `${dateObj.getMonth() + 1}/${dateObj.getDate()}`;
          
          return (
            <div key={idx} className="flex flex-col items-center flex-1 min-w-[32px] group">
              <div className="relative w-full flex justify-center h-40 items-end">
                {/* Tooltip */}
                <div className="absolute -top-10 opacity-0 group-hover:opacity-100 transition-opacity bg-neutral-900 text-white text-xs py-1 px-2 rounded whitespace-nowrap z-10 pointer-events-none">
                  ${item.amount.toFixed(2)}
                </div>
                {/* Bar */}
                <div 
                  className="w-full max-w-[24px] bg-emerald-200 hover:bg-emerald-400 rounded-t-sm transition-colors duration-300"
                  style={{ height: `${heightPct}%` }}
                />
              </div>
              <span className="text-[10px] text-neutral-400 mt-2 rotate-45 transform origin-left whitespace-nowrap">
                {label}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
