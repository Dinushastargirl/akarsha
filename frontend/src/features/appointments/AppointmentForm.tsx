import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, UserPlus, Search } from 'lucide-react';
import { appointmentService } from './appointmentService';
import type { Customer, User as StaffUser } from '../../types';

interface AppointmentFormProps {
  customersList: Customer[];
  servicesList: any[];
  staffList: StaffUser[];
  onSave: (payload: {
    customerId: number;
    serviceId: number;
    staffId: number;
    appointmentDate: string;
    startTime: string;
    endTime: string;
    notes?: string;
  }) => Promise<void>;
  onCancel: () => void;
  loading: boolean;
  onQuickAddCustomer: (fullName: string, phone: string) => Promise<Customer | null>;
}

export const AppointmentForm: React.FC<AppointmentFormProps> = ({
  customersList,
  servicesList,
  staffList,
  onSave,
  onCancel,
  loading,
  onQuickAddCustomer
}) => {
  const { t } = useTranslation();
  
  // Selection states
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [selectedService, setSelectedService] = useState<any | null>(null);
  const [selectedStaff, setSelectedStaff] = useState<StaffUser | null>(null);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().substring(0, 10));
  const [selectedTime, setSelectedTime] = useState('');
  const [notes, setNotes] = useState('');

  // Search customer state
  const [custSearch, setCustSearch] = useState('');
  const [filteredCustomers, setFilteredCustomers] = useState<Customer[]>([]);

  // Slots state
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);
  const [loadingSlots, setLoadingSlots] = useState(false);

  // Quick Customer Creation modal state
  const [showQuickCust, setShowQuickCust] = useState(false);
  const [quickName, setQuickName] = useState('');
  const [quickPhone, setQuickPhone] = useState('');
  const [quickError, setQuickError] = useState<string | null>(null);

  // Filter customers on search query
  useEffect(() => {
    if (custSearch.trim() === '') {
      setFilteredCustomers([]);
    } else {
      const q = custSearch.toLowerCase();
      const matched = customersList.filter(
        c => c.fullName.toLowerCase().includes(q) || c.phone.includes(q)
      );
      setFilteredCustomers(matched.slice(0, 5));
    }
  }, [custSearch, customersList]);

  // Load available time slots when staff + date + service duration are determined
  useEffect(() => {
    if (selectedStaff && selectedDate && selectedService) {
      setLoadingSlots(true);
      setSelectedTime('');
      appointmentService.getSlots(selectedStaff.id, selectedDate, selectedService.durationMinutes)
        .then(async (res) => {
          if (res.status === 200) {
            const slots = await res.json();
            setAvailableSlots(slots);
          }
        })
        .catch(() => {})
        .finally(() => setLoadingSlots(false));
    } else {
      setAvailableSlots([]);
    }
  }, [selectedStaff, selectedDate, selectedService]);

  const handleQuickAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!quickName || !quickPhone) {
      setQuickError('Name and Phone are required.');
      return;
    }
    setQuickError(null);
    const newCust = await onQuickAddCustomer(quickName, quickPhone);
    if (newCust) {
      setSelectedCustomer(newCust);
      setCustSearch('');
      setShowQuickCust(false);
      setQuickName('');
      setQuickPhone('');
    } else {
      setQuickError('Failed to add customer. Ensure phone number is valid.');
    }
  };

  const handleConfirm = () => {
    if (!selectedCustomer || !selectedService || !selectedStaff || !selectedDate || !selectedTime) {
      return;
    }
    // Calculate end time
    const [hours, minutes] = selectedTime.split(':').map(Number);
    const totalMinutes = hours * 60 + minutes + selectedService.durationMinutes;
    const endH = Math.floor(totalMinutes / 60);
    const endM = totalMinutes % 60;
    const pad = (n: number) => String(n).padStart(2, '0');
    const endTimeStr = `${pad(endH)}:${pad(endM)}:00`;
    const startTimeStr = `${selectedTime}:00`;

    onSave({
      customerId: selectedCustomer.id,
      serviceId: selectedService.id,
      staffId: selectedStaff.id,
      appointmentDate: selectedDate,
      startTime: startTimeStr,
      endTime: endTimeStr,
      notes: notes || undefined
    });
  };

  return (
    <div className="max-w-md w-full mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-neutral-200 pb-3">
        <button 
          onClick={onCancel}
          className="flex items-center gap-1.5 text-xs text-neutral-500 hover:text-neutral-800 font-medium"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Cancel</span>
        </button>
        <span className="text-xs uppercase tracking-widest text-neutral-400 font-medium">
          New Booking
        </span>
      </div>

      <div className="space-y-6 bg-white border border-neutral-200 rounded p-6 shadow-sm-flat text-left">
        {/* Step 1: Select Customer */}
        <div className="space-y-2">
          <div className="flex justify-between items-center">
            <label className="akarsha-label mb-0">{t('selectCustomer')} *</label>
            <button 
              type="button"
              onClick={() => setShowQuickCust(!showQuickCust)}
              className="text-xs font-semibold text-neutral-600 hover:text-neutral-900 flex items-center gap-1"
            >
              <UserPlus className="w-3.5 h-3.5" />
              <span>{t('quickAddCustomer')}</span>
            </button>
          </div>

          {showQuickCust ? (
            <form onSubmit={handleQuickAdd} className="p-4 bg-neutral-50 rounded border border-neutral-200 space-y-3">
              <span className="block text-xs font-semibold uppercase tracking-wider text-neutral-400">Quick Create</span>
              {quickError && <p className="text-xs text-red-600 font-semibold">{quickError}</p>}
              <div className="akarsha-form-group">
                <input 
                  type="text" 
                  value={quickName}
                  onChange={(e) => setQuickName(e.target.value)}
                  placeholder="Full Name"
                  className="akarsha-input py-1.5"
                  required
                />
              </div>
              <div className="akarsha-form-group">
                <input 
                  type="text" 
                  value={quickPhone}
                  onChange={(e) => setQuickPhone(e.target.value)}
                  placeholder="Phone Number"
                  className="akarsha-input py-1.5"
                  required
                />
              </div>
              <div className="flex gap-2">
                <button 
                  type="button" 
                  onClick={() => setShowQuickCust(false)}
                  className="flex-1 text-xs border border-neutral-200 rounded py-1.5 hover:bg-neutral-100 font-medium"
                >
                  Cancel
                </button>
                <button 
                  type="submit"
                  className="flex-1 text-xs bg-neutral-900 text-white rounded py-1.5 hover:bg-neutral-800 font-medium"
                >
                  Save & Select
                </button>
              </div>
            </form>
          ) : (
            <div className="relative">
              {selectedCustomer ? (
                <div className="flex justify-between items-center p-2.5 bg-neutral-50 border border-neutral-200 rounded text-sm font-medium">
                  <div>
                    <span className="text-neutral-950 font-semibold">{selectedCustomer.fullName}</span>
                    <span className="text-xs text-neutral-500 block">{selectedCustomer.phone}</span>
                  </div>
                  <button 
                    type="button" 
                    onClick={() => setSelectedCustomer(null)}
                    className="text-xs text-red-600 hover:underline"
                  >
                    Clear
                  </button>
                </div>
              ) : (
                <div className="space-y-1.5">
                  <div className="flex rounded border border-neutral-200 bg-white focus-within:border-neutral-500 overflow-hidden">
                    <span className="px-2.5 flex items-center">
                      <Search className="w-3.5 h-3.5 text-neutral-400" />
                    </span>
                    <input 
                      type="text"
                      value={custSearch}
                      onChange={(e) => setCustSearch(e.target.value)}
                      className="w-full py-2 pr-3 text-xs outline-none bg-transparent"
                      placeholder="Search existing customer by name..."
                    />
                  </div>
                  {filteredCustomers.length > 0 && (
                    <div className="absolute z-10 w-full bg-white border border-neutral-200 rounded shadow-lg overflow-hidden">
                      {filteredCustomers.map(c => (
                        <div 
                          key={c.id} 
                          onClick={() => { setSelectedCustomer(c); setFilteredCustomers([]); setCustSearch(''); }}
                          className="px-4 py-2 hover:bg-neutral-50 text-xs text-left cursor-pointer border-b border-neutral-100 last:border-0"
                        >
                          <span className="font-semibold text-neutral-900 block">{c.fullName}</span>
                          <span className="text-[10px] text-neutral-400">{c.phone}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Step 2: Select Service */}
        <div className="akarsha-form-group">
          <label className="akarsha-label">{t('selectService')} *</label>
          <select 
            value={selectedService ? selectedService.id : ''}
            onChange={(e) => {
              const matched = servicesList.find(s => String(s.id) === e.target.value);
              setSelectedService(matched || null);
            }}
            className="akarsha-input cursor-pointer"
          >
            <option value="">Select Service</option>
            {servicesList.map(s => (
              <option key={s.id} value={s.id}>{s.name} ({s.durationMinutes} min) - LKR {s.price}</option>
            ))}
          </select>
        </div>

        {/* Step 3: Select Staff Provider */}
        <div className="akarsha-form-group">
          <label className="akarsha-label">{t('selectStaff')} *</label>
          <select 
            value={selectedStaff ? selectedStaff.id : ''}
            onChange={(e) => {
              const matched = staffList.find(s => String(s.id) === e.target.value);
              setSelectedStaff(matched || null);
            }}
            className="akarsha-input cursor-pointer"
          >
            <option value="">Select Provider</option>
            {staffList.map(s => (
              <option key={s.id} value={s.id}>{s.fullName}</option>
            ))}
          </select>
        </div>

        {/* Step 4: Select Date */}
        <div className="akarsha-form-group">
          <label className="akarsha-label">{t('selectDate')} *</label>
          <input 
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="akarsha-input"
          />
        </div>

        {/* Step 5: Select Available Time Slots */}
        {selectedStaff && selectedService && (
          <div className="space-y-2">
            <label className="akarsha-label">{t('selectTime')} *</label>
            {loadingSlots ? (
              <p className="text-xs text-neutral-400">Loading available times...</p>
            ) : availableSlots.length === 0 ? (
              <p className="text-xs text-red-500 font-medium">No slots available. Try selecting another date or staff member.</p>
            ) : (
              <div className="grid grid-cols-4 gap-2">
                {availableSlots.map(slot => (
                  <button 
                    key={slot}
                    type="button"
                    onClick={() => setSelectedTime(slot.substring(0, 5))}
                    className={`py-1.5 px-2.5 rounded border text-xs font-semibold text-center transition-all ${selectedTime === slot.substring(0, 5) ? 'bg-neutral-900 border-neutral-900 text-white' : 'bg-neutral-50 hover:bg-neutral-100 text-neutral-800 border-neutral-200'}`}
                  >
                    {slot.substring(0, 5)}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Optional Notes */}
        <div className="akarsha-form-group pt-2">
          <label className="akarsha-label">{t('optionalNotes')}</label>
          <textarea 
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            className="akarsha-input min-h-[60px]"
            placeholder="Write details like allergy/preference..."
          />
        </div>
      </div>

      {/* Action Buttons */}
      <button 
        type="button" 
        onClick={handleConfirm}
        disabled={loading || !selectedCustomer || !selectedService || !selectedStaff || !selectedTime}
        className="w-full akarsha-btn-primary disabled:opacity-40"
      >
        {loading ? '...' : t('saveBooking')}
      </button>
    </div>
  );
};
