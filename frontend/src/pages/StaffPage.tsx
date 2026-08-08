import React, { useState, useEffect } from 'react';
import { StaffList } from '../features/staff/StaffList';
import { StaffForm } from '../features/staff/StaffForm';
import { StaffProfile } from '../features/staff/StaffProfile';
import { staffService } from '../features/staff/staffService';
import { appointmentService } from '../features/appointments/appointmentService';
import type { User as StaffUser, StaffSchedule } from '../types';

interface StaffPageProps {
  onError: (msg: string) => void;
}

type SubScreen = 'LIST' | 'FORM' | 'PROFILE';

export const StaffPage: React.FC<StaffPageProps> = ({ onError }) => {
  const [subScreen, setSubScreen] = useState<SubScreen>('LIST');
  const [staffMembers, setStaffMembers] = useState<StaffUser[]>([]);
  const [selectedStaff, setSelectedStaff] = useState<StaffUser | null>(null);

  // Filter states
  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilter, setActiveFilter] = useState('');

  // Lookup & Detail states
  const [servicesList, setServicesList] = useState<any[]>([]);
  const [assignedServices, setAssignedServices] = useState<any[]>([]);
  const [scheduleList, setScheduleList] = useState<StaffSchedule[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchStaffMembers = async () => {
    setLoading(true);
    try {
      const activeParam = activeFilter === 'true' ? true : activeFilter === 'false' ? false : undefined;
      const res = await staffService.getStaff(searchQuery || undefined, activeParam);
      if (res.status === 200) {
        const data = await res.json();
        setStaffMembers(data.content);
      } else {
        onError('Failed to load team list.');
      }
    } catch (err) {
      onError('Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  const loadServicesList = async () => {
    try {
      const res = await appointmentService.getServices();
      if (res.status === 200) {
        const data = await res.json();
        setServicesList(data);
      }
    } catch (err) {}
  };

  const loadStaffDetails = async (staff: StaffUser) => {
    setLoading(true);
    try {
      // 1. Get schedule
      const schedRes = await staffService.getSchedule(staff.id);
      if (schedRes.status === 200) {
        const sched = await schedRes.json();
        setScheduleList(sched);
      }
      // 2. Get services
      const servRes = await staffService.getServices(staff.id);
      if (servRes.status === 200) {
        const serv = await servRes.json();
        setAssignedServices(serv);
      }
      setSelectedStaff(staff);
      setSubScreen('PROFILE');
    } catch (err) {
      onError('Failed to load staff schedule details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStaffMembers();
    loadServicesList();
  }, [searchQuery, activeFilter]);

  const handleSaveStaff = async (payload: Omit<StaffUser, 'id' | 'services'>) => {
    setLoading(true);
    try {
      let res;
      if (selectedStaff) {
        res = await staffService.updateStaff(selectedStaff.id, payload);
      } else {
        res = await staffService.createStaff(payload);
      }

      if (res.status === 200 || res.status === 201) {
        const saved = await res.json();
        setSelectedStaff(saved);
        setSubScreen('LIST');
        fetchStaffMembers();
      } else if (res.status === 409) {
        onError('Email is already registered by another team member.');
      } else {
        const msg = await res.text();
        onError(msg || 'Failed to save staff member.');
      }
    } catch (err) {
      onError('Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusToggle = async (active: boolean) => {
    if (!selectedStaff) return;
    setLoading(true);
    try {
      const res = await staffService.patchStatus(selectedStaff.id, active);
      if (res.status === 200) {
        const updated = await res.json();
        setSelectedStaff(updated);
        fetchStaffMembers();
      } else {
        onError('Failed to update staff status.');
      }
    } catch (err) {
      onError('Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveSchedule = async (payload: any[]) => {
    if (!selectedStaff) return;
    setLoading(true);
    try {
      const res = await staffService.updateSchedule(selectedStaff.id, payload);
      if (res.status === 200) {
        const updatedSched = await res.json();
        setScheduleList(updatedSched);
        alert('Working schedule updated successfully.');
      } else {
        onError('Failed to update working hours.');
      }
    } catch (err) {
      onError('Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveServices = async (serviceIds: number[]) => {
    if (!selectedStaff) return;
    setLoading(true);
    try {
      const res = await staffService.updateServices(selectedStaff.id, serviceIds);
      if (res.status === 200) {
        const updatedServ = await res.json();
        setAssignedServices(updatedServ);
        alert('Assigned services updated successfully.');
      } else {
        onError('Failed to update assigned services.');
      }
    } catch (err) {
      onError('Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {subScreen === 'LIST' && (
        <StaffList 
          staffMembers={staffMembers}
          searchQuery={searchQuery}
          onSearchChange={setSearchQuery}
          activeFilter={activeFilter}
          onActiveFilterChange={setActiveFilter}
          onAddStaff={() => { setSelectedStaff(null); setSubScreen('FORM'); }}
          onSelectStaff={loadStaffDetails}
        />
      )}

      {subScreen === 'FORM' && (
        <StaffForm 
          isEdit={selectedStaff !== null}
          initialData={selectedStaff || {}}
          onSave={handleSaveStaff}
          onCancel={() => setSubScreen('LIST')}
          loading={loading}
        />
      )}

      {subScreen === 'PROFILE' && selectedStaff && (
        <StaffProfile 
          staff={selectedStaff}
          scheduleList={scheduleList}
          servicesList={servicesList}
          assignedServices={assignedServices}
          onBack={() => setSubScreen('LIST')}
          onEdit={() => setSubScreen('FORM')}
          onSaveSchedule={handleSaveSchedule}
          onSaveServices={handleSaveServices}
          onStatusToggle={handleStatusToggle}
          loading={loading}
        />
      )}
    </div>
  );
};
