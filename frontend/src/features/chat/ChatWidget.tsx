import React, { useState, useEffect, useRef } from 'react';
import { MessageSquare, X, Send } from 'lucide-react';
import { apiFetch } from '../../services/api';

type Message = {
    id: string;
    text: string;
    sender: 'user' | 'ai' | 'staff' | 'system';
    timestamp: Date;
};

interface ChatWidgetProps {
    salonSlug?: string; // Optional for admin preview, required for public
}

export const ChatWidget: React.FC<ChatWidgetProps> = ({ salonSlug = 'demo' }) => {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState<Message[]>([]);
    const [inputValue, setInputValue] = useState('');
    
    // Identifier for guest or customer
    const [identity, setIdentity] = useState('');
    const [isIdentified, setIsIdentified] = useState(false);
    const [sessionId, setSessionId] = useState<string>('');
    
    // Config
    const [language, setLanguage] = useState<'en' | 'si' | 'ta' | 'si-Latn' | 'ta-Latn'>((localStorage.getItem(`ai_language_${salonSlug}`) as any) || 'en');
    const [config, setConfig] = useState<any>(null);

    const messagesEndRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    // Load config on open
    useEffect(() => {
        if (isOpen && !config) {
            apiFetch(`/public/chat/${salonSlug}/config`)
                .then(res => res.json())
                .then(data => {
                    setConfig(data);
                    if (!data.enabled) {
                        setIsOpen(false);
                        return;
                    }
                })
                .catch(err => console.error("Could not load AI config", err));
        }
    }, [isOpen, salonSlug, config]);

    // Resume session if exists
    useEffect(() => {
        const savedSession = localStorage.getItem(`ai_session_${salonSlug}`);
        const savedIdentity = localStorage.getItem(`ai_identity_${salonSlug}`);
        if (savedSession && savedIdentity) {
            setSessionId(savedSession);
            setIdentity(savedIdentity);
            setIsIdentified(true);
            
            // Load history
            apiFetch(`/public/chat/${salonSlug}/history/${savedSession}`)
                .then(res => {
                    if (res.ok) return res.json();
                    throw new Error();
                })
                .then(data => {
                    if (data && data.length > 0) {
                        setMessages(data.map((m: any) => ({
                            id: m.id.toString(),
                            text: m.text,
                            sender: m.sender.toLowerCase() as any,
                            timestamp: new Date(m.timestamp)
                        })));
                    } else {
                        // Start fresh if no history returned but we had a session
                        startNewChat(savedIdentity);
                    }
                })
                .catch(() => {
                    // Session invalid or cleared
                    localStorage.removeItem(`ai_session_${salonSlug}`);
                    localStorage.removeItem(`ai_identity_${salonSlug}`);
                    setIsIdentified(false);
                });
        }
    }, [salonSlug]);

    const startNewChat = async (id: string) => {
        const newSessionId = crypto.randomUUID();
        setSessionId(newSessionId);
        localStorage.setItem(`ai_session_${salonSlug}`, newSessionId);
        localStorage.setItem(`ai_identity_${salonSlug}`, id);
        
        // Let's send an invisible init message or just a greeting
        try {
            const res = await apiFetch(`/public/chat/${salonSlug}/send`, {
                method: 'POST',
                body: JSON.stringify({
                    sessionId: newSessionId,
                    identifier: id,
                    message: "Hello", // Trigger greeting
                    language: language
                })
            });
            if (res.ok) {
                const data = await res.json();
                setMessages([{
                    id: Date.now().toString(),
                    text: data.message,
                    sender: 'ai',
                    timestamp: new Date()
                }]);
            }
        } catch (e) {
            console.error("Failed to start chat", e);
        }
    };

    const handleIdentify = (e: React.FormEvent) => {
        e.preventDefault();
        if (identity.trim()) {
            setIsIdentified(true);
            startNewChat(identity);
        }
    };

    const handleSend = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!inputValue.trim()) return;

        const userMsg: Message = {
            id: Date.now().toString(),
            text: inputValue,
            sender: 'user',
            timestamp: new Date()
        };

        setMessages(prev => [...prev, userMsg]);
        setInputValue('');

        try {
            const response = await apiFetch(`/public/chat/${salonSlug}/send`, {
                method: 'POST',
                body: JSON.stringify({
                    sessionId: sessionId,
                    identifier: identity,
                    message: userMsg.text,
                    language: language
                })
            });
            
            if (!response.ok) throw new Error('Failed to send message');
            
            const data = await response.json();

            const aiMsg: Message = {
                id: (Date.now() + 1).toString(),
                text: data.message,
                sender: 'ai',
                timestamp: new Date()
            };
            setMessages(prev => [...prev, aiMsg]);
        } catch (error) {
            console.error('Chat error', error);
        }
    };

    const handleHandoff = async () => {
        const msg = "I need human assistance.";
        setInputValue(msg);
        // We can just invoke handleSend directly but we need an event, so we just set it and simulate it or send directly
        const userMsg: Message = { id: Date.now().toString(), text: msg, sender: 'user', timestamp: new Date() };
        setMessages(prev => [...prev, userMsg]);
        
        try {
            const response = await apiFetch(`/public/chat/${salonSlug}/send`, {
                method: 'POST',
                body: JSON.stringify({ sessionId, identifier: identity, message: "handoff", language })
            });
            if (response.ok) {
                const data = await response.json();
                setMessages(prev => [...prev, { id: Date.now().toString(), text: data.message, sender: 'ai', timestamp: new Date() }]);
            }
        } catch (e) {}
    };

    if (!isOpen) {
        return (
            <button
                onClick={() => setIsOpen(true)}
                className="fixed bottom-6 right-6 bg-brand-800 text-white p-4 rounded-full shadow-lg hover:bg-brand-900 transition-all z-50 flex items-center justify-center w-14 h-14"
            >
                <MessageSquare size={24} />
            </button>
        );
    }

    return (
        <div className="fixed bottom-6 right-6 w-80 sm:w-96 h-[500px] max-h-[80vh] bg-white rounded-2xl shadow-2xl flex flex-col z-50 border border-brand-100 overflow-hidden font-sans">
            <div className="bg-brand-900 text-brand-50 p-4 flex justify-between items-center shadow-md z-10">
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-brand-200 flex items-center justify-center">
                        <MessageSquare size={18} className="text-brand-900" />
                    </div>
                    <div>
                        <h3 className="font-semibold text-sm">{config?.assistantName || 'Akarsha Assistant'}</h3>
                        <p className="text-xs text-brand-200">Usually replies instantly</p>
                    </div>
                </div>
                <button onClick={() => setIsOpen(false)} className="hover:bg-brand-800 p-2 rounded-full transition-colors text-brand-200 hover:text-white">
                    <X size={20} />
                </button>
            </div>

            {!isIdentified ? (
                <div className="flex-1 p-6 flex flex-col justify-center items-center bg-brand-50">
                    <div className="bg-white p-6 rounded-xl shadow-sm border border-brand-100 w-full animate-subtle-fade">
                        <h4 className="text-center font-medium text-brand-900 mb-4">Start Chatting</h4>
                        <form onSubmit={handleIdentify} className="space-y-4">
                            <div>
                                <label className="block text-xs font-medium text-brand-700 mb-1">Select Language</label>
                                <select 
                                    value={language}
                                    onChange={(e) => {
                                        const val = e.target.value as any;
                                        setLanguage(val);
                                        localStorage.setItem(`ai_language_${salonSlug}`, val);
                                    }}
                                    className="w-full border border-brand-200 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                                >
                                    <option value="en">English</option>
                                    <option value="si">සිංහල</option>
                                    <option value="ta">தமிழ்</option>
                                    <option value="si-Latn">Singlish</option>
                                    <option value="ta-Latn">Tanglish</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-xs font-medium text-brand-700 mb-1">Phone or Email</label>
                                <input 
                                    type="text" 
                                    required
                                    value={identity}
                                    onChange={(e) => setIdentity(e.target.value)}
                                    className="w-full border border-brand-200 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                                    placeholder="Enter your contact info..."
                                />
                            </div>
                            <button type="submit" className="w-full bg-brand-800 text-white rounded-lg py-2.5 text-sm font-medium hover:bg-brand-900 transition-colors">
                                Start Chat
                            </button>
                        </form>
                    </div>
                </div>
            ) : (
                <>
                    <div className="bg-brand-50 px-4 py-2 border-b border-brand-100 flex justify-between items-center text-xs">
                        <select 
                            value={language}
                            onChange={(e) => {
                                const val = e.target.value as any;
                                setLanguage(val);
                                localStorage.setItem(`ai_language_${salonSlug}`, val);
                            }}
                            className="bg-transparent border-none focus:ring-0 text-brand-700 font-medium cursor-pointer"
                        >
                            <option value="en">English</option>
                            <option value="si">සිංහල</option>
                            <option value="ta">தமிழ்</option>
                            <option value="si-Latn">Singlish</option>
                            <option value="ta-Latn">Tanglish</option>
                        </select>
                        <button onClick={handleHandoff} className="text-brand-600 hover:text-brand-900 font-medium transition-colors">
                            Need Human?
                        </button>
                    </div>
                    
                    <div className="flex-1 p-4 overflow-y-auto bg-brand-50 space-y-4">
                        {messages.map((msg, index) => {
                            const isUser = msg.sender === 'user';
                            return (
                                <div key={`${msg.id}-${index}`} className={`flex flex-col ${isUser ? 'items-end' : 'items-start'}`}>
                                    <div className={`max-w-[85%] p-3 text-sm shadow-sm ${
                                        isUser 
                                            ? 'bg-brand-800 text-white rounded-2xl rounded-tr-sm' 
                                            : 'bg-white text-brand-900 border border-brand-100 rounded-2xl rounded-tl-sm'
                                    }`}>
                                        {msg.text}
                                    </div>
                                    <span className="text-[10px] text-brand-400 mt-1 px-1">
                                        {msg.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                    </span>
                                </div>
                            );
                        })}
                        <div ref={messagesEndRef} />
                    </div>
                    <form onSubmit={handleSend} className="p-3 bg-white border-t border-brand-100 flex gap-2 items-center">
                        <input 
                            type="text"
                            value={inputValue}
                            onChange={(e) => setInputValue(e.target.value)}
                            placeholder="Type a message..."
                            className="flex-1 border border-brand-200 rounded-full px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                        />
                        <button 
                            type="submit"
                            disabled={!inputValue.trim()}
                            className="bg-brand-800 text-white w-10 h-10 rounded-full flex items-center justify-center disabled:opacity-50 hover:bg-brand-900 transition-colors shadow-sm"
                        >
                            <Send size={16} className="-ml-0.5 mt-0.5" />
                        </button>
                    </form>
                </>
            )}
        </div>
    );
};
