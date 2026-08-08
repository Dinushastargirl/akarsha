import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { reportsService } from '../features/reports/reportsService';
import { ReportFilters } from '../features/reports/components/ReportFilters';
import { ReportKpiCards } from '../features/reports/components/ReportKpiCards';
import { RevenueChart } from '../features/reports/components/RevenueChart';
import { PerformanceTable } from '../features/reports/components/PerformanceTable';
import type { 
  ReportOverview, 
  RevenueReport, 
  ServicePerformance, 
  StaffPerformance,
  CustomerInsights,
  NoShowReport
} from '../features/reports/types';

interface ReportsPageProps {
  onError: (msg: string) => void;
}

export function ReportsPage({ onError }: ReportsPageProps) {
  const { t } = useTranslation();
  
  const [startDate, setStartDate] = useState(() => {
    const d = new Date();
    d.setDate(1); // First day of current month
    return d.toISOString().split('T')[0];
  });
  
  const [endDate, setEndDate] = useState(() => {
    return new Date().toISOString().split('T')[0];
  });
  
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'OVERVIEW' | 'SERVICES' | 'STAFF' | 'CUSTOMERS'>('OVERVIEW');

  const [overview, setOverview] = useState<ReportOverview | null>(null);
  const [revenue, setRevenue] = useState<RevenueReport | null>(null);
  const [services, setServices] = useState<ServicePerformance[]>([]);
  const [staff, setStaff] = useState<StaffPerformance[]>([]);
  const [customers, setCustomers] = useState<CustomerInsights | null>(null);
  const [noShows, setNoShows] = useState<NoShowReport | null>(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      if (activeTab === 'OVERVIEW') {
        const [ovData, revData] = await Promise.all([
          reportsService.getOverview(startDate, endDate),
          reportsService.getRevenue(startDate, endDate)
        ]);
        setOverview(ovData);
        setRevenue(revData);
      } else if (activeTab === 'SERVICES') {
        const svcData = await reportsService.getServices(startDate, endDate);
        setServices(svcData);
      } else if (activeTab === 'STAFF') {
        const stfData = await reportsService.getStaff(startDate, endDate);
        setStaff(stfData);
      } else if (activeTab === 'CUSTOMERS') {
        const [custData, noShowData] = await Promise.all([
          reportsService.getCustomers(startDate, endDate),
          reportsService.getNoShows(startDate, endDate)
        ]);
        setCustomers(custData);
        setNoShows(noShowData);
      }
    } catch (err: any) {
      onError(err.message || t('errorFetchingReports'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [startDate, endDate, activeTab]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-serif text-neutral-900 tracking-tight">{t('navReports')}</h1>
      </div>

      <ReportFilters 
        startDate={startDate}
        endDate={endDate}
        onStartDateChange={setStartDate}
        onEndDateChange={setEndDate}
        onRefresh={fetchData}
        loading={loading}
      />

      <div className="flex border-b border-neutral-200 space-x-6 overflow-x-auto">
        {(['OVERVIEW', 'SERVICES', 'STAFF', 'CUSTOMERS'] as const).map(tab => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`pb-3 text-sm font-medium transition-colors whitespace-nowrap ${
              activeTab === tab 
                ? 'border-b-2 border-neutral-900 text-neutral-900' 
                : 'text-neutral-500 hover:text-neutral-800'
            }`}
          >
            {t(`tab${tab.charAt(0) + tab.slice(1).toLowerCase()}`)}
          </button>
        ))}
      </div>

      <div className="min-h-[400px]">
        {loading && <div className="text-neutral-400 text-sm animate-pulse">{t('loading')}...</div>}
        
        {!loading && activeTab === 'OVERVIEW' && (
          <div className="space-y-6 animate-fade-in-up">
            <ReportKpiCards data={overview} />
            <RevenueChart data={revenue} />
          </div>
        )}

        {!loading && activeTab === 'SERVICES' && (
          <div className="animate-fade-in-up">
            <PerformanceTable
              title={t('servicePerformance')}
              data={services}
              columns={[
                { header: t('serviceName'), accessor: (s) => <span className="font-medium">{s.serviceName}</span> },
                { header: t('completedAppts'), accessor: (s) => s.completedAppointments },
                { header: t('cancelledAppts'), accessor: (s) => <span className="text-neutral-400">{s.cancelledAppointments}</span> },
                { header: t('noShows'), accessor: (s) => <span className="text-red-500">{s.noShowAppointments}</span> },
                { header: t('grossRevenue'), accessor: (s) => <span className="font-medium text-emerald-600">${s.revenue.toFixed(2)}</span> },
              ]}
            />
          </div>
        )}

        {!loading && activeTab === 'STAFF' && (
          <div className="animate-fade-in-up">
            <PerformanceTable
              title={t('staffPerformance')}
              data={staff}
              columns={[
                { header: t('staffName'), accessor: (s) => <span className="font-medium">{s.staffName}</span> },
                { header: t('completedAppts'), accessor: (s) => s.completedAppointments },
                { header: t('completionRate'), accessor: (s) => `${(s.completionRate * 100).toFixed(1)}%` },
                { header: t('revenueGenerated'), accessor: (s) => <span className="font-medium text-emerald-600">${s.revenueGenerated.toFixed(2)}</span> },
              ]}
            />
          </div>
        )}

        {!loading && activeTab === 'CUSTOMERS' && customers && (
          <div className="space-y-6 animate-fade-in-up">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="p-4 rounded-xl border border-neutral-100 bg-white shadow-sm">
                <p className="text-xs text-neutral-500 font-medium uppercase tracking-wider">{t('totalCustomers')}</p>
                <p className="text-2xl font-serif text-neutral-900 mt-1">{customers.totalCustomers}</p>
              </div>
              <div className="p-4 rounded-xl border border-neutral-100 bg-white shadow-sm">
                <p className="text-xs text-neutral-500 font-medium uppercase tracking-wider">{t('newCustomers')}</p>
                <p className="text-2xl font-serif text-neutral-900 mt-1">{customers.newCustomers}</p>
              </div>
              <div className="p-4 rounded-xl border border-neutral-100 bg-white shadow-sm">
                <p className="text-xs text-neutral-500 font-medium uppercase tracking-wider">{t('returningCustomers')}</p>
                <p className="text-2xl font-serif text-neutral-900 mt-1">{customers.returningCustomers}</p>
              </div>
              <div className="p-4 rounded-xl border border-neutral-100 bg-white shadow-sm">
                <p className="text-xs text-neutral-500 font-medium uppercase tracking-wider">{t('repeatVisitRate')}</p>
                <p className="text-2xl font-serif text-neutral-900 mt-1">{(customers.repeatVisitRate * 100).toFixed(1)}%</p>
              </div>
            </div>

            {noShows && (
              <PerformanceTable
                title={t('topNoShowServices')}
                data={noShows.noShowsByService.slice(0, 5)}
                columns={[
                  { header: t('serviceName'), accessor: (n) => <span className="font-medium">{n.serviceName}</span> },
                  { header: t('noShowCount'), accessor: (n) => <span className="text-red-500">{n.count}</span> },
                ]}
              />
            )}
          </div>
        )}
      </div>
    </div>
  );
}
