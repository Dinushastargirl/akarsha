import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { AppointmentList } from '../features/appointments/AppointmentList';
import { AppointmentForm } from '../features/appointments/AppointmentForm';
import { AppointmentDetails } from '../features/appointments/AppointmentDetails';
import { CheckoutModal } from '../features/appointments/CheckoutModal';
import { appointmentService } from '../features/appointments/appointmentService';
import { customerService } from '../features/customers/customerService';
import type { Appointment, Customer, User as StaffUser } from '../types';

interface AppointmentsPageProps {
  onError: (msg: string) => void;
}

type SubScreen = 'LIST' | 'FORM' | 'DETAILS';

export const AppointmentsPage: React.FC<AppointmentsPageProps> = ({ onError }) => {
  const { t } = useTranslation();
  const [subScreen, setSubScreen] = useState<SubScreen>('LIST');
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [selectedAppointment, setSelectedAppointment] = useState<Appointment | null>(null);
  const [showCheckout, setShowCheckout] = useState(false);

  // Filter states
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().substring(0, 10));
  const [selectedStaffId, setSelectedStaffId] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');

  // Lookup lists loaded once or on demand
  const [staffList, setStaffList] = useState<StaffUser[]>([]);
  const [servicesList, setServicesList] = useState<any[]>([]);
  const [customersList, setCustomersList] = useState<Customer[]>([]);
  
  const [loading, setLoading] = useState(false);

  const fetchAppointments = async () => {
    setLoading(true);
    try {
      const staffNum = selectedStaffId ? Number(selectedStaffId) : undefined;
      const statusStr = selectedStatus || undefined;
      const res = await appointmentService.getAppointments(selectedDate, staffNum, undefined, statusStr);
      if (res.status === 200) {
        const data = await res.json();
        setAppointments(data.content);
      } else {
        onError('Failed to load appointments timeline.');
      }
    } catch (err) {
      onError('Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  const loadLookupData = async () => {
    try {
      // 1. Load staff
      const staffRes = await appointmentService.getStaff();
      if (staffRes.status === 200) {
        const staffData = await staffRes.json();
        setStaffList(staffData);
      }
      
      // 2. Load services
      const servicesRes = await appointmentService.getServices();
      if (servicesRes.status === 200) {
        const servData = await servicesRes.json();
        setServicesList(servData);
      }

      // 3. Load customers
      const customersRes = await customerService.getCustomers(0, 100);
      if (customersRes.status === 200) {
        const custData = await customersRes.json();
        setCustomersList(custData.content);
      }
    } catch (err) {
      onError('Failed to load scheduling lookup data.');
    }
  };

  useEffect(() => {
    loadLookupData();
  }, []);

  useEffect(() => {
    fetchAppointments();
  }, [selectedDate, selectedStaffId, selectedStatus]);

  const handleBook = async (payload: {
    customerId: number;
    serviceId: number;
    staffId: number;
    appointmentDate: string;
    startTime: string;
    endTime: string;
    notes?: string;
  }) => {
    setLoading(true);
    try {
      const res = await appointmentService.createAppointment(payload);
      if (res.status === 201) {
        setSubScreen('LIST');
        fetchAppointments();
      } else if (res.status === 409) {
        onError(t('conflictError'));
      } else {
        const msg = await res.text();
        onError(msg || 'Failed to book appointment.');
      }
    } catch (err) {
      onError('Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (status: 'BOOKED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW') => {
    if (!selectedAppointment) return;
    
    if (status === 'COMPLETED') {
      setShowCheckout(true);
      return;
    }

    try {
      const res = await appointmentService.patchStatus(selectedAppointment.id, status);
      if (res.status === 200) {
        const updated = await res.json();
        setSelectedAppointment(updated);
        fetchAppointments();
      } else {
        onError('Failed to update booking status.');
      }
    } catch (err) {
      onError('Server connection failed.');
    }
  };

  const handleDelete = async () => {
    if (!selectedAppointment) return;
    const confirm = window.confirm(t('deleteAppConfirm'));
    if (!confirm) return;
    try {
      const res = await appointmentService.deleteAppointment(selectedAppointment.id);
      if (res.status === 200) {
        setSelectedAppointment(null);
        setSubScreen('LIST');
        fetchAppointments();
      } else {
        onError('Failed to delete booking.');
      }
    } catch (err) {
      onError('Server connection failed.');
    }
  };

  const handleQuickCustomer = async (fullName: string, phone: string): Promise<Customer | null> => {
    try {
      const res = await customerService.createCustomer({ fullName, phone });
      if (res.status === 200 || res.status === 201) {
        const saved: Customer = await res.json();
        // Refresh local customers list
        setCustomersList(prev => [saved, ...prev]);
        return saved;
      }
    } catch (err) {}
    return null;
  };

  return (
    <div>
      {subScreen === 'LIST' && (
        <AppointmentList 
          appointments={appointments}
          selectedDate={selectedDate}
          onDateChange={setSelectedDate}
          staffList={staffList}
          selectedStaffId={selectedStaffId}
          onStaffChange={setSelectedStaffId}
          selectedStatus={selectedStatus}
          onStatusChange={setSelectedStatus}
          onBookAppointment={() => setSubScreen('FORM')}
          onSelectAppointment={(a) => { setSelectedAppointment(a); setSubScreen('DETAILS'); }}
        />
      )}

      {subScreen === 'FORM' && (
        <AppointmentForm 
          customersList={customersList}
          servicesList={servicesList}
          staffList={staffList}
          onSave={handleBook}
          onCancel={() => setSubScreen('LIST')}
          loading={loading}
          onQuickAddCustomer={handleQuickCustomer}
        />
      )}

      {subScreen === 'DETAILS' && selectedAppointment && (
        <AppointmentDetails 
          appointment={selectedAppointment}
          onBack={() => setSubScreen('LIST')}
          onStatusChange={handleStatusChange}
          onDelete={handleDelete}
        />
      )}

      {showCheckout && selectedAppointment && (
        <CheckoutModal
          appointment={selectedAppointment}
          onClose={() => setShowCheckout(false)}
          onSuccess={() => {
            setShowCheckout(false);
            setSubScreen('LIST');
            fetchAppointments();
          }}
          onError={onError}
        />
      )}
    </div>
  );
};
