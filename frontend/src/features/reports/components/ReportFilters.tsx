import { useTranslation } from 'react-i18next';
import { Calendar, RefreshCw } from 'lucide-react';

interface ReportFiltersProps {
  startDate: string;
  endDate: string;
  onStartDateChange: (val: string) => void;
  onEndDateChange: (val: string) => void;
  onRefresh: () => void;
  loading: boolean;
}

export function ReportFilters({ 
  startDate, endDate, onStartDateChange, onEndDateChange, onRefresh, loading 
}: ReportFiltersProps) {
  const { t } = useTranslation();

  return (
    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-white p-4 rounded-lg shadow-sm-flat border border-neutral-100">
      <div className="flex items-center space-x-4">
        <div className="flex items-center space-x-2 text-sm">
          <Calendar className="w-4 h-4 text-neutral-400" />
          <span className="font-medium text-neutral-700">{t('dateRange')}:</span>
        </div>
        <div className="flex items-center space-x-2">
          <input
            type="date"
            value={startDate}
            onChange={(e) => onStartDateChange(e.target.value)}
            className="text-sm border-neutral-200 rounded focus:ring-neutral-900 focus:border-neutral-900 py-1.5 px-3"
          />
          <span className="text-neutral-400">to</span>
          <input
            type="date"
            value={endDate}
            onChange={(e) => onEndDateChange(e.target.value)}
            className="text-sm border-neutral-200 rounded focus:ring-neutral-900 focus:border-neutral-900 py-1.5 px-3"
          />
        </div>
      </div>
      
      <button
        onClick={onRefresh}
        disabled={loading}
        className="flex items-center space-x-2 bg-neutral-900 text-white px-4 py-1.5 rounded hover:bg-neutral-800 transition-colors text-sm disabled:opacity-50"
      >
        <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
        <span>{t('refreshBtn')}</span>
      </button>
    </div>
  );
}
