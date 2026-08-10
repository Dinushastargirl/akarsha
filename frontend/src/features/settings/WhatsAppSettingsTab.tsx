import React, { useState, useEffect } from 'react';
import { Smartphone, Send, ShieldCheck, RefreshCw } from 'lucide-react';
import { apiFetch } from '../../services/api';

interface WhatsAppSettingsTabProps {
  onError: (msg: string) => void;
  onSuccess: (msg: string) => void;
}

export const WhatsAppSettingsTab: React.FC<WhatsAppSettingsTabProps> = ({ onError, onSuccess }) => {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [verifying, setVerifying] = useState(false);
  const [sendingTest, setSendingTest] = useState(false);
  const [testPhone, setTestPhone] = useState('');
  const [connectionStatus, setConnectionStatus] = useState<'NOT_CONNECTED' | 'CONNECTED' | 'ERROR' | 'DISABLED'>('NOT_CONNECTED');
  const [formData, setFormData] = useState({
    phoneNumberId: '',
    wabaId: '',
    accessToken: '',
    displayPhoneNumber: '',
    enabled: false,
    webhookVerified: false
  });

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      setLoading(true);
      const res = await apiFetch('/settings/whatsapp');
      if (res.ok) {
        const data = await res.json();
        const configData = {
          phoneNumberId: data.phoneNumberId || '',
          wabaId: data.wabaId || '',
          accessToken: data.accessToken || '',
          displayPhoneNumber: data.displayPhoneNumber || '',
          enabled: !!data.enabled,
          webhookVerified: !!data.webhookVerified
        };
        setFormData(configData);

        if (!configData.enabled) {
          setConnectionStatus('DISABLED');
        } else if (configData.webhookVerified) {
          setConnectionStatus('CONNECTED');
        } else {
          setConnectionStatus('NOT_CONNECTED');
        }
      }
    } catch (err) {
      console.error(err);
      onError('Failed to load WhatsApp settings');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSaving(true);
      const res = await apiFetch('/settings/whatsapp', {
        method: 'POST',
        body: JSON.stringify(formData)
      });
      if (res.ok) {
        onSuccess('WhatsApp settings updated successfully');
        loadSettings();
      } else {
        onError('Failed to update WhatsApp settings');
      }
    } catch (err) {
      onError('Failed to update WhatsApp settings');
    } finally {
      setSaving(false);
    }
  };

  const handleVerify = async () => {
    try {
      setVerifying(true);
      const res = await apiFetch('/settings/whatsapp/verify', { method: 'POST' });
      if (res.ok) {
        onSuccess('Connection verified successfully!');
        setConnectionStatus('CONNECTED');
        setFormData(prev => ({ ...prev, webhookVerified: true }));
      } else {
        setConnectionStatus('ERROR');
        onError('Failed to verify Meta Cloud API connection');
      }
    } catch (e) {
      setConnectionStatus('ERROR');
      onError('Error verifying connection');
    } finally {
      setVerifying(false);
    }
  };

  const handleSendTest = async () => {
    if (!testPhone) {
      onError('Please enter a recipient phone number for the test');
      return;
    }
    try {
      setSendingTest(true);
      const res = await apiFetch('/settings/whatsapp/test', {
        method: 'POST',
        body: JSON.stringify({ to: testPhone })
      });
      if (res.ok) {
        onSuccess('Test message sent successfully!');
      } else {
        onError('Failed to dispatch test message');
      }
    } catch (e) {
      onError('Error dispatching test message');
    } finally {
      setSendingTest(false);
    }
  };

  if (loading) return <div className="p-8 text-neutral-500 font-light text-center">Loading settings...</div>;

  return (
    <div className="space-y-8 animate-subtle-fade">
      <form onSubmit={handleSubmit} className="space-y-8">
        <div className="bg-white p-6 md:p-8 rounded-xl border border-neutral-100 shadow-sm">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
            <div>
              <h2 className="text-lg font-medium text-neutral-900 flex items-center space-x-2">
                <Smartphone className="w-5 h-5 text-brand-900" />
                <span>WhatsApp Cloud API Config</span>
              </h2>
              <p className="text-sm text-neutral-500 mt-1">Configure Meta Developer credentials for your salon's own WhatsApp number.</p>
            </div>
            
            <div className="flex items-center space-x-3">
              <span className={`text-xs px-3 py-1 rounded-full font-medium ${
                connectionStatus === 'CONNECTED' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' :
                connectionStatus === 'DISABLED' ? 'bg-neutral-100 text-neutral-600 border border-neutral-200' :
                connectionStatus === 'ERROR' ? 'bg-rose-50 text-rose-700 border border-rose-200' :
                'bg-amber-50 text-amber-700 border border-amber-200'
              }`}>
                {connectionStatus === 'CONNECTED' && 'Connected'}
                {connectionStatus === 'DISABLED' && 'Disabled'}
                {connectionStatus === 'ERROR' && 'Configuration Error'}
                {connectionStatus === 'NOT_CONNECTED' && 'Needs Verification'}
              </span>

              <label className="flex items-center cursor-pointer">
                <input 
                  type="checkbox" 
                  className="sr-only" 
                  checked={formData.enabled}
                  onChange={e => setFormData({ ...formData, enabled: e.target.checked })}
                />
                <div className={`relative w-11 h-6 bg-neutral-200 rounded-full transition-colors ${formData.enabled ? 'bg-brand-900' : ''}`}>
                  <div className={`absolute top-0.5 left-0.5 bg-white w-5 h-5 rounded-full transition-transform ${formData.enabled ? 'transform translate-x-5' : ''}`}></div>
                </div>
              </label>
            </div>
          </div>

          <div className="space-y-5">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label className="block text-xs font-semibold uppercase text-neutral-500 tracking-wider mb-1.5">Phone Number ID</label>
                <input
                  type="text"
                  required
                  value={formData.phoneNumberId}
                  onChange={e => setFormData({ ...formData, phoneNumberId: e.target.value })}
                  className="w-full px-4 py-2.5 rounded-lg border border-neutral-200 focus:border-brand-500 focus:ring-1 focus:ring-brand-500 outline-none transition-all text-sm"
                  placeholder="e.g. 1093847293847"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold uppercase text-neutral-500 tracking-wider mb-1.5">WhatsApp Business Account ID (WABA ID)</label>
                <input
                  type="text"
                  required
                  value={formData.wabaId}
                  onChange={e => setFormData({ ...formData, wabaId: e.target.value })}
                  className="w-full px-4 py-2.5 rounded-lg border border-neutral-200 focus:border-brand-500 focus:ring-1 focus:ring-brand-500 outline-none transition-all text-sm"
                  placeholder="e.g. 2938472938472"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase text-neutral-500 tracking-wider mb-1.5">System Access Token (Meta Cloud API)</label>
              <input
                type="password"
                required
                value={formData.accessToken}
                onChange={e => setFormData({ ...formData, accessToken: e.target.value })}
                className="w-full px-4 py-2.5 rounded-lg border border-neutral-200 focus:border-brand-500 focus:ring-1 focus:ring-brand-500 outline-none transition-all text-sm"
                placeholder="Paste Access Token here"
              />
              <p className="text-xs text-neutral-400 mt-1 flex items-center gap-1">
                <ShieldCheck className="w-3.5 h-3.5 text-neutral-400" />
                <span>Tokens are stored with production-grade AES-256 encryption. We never display saved tokens.</span>
              </p>
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase text-neutral-500 tracking-wider mb-1.5">Display Phone Number</label>
              <input
                type="text"
                value={formData.displayPhoneNumber}
                onChange={e => setFormData({ ...formData, displayPhoneNumber: e.target.value })}
                className="w-full px-4 py-2.5 rounded-lg border border-neutral-200 focus:border-brand-500 focus:ring-1 focus:ring-brand-500 outline-none transition-all text-sm"
                placeholder="e.g. +94 77 123 4567"
              />
            </div>
          </div>
        </div>

        <div className="flex justify-end pt-4 border-t border-neutral-100 gap-3">
          {formData.enabled && (
            <button
              type="button"
              onClick={handleVerify}
              disabled={verifying}
              className="flex items-center gap-2 px-5 py-2.5 border border-neutral-300 text-neutral-700 rounded-lg hover:bg-neutral-50 transition-colors text-sm font-medium"
            >
              <RefreshCw className={`w-4 h-4 ${verifying ? 'animate-spin' : ''}`} />
              Verify Connection
            </button>
          )}
          <button
            type="submit"
            disabled={saving}
            className="flex items-center px-6 py-2.5 bg-brand-900 text-white rounded-lg hover:bg-brand-800 transition-colors disabled:opacity-50 text-sm font-medium"
          >
            {saving ? 'Saving...' : 'Save Settings'}
          </button>
        </div>
      </form>

      {/* Test Console */}
      {formData.webhookVerified && formData.enabled && (
        <div className="bg-white p-6 md:p-8 rounded-xl border border-neutral-100 shadow-sm animate-subtle-fade">
          <h3 className="text-sm font-semibold text-neutral-900 mb-2 uppercase tracking-wider">Test Sandbox</h3>
          <p className="text-xs text-neutral-500 mb-4">Send a quick outbound test message to check if your access token has been verified.</p>
          
          <div className="flex gap-3 max-w-md">
            <input
              type="text"
              value={testPhone}
              onChange={e => setTestPhone(e.target.value)}
              placeholder="Recipient phone (e.g. +94771234567)"
              className="flex-grow px-4 py-2 rounded-lg border border-neutral-200 focus:border-brand-500 focus:ring-1 focus:ring-brand-500 outline-none text-sm"
            />
            <button
              type="button"
              onClick={handleSendTest}
              disabled={sendingTest}
              className="flex items-center gap-2 px-4 py-2 bg-neutral-900 text-white rounded-lg hover:bg-neutral-800 transition-colors text-sm font-medium disabled:opacity-50"
            >
              <Send className="w-3.5 h-3.5" />
              {sendingTest ? 'Sending...' : 'Send Test'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
