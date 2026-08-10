import React, { useState, useEffect, useRef } from 'react';
import { MessageSquare, User, Bot, Send, Clock, AlertCircle, CheckCircle, Smartphone } from 'lucide-react';
import { apiFetch } from '../services/api';

interface AiMessage {
    id: number;
    senderType: string;
    content: string;
    timestamp: string;
}

interface AiInteraction {
    id: number;
    guestIdentifier: string;
    channel: string;
    sessionId: string;
    languagePreference: string;
    status: string;
    lastActivity: string;
    unreadCount: number;
    assignedStaffName: string | null;
    assignedStaffId: number | null;
    messages: AiMessage[];
}

export const ConversationsPage: React.FC = () => {
    const [conversations, setConversations] = useState<AiInteraction[]>([]);
    const [activeId, setActiveId] = useState<number | null>(null);
    const [activeInteraction, setActiveInteraction] = useState<AiInteraction | null>(null);
    const [replyText, setReplyText] = useState('');
    const [filter, setFilter] = useState('ALL');
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const fetchConversations = async () => {
        try {
            const response = await apiFetch('/inbox/conversations');
            if (response.ok) {
                const data = await response.json();
                setConversations(data.content || data);
            }
        } catch (error) {
            console.error('Failed to fetch conversations', error);
        }
    };

    const fetchActiveInteraction = async (id: number) => {
        try {
            const response = await apiFetch(`/inbox/conversations/${id}`);
            if (response.ok) {
                const data = await response.json();
                setActiveInteraction(data);
                // Update the unread count in the list instantly
                setConversations(prev => prev.map(c => c.id === id ? { ...c, unreadCount: 0 } : c));
                scrollToBottom();
            }
        } catch (error) {
            console.error('Failed to fetch conversation details', error);
        }
    };

    useEffect(() => {
        fetchConversations();
        const interval = setInterval(fetchConversations, 5000);
        return () => clearInterval(interval);
    }, []);

    useEffect(() => {
        if (activeId) {
            fetchActiveInteraction(activeId);
            const interval = setInterval(() => fetchActiveInteraction(activeId), 3000);
            return () => clearInterval(interval);
        } else {
            setActiveInteraction(null);
        }
    }, [activeId]);

    const scrollToBottom = () => {
        setTimeout(() => {
            messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
        }, 100);
    };

    const handleSendReply = async () => {
        if (!replyText.trim() || !activeId) return;
        const text = replyText;
        setReplyText('');
        
        // Optimistic UI update
        if (activeInteraction) {
            const tempMsg: AiMessage = {
                id: Date.now(),
                senderType: 'STAFF',
                content: text,
                timestamp: new Date().toISOString()
            };
            setActiveInteraction({ ...activeInteraction, messages: [...activeInteraction.messages, tempMsg], status: 'HANDED_OFF' });
            scrollToBottom();
        }

        try {
            await apiFetch(`/inbox/conversations/${activeId}/messages`, {
                method: 'POST',
                body: JSON.stringify({ message: text })
            });
            fetchActiveInteraction(activeId);
            fetchConversations();
        } catch (error) {
            console.error('Failed to send message', error);
        }
    };

    const handleAction = async (action: string) => {
        if (!activeId) return;
        try {
            await apiFetch(`/inbox/conversations/${activeId}/${action}`, { method: 'POST' });
            fetchActiveInteraction(activeId);
            fetchConversations();
        } catch (error) {
            console.error(`Failed to execute ${action}`, error);
        }
    };

    const formatLanguage = (lang: string) => {
        switch(lang) {
            case 'si': return 'සිංහල';
            case 'ta': return 'தமிழ்';
            case 'en': return 'English';
            case 'si-Latn': return 'Singlish';
            case 'ta-Latn': return 'Tanglish';
            default: return lang || 'English';
        }
    };

    const filteredConversations = conversations.filter(c => {
        if (filter === 'UNREAD') return c.unreadCount > 0;
        if (filter === 'HANDED_OFF') return c.status === 'HANDED_OFF' || c.status === 'WAITING_FOR_STAFF';
        if (filter === 'RESOLVED') return c.status === 'RESOLVED';
        return true;
    });

    return (
        <div className="h-[calc(100vh-100px)] flex bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden mx-6 mb-6">
            
            {/* Left Panel: Conversation List */}
            <div className="w-80 border-r border-gray-200 flex flex-col bg-gray-50/50">
                <div className="p-4 border-b border-gray-200 bg-white">
                    <h1 className="text-xl font-bold text-[#211F23] mb-4">Inbox</h1>
                    <div className="flex gap-2 overflow-x-auto pb-2 scrollbar-hide">
                        {['ALL', 'UNREAD', 'HANDED_OFF', 'RESOLVED'].map(f => (
                            <button 
                                key={f}
                                onClick={() => setFilter(f)}
                                className={`px-3 py-1 text-xs font-medium rounded-full whitespace-nowrap transition-colors ${
                                    filter === f ? 'bg-[#4A1942] text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                                }`}
                            >
                                {f.replace('_', ' ')}
                            </button>
                        ))}
                    </div>
                </div>
                
                <div className="flex-1 overflow-y-auto">
                    {filteredConversations.length === 0 ? (
                        <div className="p-8 text-center text-gray-500">
                            <MessageSquare size={32} className="mx-auto text-gray-300 mb-2" />
                            <p className="text-sm">No conversations found</p>
                        </div>
                    ) : (
                        filteredConversations.map(conv => (
                            <button
                                key={conv.id}
                                onClick={() => setActiveId(conv.id)}
                                className={`w-full text-left p-4 border-b border-gray-100 transition-colors ${
                                    activeId === conv.id ? 'bg-[#FAF7F2] border-l-4 border-l-[#4A1942]' : 'hover:bg-gray-50 border-l-4 border-l-transparent'
                                }`}
                            >
                                <div className="flex justify-between items-start mb-1">
                                    <span className="font-semibold text-sm text-[#211F23] truncate pr-2">
                                        {conv.guestIdentifier || 'Guest'}
                                    </span>
                                    {conv.unreadCount > 0 && (
                                        <span className="bg-[#C88A9A] text-white text-[10px] font-bold px-2 py-0.5 rounded-full">
                                            {conv.unreadCount}
                                        </span>
                                    )}
                                </div>
                                <div className="flex items-center gap-2 mb-2">
                                    {conv.channel === 'WHATSAPP' ? (
                                        <Smartphone size={12} className="text-green-600" />
                                    ) : (
                                        <MessageSquare size={12} className="text-blue-600" />
                                    )}
                                    <span className={`text-[10px] font-medium px-1.5 py-0.5 rounded ${
                                        conv.status === 'HANDED_OFF' ? 'bg-orange-100 text-orange-700' :
                                        conv.status === 'WAITING_FOR_STAFF' ? 'bg-red-100 text-red-700' :
                                        conv.status === 'ACTIVE' ? 'bg-purple-100 text-purple-700' :
                                        'bg-gray-100 text-gray-600'
                                    }`}>
                                        {conv.status.replace('_', ' ')}
                                    </span>
                                </div>
                                <p className="text-xs text-gray-500 truncate">
                                    {conv.messages && conv.messages.length > 0 
                                        ? conv.messages[0].content 
                                        : 'No messages yet...'}
                                </p>
                            </button>
                        ))
                    )}
                </div>
            </div>

            {/* Middle Panel: Active Chat */}
            <div className="flex-1 flex flex-col bg-white">
                {activeInteraction ? (
                    <>
                        {/* Chat Header */}
                        <div className="p-4 border-b border-gray-200 flex justify-between items-center bg-white shadow-sm z-10">
                            <div>
                                <h2 className="font-bold text-lg text-[#211F23]">{activeInteraction.guestIdentifier}</h2>
                                <p className="text-xs text-gray-500 flex items-center gap-1">
                                    <Clock size={12} /> {new Date(activeInteraction.lastActivity).toLocaleString()}
                                </p>
                            </div>
                            <div className="flex gap-2">
                                {activeInteraction.status === 'ACTIVE' && (
                                    <button onClick={() => handleAction('takeover')} className="px-4 py-1.5 bg-[#4A1942] text-white text-sm font-medium rounded shadow-sm hover:bg-[#3A1435] transition-colors">
                                        Take Over
                                    </button>
                                )}
                                {activeInteraction.status === 'HANDED_OFF' && (
                                    <button onClick={() => handleAction('return-to-ai')} className="px-4 py-1.5 border border-[#4A1942] text-[#4A1942] text-sm font-medium rounded hover:bg-[#FAF7F2] transition-colors">
                                        Return to AI
                                    </button>
                                )}
                                {activeInteraction.status !== 'RESOLVED' && (
                                    <button onClick={() => handleAction('resolve')} className="px-4 py-1.5 border border-gray-300 text-gray-700 text-sm font-medium rounded hover:bg-gray-50 transition-colors">
                                        Resolve
                                    </button>
                                )}
                            </div>
                        </div>

                        {/* Chat Messages */}
                        <div className="flex-1 overflow-y-auto p-6 bg-gray-50/30">
                            <div className="space-y-6">
                                {activeInteraction.messages.map((msg, idx) => {
                                    const isCustomer = msg.senderType === 'USER';
                                    const isAi = msg.senderType === 'AI';
                                    const isStaff = msg.senderType === 'STAFF';
                                    
                                    // Detect handoff transitions
                                    const prevMsg = idx > 0 ? activeInteraction.messages[idx-1] : null;
                                    const transitionToHuman = !isCustomer && isStaff && prevMsg && prevMsg.senderType === 'AI';
                                    const transitionToAi = !isCustomer && isAi && prevMsg && prevMsg.senderType === 'STAFF';

                                    return (
                                        <React.Fragment key={msg.id}>
                                            {transitionToHuman && (
                                                <div className="flex items-center gap-4 my-6">
                                                    <div className="h-px bg-gray-300 flex-1"></div>
                                                    <span className="text-xs font-medium text-gray-500 uppercase tracking-wider">Human Agent Took Over</span>
                                                    <div className="h-px bg-gray-300 flex-1"></div>
                                                </div>
                                            )}
                                            {transitionToAi && (
                                                <div className="flex items-center gap-4 my-6">
                                                    <div className="h-px bg-gray-300 flex-1"></div>
                                                    <span className="text-xs font-medium text-gray-500 uppercase tracking-wider">AI Receptionist Resumed</span>
                                                    <div className="h-px bg-gray-300 flex-1"></div>
                                                </div>
                                            )}

                                            <div className={`flex flex-col ${isCustomer ? 'items-end' : 'items-start'}`}>
                                                <div className={`max-w-[75%] rounded-2xl px-4 py-3 shadow-sm text-sm ${
                                                    isCustomer ? 'bg-[#211F23] text-white rounded-br-none' : 
                                                    isAi ? 'bg-white border-2 border-[#FAF7F2] text-[#211F23] rounded-bl-none' : 
                                                    'bg-[#FAF7F2] border border-[#D8B878]/30 text-[#4A1942] rounded-bl-none'
                                                }`}>
                                                    <div className="whitespace-pre-wrap">{msg.content}</div>
                                                </div>
                                                <span className="text-[10px] text-gray-400 mt-1 flex items-center gap-1">
                                                    {isAi && <Bot size={10} />}
                                                    {isStaff && <User size={10} />}
                                                    {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                                </span>
                                            </div>
                                        </React.Fragment>
                                    );
                                })}
                                <div ref={messagesEndRef} />
                            </div>
                        </div>

                        {/* Chat Input */}
                        {activeInteraction.status !== 'RESOLVED' && (
                            <div className="p-4 bg-white border-t border-gray-200">
                                {activeInteraction.status === 'ACTIVE' && (
                                    <div className="mb-3 px-3 py-2 bg-[#FAF7F2] border border-[#D8B878]/50 rounded text-xs text-[#4A1942] flex items-center gap-2">
                                        <AlertCircle size={14} />
                                        Sending a message will automatically take over this conversation from the AI.
                                    </div>
                                )}
                                <div className="flex items-end gap-2">
                                    <textarea
                                        value={replyText}
                                        onChange={(e) => setReplyText(e.target.value)}
                                        onKeyDown={(e) => {
                                            if (e.key === 'Enter' && !e.shiftKey) {
                                                e.preventDefault();
                                                handleSendReply();
                                            }
                                        }}
                                        placeholder="Type your reply... (Press Enter to send)"
                                        className="flex-1 max-h-32 min-h-[44px] resize-none border border-gray-300 rounded-lg p-3 text-sm focus:outline-none focus:ring-2 focus:ring-[#4A1942]/20 focus:border-[#4A1942]"
                                        rows={1}
                                    />
                                    <button 
                                        onClick={handleSendReply}
                                        disabled={!replyText.trim()}
                                        className="bg-[#4A1942] text-white h-[44px] w-[44px] rounded-lg flex items-center justify-center hover:bg-[#3A1435] transition-colors disabled:opacity-50"
                                    >
                                        <Send size={18} />
                                    </button>
                                </div>
                            </div>
                        )}
                    </>
                ) : (
                    <div className="flex-1 flex flex-col items-center justify-center text-gray-400 bg-gray-50/30">
                        <MessageSquare size={64} className="mb-4 text-gray-200" />
                        <h2 className="text-xl font-medium text-gray-500">No conversation selected</h2>
                        <p className="text-sm mt-2 text-gray-400">Select a conversation from the inbox to start replying</p>
                    </div>
                )}
            </div>

            {/* Right Panel: Customer Context */}
            {activeInteraction && (
                <div className="w-72 border-l border-gray-200 bg-white p-6 overflow-y-auto hidden lg:block">
                    <div className="text-center mb-6">
                        <div className="w-20 h-20 bg-[#FAF7F2] text-[#4A1942] rounded-full flex items-center justify-center mx-auto mb-3 shadow-sm border border-[#D8B878]/30">
                            <User size={32} />
                        </div>
                        <h3 className="font-bold text-[#211F23] text-lg">{activeInteraction.guestIdentifier}</h3>
                    </div>

                    <div className="space-y-6">
                        <div>
                            <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">Session Details</h4>
                            <div className="space-y-3 text-sm">
                                <div className="flex justify-between">
                                    <span className="text-gray-500">Channel</span>
                                    <span className="font-medium text-[#211F23] flex items-center gap-1">
                                        {activeInteraction.channel === 'WHATSAPP' ? <Smartphone size={14}/> : <MessageSquare size={14}/>}
                                        {activeInteraction.channel}
                                    </span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-500">Language</span>
                                    <span className="font-medium text-[#211F23]">{formatLanguage(activeInteraction.languagePreference)}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-500">Status</span>
                                    <span className="font-medium text-[#211F23]">{activeInteraction.status.replace('_', ' ')}</span>
                                </div>
                            </div>
                        </div>

                        <div>
                            <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">AI Context</h4>
                            <div className="bg-gray-50 rounded-lg p-3 border border-gray-100 text-sm text-gray-600">
                                {activeInteraction.status === 'HANDED_OFF' || activeInteraction.status === 'WAITING_FOR_STAFF' ? (
                                    <p className="flex items-start gap-2">
                                        <AlertCircle size={16} className="text-orange-500 mt-0.5 shrink-0" />
                                        Customer requires human assistance or requested to speak with staff.
                                    </p>
                                ) : (
                                    <p className="flex items-start gap-2">
                                        <CheckCircle size={16} className="text-green-500 mt-0.5 shrink-0" />
                                        AI is currently handling the inquiry automatically.
                                    </p>
                                )}
                            </div>
                        </div>

                        <div>
                            <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">Quick Actions</h4>
                            <div className="space-y-2">
                                <button className="w-full text-left px-3 py-2 text-sm text-[#4A1942] hover:bg-[#FAF7F2] rounded transition-colors font-medium">
                                    Book Appointment
                                </button>
                                <button className="w-full text-left px-3 py-2 text-sm text-[#4A1942] hover:bg-[#FAF7F2] rounded transition-colors font-medium">
                                    View Customer Profile
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};
