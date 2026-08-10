import { useState, useEffect } from 'react';
import { Calendar, Clock, User, CheckCircle, ArrowLeft } from 'lucide-react';
import { ChatWidget } from '../../features/chat/ChatWidget';
import { apiFetch } from '../../services/api';
import type { SalonData } from '../../types';

interface BackendServiceData {
  id: number;
  name: string;
  description: string;
  price: number;
  durationMinutes: number;
}

interface PublicBookingPageProps {
  salonSlug: string;
}

type Step = 'SERVICE' | 'STAFF' | 'DATETIME' | 'DETAILS' | 'CONFIRMATION';

export function PublicBookingPage({ salonSlug }: PublicBookingPageProps) {
  const [salon, setSalon] = useState<SalonData | null>(null);
  const [services, setServices] = useState<BackendServiceData[]>([]);
  const [staff, setStaff] = useState<any[]>([]);
  
  const [step, setStep] = useState<Step>('SERVICE');
  const [selectedService, setSelectedService] = useState<BackendServiceData | null>(null);
  const [selectedStaff, setSelectedStaff] = useState<any | null>(null);
  
  const [selectedDate, setSelectedDate] = useState<string>('');
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);
  const [selectedTime, setSelectedTime] = useState<string>('');
  
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    email: '',
    password: '',
    notes: ''
  });
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchSalonInfo = async () => {
      try {
        const res = await apiFetch(`/public/booking/${salonSlug}/info`);
        if (!res.ok) throw new Error('Salon not found');
        const data = await res.json();
        setSalon(data);
        
        const [servicesRes, staffRes] = await Promise.all([
          apiFetch(`/public/booking/${salonSlug}/services`),
          apiFetch(`/public/booking/${salonSlug}/staff`)
        ]);
        
        if (servicesRes.ok) setServices(await servicesRes.json());
        if (staffRes.ok) setStaff(await staffRes.json());
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    fetchSalonInfo();
  }, [salonSlug]);

  useEffect(() => {
    if (selectedDate && selectedStaff && selectedService) {
      const fetchSlots = async () => {
        try {
          const res = await apiFetch(
            `/public/booking/${salonSlug}/availability?staffId=${selectedStaff.id}&date=${selectedDate}&durationMinutes=${selectedService.durationMinutes}`
          );
          if (res.ok) {
            setAvailableSlots(await res.json());
          }
        } catch (err) {
          console.error(err);
        }
      };
      fetchSlots();
    }
  }, [selectedDate, selectedStaff, selectedService, salonSlug]);

  const handleBook = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      const res = await apiFetch(`/public/booking/${salonSlug}/book`, {
        method: 'POST',
        body: JSON.stringify({
          serviceId: selectedService?.id,
          staffId: selectedStaff?.id,
          date: selectedDate,
          time: selectedTime,
          customerName: formData.name,
          customerPhone: formData.phone,
          customerEmail: formData.email,
          customerPassword: formData.password,
          notes: formData.notes
        })
      });
      if (!res.ok) {
        const msg = await res.text();
        throw new Error(msg);
      }
      setStep('CONFIRMATION');
    } catch (err: any) {
      setError(err.message || 'Failed to book appointment');
    }
  };

  if (loading) return <div className="min-h-screen flex items-center justify-center text-brand-800">Loading...</div>;
  if (error && !salon) return <div className="min-h-screen flex items-center justify-center text-red-600">{error}</div>;

  return (
    <div className="min-h-screen bg-brand-50 font-sans text-brand-900">
      <div className="bg-brand-900 text-brand-50 py-12 px-4 shadow-md text-center">
        <h1 className="text-3xl font-bold">{salon?.name}</h1>
        <p className="mt-2 text-brand-200">{salon?.address}, {salon?.city}</p>
      </div>

      <div className="max-w-3xl mx-auto py-8 px-4">
        {error && (
          <div className="mb-6 p-4 bg-red-50 text-red-700 rounded-lg border border-red-200">
            {error}
          </div>
        )}

        <div className="bg-white rounded-xl shadow-lg border border-brand-100 overflow-hidden">
          {step === 'SERVICE' && (
            <div className="p-8 animate-subtle-fade">
              <h2 className="text-2xl font-semibold mb-6">Select a Service</h2>
              <div className="grid gap-4">
                {services.map(s => (
                  <button
                    key={s.id}
                    onClick={() => { setSelectedService(s); setStep('STAFF'); }}
                    className="flex justify-between items-center p-4 border border-brand-100 rounded-lg hover:border-brand-300 hover:bg-brand-50 transition-colors text-left"
                  >
                    <div>
                      <div className="font-medium text-lg">{s.name}</div>
                      <div className="text-brand-500 text-sm mt-1">{s.durationMinutes} mins • {s.description}</div>
                    </div>
                    <div className="font-semibold text-brand-800">${s.price.toFixed(2)}</div>
                  </button>
                ))}
              </div>
            </div>
          )}

          {step === 'STAFF' && (
            <div className="p-8 animate-subtle-fade">
              <button onClick={() => setStep('SERVICE')} className="flex items-center text-brand-500 hover:text-brand-700 mb-6 transition-colors">
                <ArrowLeft className="w-4 h-4 mr-2" /> Back
              </button>
              <h2 className="text-2xl font-semibold mb-6">Select a Specialist</h2>
              <div className="grid sm:grid-cols-2 gap-4">
                {staff.map(st => (
                  <button
                    key={st.id}
                    onClick={() => { setSelectedStaff(st); setStep('DATETIME'); }}
                    className="flex items-center p-4 border border-brand-100 rounded-lg hover:border-brand-300 hover:bg-brand-50 transition-colors text-left"
                  >
                    <div className="w-12 h-12 rounded-full bg-brand-100 flex items-center justify-center text-brand-600 mr-4">
                      <User className="w-6 h-6" />
                    </div>
                    <div>
                      <div className="font-medium">{st.fullName}</div>
                      <div className="text-sm text-brand-500 capitalize">{st.role.toLowerCase()}</div>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}

          {step === 'DATETIME' && (
            <div className="p-8 animate-subtle-fade">
              <button onClick={() => setStep('STAFF')} className="flex items-center text-brand-500 hover:text-brand-700 mb-6 transition-colors">
                <ArrowLeft className="w-4 h-4 mr-2" /> Back
              </button>
              <h2 className="text-2xl font-semibold mb-6">Select Date & Time</h2>
              
              <div className="mb-6">
                <label className="block text-sm font-medium text-brand-700 mb-2">Date</label>
                <input
                  type="date"
                  min={new Date().toISOString().split('T')[0]}
                  value={selectedDate}
                  onChange={(e) => { setSelectedDate(e.target.value); setSelectedTime(''); }}
                  className="w-full sm:w-64 px-4 py-2 border border-brand-200 rounded-lg focus:ring-2 focus:ring-brand-500 outline-none"
                />
              </div>

              {selectedDate && (
                <div>
                  <label className="block text-sm font-medium text-brand-700 mb-2">Available Times</label>
                  {availableSlots.length > 0 ? (
                    <div className="grid grid-cols-3 sm:grid-cols-4 gap-3">
                      {availableSlots.map(time => (
                        <button
                          key={time}
                          onClick={() => { setSelectedTime(time); setStep('DETAILS'); }}
                          className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                            selectedTime === time 
                              ? 'bg-brand-600 text-white' 
                              : 'bg-brand-50 text-brand-700 hover:bg-brand-100 border border-brand-200'
                          }`}
                        >
                          {time}
                        </button>
                      ))}
                    </div>
                  ) : (
                    <div className="text-brand-500 italic">No available slots for this date.</div>
                  )}
                </div>
              )}
            </div>
          )}

          {step === 'DETAILS' && (
            <div className="p-8 animate-subtle-fade">
              <button onClick={() => setStep('DATETIME')} className="flex items-center text-brand-500 hover:text-brand-700 mb-6 transition-colors">
                <ArrowLeft className="w-4 h-4 mr-2" /> Back
              </button>
              <h2 className="text-2xl font-semibold mb-6">Your Details</h2>
              
              <div className="bg-brand-50 p-4 rounded-lg mb-8 flex flex-col sm:flex-row sm:items-center gap-4 sm:gap-8 text-sm">
                <div className="flex items-center"><CheckCircle className="w-4 h-4 text-brand-500 mr-2" /> {selectedService?.name}</div>
                <div className="flex items-center"><User className="w-4 h-4 text-brand-500 mr-2" /> {selectedStaff?.fullName}</div>
                <div className="flex items-center"><Calendar className="w-4 h-4 text-brand-500 mr-2" /> {selectedDate}</div>
                <div className="flex items-center"><Clock className="w-4 h-4 text-brand-500 mr-2" /> {selectedTime}</div>
              </div>

              <form onSubmit={handleBook} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-brand-700 mb-1">Full Name *</label>
                  <input required type="text" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="w-full px-4 py-2 border border-brand-200 rounded-lg focus:ring-2 focus:ring-brand-500 outline-none" />
                </div>
                <div className="grid sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-brand-700 mb-1">Phone Number *</label>
                    <input required type="tel" value={formData.phone} onChange={e => setFormData({...formData, phone: e.target.value})} className="w-full px-4 py-2 border border-brand-200 rounded-lg focus:ring-2 focus:ring-brand-500 outline-none" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-brand-700 mb-1">Email</label>
                    <input type="email" value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} className="w-full px-4 py-2 border border-brand-200 rounded-lg focus:ring-2 focus:ring-brand-500 outline-none" />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-brand-700 mb-1">Create Password (Optional, to manage bookings)</label>
                  <input type="password" value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})} className="w-full px-4 py-2 border border-brand-200 rounded-lg focus:ring-2 focus:ring-brand-500 outline-none" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-brand-700 mb-1">Notes</label>
                  <textarea value={formData.notes} onChange={e => setFormData({...formData, notes: e.target.value})} className="w-full px-4 py-2 border border-brand-200 rounded-lg focus:ring-2 focus:ring-brand-500 outline-none h-24" />
                </div>
                
                <button type="submit" className="w-full mt-6 bg-brand-800 text-white font-medium py-3 rounded-lg hover:bg-brand-900 transition-colors">
                  Confirm Booking
                </button>
              </form>
            </div>
          )}

          {step === 'CONFIRMATION' && (
            <div className="p-12 text-center animate-subtle-fade">
              <div className="w-16 h-16 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto mb-6">
                <CheckCircle className="w-8 h-8" />
              </div>
              <h2 className="text-3xl font-bold mb-4">Booking Confirmed!</h2>
              <p className="text-brand-600 mb-8">We've received your booking for {selectedService?.name} on {selectedDate} at {selectedTime}.</p>
              
              <div className="space-x-4">
                <button onClick={() => window.location.href = `/portal/${salonSlug}`} className="bg-brand-100 text-brand-800 px-6 py-2 rounded hover:bg-brand-200 transition-colors font-medium">
                  Manage My Bookings
                </button>
                <button onClick={() => window.location.reload()} className="text-brand-600 hover:text-brand-800 font-medium">
                  Book Another
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
      <ChatWidget salonSlug={salonSlug} />
    </div>
  );
}
