import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, Edit2, Trash2, CalendarPlus, Calendar, Clock } from 'lucide-react';
import type { Customer, CustomerStats, Appointment } from '../../types';
import { customerService } from './customerService';

interface CustomerProfileProps {
  customer: Customer;
  onBack: () => void;
  onEdit: () => void;
  onDelete: () => void;
  onBookAppointment?: (customer: Customer) => void;
}

export const CustomerProfile: React.FC<CustomerProfileProps> = ({
  customer,
  onBack,
  onEdit,
  onDelete,
  onBookAppointment
}) => {
  const { t } = useTranslation();
  const [stats, setStats] = useState<CustomerStats | null>(null);
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  const [viewMode, setViewMode] = useState<'APPOINTMENTS' | 'INVOICES'>('APPOINTMENTS');
  const [invoices, setInvoices] = useState<any[]>([]);
  const [invoicePage, setInvoicePage] = useState(0);
  const [invoiceTotalPages, setInvoiceTotalPages] = useState(0);

  const fetchProfileData = async (currentPage: number) => {
    setLoading(true);
    try {
      const [statsRes, apptsRes] = await Promise.all([
        customerService.getCustomerStats(customer.id),
        customerService.getCustomerAppointments(customer.id, currentPage)
      ]);

      if (statsRes.status === 200) {
        setStats(await statsRes.json());
      }
      if (apptsRes.status === 200) {
        const data = await apptsRes.json();
        setAppointments(data.content);
        setTotalPages(data.totalPages);
        setPage(data.number);
      }
      
      const invRes = await customerService.getCustomerInvoices(customer.id, 0);
      if (invRes.status === 200) {
        const data = await invRes.json();
        setInvoices(data.content);
        setInvoiceTotalPages(data.totalPages);
        setInvoicePage(data.number);
      }
    } catch (err) {
      console.error('Failed to load profile data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfileData(0);
  }, [customer.id]);

  const fetchInvoicePage = async (page: number) => {
    setLoading(true);
    try {
      const invRes = await customerService.getCustomerInvoices(customer.id, page);
      if (invRes.status === 200) {
        const data = await invRes.json();
        setInvoices(data.content);
        setInvoiceTotalPages(data.totalPages);
        setInvoicePage(data.number);
      }
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'BOOKED': return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'CONFIRMED': return 'bg-purple-50 text-purple-700 border-purple-200';
      case 'COMPLETED': return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'CANCELLED': return 'bg-neutral-50 text-neutral-600 border-neutral-200';
      case 'NO_SHOW': return 'bg-red-50 text-red-700 border-red-200';
      default: return 'bg-neutral-50 text-neutral-600 border-neutral-200';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-neutral-200 pb-3">
        <button 
          onClick={onBack}
          className="flex items-center gap-1.5 text-xs text-neutral-500 hover:text-neutral-800 font-medium w-max"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>{t('backToList')}</span>
        </button>
        
        <div className="flex items-center flex-wrap gap-2">
          {onBookAppointment && (
            <button 
              onClick={() => onBookAppointment(customer)}
              className="text-xs font-medium text-white hover:bg-neutral-800 bg-neutral-900 rounded px-3 py-1.5 flex items-center gap-1.5 transition-colors mr-2"
            >
              <CalendarPlus className="w-3.5 h-3.5" />
              <span>{t('bookAppointmentBtn')}</span>
            </button>
          )}
          <button 
            onClick={onEdit}
            className="text-xs font-medium text-neutral-600 hover:text-neutral-900 border border-neutral-200 rounded px-3 py-1.5 bg-white flex items-center gap-1.5"
          >
            <Edit2 className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">{t('editCustomer')}</span>
          </button>
          <button 
            onClick={onDelete}
            className="text-xs font-medium text-red-600 hover:text-red-800 border border-red-200 rounded px-3 py-1.5 bg-white flex items-center gap-1.5"
          >
            <Trash2 className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">{t('deleteCustomer')}</span>
          </button>
        </div>
      </div>

      {/* Stats Row */}
      {stats && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="bg-white border border-neutral-200 p-4 rounded shadow-sm-flat flex flex-col">
            <span className="akarsha-label mb-1">{t('totalVisits')}</span>
            <span className="text-2xl font-semibold text-neutral-900">{stats.totalVisits}</span>
          </div>
          <div className="bg-white border border-neutral-200 p-4 rounded shadow-sm-flat flex flex-col">
            <span className="akarsha-label mb-1">{t('completedVisits')}</span>
            <span className="text-2xl font-semibold text-emerald-600">{stats.completedVisits}</span>
          </div>
          <div className="bg-white border border-neutral-200 p-4 rounded shadow-sm-flat flex flex-col">
            <span className="akarsha-label mb-1">{t('noShows')}</span>
            <span className="text-2xl font-semibold text-red-600">{stats.noShowCount}</span>
          </div>
          <div className="bg-white border border-neutral-200 p-4 rounded shadow-sm-flat flex flex-col">
            <span className="akarsha-label mb-1">{t('totalSpent')}</span>
            <span className="text-2xl font-semibold text-neutral-900">
              Rs. {stats.totalRevenue ? stats.totalRevenue.toLocaleString() : '0'}
            </span>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Bio Details */}
        <div className="lg:col-span-1 bg-white border border-neutral-200 rounded p-6 space-y-4 shadow-sm-flat self-start">
          <div>
            <h2 className="akarsha-heading text-xl font-medium mb-0.5">{customer.fullName}</h2>
            <span className="text-xs text-neutral-400 block mb-1">Created: {new Date(customer.createdAt).toLocaleDateString()}</span>
            {stats?.lastVisitDate && (
              <span className="text-xs font-medium text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">
                {t('lastVisit')}: {stats.lastVisitDate}
              </span>
            )}
            {!stats?.lastVisitDate && stats !== null && (
              <span className="text-xs font-medium text-neutral-500 bg-neutral-50 px-2 py-0.5 rounded border border-neutral-200">
                {t('neverVisited')}
              </span>
            )}
          </div>
          
          <div className="space-y-3 pt-3 border-t border-neutral-100">
            <div>
              <span className="akarsha-label">{t('phoneLabel')}</span>
              <span className="text-sm font-medium text-neutral-800">{customer.phone}</span>
            </div>
            <div>
              <span className="akarsha-label">{t('emailLabel')}</span>
              <span className="text-sm font-medium text-neutral-800">{customer.email || '-'}</span>
            </div>
            <div>
              <span className="akarsha-label">{t('birthdayLabel')}</span>
              <span className="text-sm font-medium text-neutral-800">{customer.birthday || '-'}</span>
            </div>
            <div>
              <span className="akarsha-label">{t('notesLabel')}</span>
              <p className="text-sm text-neutral-600 whitespace-pre-wrap">{customer.notes || '-'}</p>
            </div>
          </div>
        </div>

        {/* Visit History & Invoices */}
        <div className="lg:col-span-2 bg-white border border-neutral-200 rounded p-6 shadow-sm-flat">
          <div className="flex items-center space-x-6 border-b border-neutral-200 mb-6">
            <button
              onClick={() => setViewMode('APPOINTMENTS')}
              className={`pb-3 text-sm font-medium transition-colors ${viewMode === 'APPOINTMENTS' ? 'text-neutral-900 border-b-2 border-neutral-900' : 'text-neutral-500 hover:text-neutral-800'}`}
            >
              {t('customerHistory')}
            </button>
            <button
              onClick={() => setViewMode('INVOICES')}
              className={`pb-3 text-sm font-medium transition-colors ${viewMode === 'INVOICES' ? 'text-neutral-900 border-b-2 border-neutral-900' : 'text-neutral-500 hover:text-neutral-800'}`}
            >
              Billing History
            </button>
          </div>
          
          {loading ? (
            <div className="py-12 text-center text-neutral-400 text-sm">{t('loading')}</div>
          ) : viewMode === 'APPOINTMENTS' ? (
            appointments.length === 0 ? (
              <div className="border border-dashed border-neutral-200 rounded-lg p-10 text-center">
                <Calendar className="w-8 h-8 text-neutral-300 mx-auto mb-3" />
                <p className="text-sm text-neutral-500 font-medium">{t('customerHistoryEmpty')}</p>
              </div>
            ) : (
              <div className="space-y-4">
                {appointments.map((appt) => (
                  <div key={appt.id} className="border border-neutral-200 rounded p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div className="flex items-start gap-4">
                      <div className="bg-neutral-50 border border-neutral-200 rounded-md p-2 text-center min-w-[60px]">
                        <span className="block text-xs font-semibold text-neutral-500 uppercase">
                          {new Date(appt.appointmentDate).toLocaleDateString(undefined, { month: 'short' })}
                        </span>
                        <span className="block text-xl font-bold text-neutral-900 leading-none mt-1">
                          {new Date(appt.appointmentDate).getDate()}
                        </span>
                      </div>
                      <div>
                        <h4 className="font-medium text-neutral-900 text-sm mb-0.5">{appt.service.name}</h4>
                        <div className="flex items-center gap-3 text-xs text-neutral-500">
                          <span className="flex items-center gap-1">
                            <Clock className="w-3 h-3" />
                            {appt.startTime.substring(0, 5)} - {appt.endTime.substring(0, 5)}
                          </span>
                          <span>•</span>
                          <span>{appt.staff.fullName}</span>
                        </div>
                        {appt.notes && (
                          <p className="text-xs text-neutral-600 mt-2 bg-neutral-50 p-2 rounded italic">"{appt.notes}"</p>
                        )}
                      </div>
                    </div>
                    <div className="flex sm:flex-col items-center sm:items-end justify-between sm:justify-center gap-2">
                      <span className={`text-[10px] font-bold uppercase tracking-wider px-2 py-1 rounded border ${getStatusColor(appt.status)}`}>
                        {t(`status${appt.status}`)}
                      </span>
                      <span className="text-sm font-semibold text-neutral-900">
                        Rs. {appt.service.price.toLocaleString()}
                      </span>
                    </div>
                  </div>
                ))}

                {totalPages > 1 && (
                  <div className="flex items-center justify-between border-t border-neutral-200 pt-4 mt-6">
                    <button 
                      disabled={page === 0}
                      onClick={() => fetchProfileData(page - 1)}
                      className="akarsha-btn-secondary py-1.5 px-3 text-xs disabled:opacity-40"
                    >
                      Previous
                    </button>
                    <span className="text-xs text-neutral-500 font-medium">Page {page + 1} of {totalPages}</span>
                    <button 
                      disabled={page + 1 >= totalPages}
                      onClick={() => fetchProfileData(page + 1)}
                      className="akarsha-btn-secondary py-1.5 px-3 text-xs disabled:opacity-40"
                    >
                      Next
                    </button>
                  </div>
                )}
              </div>
            )
          ) : viewMode === 'INVOICES' ? (
            invoices.length === 0 ? (
              <div className="border border-dashed border-neutral-200 rounded-lg p-10 text-center">
                <p className="text-sm text-neutral-500 font-medium">No billing history found.</p>
              </div>
            ) : (
              <div className="space-y-4">
              {invoices.map((inv) => (
                <div key={inv.id} className="border border-neutral-200 rounded p-4 flex flex-col sm:flex-row justify-between gap-4">
                  <div>
                    <h4 className="font-medium text-neutral-900 text-sm mb-1">Invoice #{inv.id}</h4>
                    <span className="text-xs text-neutral-500">
                      {new Date(inv.createdAt).toLocaleDateString()}
                    </span>
                    <div className="mt-2 text-xs text-neutral-600">
                      {inv.lineItems?.map((item: any) => (
                        <div key={item.id}>- {item.description} (Qty: {item.quantity})</div>
                      ))}
                    </div>
                  </div>
                  <div className="flex sm:flex-col items-center sm:items-end justify-between sm:justify-center gap-2">
                    <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-1 rounded border bg-emerald-50 text-emerald-700 border-emerald-200">
                      {inv.status}
                    </span>
                    <span className="text-sm font-semibold text-neutral-900 mt-1">
                      Rs. {inv.totalAmount?.toLocaleString()}
                    </span>
                  </div>
                </div>
              ))}

              {invoiceTotalPages > 1 && (
                <div className="flex items-center justify-between border-t border-neutral-200 pt-4 mt-6">
                  <button 
                    disabled={invoicePage === 0}
                    onClick={() => fetchInvoicePage(invoicePage - 1)}
                    className="akarsha-btn-secondary py-1.5 px-3 text-xs disabled:opacity-40"
                  >
                    Previous
                  </button>
                  <span className="text-xs text-neutral-500 font-medium">Page {invoicePage + 1} of {invoiceTotalPages}</span>
                  <button 
                    disabled={invoicePage + 1 >= invoiceTotalPages}
                    onClick={() => fetchInvoicePage(invoicePage + 1)}
                    className="akarsha-btn-secondary py-1.5 px-3 text-xs disabled:opacity-40"
                  >
                    Next
                  </button>
                </div>
              )}
            </div>
            )
          ) : null}
        </div>
      </div>
    </div>
  );
};
