import React, { useState, useEffect } from 'react';
import { Loader2, Send, Bot, User, RefreshCw } from 'lucide-react';
import { apiFetch } from '../../services/api';

interface AiSettingsTabProps {
  subdomain: string;
}

export const AiSettingsTab: React.FC<AiSettingsTabProps> = ({ subdomain }) => {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [config, setConfig] = useState<any>({});
  
  // Test playground state
  const [testMessage, setTestMessage] = useState('');
  const [chatHistory, setChatHistory] = useState<Array<{ sender: 'AI' | 'USER'; text: string }>>([]);
  const [sessionId, setSessionId] = useState<string>('');
  const [sendingChat, setSendingChat] = useState(false);

  useEffect(() => {
    loadConfig();
    // Generate a unique session ID for testing
    setSessionId('test_session_' + Math.random().toString(36).substring(2, 9));
  }, []);

  const loadConfig = async () => {
    try {
      setLoading(true);
      const res = await apiFetch('/settings/ai');
      if (res.ok) {
        setConfig(await res.json());
      }
    } catch (e) {
      console.error("Failed to load AI config", e);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSaving(true);
      const res = await apiFetch('/settings/ai', {
        method: 'PUT',
        body: JSON.stringify(config)
      });
      if (res.ok) {
        alert("AI Configuration saved successfully!");
      }
    } catch (e) {
      alert("Failed to save configuration.");
    } finally {
      setSaving(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;
    setConfig((prev: any) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSendTestChat = async () => {
    if (!testMessage.trim()) return;
    const userText = testMessage;
    setChatHistory(prev => [...prev, { sender: 'USER', text: userText }]);
    setTestMessage('');
    setSendingChat(true);

    try {
      // Talk to the public chat send endpoint
      const res = await apiFetch(`/public/chat/${subdomain || 'test'}/send`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          sessionId: sessionId,
          identifier: 'playground_user',
          message: userText,
          language: 'en'
        })
      });

      if (res.ok) {
        const data = await res.json();
        setChatHistory(prev => [...prev, { sender: 'AI', text: data.message }]);
      } else {
        setChatHistory(prev => [...prev, { sender: 'AI', text: 'Error communicating with AI receptionist.' }]);
      }
    } catch (e) {
      setChatHistory(prev => [...prev, { sender: 'AI', text: 'Network error. Please make sure backend is running.' }]);
    } finally {
      setSendingChat(false);
    }
  };

  const resetPlayground = () => {
    setChatHistory([]);
    setSessionId('test_session_' + Math.random().toString(36).substring(2, 9));
  };

  if (loading) return <div className="flex justify-center p-8"><Loader2 className="animate-spin text-brand-900" /></div>;

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
      {/* Settings Form */}
      <form onSubmit={handleSave} className="lg:col-span-2 space-y-6">
        <div className="bg-white p-6 rounded-xl border border-neutral-100 shadow-sm">
          <div className="flex items-center justify-between p-4 bg-neutral-50 rounded-lg mb-6 border border-neutral-150">
            <div>
              <p className="font-medium text-neutral-900">Enable AI Receptionist</p>
              <p className="text-xs text-neutral-500">Enable AI widget on public booking site and WhatsApp.</p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" name="enabled" checked={config.enabled || false} onChange={handleChange} className="sr-only peer" />
              <div className="w-11 h-6 bg-neutral-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-brand-900"></div>
            </label>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div>
              <label className="block text-xs font-semibold uppercase text-neutral-500 tracking-wider mb-1.5">Assistant Name</label>
              <input type="text" name="assistantName" value={config.assistantName || ''} onChange={handleChange} className="w-full border border-neutral-200 rounded-lg p-2.5 focus:outline-none focus:ring-1 focus:ring-brand-500 text-sm" required />
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase text-neutral-500 tracking-wider mb-1.5">Tone of Voice</label>
              <input type="text" name="tone" value={config.tone || ''} onChange={handleChange} className="w-full border border-neutral-200 rounded-lg p-2.5 focus:outline-none focus:ring-1 focus:ring-brand-500 text-sm" />
            </div>
            <div className="md:col-span-2">
              <label className="block text-xs font-semibold uppercase text-neutral-500 tracking-wider mb-1.5">Initial Greeting</label>
              <textarea name="greeting" value={config.greeting || ''} onChange={handleChange} rows={2} className="w-full border border-neutral-200 rounded-lg p-2.5 focus:outline-none focus:ring-1 focus:ring-brand-500 text-sm" required />
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase text-neutral-500 tracking-wider mb-1.5">Model Provider</label>
              <select name="providerName" value={config.providerName || 'mock'} onChange={handleChange} className="w-full border border-neutral-200 rounded-lg p-2.5 focus:outline-none focus:ring-1 focus:ring-brand-500 text-sm bg-white">
                <option value="mock">Local Development (Mock)</option>
                <option value="production">Production LLM Engine</option>
              </select>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-xl border border-neutral-100 shadow-sm">
          <h3 className="text-sm font-semibold uppercase text-neutral-900 tracking-wider mb-4">Capabilities</h3>
          <div className="space-y-3.5">
            <label className="flex items-center gap-3 cursor-pointer">
              <input type="checkbox" name="bookingEnabled" checked={config.bookingEnabled || false} onChange={handleChange} className="w-4 h-4 rounded border-neutral-300 text-brand-900 focus:ring-brand-500" />
              <span className="text-neutral-700 text-sm">Allow AI to look up availability and book appointments</span>
            </label>
            <label className="flex items-center gap-3 cursor-pointer">
              <input type="checkbox" name="cancellationEnabled" checked={config.cancellationEnabled || false} onChange={handleChange} className="w-4 h-4 rounded border-neutral-300 text-brand-900 focus:ring-brand-500" />
              <span className="text-neutral-700 text-sm">Allow AI to cancellation/rescheduling requests</span>
            </label>
            <label className="flex items-center gap-3 cursor-pointer">
              <input type="checkbox" name="humanHandoffEnabled" checked={config.humanHandoffEnabled || false} onChange={handleChange} className="w-4 h-4 rounded border-neutral-300 text-brand-900 focus:ring-brand-500" />
              <span className="text-neutral-700 text-sm">Enable automated human handoff when customer requests agent</span>
            </label>
          </div>
        </div>

        <div className="bg-white p-6 rounded-xl border border-neutral-100 shadow-sm">
          <h3 className="text-sm font-semibold uppercase text-neutral-900 tracking-wider mb-2">Business Context & FAQs</h3>
          <p className="text-xs text-neutral-500 mb-4">Write special rules, policies (e.g. cancellation within 24 hours), parking guidelines, etc.</p>
          <textarea name="businessContext" value={config.businessContext || ''} onChange={handleChange} rows={4} className="w-full border border-neutral-200 rounded-lg p-2.5 focus:outline-none focus:ring-1 focus:ring-brand-500 text-sm" placeholder="e.g. We have free client parking at the rear of the building. Appointments cancelled less than 24 hours in advance incur a 50% charge." />
        </div>

        <div className="flex justify-end pt-4">
          <button type="submit" disabled={saving} className="bg-brand-900 text-white px-8 py-3 rounded-lg font-medium flex items-center gap-2 hover:bg-brand-800 transition-colors disabled:opacity-70 text-sm">
            {saving ? <Loader2 size={16} className="animate-spin" /> : null}
            {saving ? 'Saving...' : 'Save AI Configuration'}
          </button>
        </div>
      </form>

      {/* Test AI Playground */}
      <div className="lg:col-span-1 bg-neutral-100 border border-neutral-200 rounded-xl p-5 flex flex-col h-[550px] shadow-inner">
        <div className="flex justify-between items-center pb-3 border-b border-neutral-200 mb-4">
          <div className="flex items-center gap-2">
            <Bot className="w-4 h-4 text-brand-900" />
            <span className="text-xs font-semibold uppercase tracking-wider text-neutral-700">Test AI Assistant</span>
          </div>
          <button onClick={resetPlayground} className="text-xs text-neutral-500 hover:text-neutral-700 flex items-center gap-1">
            <RefreshCw className="w-3 h-3" />
            Clear
          </button>
        </div>

        {/* Message logs */}
        <div className="flex-grow overflow-y-auto space-y-3.5 pr-2 mb-4 scrollbar-thin">
          {chatHistory.length === 0 ? (
            <div className="text-center text-xs text-neutral-400 mt-12 px-4">
              <Bot className="w-8 h-8 text-neutral-300 mx-auto mb-2" />
              Send a message below to test your config settings in a simulated chat session.
            </div>
          ) : (
            chatHistory.map((chat, idx) => (
              <div key={idx} className={`flex ${chat.sender === 'USER' ? 'justify-end' : 'justify-start'}`}>
                <div className={`max-w-[85%] rounded-xl p-3 text-xs leading-relaxed ${
                  chat.sender === 'USER' 
                    ? 'bg-brand-900 text-white rounded-tr-none' 
                    : 'bg-white text-neutral-800 border border-neutral-200 rounded-tl-none'
                }`}>
                  <p className="font-semibold text-[10px] uppercase opacity-65 mb-1 flex items-center gap-1">
                    {chat.sender === 'USER' ? <User className="w-2.5 h-2.5" /> : <Bot className="w-2.5 h-2.5" />}
                    {chat.sender === 'USER' ? 'Guest' : config.assistantName || 'AI Receptionist'}
                  </p>
                  <span>{chat.text}</span>
                </div>
              </div>
            ))
          )}
          {sendingChat && (
            <div className="flex justify-start">
              <div className="bg-white border border-neutral-200 rounded-xl rounded-tl-none p-3 text-xs flex items-center gap-2">
                <Loader2 className="w-3.5 h-3.5 animate-spin text-neutral-500" />
                <span className="text-neutral-400">AI is thinking...</span>
              </div>
            </div>
          )}
        </div>

        {/* Chat input */}
        <div className="flex gap-2">
          <input
            type="text"
            value={testMessage}
            onChange={e => setTestMessage(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSendTestChat()}
            placeholder="Type a message..."
            className="flex-grow px-3 py-2 rounded-lg border border-neutral-200 focus:border-brand-500 focus:ring-1 focus:ring-brand-500 outline-none text-xs bg-white"
          />
          <button
            onClick={handleSendTestChat}
            disabled={sendingChat || !testMessage.trim()}
            className="p-2.5 bg-brand-900 text-white rounded-lg hover:bg-brand-800 transition-colors disabled:opacity-50"
          >
            <Send className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
};
