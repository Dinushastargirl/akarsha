import React from 'react';

interface StatCardProps {
  label: string;
  value: string | number;
  sub?: string;
  accent?: 'default' | 'success' | 'warning' | 'muted';
}

export const StatCard: React.FC<StatCardProps> = ({ label, value, sub, accent = 'default' }) => {
  const valueColor =
    accent === 'success'
      ? 'text-success'
      : accent === 'warning'
      ? 'text-warning'
      : accent === 'muted'
      ? 'text-neutral-400'
      : 'text-neutral-900';

  return (
    <div className="bg-white border border-neutral-200 rounded p-5 flex flex-col gap-1 shadow-sm-flat">
      <span className="akarsha-label">{label}</span>
      <span className={`text-3xl font-semibold tracking-tight ${valueColor}`}>{value}</span>
      {sub && <span className="text-xs text-neutral-400 mt-0.5">{sub}</span>}
    </div>
  );
};
