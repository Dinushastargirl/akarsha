import React from 'react';
import { useTranslation } from 'react-i18next';
import { Search, Plus, Mail, Phone, ChevronRight } from 'lucide-react';
import type { User as StaffUser } from '../../types';

interface StaffListProps {
  staffMembers: StaffUser[];
  searchQuery: string;
  onSearchChange: (q: string) => void;
  activeFilter: string;
  onActiveFilterChange: (val: string) => void;
  onAddStaff: () => void;
  onSelectStaff: (s: StaffUser) => void;
}

export const StaffList: React.FC<StaffListProps> = ({
  staffMembers,
  searchQuery,
  onSearchChange,
  activeFilter,
  onActiveFilterChange,
  onAddStaff,
  onSelectStaff
}) => {
  const { t } = useTranslation();

  const getRoleLabel = (role: string) => {
    switch (role) {
      case 'SALON_OWNER':
      case 'OWNER':
        return t('roleOwner');
      case 'MANAGER':
        return t('roleManager');
      default:
        return t('roleStaff');
    }
  };

  return (
    <div className="space-y-6">
      {/* Title Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="akarsha-heading text-3xl mb-1">{t('staffTitle')}</h1>
          <p className="akarsha-body text-sm">{t('staffSubtitle')}</p>
        </div>
        <button 
          onClick={onAddStaff}
          className="akarsha-btn-primary flex items-center gap-2 self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>{t('addStaffBtn')}</span>
        </button>
      </div>

      {/* Filter Options */}
      <div className="flex flex-col sm:flex-row gap-4">
        {/* Search */}
        <div className="flex-grow flex rounded border border-neutral-200 bg-white focus-within:border-neutral-500 overflow-hidden shadow-sm-flat">
          <span className="px-3 flex items-center">
            <Search className="w-4 h-4 text-neutral-400" />
          </span>
          <input 
            type="text" 
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            className="w-full py-2.5 pr-4 text-sm bg-transparent outline-none border-none focus:ring-0"
            placeholder="Search by name, email, or phone..."
          />
        </div>

        {/* Active/Inactive Status Filter */}
        <div className="w-full sm:w-48">
          <select 
            value={activeFilter}
            onChange={(e) => onActiveFilterChange(e.target.value)}
            className="w-full px-3 py-2.5 bg-white border border-neutral-200 rounded text-sm focus:outline-none focus:border-neutral-500 cursor-pointer shadow-sm-flat font-medium"
          >
            <option value="">All Statuses</option>
            <option value="true">{t('activatedStatus')}</option>
            <option value="false">{t('deactivatedStatus')}</option>
          </select>
        </div>
      </div>

      {/* Staff Grid Cards */}
      {staffMembers.length === 0 ? (
        <div className="text-center py-16 bg-white border border-neutral-200 rounded shadow-sm-flat">
          <p className="font-medium text-neutral-700 text-lg mb-1">{t('noStaff')}</p>
          <p className="text-sm text-neutral-400 mb-6">{t('noStaffDesc')}</p>
          <button 
            onClick={onAddStaff}
            className="akarsha-btn-secondary inline-flex items-center gap-2"
          >
            <Plus className="w-4 h-4" />
            <span>{t('addStaffBtn')}</span>
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {staffMembers.map((s) => (
            <div 
              key={s.id}
              onClick={() => onSelectStaff(s)}
              className="bg-white border border-neutral-200 hover:border-neutral-400 rounded p-5 flex justify-between items-center shadow-sm-flat transition-all cursor-pointer text-left"
            >
              <div className="space-y-2">
                <div>
                  <div className="flex items-center space-x-2">
                    <span className="text-sm font-semibold text-neutral-900">{s.fullName}</span>
                    <span className={`text-[9px] font-bold uppercase tracking-wider px-1.5 py-0.5 rounded ${s.active ? 'bg-green-50 text-green-700 border border-green-200' : 'bg-neutral-100 text-neutral-500 border border-neutral-200'}`}>
                      {s.active ? t('activatedStatus') : t('deactivatedStatus')}
                    </span>
                  </div>
                  <span className="text-xs text-neutral-400 block mt-0.5">{getRoleLabel(s.role)}</span>
                </div>

                <div className="space-y-1 text-xs text-neutral-500 pt-1">
                  {s.phone && (
                    <div className="flex items-center space-x-1.5">
                      <Phone className="w-3.5 h-3.5 text-neutral-400" />
                      <span>{s.phone}</span>
                    </div>
                  )}
                  <div className="flex items-center space-x-1.5">
                    <Mail className="w-3.5 h-3.5 text-neutral-400" />
                    <span>{s.email}</span>
                  </div>
                </div>
              </div>

              <ChevronRight className="w-5 h-5 text-neutral-300" />
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
