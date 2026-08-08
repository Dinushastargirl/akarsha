import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { apiFetch } from '../../services/api';
import akarshaLogo from '../../assets/akarsha-logo.svg';

interface LoginScreenProps {
  onLoginSuccess: (token: string) => void;
  onSignupRedirect: () => void;
  onError: (msg: string) => void;
}

export const LoginScreen: React.FC<LoginScreenProps> = ({ onLoginSuccess, onSignupRedirect, onError }) => {
  const { t } = useTranslation();
  const [emailInput, setEmailInput] = useState('');
  const [passwordInput, setPasswordInput] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!emailInput || !passwordInput) {
      onError(t('allFieldsRequired'));
      return;
    }
    setLoading(true);
    try {
      const res = await apiFetch('/public/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email: emailInput, password: passwordInput })
      });
      if (res.status === 200) {
        const data = await res.json();
        onLoginSuccess(data.token);
      } else {
        const msg = await res.text();
        onError(msg || t('authFailed'));
      }
    } catch (err) {
      onError('Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md w-full mx-auto">
      <form onSubmit={handleLogin} className="space-y-6">
        {/* Brand Logo */}
        <div className="flex justify-center mb-4">
          <div className="bg-brand-800 rounded-2xl px-8 py-5 shadow-md-flat">
            <img src={akarshaLogo} alt="Akarsha" className="h-10" />
          </div>
        </div>
        <div>
          <h1 className="akarsha-heading text-3xl mb-2">{t('loginTitle')}</h1>
          <p className="akarsha-body text-sm">{t('loginSubtitle')}</p>
        </div>
        
        <div className="space-y-4">
          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('email')}</label>
            <input 
              type="email" 
              value={emailInput} 
              onChange={(e) => setEmailInput(e.target.value)}
              className="akarsha-input"
              placeholder="name@salon.com"
            />
          </div>
          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('password')}</label>
            <input 
              type="password" 
              value={passwordInput} 
              onChange={(e) => setPasswordInput(e.target.value)}
              className="akarsha-input"
              placeholder="••••••••"
            />
          </div>
        </div>

        <button 
          type="submit" 
          disabled={loading}
          className="w-full akarsha-btn-primary disabled:opacity-50"
        >
          {loading ? '...' : t('loginBtn')}
        </button>

        <p className="text-center text-xs text-neutral-500">
          {t('noAccount')}{' '}
          <button 
            type="button" 
            onClick={onSignupRedirect}
            className="text-brand-800 font-medium underline"
          >
            {t('createAccountLink')}
          </button>
        </p>
      </form>
    </div>
  );
};
