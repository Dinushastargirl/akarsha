import React, { useState, useEffect } from 'react';
import { MessageSquare, RefreshCw, User, Bot } from 'lucide-react';
import { apiFetch } from '../services/api';

export const ConversationsPage: React.FC = () => {
    const [conversations, setConversations] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);

    const fetchConversations = async () => {
        setLoading(true);
        try {
            const response = await apiFetch('/conversations');
            if (response.ok) {
                const data = await response.json();
                setConversations(data);
            }
        } catch (error) {
            console.error('Failed to fetch conversations', error);
        }
        setLoading(false);
    };

    useEffect(() => {
        fetchConversations();
    }, []);

    return (
        <div className="p-6 max-w-6xl mx-auto">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-[#211F23]">Conversations</h1>
                    <p className="text-sm text-gray-500">Manage customer inquiries from Web Chat and WhatsApp</p>
                </div>
                <button 
                    onClick={fetchConversations}
                    disabled={loading}
                    className="flex items-center gap-2 bg-white border border-gray-200 px-4 py-2 rounded-lg text-sm font-medium hover:bg-gray-50 disabled:opacity-50"
                >
                    <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
                    Refresh
                </button>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="bg-[#FAF7F2] border-b border-gray-200">
                            <th className="p-4 text-xs font-semibold text-gray-600 uppercase">Customer</th>
                            <th className="p-4 text-xs font-semibold text-gray-600 uppercase">Channel</th>
                            <th className="p-4 text-xs font-semibold text-gray-600 uppercase">Status</th>
                            <th className="p-4 text-xs font-semibold text-gray-600 uppercase">Language</th>
                            <th className="p-4 text-xs font-semibold text-gray-600 uppercase text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {conversations.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="p-8 text-center text-gray-500">
                                    <MessageSquare size={48} className="mx-auto text-gray-300 mb-4" />
                                    <p>No active conversations found.</p>
                                </td>
                            </tr>
                        ) : (
                            conversations.map((conv) => (
                                <tr key={conv.id} className="border-b border-gray-100 hover:bg-gray-50">
                                    <td className="p-4 flex items-center gap-3">
                                        <div className="w-8 h-8 rounded-full bg-[#FAF7F2] flex items-center justify-center text-[#4A1942]">
                                            <User size={16} />
                                        </div>
                                        <span className="font-medium text-[#211F23]">
                                            {conv.customer?.firstName || 'Guest'}
                                        </span>
                                    </td>
                                    <td className="p-4">
                                        <span className="inline-block px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs font-medium">
                                            {conv.channel}
                                        </span>
                                    </td>
                                    <td className="p-4">
                                        <span className={`inline-flex items-center gap-1 px-2 py-1 rounded text-xs font-medium ${
                                            conv.status === 'AI' ? 'bg-purple-100 text-purple-700' : 
                                            conv.status === 'HUMAN' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'
                                        }`}>
                                            {conv.status === 'AI' ? <Bot size={12} /> : <User size={12} />}
                                            {conv.status}
                                        </span>
                                    </td>
                                    <td className="p-4 text-sm text-gray-600">{conv.language}</td>
                                    <td className="p-4 text-right">
                                        <button className="text-[#4A1942] text-sm font-medium hover:underline">
                                            View Chat
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};
