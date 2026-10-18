import React, { useState } from 'react';
import { 
  CheckCircle2, 
  Circle, 
  Pin, 
  Archive, 
  Trash2, 
  Clock, 
  Calendar, 
  Sparkles, 
  AlertCircle, 
  ChevronDown, 
  ChevronUp,
  Tag,
  CheckSquare,
  Square
} from 'lucide-react';
import { QuickPilotItem, Priority } from '../types';

interface ItemCardProps {
  item: QuickPilotItem;
  onToggleComplete: (id: string) => void;
  onToggleChecklist: (itemId: string, checkId: string) => void;
  onTogglePin: (id: string) => void;
  onArchive: (id: string) => void;
  onDelete: (id: string) => void;
  onExtractActions?: (item: QuickPilotItem) => void;
}

export const ItemCard: React.FC<ItemCardProps> = ({
  item,
  onToggleComplete,
  onToggleChecklist,
  onTogglePin,
  onArchive,
  onDelete,
  onExtractActions
}) => {
  const [isExpanded, setIsExpanded] = useState(false);

  const priorityStyles: Record<Priority, { bg: string; text: string; border: string }> = {
    URGENT: { bg: 'bg-rose-urgent/10', text: 'text-rose-urgent', border: 'border-rose-urgent/30' },
    HIGH: { bg: 'bg-amber-warning/10', text: 'text-amber-warning', border: 'border-amber-warning/30' },
    NORMAL: { bg: 'bg-blue-500/10', text: 'text-blue-400', border: 'border-blue-500/30' },
    LOW: { bg: 'bg-emerald-success/10', text: 'text-emerald-success', border: 'border-emerald-success/30' }
  };

  const pStyle = priorityStyles[item.priority] || priorityStyles.NORMAL;

  return (
    <div 
      className={`group relative rounded-2xl border transition-all duration-200 bg-slate-900/90 hover:bg-slate-850/90 p-4 sm:p-5 ${
        item.isPinned 
          ? 'border-violet-primary/40 shadow-md shadow-violet-primary/5' 
          : 'border-slate-800 hover:border-slate-700'
      } ${item.isCompleted ? 'opacity-65' : ''}`}
    >
      <div className="flex items-start justify-between gap-3">
        {/* Complete Checkbox */}
        {item.type !== 'NOTE' && item.type !== 'IDEA' ? (
          <button
            onClick={() => onToggleComplete(item.id)}
            className="mt-0.5 text-slate-500 hover:text-emerald-success transition-colors shrink-0"
            title={item.isCompleted ? 'Mark incomplete' : 'Mark complete'}
          >
            {item.isCompleted ? (
              <CheckCircle2 className="w-5 h-5 text-emerald-success" />
            ) : (
              <Circle className="w-5 h-5 text-slate-400 hover:text-violet-light" />
            )}
          </button>
        ) : (
          <div className="w-5 h-5 mt-0.5 rounded-full bg-violet-primary/20 flex items-center justify-center shrink-0">
            <span className="w-2 h-2 rounded-full bg-violet-primary" />
          </div>
        )}

        {/* Content area */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center space-x-2">
            <h3 
              className={`text-base font-semibold leading-snug cursor-pointer transition-colors ${
                item.isCompleted 
                  ? 'line-through text-slate-500' 
                  : 'text-slate-100 hover:text-violet-light'
              }`}
              onClick={() => setIsExpanded(!isExpanded)}
            >
              {item.title}
            </h3>
          </div>

          {item.description && item.description !== item.title && (
            <p className={`text-xs sm:text-sm text-slate-400 mt-1 leading-relaxed ${isExpanded ? '' : 'line-clamp-2'}`}>
              {item.description}
            </p>
          )}

          {/* Clarification Alert Box */}
          {item.clarificationQuestion && (
            <div className="mt-3 p-2.5 rounded-xl bg-amber-warning/10 border border-amber-warning/25 flex items-start space-x-2 text-xs text-amber-200">
              <AlertCircle className="w-4 h-4 text-amber-warning shrink-0 mt-0.5" />
              <div>
                <span className="font-semibold text-amber-warning">Clarification: </span>
                <span>{item.clarificationQuestion}</span>
              </div>
            </div>
          )}

          {/* Checklist Items if present */}
          {item.checklist && item.checklist.length > 0 && (
            <div className="mt-3 p-3 rounded-xl bg-slate-950/60 border border-slate-800 space-y-1.5">
              {(isExpanded ? item.checklist : item.checklist.slice(0, 3)).map(check => (
                <div 
                  key={check.id}
                  onClick={() => onToggleChecklist(item.id, check.id)}
                  className="flex items-center space-x-2 text-xs sm:text-sm cursor-pointer select-none text-slate-300 hover:text-white"
                >
                  {check.isDone ? (
                    <CheckSquare className="w-4 h-4 text-emerald-success shrink-0" />
                  ) : (
                    <Square className="w-4 h-4 text-slate-500 shrink-0" />
                  )}
                  <span className={check.isDone ? 'line-through text-slate-500' : ''}>
                    {check.text}
                  </span>
                </div>
              ))}
              {!isExpanded && item.checklist.length > 3 && (
                <button 
                  onClick={() => setIsExpanded(true)}
                  className="text-xs text-violet-light font-medium pt-1 hover:underline"
                >
                  + {item.checklist.length - 3} more checklist items...
                </button>
              )}
            </div>
          )}

          {/* Metadata Badges */}
          <div className="flex flex-wrap items-center gap-1.5 sm:gap-2 mt-3 pt-1">
            <span className="text-[11px] font-semibold px-2 py-0.5 rounded-md bg-slate-800 text-slate-300 border border-slate-700/60">
              {item.category}
            </span>

            <span className="text-[11px] font-medium px-2 py-0.5 rounded-md bg-cyan-accent/10 text-cyan-light border border-cyan-accent/20">
              {item.type}
            </span>

            {item.priority !== 'NORMAL' && (
              <span className={`text-[11px] font-semibold px-2 py-0.5 rounded-md border ${pStyle.bg} ${pStyle.text} ${pStyle.border}`}>
                {item.priority}
              </span>
            )}

            {item.dueDate && (
              <span className="text-[11px] flex items-center space-x-1 px-2 py-0.5 rounded-md bg-slate-800 text-slate-300 border border-slate-700">
                <Calendar className="w-3 h-3 text-slate-400" />
                <span>{item.dueDate}</span>
                {item.timeString && <span>• {item.timeString}</span>}
              </span>
            )}

            {item.durationMinutes > 0 && item.type === 'TASK' && (
              <span className="text-[11px] flex items-center space-x-1 px-2 py-0.5 rounded-md bg-slate-800/80 text-slate-400 border border-slate-700/60">
                <Clock className="w-3 h-3" />
                <span>{item.durationMinutes}m</span>
              </span>
            )}
          </div>
        </div>

        {/* Action icons */}
        <div className="flex items-center space-x-1 shrink-0">
          <button
            onClick={() => onTogglePin(item.id)}
            className={`p-1.5 rounded-lg transition-colors ${
              item.isPinned 
                ? 'text-violet-primary bg-violet-primary/10' 
                : 'text-slate-500 hover:text-slate-300 hover:bg-slate-800'
            }`}
            title={item.isPinned ? 'Unpin' : 'Pin to top'}
          >
            <Pin className="w-4 h-4" />
          </button>

          {item.type === 'NOTE' && onExtractActions && (
            <button
              onClick={() => onExtractActions(item)}
              className="p-1.5 rounded-lg text-violet-light hover:bg-violet-primary/10 transition-colors"
              title="Extract tasks from note"
            >
              <Sparkles className="w-4 h-4" />
            </button>
          )}

          <button
            onClick={() => onArchive(item.id)}
            className="p-1.5 rounded-lg text-slate-500 hover:text-slate-300 hover:bg-slate-800 transition-colors"
            title="Archive"
          >
            <Archive className="w-4 h-4" />
          </button>

          <button
            onClick={() => onDelete(item.id)}
            className="p-1.5 rounded-lg text-slate-500 hover:text-rose-urgent hover:bg-rose-urgent/10 transition-colors"
            title="Delete"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};
