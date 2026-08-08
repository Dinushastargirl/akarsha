import React, { useState, useEffect, useRef } from 'react';
import { MessageSquare, X, Send } from 'lucide-react';
import { apiFetch } from '../../services/api';

type Message = {
    id: string;
    text: string;
    sender: 'user' | 'ai';
    timestamp: Date;
};

export const ChatWidget: React.FC = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState<Message[]>([]);
    const [inputValue, setInputValue] = useState('');
    const [identity, setIdentity] = useState('');
    const [isIdentified, setIsIdentified] = useState(false);
    const [language, setLanguage] = useState<'ENGLISH' | 'SINHALA' | 'TAMIL'>('ENGLISH');
    
    const messagesEndRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    const handleIdentify = (e: React.FormEvent) => {
        e.preventDefault();
        if (identity.trim()) {
            setIsIdentified(true);
            setMessages([{
                id: '1',
                text: language === 'ENGLISH' ? 'Hello! How can I help you today?' : 
                      (language === 'SINHALA' ? 'ආයුබෝවන්! මට ඔබට උදව් කළ හැක්කේ කෙසේද?' : 'வணக்கம்! நான் உங்களுக்கு எப்படி உதவ முடியும்?'),
                sender: 'ai',
                timestamp: new Date()
            }]);
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
            const response = await apiFetch('/chat/send', {
                method: 'POST',
                body: JSON.stringify({
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

    if (!isOpen) {
        return (
            <button
                onClick={() => setIsOpen(true)}
                className="fixed bottom-6 right-6 bg-[#4A1942] text-white p-4 rounded-full shadow-lg hover:bg-opacity-90 transition-all z-50"
            >
                <MessageSquare size={24} />
            </button>
        );
    }

    return (
        <div className="fixed bottom-6 right-6 w-80 sm:w-96 h-[500px] max-h-[80vh] bg-white rounded-2xl shadow-2xl flex flex-col z-50 border border-gray-100 overflow-hidden">
            <div className="bg-[#4A1942] text-[#FAF7F2] p-4 flex justify-between items-center">
                <div className="flex items-center gap-2">
                    <div className="w-8 h-8 rounded-full bg-[#C88A9A] flex items-center justify-center">
                        <MessageSquare size={16} className="text-[#4A1942]" />
                    </div>
                    <div>
                        <h3 className="font-semibold text-sm">Akarsha Assistant</h3>
                        <p className="text-xs text-[#C88A9A]">Usually replies instantly</p>
                    </div>
                </div>
                <button onClick={() => setIsOpen(false)} className="hover:bg-white/10 p-1 rounded">
                    <X size={20} />
                </button>
            </div>

            {!isIdentified ? (
                <div className="flex-1 p-6 flex flex-col justify-center items-center bg-[#FAF7F2]">
                    <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 w-full">
                        <h4 className="text-center font-medium text-[#211F23] mb-4">Start Chatting</h4>
                        <form onSubmit={handleIdentify} className="space-y-4">
                            <div>
                                <label className="block text-xs font-medium text-gray-500 mb-1">Select Language</label>
                                <select 
                                    value={language}
                                    onChange={(e) => setLanguage(e.target.value as any)}
                                    className="w-full border border-gray-200 rounded-lg p-2 text-sm focus:outline-none focus:border-[#C88A9A]"
                                >
                                    <option value="ENGLISH">English</option>
                                    <option value="SINHALA">සිංහල</option>
                                    <option value="TAMIL">தமிழ்</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-xs font-medium text-gray-500 mb-1">Email or Phone</label>
                                <input 
                                    type="text" 
                                    required
                                    value={identity}
                                    onChange={(e) => setIdentity(e.target.value)}
                                    className="w-full border border-gray-200 rounded-lg p-2 text-sm focus:outline-none focus:border-[#C88A9A]"
                                    placeholder="Enter to begin..."
                                />
                            </div>
                            <button type="submit" className="w-full bg-[#4A1942] text-white rounded-lg py-2 text-sm font-medium hover:bg-opacity-90">
                                Start Chat
                            </button>
                        </form>
                    </div>
                </div>
            ) : (
                <>
                    <div className="flex-1 p-4 overflow-y-auto bg-[#FAF7F2] space-y-4">
                        {messages.map(msg => (
                            <div key={msg.id} className={`flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}>
                                <div className={`max-w-[80%] p-3 rounded-2xl text-sm ${
                                    msg.sender === 'user' 
                                        ? 'bg-[#4A1942] text-white rounded-tr-sm' 
                                        : 'bg-white text-[#211F23] shadow-sm border border-gray-100 rounded-tl-sm'
                                }`}>
                                    {msg.text}
                                </div>
                            </div>
                        ))}
                        <div ref={messagesEndRef} />
                    </div>
                    <form onSubmit={handleSend} className="p-3 bg-white border-t border-gray-100 flex gap-2">
                        <input 
                            type="text"
                            value={inputValue}
                            onChange={(e) => setInputValue(e.target.value)}
                            placeholder="Type your message..."
                            className="flex-1 border border-gray-200 rounded-full px-4 py-2 text-sm focus:outline-none focus:border-[#C88A9A]"
                        />
                        <button 
                            type="submit"
                            disabled={!inputValue.trim()}
                            className="bg-[#4A1942] text-[#D8B878] p-2 rounded-full disabled:opacity-50 hover:bg-opacity-90"
                        >
                            <Send size={18} className="ml-0.5" />
                        </button>
                    </form>
                </>
            )}
        </div>
    );
};
