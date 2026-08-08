import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Globe, LogOut, Settings } from 'lucide-react';
import akarshaLogoDark from './assets/akarsha-logo-dark.svg';
import akarshaMark from './assets/akarsha-mark.svg';
import { LoginScreen } from './features/auth/LoginScreen';
import { SignupScreen } from './features/auth/SignupScreen';
import { ChatWidget } from './features/chat/ChatWidget';
import { CreateSalonScreen } from './features/onboarding/CreateSalonScreen';
import { SetupWizard } from './features/onboarding/SetupWizard';
import { Dashboard } from './pages/Dashboard';
import { CustomersPage } from './pages/CustomersPage';
import { AppointmentsPage } from './pages/AppointmentsPage';
import { StaffPage } from './pages/StaffPage';
import { ServicesPage } from './pages/ServicesPage';
import { SettingsPage } from './pages/SettingsPage';
import type { SalonData } from './types';
import { ReportsPage } from './pages/ReportsPage';
import { ConversationsPage } from './pages/ConversationsPage';
import { PlatformDashboardPage } from './pages/platform/PlatformDashboardPage';
import { PlatformSalonsPage } from './pages/platform/PlatformSalonsPage';
import { PublicBookingPage } from './pages/public/PublicBookingPage';
import { CustomerPortalPage } from './pages/public/CustomerPortalPage';

type ScreenState = 'LOGIN' | 'SIGNUP' | 'CREATE_SALON' | 'SETUP_WIZARD' | 'DASHBOARD' | 'CUSTOMERS' | 'APPOINTMENTS' | 'STAFF' | 'SERVICES' | 'SETTINGS' | 'REPORTS' | 'CONVERSATIONS' | 'PLATFORM_DASHBOARD' | 'PLATFORM_SALONS';

function App() {
  const { t, i18n } = useTranslation();
  const [screen, setScreen] = useState<ScreenState>('LOGIN');
  const [token, setToken] = useState<string | null>(localStorage.getItem('akarsha_token'));
  const [error, setError] = useState<string | null>(null);

  // Global user state (cached for dashboard view context)
  const [userEmail, setUserEmail] = useState('');
  const [userRole, setUserRole] = useState<string>('');
  const [salon, setSalon] = useState<SalonData>({
    name: '',
    subdomain: '',
    phone: '',
    address: '',
    city: '',
    businessType: 'Salon'
  });

  // Client-side routing for public pages
  const path = window.location.pathname;
  if (path.startsWith('/book/')) {
    const slug = path.split('/')[2];
    return <PublicBookingPage salonSlug={slug} />;
  }
  if (path.startsWith('/portal/')) {
    const slug = path.split('/')[2];
    return <CustomerPortalPage salonSlug={slug} />;
  }

  useEffect(() => {
    if (token) {
      localStorage.setItem('akarsha_token', token);
      const payload = JSON.parse(atob(token.split('.')[1]));
      setUserEmail(payload.sub || '');
      setUserRole(payload.role || '');
      if (!payload.tenantId && payload.role === 'SUPER_ADMIN') {
        if (screen === 'LOGIN' || screen === 'SIGNUP') {
          setScreen('PLATFORM_DASHBOARD');
        }
      } else if (!payload.tenantId) {
        setScreen('CREATE_SALON');
      } else {
        setSalon(prev => ({ ...prev, subdomain: payload.tenantId }));
        // If they just logged in, default to their dashboard
        if (screen === 'LOGIN' || screen === 'SIGNUP') {
          setScreen('DASHBOARD');
        }
      }
    } else {
      localStorage.removeItem('akarsha_token');
      setUserEmail('');
    }
  }, [token]);

  const changeLanguage = (lng: string) => {
    i18n.changeLanguage(lng);
  };

  const handleError = (msg: string) => {
    setError(msg);
    setTimeout(() => setError(null), 5000);
  };

  const handleLogout = () => {
    setToken(null);
    setScreen('LOGIN');
    setSalon({ name: '', subdomain: '', phone: '', address: '', city: '', businessType: 'Salon' });
  };

  return (
    <div className="min-h-screen bg-neutral-50 flex flex-col justify-between py-10 px-6 sm:px-12 md:px-24 text-neutral-800 animate-subtle-fade">
      
      {/* Header */}
      <header className="max-w-6xl w-full mx-auto flex flex-wrap items-center justify-between gap-y-4 border-b border-neutral-200 pb-6">
        <div className="flex items-center space-x-4 lg:space-x-8">
          <img src={akarshaLogoDark} alt="Akarsha" className="h-8 hidden sm:block" />
          <img src={akarshaMark} alt="Akarsha" className="h-8 sm:hidden bg-brand-800 rounded-md p-1" />
          {token && ['PLATFORM_DASHBOARD', 'PLATFORM_SALONS'].includes(screen) && (
            <nav className="hidden sm:flex space-x-3 lg:space-x-6 pl-4 border-l border-neutral-200 overflow-x-auto">
              <button 
                onClick={() => { setScreen('PLATFORM_DASHBOARD'); setError(null); }}
                className={`text-sm font-medium transition-colors py-1 ${screen === 'PLATFORM_DASHBOARD' ? 'text-brand-800 border-b-2 border-brand-800' : 'text-neutral-500 hover:text-neutral-800'}`}
              >
                Platform Overview
              </button>
              <button 
                onClick={() => { setScreen('PLATFORM_SALONS'); setError(null); }}
                className={`text-sm font-medium transition-colors py-1 ${screen === 'PLATFORM_SALONS' ? 'text-brand-800 border-b-2 border-brand-800' : 'text-neutral-500 hover:text-neutral-800'}`}
              >
                Salons Management
              </button>
            </nav>
          )}

          {token && ['DASHBOARD', 'CUSTOMERS', 'APPOINTMENTS', 'STAFF', 'SERVICES'].includes(screen) && (
            <nav className="hidden sm:flex space-x-3 lg:space-x-6 pl-4 border-l border-neutral-200 overflow-x-auto">
              <button 
                onClick={() => { setScreen('DASHBOARD'); setError(null); }}
                className={`text-sm font-medium transition-colors py-1 ${screen === 'DASHBOARD' ? 'text-brand-800 border-b-2 border-brand-800' : 'text-neutral-500 hover:text-neutral-800'}`}
              >
                {t('navDashboard')}
              </button>
              <button 
                onClick={() => { setScreen('APPOINTMENTS'); setError(null); }}
                className={`text-sm font-medium transition-colors py-1 ${screen === 'APPOINTMENTS' ? 'text-brand-800 border-b-2 border-brand-800' : 'text-neutral-500 hover:text-neutral-800'}`}
              >
                {t('navAppointments')}
              </button>
              
              {userRole !== 'STAFF' && userRole !== 'RECEPTIONIST' && (
                <button 
                  onClick={() => { setScreen('STAFF'); setError(null); }}
                  className={`text-sm font-medium transition-colors py-1 ${screen === 'STAFF' ? 'text-brand-800 border-b-2 border-brand-800' : 'text-neutral-500 hover:text-neutral-800'}`}
                >
                  {t('navStaff')}
                </button>
              )}

              {userRole !== 'STAFF' && (
                <button 
                  onClick={() => { setScreen('SERVICES'); setError(null); }}
                  className={`text-sm font-medium transition-colors py-1 ${screen === 'SERVICES' ? 'text-brand-800 border-b-2 border-brand-800' : 'text-neutral-500 hover:text-neutral-800'}`}
                >
                  {t('navServices')}
                </button>
              )}

              {userRole !== 'STAFF' && userRole !== 'RECEPTIONIST' && (
                <button 
                  onClick={() => { setScreen('REPORTS'); setError(null); }}
                  className={`text-sm font-medium transition-colors py-1 ${screen === 'REPORTS' ? 'text-brand-800 border-b-2 border-brand-800' : 'text-neutral-500 hover:text-neutral-800'}`}
                >
                  {t('navReports')}
                </button>
              )}
              <button 
                onClick={() => { setScreen('CUSTOMERS'); setError(null); }}
                className={`text-sm font-medium transition-colors py-1 ${screen === 'CUSTOMERS' ? 'text-brand-800 border-b-2 border-brand-800' : 'text-neutral-500 hover:text-neutral-800'}`}
              >
                {t('navCustomers')}
              </button>
              
              {userRole !== 'STAFF' && (
                <button 
                  onClick={() => { setScreen('CONVERSATIONS'); setError(null); }}
                  className={`text-sm font-medium transition-colors py-1 ${screen === 'CONVERSATIONS' ? 'text-brand-800 border-b-2 border-brand-800' : 'text-neutral-500 hover:text-neutral-800'}`}
                >
                  Inbox
                </button>
              )}
            </nav>
          )}
        </div>
        
        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-2 border border-neutral-200 rounded px-2.5 py-1 bg-white">
            <Globe className="w-3.5 h-3.5 text-neutral-400" />
            <select 
              onChange={(e) => changeLanguage(e.target.value)} 
              value={i18n.language}
              className="text-xs bg-transparent border-none text-neutral-600 focus:ring-0 cursor-pointer font-medium hover:text-neutral-900 outline-none"
            >
              <option value="en">English</option>
              <option value="si_lk">සිංහල (Sinhala)</option>
              <option value="ta_lk">தமிழ் (Tamil)</option>
            </select>
          </div>

          {token && (
            <div className="flex items-center space-x-4">
              {userRole !== 'STAFF' && userRole !== 'RECEPTIONIST' && (
                <button 
                  onClick={() => { setScreen('SETTINGS'); setError(null); }}
                  className={`flex items-center justify-center p-1.5 rounded transition-colors ${screen === 'SETTINGS' ? 'bg-neutral-200 text-neutral-900' : 'text-neutral-500 hover:bg-neutral-100 hover:text-neutral-800'}`}
                  title={t('navSettings')}
                >
                  <Settings className="w-4 h-4" />
                </button>
              )}
              
              <button 
                onClick={handleLogout}
                className="flex items-center space-x-1 text-xs text-neutral-500 hover:text-neutral-800 transition-colors"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">{t('logoutBtn')}</span>
              </button>
            </div>
          )}
        </div>
      </header>

      {/* Error Alert Box */}
      {error && (
        <div className="max-w-md w-full mx-auto mt-4 bg-red-50 border-l-4 border-red-500 p-4 flex items-start space-x-3 rounded-r shadow-sm-flat">
          <span className="text-sm text-red-700">{error}</span>
        </div>
      )}

      {/* Main Content */}
      <main className="max-w-4xl w-full mx-auto my-12 flex-grow flex flex-col justify-center">
        {screen === 'LOGIN' && (
          <LoginScreen 
            onLoginSuccess={(tok) => setToken(tok)}
            onSignupRedirect={() => setScreen('SIGNUP')}
            onError={handleError}
          />
        )}

        {screen === 'SIGNUP' && (
          <SignupScreen 
            onSignupSuccess={(tok) => setToken(tok)}
            onLoginRedirect={() => setScreen('LOGIN')}
            onError={handleError}
          />
        )}

        {screen === 'CREATE_SALON' && (
          <CreateSalonScreen 
            salon={salon}
            onChange={(s) => setSalon(s)}
            onCreateSuccess={(tok) => { setToken(tok); setScreen('SETUP_WIZARD'); }}
            onError={handleError}
          />
        )}

        {screen === 'SETUP_WIZARD' && (
          <SetupWizard 
            onSetupFinished={() => setScreen('DASHBOARD')}
            onError={handleError}
          />
        )}

        {screen === 'DASHBOARD' && (
          <Dashboard
            salon={salon}
            emailInput={userEmail}
            onNavigateToCustomers={() => setScreen('CUSTOMERS')}
            onNavigateToAppointments={() => setScreen('APPOINTMENTS')}
            onNavigateToStaff={() => setScreen('STAFF')}
            onNavigateToServices={() => setScreen('SERVICES')}
          />
        )}

        {screen === 'CUSTOMERS' && (
          <CustomersPage 
            onError={handleError}
            onNavigateToAppointments={() => setScreen('APPOINTMENTS')}
          />
        )}

        {screen === 'APPOINTMENTS' && (
          <AppointmentsPage 
            onError={handleError}
          />
        )}

        {screen === 'STAFF' && (
          userRole !== 'STAFF' && userRole !== 'RECEPTIONIST' ? (
            <StaffPage 
              onError={handleError}
            />
          ) : (
            <div className="flex justify-center py-20 text-neutral-500">Access Denied</div>
          )
        )}

        {screen === 'SERVICES' && (
          userRole !== 'STAFF' ? (
            <ServicesPage
              onError={handleError}
            />
          ) : (
            <div className="flex justify-center py-20 text-neutral-500">Access Denied</div>
          )
        )}

        {screen === 'SETTINGS' && (
          userRole !== 'STAFF' && userRole !== 'RECEPTIONIST' ? (
            <SettingsPage
              onError={handleError}
              onSuccess={msg => alert(msg)}
            />
          ) : (
            <div className="flex justify-center py-20 text-neutral-500">Access Denied</div>
          )
        )}

        {screen === 'REPORTS' && (
          userRole !== 'STAFF' && userRole !== 'RECEPTIONIST' ? (
            <ReportsPage
              onError={handleError}
            />
          ) : (
            <div className="flex justify-center py-20 text-neutral-500">Access Denied</div>
          )
        )}

        {screen === 'CONVERSATIONS' && (
          userRole !== 'STAFF' ? (
            <ConversationsPage />
          ) : (
            <div className="flex justify-center py-20 text-neutral-500">Access Denied</div>
          )
        )}

        {screen === 'PLATFORM_DASHBOARD' && (
          userRole === 'SUPER_ADMIN' || userRole === 'ROLE_SUPER_ADMIN' ? (
            <PlatformDashboardPage onError={handleError} />
          ) : (
            <div className="flex justify-center py-20 text-neutral-500">Access Denied</div>
          )
        )}

        {screen === 'PLATFORM_SALONS' && (
          userRole === 'SUPER_ADMIN' || userRole === 'ROLE_SUPER_ADMIN' ? (
            <PlatformSalonsPage onError={handleError} />
          ) : (
            <div className="flex justify-center py-20 text-neutral-500">Access Denied</div>
          )
        )}
      </main>

      {/* Footer */}
      <footer className="max-w-4xl w-full mx-auto border-t border-neutral-200 pt-6 flex flex-col sm:flex-row justify-between items-center text-xs text-neutral-400 gap-4">
        <div>
          <span>© {new Date().getFullYear()} Akarsha. All rights reserved.</span>
        </div>
        <div className="flex space-x-6">
          <a href="#" className="hover:text-neutral-600 transition-colors">Privacy</a>
          <a href="#" className="hover:text-neutral-600 transition-colors">Terms</a>
          <a href="#" className="hover:text-neutral-600 transition-colors">Documentation</a>
        </div>
      </footer>
      <ChatWidget />
    </div>
  );
}

export default App;
