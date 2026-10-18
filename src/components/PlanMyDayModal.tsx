import React from 'react';
import { 
  X, 
  Sparkles, 
  Coffee, 
  Clock, 
  Check, 
  RotateCcw,
  CheckCircle2
} from 'lucide-react';
import { ScheduleBlock, UserPreferences } from '../types';

interface PlanMyDayModalProps {
  isOpen: boolean;
  onClose: () => void;
  schedule: ScheduleBlock[];
  preferences: UserPreferences;
  onRebalance: () => void;
  onAcceptSchedule: (blocks: ScheduleBlock[]) => void;
}

export const PlanMyDayModal: React.FC<PlanMyDayModalProps> = ({
  isOpen,
  onClose,
  schedule,
  preferences,
  onRebalance,
  onAcceptSchedule
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-2xl max-h-[90vh] flex flex-col shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-slate-800">
          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 rounded-xl bg-cyan-accent/20 border border-cyan-accent/30 flex items-center justify-center text-cyan-accent">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-white">Smart Daily Planner</h2>
              <p className="text-xs text-slate-400">
                Pacing {preferences.workStartHour}:00 - {preferences.workEndHour}:00 • {preferences.planningStyle} mode
              </p>
            </div>
          </div>
          <button 
            onClick={onClose}
            className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Schedule timeline */}
        <div className="p-5 overflow-y-auto space-y-3">
          <div className="p-3.5 rounded-xl bg-violet-primary/10 border border-violet-primary/20 text-xs text-slate-300">
            💡 QuickPilot AI spaced your urgent and high-priority tasks with 15-minute recharge breaks to keep you energized and prevent burnout.
          </div>

          {schedule.length === 0 ? (
            <div className="text-center py-12">
              <CheckCircle2 className="w-12 h-12 text-emerald-success mx-auto mb-3" />
              <h3 className="text-base font-semibold text-white">All clear for today!</h3>
              <p className="text-xs text-slate-400 mt-1">Capture more tasks or enjoy your free time.</p>
            </div>
          ) : (
            <div className="space-y-2.5">
              {schedule.map(block => (
                <div 
                  key={block.id}
                  className={`flex items-center justify-between p-3.5 rounded-xl border transition-all ${
                    block.isBreak 
                      ? 'bg-cyan-accent/5 border-cyan-accent/20 text-cyan-200' 
                      : 'bg-slate-950/70 border-slate-800 text-slate-200'
                  }`}
                >
                  <div className="flex items-center space-x-3">
                    <div className={`w-8 h-8 rounded-lg flex items-center justify-center shrink-0 ${
                      block.isBreak ? 'bg-cyan-accent/20 text-cyan-accent' : 'bg-violet-primary/20 text-violet-primary'
                    }`}>
                      {block.isBreak ? <Coffee className="w-4 h-4" /> : <Clock className="w-4 h-4" />}
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-white">{block.title}</p>
                      <p className="text-xs text-slate-400">{block.timeSlot} ({block.durationMinutes} mins)</p>
                    </div>
                  </div>

                  <span className="text-[11px] font-medium px-2 py-0.5 rounded-md bg-slate-800 text-slate-300">
                    {block.category}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer actions */}
        <div className="p-4 border-t border-slate-800 bg-slate-900 flex items-center justify-between">
          <button
            onClick={onRebalance}
            className="flex items-center space-x-1.5 px-3 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>Rebalance</span>
          </button>

          <div className="flex items-center space-x-2">
            <button
              onClick={onClose}
              className="px-3.5 py-2 rounded-xl text-xs font-semibold text-slate-400 hover:text-white"
            >
              Cancel
            </button>
            <button
              onClick={() => {
                onAcceptSchedule(schedule);
                onClose();
              }}
              className="flex items-center space-x-1.5 px-4 py-2 rounded-xl bg-violet-primary hover:bg-violet-light text-white text-xs font-semibold shadow-md shadow-violet-primary/20 transition-all"
            >
              <Check className="w-4 h-4" />
              <span>Accept Schedule</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
