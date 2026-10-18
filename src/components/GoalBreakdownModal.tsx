import React, { useState } from 'react';
import { X, Sparkles, Plus, Check } from 'lucide-react';
import { breakDownGoalIntoMilestones } from '../nlpEngine';

interface GoalBreakdownModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCreateGoal: (title: string, milestones: string[]) => void;
}

export const GoalBreakdownModal: React.FC<GoalBreakdownModalProps> = ({
  isOpen,
  onClose,
  onCreateGoal
}) => {
  const [goalTitle, setGoalTitle] = useState('');
  const [suggestedSteps, setSuggestedSteps] = useState<string[]>([]);

  if (!isOpen) return null;

  const handleDecompose = () => {
    if (!goalTitle.trim()) return;
    const steps = breakDownGoalIntoMilestones(goalTitle);
    setSuggestedSteps(steps);
  };

  const handleCreate = () => {
    if (!goalTitle.trim()) return;
    onCreateGoal(goalTitle, suggestedSteps);
    setGoalTitle('');
    setSuggestedSteps([]);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-xl max-h-[90vh] flex flex-col shadow-2xl overflow-hidden">
        <div className="flex items-center justify-between p-5 border-b border-slate-800">
          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 rounded-xl bg-cyan-accent/20 border border-cyan-accent/30 flex items-center justify-center text-cyan-accent">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-white">AI Goal Breakdown</h2>
              <p className="text-xs text-slate-400">Deconstruct any big goal into actionable milestones.</p>
            </div>
          </div>
          <button onClick={onClose} className="p-2 rounded-xl text-slate-400 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-5 space-y-4 overflow-y-auto">
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Goal or Large Objective
            </label>
            <div className="flex space-x-2">
              <input
                type="text"
                value={goalTitle}
                onChange={e => setGoalTitle(e.target.value)}
                placeholder="e.g. Launch my website / Study for physics final exam"
                className="flex-1 bg-slate-950/80 border border-slate-800 focus:border-cyan-accent rounded-xl px-4 py-2.5 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none"
              />
              <button
                type="button"
                onClick={handleDecompose}
                disabled={!goalTitle.trim()}
                className="px-4 py-2.5 rounded-xl bg-cyan-accent hover:bg-cyan-light text-slate-950 font-bold text-xs disabled:opacity-40 transition-all shrink-0"
              >
                Break Down
              </button>
            </div>
          </div>

          {suggestedSteps.length > 0 && (
            <div className="space-y-2 pt-2 border-t border-slate-800">
              <p className="text-xs font-semibold text-slate-300">Suggested Action Milestones:</p>
              <div className="space-y-1.5">
                {suggestedSteps.map((step, idx) => (
                  <div key={idx} className="flex items-center space-x-2 p-2.5 rounded-lg bg-slate-950/60 border border-slate-800 text-xs text-slate-200">
                    <span className="w-4 h-4 rounded-full bg-cyan-accent/20 text-cyan-accent flex items-center justify-center font-bold text-[10px]">
                      {idx + 1}
                    </span>
                    <span>{step}</span>
                  </div>
                ))}
              </div>

              <button
                onClick={handleCreate}
                className="w-full mt-4 flex items-center justify-center space-x-2 py-2.5 rounded-xl bg-violet-primary hover:bg-violet-light text-white font-semibold text-xs shadow-md shadow-violet-primary/20 transition-all"
              >
                <Check className="w-4 h-4" />
                <span>Create Goal with {suggestedSteps.length} Milestones</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
