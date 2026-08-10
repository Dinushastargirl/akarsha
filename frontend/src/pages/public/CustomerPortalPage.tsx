import { useState, useEffect } from 'react';
import { Calendar, Clock, User, XCircle, LogOut } from 'lucide-react';
import { ChatWidget } from '../../features/chat/ChatWidget';
import { apiFetch } from '../../services/api';
import type { SalonData } from '../../types';

interface CustomerPortalPageProps {
  salonSlug: string;
}

export function CustomerPortalPage({ salonSlug }: CustomerPortalPageProps) {
  const [salon, setSalon] = useState<SalonData | null>(null);
  const [appointments, setAppointments] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Login state
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem(`customer_token_${salonSlug}`));
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [loginError, setLoginError] = useState<string | null>(null);

  useEffect(() => {
    const fetchSalonInfo = async () => {
      try {
        const res = await apiFetch(`/public/booking/${salonSlug}/info`);
        if (res.ok) setSalon(await res.json());
      } catch (err) {
        console.error(err);
      }
    };
    fetchSalonInfo();
  }, [salonSlug]);

  useEffect(() => {
    if (isLoggedIn) {
      fetchAppointments();
    }
  }, [isLoggedIn, salonSlug]);

  const fetchAppointments = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem(`customer_token_${salonSlug}`);
      // Send the request to /portal/my-appointments using standard apiFetch but with the custom token
      const res = await fetch(`/api/v1/portal/my-appointments`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      if (!res.ok) {
        if (res.status === 401 || res.status === 403) {
          handleLogout();
          throw new Error('Session expired. Please log in again.');
        }
        throw new Error('Failed to fetch appointments');
      }
      setAppointments(await res.json());
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginError(null);
    try {
      const res = await apiFetch(`/public/booking/${salonSlug}/login`, {
        method: 'POST',
        body: JSON.stringify({ phone, password })
      });
      if (!res.ok) throw new Error('Invalid credentials');
      const data = await res.json();
      localStorage.setItem(`customer_token_${salonSlug}`, data.token);
      setIsLoggedIn(true);
    } catch (err: any) {
      setLoginError(err.message);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem(`customer_token_${salonSlug}`);
    setIsLoggedIn(false);
    setAppointments([]);
  };

  const handleCancel = async (id: number) => {
    if (!confirm('Are you sure you want to cancel this appointment?')) return;
    try {
      const token = localStorage.getItem(`customer_token_${salonSlug}`);
      const res = await fetch(`/api/v1/portal/my-appointments/${id}/cancel`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      if (!res.ok) throw new Error('Failed to cancel');
      await fetchAppointments();
    } catch (err: any) {
      alert(err.message);
    }
  };

  if (!isLoggedIn) {
    return (
      <div className="min-h-screen bg-brand-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8 font-sans">
        <div className="sm:mx-auto sm:w-full sm:max-w-md text-center mb-8">
          <h1 className="text-3xl font-bold text-brand-900">{salon?.name || 'Customer Portal'}</h1>
          <p className="text-brand-600 mt-2">Log in to manage your appointments</p>
        </div>
        <div className="sm:mx-auto sm:w-full sm:max-w-md">
          <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10 border border-brand-100">
            {loginError && <div className="mb-4 text-sm text-red-600 bg-red-50 p-3 rounded">{loginError}</div>}
            <form className="space-y-6" onSubmit={handleLogin}>
              <div>
                <label className="block text-sm font-medium text-brand-700">Phone Number</label>
                <input required type="tel" value={phone} onChange={e => setPhone(e.target.value)} className="mt-1 w-full px-3 py-2 border border-brand-300 rounded-md focus:ring-brand-500 focus:border-brand-500 outline-none" />
              </div>
              <div>
                <label className="block text-sm font-medium text-brand-700">Password</label>
                <input required type="password" value={password} onChange={e => setPassword(e.target.value)} className="mt-1 w-full px-3 py-2 border border-brand-300 rounded-md focus:ring-brand-500 focus:border-brand-500 outline-none" />
              </div>
              <button type="submit" className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-brand-800 hover:bg-brand-900 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-brand-500 transition-colors">
                Log in
              </button>
            </form>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-brand-50 font-sans text-brand-900">
      <header className="bg-brand-900 text-brand-50 py-6 px-4 shadow-md flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">{salon?.name} Portal</h1>
          <p className="text-sm text-brand-200">Manage your appointments</p>
        </div>
        <button onClick={handleLogout} className="flex items-center text-brand-200 hover:text-white transition-colors">
          <LogOut className="w-5 h-5 mr-2" /> Logout
        </button>
      </header>

      <main className="max-w-4xl mx-auto py-8 px-4">
        <div className="bg-white rounded-xl shadow-sm border border-brand-100 overflow-hidden">
          <div className="p-6 border-b border-brand-100 flex justify-between items-center">
            <h2 className="text-xl font-semibold">My Appointments</h2>
            <button onClick={() => window.location.href = `/book/${salonSlug}`} className="text-sm font-medium text-brand-600 hover:text-brand-800">
              Book New Appointment
            </button>
          </div>
          
          {loading ? (
            <div className="p-8 text-center text-brand-500">Loading...</div>
          ) : error ? (
            <div className="p-8 text-center text-red-500">{error}</div>
          ) : appointments.length === 0 ? (
            <div className="p-12 text-center text-brand-500">
              <Calendar className="w-12 h-12 mx-auto text-brand-200 mb-4" />
              <p>You have no appointments yet.</p>
            </div>
          ) : (
            <div className="divide-y divide-brand-100">
              {appointments.map(appt => (
                <div key={appt.id} className="p-6 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 hover:bg-brand-50 transition-colors">
                  <div>
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="font-semibold text-lg">{appt.service.name}</h3>
                      <span className={`px-2 py-0.5 rounded text-xs font-medium ${
                        appt.status === 'BOOKED' ? 'bg-blue-100 text-blue-800' :
                        appt.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                        'bg-red-100 text-red-800'
                      }`}>
                        {appt.status}
                      </span>
                    </div>
                    <div className="text-sm text-brand-600 space-y-1">
                      <div className="flex items-center"><Calendar className="w-4 h-4 mr-2" /> {appt.appointmentDate}</div>
                      <div className="flex items-center"><Clock className="w-4 h-4 mr-2" /> {appt.startTime} - {appt.endTime}</div>
                      <div className="flex items-center"><User className="w-4 h-4 mr-2" /> with {appt.staff.fullName}</div>
                    </div>
                  </div>
                  {appt.status === 'BOOKED' && (
                    <button 
                      onClick={() => handleCancel(appt.id)}
                      className="flex items-center px-4 py-2 text-sm font-medium text-red-700 bg-red-50 hover:bg-red-100 rounded transition-colors"
                    >
                      <XCircle className="w-4 h-4 mr-2" /> Cancel
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
      <ChatWidget salonSlug={salonSlug} />
    </div>
  );
}
