import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { apiFetch } from '../../services/api';
import akarshaLogo from '../../assets/akarsha-logo.svg';

interface SignupScreenProps {
  onSignupSuccess: (token: string) => void;
  onLoginRedirect: () => void;
  onError: (msg: string) => void;
}

export const SignupScreen: React.FC<SignupScreenProps> = ({ onSignupSuccess, onLoginRedirect, onError }) => {
  const { t } = useTranslation();
  const [emailInput, setEmailInput] = useState('');
  const [passwordInput, setPasswordInput] = useState('');
  const [confirmPasswordInput, setConfirmPasswordInput] = useState('');
  const [fullNameInput, setFullNameInput] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullNameInput || !emailInput || !passwordInput || !confirmPasswordInput) {
      onError(t('allFieldsRequired'));
      return;
    }
    if (passwordInput !== confirmPasswordInput) {
      onError(t('passwordMismatch'));
      return;
    }
    setLoading(true);
    try {
      const res = await apiFetch('/public/auth/signup', {
        method: 'POST',
        body: JSON.stringify({ fullName: fullNameInput, email: emailInput, password: passwordInput })
      });
      if (res.status === 200) {
        const data = await res.json();
        onSignupSuccess(data.token);
      } else {
        const msg = await res.text();
        onError(msg || 'Signup failed.');
      }
    } catch (err) {
      onError('Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md w-full mx-auto">
      <form onSubmit={handleSignup} className="space-y-6">
        {/* Brand Logo */}
        <div className="flex justify-center mb-4">
          <div className="bg-brand-800 rounded-2xl px-8 py-5 shadow-md-flat">
            <img src={akarshaLogo} alt="Akarsha" className="h-10" />
          </div>
        </div>
        <div>
          <h1 className="akarsha-heading text-3xl mb-2">{t('signupTitle')}</h1>
          <p className="akarsha-body text-sm">{t('signupSubtitle')}</p>
        </div>

        <div className="space-y-4">
          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('fullName')}</label>
            <input 
              type="text" 
              value={fullNameInput} 
              onChange={(e) => setFullNameInput(e.target.value)}
              className="akarsha-input"
              placeholder="Sunil Perera"
            />
          </div>
          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('email')}</label>
            <input 
              type="email" 
              value={emailInput} 
              onChange={(e) => setEmailInput(e.target.value)}
              className="akarsha-input"
              placeholder="sunil@example.com"
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
          <div className="akarsha-form-group">
            <label className="akarsha-label">{t('confirmPassword')}</label>
            <input 
              type="password" 
              value={confirmPasswordInput} 
              onChange={(e) => setConfirmPasswordInput(e.target.value)}
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
          {loading ? '...' : t('signupBtn')}
        </button>

        <p className="text-center text-xs text-neutral-500">
          {t('alreadyHaveAccount')}{' '}
          <button 
            type="button" 
            onClick={onLoginRedirect}
            className="text-brand-800 font-medium underline"
          >
            {t('loginLink')}
          </button>
        </p>
      </form>
    </div>
  );
};
