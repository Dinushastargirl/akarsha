import React, { useState } from 'react';
import { X, Plus, Trash2 } from 'lucide-react';
import type { Appointment } from '../../types';
import { appointmentService } from './appointmentService';

interface CheckoutModalProps {
  appointment: Appointment;
  onClose: () => void;
  onSuccess: () => void;
  onError: (msg: string) => void;
}

export const CheckoutModal: React.FC<CheckoutModalProps> = ({ appointment, onClose, onSuccess, onError }) => {
  const [loading, setLoading] = useState(false);
  
  const [lineItems, setLineItems] = useState<any[]>([
    {
      itemType: 'SERVICE',
      referenceId: appointment.service?.id,
      description: appointment.service?.name || 'Service',
      quantity: 1,
      unitPrice: appointment.service?.price || 0
    }
  ]);
  
  const [taxAmount, setTaxAmount] = useState(0);
  const [discountAmount, setDiscountAmount] = useState(0);
  const [paymentMethod, setPaymentMethod] = useState('CARD');
  const [notes, setNotes] = useState('');

  const subtotal = lineItems.reduce((acc, item) => acc + (item.unitPrice * item.quantity), 0);
  const total = Math.max(0, subtotal + taxAmount - discountAmount);

  const handleAddLineItem = () => {
    setLineItems([
      ...lineItems,
      { itemType: 'CUSTOM', description: '', quantity: 1, unitPrice: 0 }
    ]);
  };

  const handleUpdateLineItem = (index: number, field: string, value: any) => {
    const updated = [...lineItems];
    (updated[index] as any)[field] = value;
    setLineItems(updated);
  };

  const handleRemoveLineItem = (index: number) => {
    setLineItems(lineItems.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await appointmentService.checkoutAppointment(appointment.id, {
        paymentMethod,
        taxAmount,
        discountAmount,
        notes,
        lineItems
      });
      if (res.status === 201) {
        onSuccess();
      } else {
        const msg = await res.text();
        onError(msg || 'Checkout failed');
      }
    } catch (err) {
      onError('Network error during checkout');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto m-4">
        <div className="flex items-center justify-between p-6 border-b border-neutral-100 sticky top-0 bg-white z-10">
          <h2 className="text-lg font-medium text-neutral-900">Checkout</h2>
          <button onClick={onClose} className="p-2 text-neutral-400 hover:text-neutral-600 rounded-full hover:bg-neutral-50">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          <div className="bg-neutral-50 rounded-lg p-4 mb-4">
            <h3 className="font-medium text-neutral-900">{appointment.customer?.fullName}</h3>
            <p className="text-sm text-neutral-500">{appointment.appointmentDate} • {appointment.startTime}</p>
          </div>

          <div>
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-sm font-medium text-neutral-900">Line Items</h3>
              <button
                type="button"
                onClick={handleAddLineItem}
                className="text-xs font-medium text-neutral-900 bg-neutral-100 hover:bg-neutral-200 px-3 py-1.5 rounded flex items-center space-x-1"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Add Item</span>
              </button>
            </div>
            
            <div className="space-y-3">
              {lineItems.map((item, index) => (
                <div key={index} className="flex space-x-3 items-start">
                  <div className="flex-1">
                    <input
                      type="text"
                      placeholder="Description"
                      value={item.description}
                      onChange={(e) => handleUpdateLineItem(index, 'description', e.target.value)}
                      className="w-full text-sm border-neutral-200 rounded-md focus:ring-neutral-900 focus:border-neutral-900"
                      required
                    />
                  </div>
                  <div className="w-24">
                    <input
                      type="number"
                      min="1"
                      placeholder="Qty"
                      value={item.quantity}
                      onChange={(e) => handleUpdateLineItem(index, 'quantity', parseInt(e.target.value) || 1)}
                      className="w-full text-sm border-neutral-200 rounded-md focus:ring-neutral-900 focus:border-neutral-900"
                      required
                    />
                  </div>
                  <div className="w-32">
                    <div className="relative">
                      <span className="absolute left-3 top-2 text-neutral-500">$</span>
                      <input
                        type="number"
                        min="0"
                        step="0.01"
                        placeholder="Price"
                        value={item.unitPrice}
                        onChange={(e) => handleUpdateLineItem(index, 'unitPrice', parseFloat(e.target.value) || 0)}
                        className="w-full text-sm pl-7 border-neutral-200 rounded-md focus:ring-neutral-900 focus:border-neutral-900"
                        required
                      />
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => handleRemoveLineItem(index)}
                    className="p-2 text-red-500 hover:bg-red-50 rounded-md"
                    disabled={lineItems.length === 1}
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))}
            </div>
          </div>

          <div className="border-t border-neutral-100 pt-6">
            <div className="grid grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-1">Payment Method</label>
                <select
                  value={paymentMethod}
                  onChange={(e) => setPaymentMethod(e.target.value)}
                  className="w-full text-sm border-neutral-200 rounded-md focus:ring-neutral-900 focus:border-neutral-900"
                >
                  <option value="CARD">Credit/Debit Card</option>
                  <option value="CASH">Cash</option>
                  <option value="TRANSFER">Bank Transfer</option>
                  <option value="OTHER">Other</option>
                </select>

                <label className="block text-sm font-medium text-neutral-700 mt-4 mb-1">Notes (Optional)</label>
                <textarea
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  className="w-full text-sm border-neutral-200 rounded-md focus:ring-neutral-900 focus:border-neutral-900"
                  rows={2}
                />
              </div>

              <div className="bg-neutral-50 p-4 rounded-lg space-y-3">
                <div className="flex justify-between text-sm">
                  <span className="text-neutral-500">Subtotal</span>
                  <span className="font-medium">${subtotal.toFixed(2)}</span>
                </div>
                <div className="flex justify-between items-center text-sm">
                  <span className="text-neutral-500">Discount</span>
                  <div className="w-24 relative">
                    <span className="absolute left-2 top-1 text-neutral-500">$</span>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      value={discountAmount}
                      onChange={(e) => setDiscountAmount(parseFloat(e.target.value) || 0)}
                      className="w-full text-xs pl-5 py-1 border-neutral-200 rounded"
                    />
                  </div>
                </div>
                <div className="flex justify-between items-center text-sm">
                  <span className="text-neutral-500">Tax</span>
                  <div className="w-24 relative">
                    <span className="absolute left-2 top-1 text-neutral-500">$</span>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      value={taxAmount}
                      onChange={(e) => setTaxAmount(parseFloat(e.target.value) || 0)}
                      className="w-full text-xs pl-5 py-1 border-neutral-200 rounded"
                    />
                  </div>
                </div>
                <div className="pt-3 border-t border-neutral-200 flex justify-between">
                  <span className="font-medium text-neutral-900">Total</span>
                  <span className="font-semibold text-lg text-neutral-900">${total.toFixed(2)}</span>
                </div>
              </div>
            </div>
          </div>

          <div className="flex justify-end space-x-3 pt-6">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-neutral-700 bg-white border border-neutral-300 rounded-lg hover:bg-neutral-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || lineItems.length === 0}
              className="px-4 py-2 text-sm font-medium text-white bg-neutral-900 rounded-lg hover:bg-neutral-800 disabled:opacity-50"
            >
              {loading ? 'Processing...' : `Charge $${total.toFixed(2)}`}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
