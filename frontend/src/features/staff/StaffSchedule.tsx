import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Clock } from 'lucide-react';
import type { StaffSchedule } from '../../types';

interface StaffScheduleProps {
  scheduleList: StaffSchedule[];
  onSave: (payload: Array<{
    dayOfWeek: number;
    working: boolean;
    startTime: string;
    endTime: string;
  }>) => Promise<void>;
  loading: boolean;
}

export const StaffScheduleEditor: React.FC<StaffScheduleProps> = ({
  scheduleList,
  onSave,
  loading
}) => {
  const { t } = useTranslation();
  
  const [localSchedule, setLocalSchedule] = useState<Array<{
    dayOfWeek: number;
    working: boolean;
    startTime: string;
    endTime: string;
  }>>([]);

  const daysLabel = [
    "",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday",
    "Sunday"
  ];

  useEffect(() => {
    // Populate default Mon-Sun days if empty
    const items = [];
    for (let i = 1; i <= 7; i++) {
      const match = scheduleList.find(s => s.dayOfWeek === i);
      items.push({
        dayOfWeek: i,
        working: match ? match.working : i !== 7,
        startTime: match ? match.startTime.substring(0, 5) : '09:00',
        endTime: match ? match.endTime.substring(0, 5) : '18:00'
      });
    }
    setLocalSchedule(items);
  }, [scheduleList]);

  const handleWorkingToggle = (dayIndex: number, val: boolean) => {
    setLocalSchedule(prev => prev.map(s => s.dayOfWeek === dayIndex ? { ...s, working: val } : s));
  };

  const handleTimeChange = (dayIndex: number, field: 'startTime' | 'endTime', val: string) => {
    setLocalSchedule(prev => prev.map(s => s.dayOfWeek === dayIndex ? { ...s, [field]: val } : s));
  };

  const handleSaveClick = () => {
    // Validate opening < closing
    for (const s of localSchedule) {
      if (s.working && s.startTime >= s.endTime) {
        alert(`For ${daysLabel[s.dayOfWeek]}, start time must be before end time.`);
        return;
      }
    }
    // format as HH:mm:00
    const formatted = localSchedule.map(s => ({
      dayOfWeek: s.dayOfWeek,
      working: s.working,
      startTime: `${s.startTime}:00`,
      endTime: `${s.endTime}:00`
    }));
    onSave(formatted);
  };

  return (
    <div className="space-y-6 text-left">
      <div className="space-y-4">
        {localSchedule.map((s) => (
          <div 
            key={s.dayOfWeek}
            className="flex flex-col sm:flex-row sm:items-center justify-between border border-neutral-200 rounded p-4 bg-white shadow-sm-flat gap-4"
          >
            {/* Day name & toggle checkbox */}
            <div className="flex items-center space-x-3 w-40">
              <input 
                type="checkbox" 
                id={`working-${s.dayOfWeek}`}
                checked={s.working}
                onChange={(e) => handleWorkingToggle(s.dayOfWeek, e.target.checked)}
                className="rounded border-neutral-300 text-neutral-900 focus:ring-0 cursor-pointer h-4 w-4"
              />
              <label htmlFor={`working-${s.dayOfWeek}`} className="text-sm font-semibold text-neutral-900 cursor-pointer">
                {daysLabel[s.dayOfWeek]}
              </label>
            </div>

            {/* Time Pickers (visible only if working) */}
            {s.working ? (
              <div className="flex items-center gap-3">
                <div className="flex items-center space-x-2 border border-neutral-200 rounded px-2.5 py-1.5 bg-neutral-50 focus-within:border-neutral-500">
                  <Clock className="w-3.5 h-3.5 text-neutral-400" />
                  <input 
                    type="time" 
                    value={s.startTime}
                    onChange={(e) => handleTimeChange(s.dayOfWeek, 'startTime', e.target.value)}
                    className="text-xs bg-transparent border-none outline-none font-medium focus:ring-0"
                  />
                </div>
                <span className="text-xs text-neutral-400 font-medium">to</span>
                <div className="flex items-center space-x-2 border border-neutral-200 rounded px-2.5 py-1.5 bg-neutral-50 focus-within:border-neutral-500">
                  <Clock className="w-3.5 h-3.5 text-neutral-400" />
                  <input 
                    type="time" 
                    value={s.endTime}
                    onChange={(e) => handleTimeChange(s.dayOfWeek, 'endTime', e.target.value)}
                    className="text-xs bg-transparent border-none outline-none font-medium focus:ring-0"
                  />
                </div>
              </div>
            ) : (
              <span className="text-xs text-neutral-400 font-medium italic sm:pr-8 py-2">Day Off</span>
            )}
          </div>
        ))}
      </div>

      <button 
        type="button" 
        onClick={handleSaveClick}
        disabled={loading}
        className="w-full sm:w-auto sm:px-8 akarsha-btn-primary disabled:opacity-50"
      >
        {loading ? '...' : t('finishBtn')}
      </button>
    </div>
  );
};
