import React from 'react';
import { useTranslation } from 'react-i18next';
import { Search, Plus, Edit2, Trash2, ChevronRight } from 'lucide-react';
import type { Customer } from '../../types';

interface CustomerListProps {
  customers: Customer[];
  searchQuery: string;
  onSearchChange: (query: string) => void;
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  onAddCustomer: () => void;
  onSelectCustomer: (c: Customer) => void;
  onEditCustomer: (c: Customer) => void;
  onDeleteCustomer: (id: number) => void;
}

export const CustomerList: React.FC<CustomerListProps> = ({
  customers,
  searchQuery,
  onSearchChange,
  currentPage,
  totalPages,
  onPageChange,
  onAddCustomer,
  onSelectCustomer,
  onEditCustomer,
  onDeleteCustomer
}) => {
  const { t } = useTranslation();

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="akarsha-heading text-3xl mb-1">{t('customersTitle')}</h1>
          <p className="akarsha-body text-sm">{t('customersSubtitle')}</p>
        </div>
        <button 
          onClick={onAddCustomer}
          className="akarsha-btn-primary flex items-center gap-2 self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>{t('addCustomerBtn')}</span>
        </button>
      </div>

      {/* Search Bar */}
      <div className="flex rounded border border-neutral-200 bg-white focus-within:border-neutral-500 overflow-hidden shadow-sm-flat">
        <span className="px-3 flex items-center bg-transparent">
          <Search className="w-4 h-4 text-neutral-400" />
        </span>
        <input 
          type="text" 
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="w-full py-2.5 pr-4 text-sm bg-transparent outline-none border-none focus:ring-0"
          placeholder={t('searchPlaceholder')}
        />
      </div>

      {/* Empty State */}
      {customers.length === 0 ? (
        <div className="text-center py-16 bg-white border border-neutral-200 rounded shadow-sm-flat">
          <p className="font-medium text-neutral-700 text-lg mb-1">{t('noCustomers')}</p>
          <p className="text-sm text-neutral-400 mb-6">{t('noCustomersDesc')}</p>
          <button 
            onClick={onAddCustomer}
            className="akarsha-btn-secondary inline-flex items-center gap-2"
          >
            <Plus className="w-4 h-4" />
            <span>{t('addCustomerBtn')}</span>
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {/* Desktop view */}
          <div className="hidden sm:block bg-white border border-neutral-200 rounded overflow-hidden shadow-sm-flat">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-neutral-200 bg-neutral-50">
                  <th className="akarsha-label px-6 py-3">{t('fullNameLabel')}</th>
                  <th className="akarsha-label px-6 py-3">{t('phoneLabel')}</th>
                  <th className="akarsha-label px-6 py-3">{t('emailLabel')}</th>
                  <th className="akarsha-label px-6 py-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody>
                {customers.map((c) => (
                  <tr key={c.id} className="border-b border-neutral-100 hover:bg-neutral-50 transition-colors cursor-pointer" onClick={() => onSelectCustomer(c)}>
                    <td className="px-6 py-4 text-sm font-medium text-neutral-900">{c.fullName}</td>
                    <td className="px-6 py-4 text-sm text-neutral-600">{c.phone}</td>
                    <td className="px-6 py-4 text-sm text-neutral-400">{c.email || '-'}</td>
                    <td className="px-6 py-4 text-sm text-right" onClick={(e) => e.stopPropagation()}>
                      <button onClick={() => onEditCustomer(c)} className="text-neutral-500 hover:text-neutral-900 mr-4">
                        <Edit2 className="w-3.5 h-3.5 inline" />
                      </button>
                      <button onClick={() => onDeleteCustomer(c.id)} className="text-neutral-400 hover:text-red-600">
                        <Trash2 className="w-3.5 h-3.5 inline" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Mobile view */}
          <div className="sm:hidden space-y-3">
            {customers.map((c) => (
              <div 
                key={c.id} 
                onClick={() => onSelectCustomer(c)}
                className="bg-white border border-neutral-200 rounded p-4 flex justify-between items-center shadow-sm-flat"
              >
                <div className="text-left">
                  <span className="block text-sm font-medium text-neutral-900">{c.fullName}</span>
                  <span className="block text-xs text-neutral-500 mt-0.5">{c.phone}</span>
                </div>
                <ChevronRight className="w-4 h-4 text-neutral-400" />
              </div>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between border-t border-neutral-200 pt-4">
              <button 
                disabled={currentPage === 0}
                onClick={() => onPageChange(currentPage - 1)}
                className="akarsha-btn-secondary py-1.5 px-3 text-xs disabled:opacity-40"
              >
                Previous
              </button>
              <span className="text-xs text-neutral-500 font-medium">Page {currentPage + 1} of {totalPages}</span>
              <button 
                disabled={currentPage + 1 >= totalPages}
                onClick={() => onPageChange(currentPage + 1)}
                className="akarsha-btn-secondary py-1.5 px-3 text-xs disabled:opacity-40"
              >
                Next
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
