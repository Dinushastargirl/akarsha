import React, { useState, useEffect } from 'react';
import { CustomerList } from '../features/customers/CustomerList';
import { CustomerForm } from '../features/customers/CustomerForm';
import { CustomerProfile } from '../features/customers/CustomerProfile';
import { customerService } from '../features/customers/customerService';
import type { Customer } from '../types';

interface CustomersPageProps {
  onError: (msg: string) => void;
  onNavigateToAppointments?: () => void;
}

type SubScreen = 'LIST' | 'ADD' | 'EDIT' | 'PROFILE';

export const CustomersPage: React.FC<CustomersPageProps> = ({ onError, onNavigateToAppointments }) => {
  const [subScreen, setSubScreen] = useState<SubScreen>('LIST');
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [loading, setLoading] = useState(false);

  const fetchCustomers = async (page: number, query: string = searchQuery) => {
    setLoading(true);
    try {
      let res;
      if (query.trim()) {
        res = await customerService.searchCustomers(query, page);
      } else {
        res = await customerService.getCustomers(page);
      }

      if (res.status === 200) {
        const data = await res.json();
        setCustomers(data.content);
        setCurrentPage(data.number);
        setTotalPages(data.totalPages);
      } else {
        onError('Failed to load customers list.');
      }
    } catch (err) {
      onError('Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCustomers(0);
  }, []);

  const handleSearchChange = (query: string) => {
    setSearchQuery(query);
    fetchCustomers(0, query);
  };

  const handleSave = async (payload: Omit<Customer, 'id' | 'createdAt'>) => {
    setLoading(true);
    try {
      let res;
      if (subScreen === 'EDIT' && selectedCustomer) {
        res = await customerService.updateCustomer(selectedCustomer.id, payload);
      } else {
        res = await customerService.createCustomer(payload);
      }

      if (res.status === 200 || res.status === 201) {
        const saved = await res.json();
        setSelectedCustomer(saved);
        setSubScreen('PROFILE');
        fetchCustomers(0);
      } else {
        const msg = await res.text();
        onError(msg || 'Failed to save customer details.');
      }
    } catch (err) {
      onError('Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    setLoading(true);
    try {
      const res = await customerService.deleteCustomer(id);
      if (res.status === 200) {
        setSelectedCustomer(null);
        setSubScreen('LIST');
        fetchCustomers(0);
      } else {
        onError('Failed to delete customer.');
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
        <CustomerList 
          customers={customers}
          searchQuery={searchQuery}
          onSearchChange={handleSearchChange}
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={(p) => fetchCustomers(p)}
          onAddCustomer={() => setSubScreen('ADD')}
          onSelectCustomer={(c) => { setSelectedCustomer(c); setSubScreen('PROFILE'); }}
          onEditCustomer={(c) => { setSelectedCustomer(c); setSubScreen('EDIT'); }}
          onDeleteCustomer={handleDelete}
        />
      )}

      {(subScreen === 'ADD' || subScreen === 'EDIT') && (
        <CustomerForm 
          isEdit={subScreen === 'EDIT'}
          initialData={subScreen === 'EDIT' && selectedCustomer ? selectedCustomer : {}}
          onSave={handleSave}
          onCancel={() => setSubScreen('LIST')}
          loading={loading}
        />
      )}

      {subScreen === 'PROFILE' && selectedCustomer && (
        <CustomerProfile 
          customer={selectedCustomer}
          onBack={() => setSubScreen('LIST')}
          onEdit={() => setSubScreen('EDIT')}
          onDelete={() => handleDelete(selectedCustomer.id)}
          onBookAppointment={() => {
            if (onNavigateToAppointments) {
              // we can't easily pass the customer without global state right now, 
              // but navigating to appointments is enough.
              onNavigateToAppointments();
            }
          }}
        />
      )}
    </div>
  );
};
