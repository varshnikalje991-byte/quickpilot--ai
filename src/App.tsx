import React, { useState, useEffect } from 'react';
import { 
  Sparkles, 
  TrendingUp, 
  CalendarCheck2, 
  Clock, 
  Calendar, 
  Search, 
  Plus, 
  FolderKanban, 
  CheckCircle2, 
  AlertCircle, 
  ChevronLeft, 
  ChevronRight, 
  Share2, 
  Trash2, 
  FileText,
  Inbox,
  Filter
} from 'lucide-react';
import { 
  QuickPilotItem, 
  Project, 
  UserPreferences, 
  ScheduleBlock, 
  AppTab, 
  ItemType 
} from './types';
import { StorageService } from './storage';
import { 
  parseNaturalLanguageInput, 
  generateDailyPlanSchedule, 
  extractActionsFromNoteText,
  ParsedItemCandidate 
} from './nlpEngine';
import { Navbar } from './components/Navbar';
import { ItemCard } from './components/ItemCard';
import { QuickCaptureModal } from './components/QuickCaptureModal';
import { PlanMyDayModal } from './components/PlanMyDayModal';
import { GoalBreakdownModal } from './components/GoalBreakdownModal';

export const App: React.FC = () => {
  const [currentTab, setCurrentTab] = useState<AppTab>('dashboard');
  const [items, setItems] = useState<QuickPilotItem[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [preferences, setPreferences] = useState<UserPreferences>(StorageService.loadPreferences());
  const [schedule, setSchedule] = useState<ScheduleBlock[]>([]);

  // Modals
  const [isQuickCaptureOpen, setIsQuickCaptureOpen] = useState(false);
  const [isPlanMyDayOpen, setIsPlanMyDayOpen] = useState(false);
  const [isGoalBreakdownOpen, setIsGoalBreakdownOpen] = useState(false);

  // Filters & Search
  const [categoryFilter, setCategoryFilter] = useState<string>('All');
  const [searchQuery, setSearchQuery] = useState('');
  const [calendarDayOffset, setCalendarDayOffset] = useState(0);
  const [calendarViewMode, setCalendarViewMode] = useState<'Day' | 'Week' | 'Month'>('Day');

  // New Note state
  const [isCreateNoteOpen, setIsCreateNoteOpen] = useState(false);
  const [noteTitle, setNoteTitle] = useState('');
  const [noteContent, setNoteContent] = useState('');
  const [noteCategory, setNoteCategory] = useState('Work');

  // New Project state
  const [isCreateProjectOpen, setIsCreateProjectOpen] = useState(false);
  const [projectTitle, setProjectTitle] = useState('');
  const [projectDesc, setProjectDesc] = useState('');

  // Toast message
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3000);
  };

  // Initial load
  useEffect(() => {
    const loadedItems = StorageService.loadItems();
    const loadedProjects = StorageService.loadProjects();
    const loadedPrefs = StorageService.loadPreferences();
    const loadedSchedule = StorageService.loadSchedule();

    setItems(loadedItems);
    setProjects(loadedProjects);
    setPreferences(loadedPrefs);

    if (loadedSchedule.length > 0) {
      setSchedule(loadedSchedule);
    } else {
      const generated = generateDailyPlanSchedule(loadedItems, loadedPrefs);
      setSchedule(generated);
      StorageService.saveSchedule(generated);
    }
  }, []);

  // Save changes
  const updateItems = (newItems: QuickPilotItem[]) => {
    setItems(newItems);
    StorageService.saveItems(newItems);
  };

  const updateProjects = (newProjects: Project[]) => {
    setProjects(newProjects);
    StorageService.saveProjects(newProjects);
  };

  const updatePreferences = (newPrefs: UserPreferences) => {
    setPreferences(newPrefs);
    StorageService.savePreferences(newPrefs);
  };

  // Item actions
  const handleToggleComplete = (id: string) => {
    const updated = items.map(item => {
      if (item.id === id) {
        const nextDone = !item.isCompleted;
        return {
          ...item,
          isCompleted: nextDone,
          completedAt: nextDone ? Date.now() : null,
          updatedAt: Date.now()
        };
      }
      return item;
    });
    updateItems(updated);
  };

  const handleToggleChecklist = (itemId: string, checkId: string) => {
    const updated = items.map(item => {
      if (item.id === itemId) {
        const updatedChecklist = item.checklist.map(c => 
          c.id === checkId ? { ...c, isDone: !c.isDone } : c
        );
        return { ...item, checklist: updatedChecklist, updatedAt: Date.now() };
      }
      return item;
    });
    updateItems(updated);
  };

  const handleTogglePin = (id: string) => {
    const updated = items.map(item => 
      item.id === id ? { ...item, isPinned: !item.isPinned, updatedAt: Date.now() } : item
    );
    updateItems(updated);
  };

  const handleArchive = (id: string) => {
    const updated = items.map(item => 
      item.id === id ? { ...item, isArchived: true, updatedAt: Date.now() } : item
    );
    updateItems(updated);
    showToast('Item archived');
  };

  const handleDelete = (id: string) => {
    const updated = items.filter(item => item.id !== id);
    updateItems(updated);
    showToast('Item deleted');
  };

  // AI Quick Capture Save
  const handleSaveParsedItems = (candidates: ParsedItemCandidate[]) => {
    const now = Date.now();
    const newItems: QuickPilotItem[] = candidates.map((cand, idx) => ({
      id: `item-${now}-${idx}-${Math.random().toString(36).substr(2, 4)}`,
      title: cand.title,
      description: cand.description,
      type: cand.type,
      category: cand.category,
      priority: cand.priority,
      dueDate: cand.dueDate,
      timeString: cand.timeString,
      durationMinutes: cand.durationMinutes,
      isCompleted: false,
      completedAt: null,
      isPinned: false,
      isArchived: false,
      projectId: null,
      checklist: cand.checklist.map((text, cIdx) => ({
        id: `c-${cIdx}-${Date.now()}`,
        text,
        isDone: false
      })),
      clarificationQuestion: cand.clarificationQuestion,
      createdAt: now,
      updatedAt: now
    }));

    const merged = [...newItems, ...items];
    updateItems(merged);
    showToast(`QuickPilot organized ${newItems.length} items!`);
  };

  // Extract Actions from Note
  const handleExtractActionsFromNote = (note: QuickPilotItem) => {
    const actions = extractActionsFromNoteText(note.title, note.description);
    const now = Date.now();
    const createdTasks: QuickPilotItem[] = actions.map((act, i) => ({
      id: `extracted-${now}-${i}`,
      title: act,
      description: `Action item extracted from note: ${note.title}`,
      type: 'TASK',
      category: note.category,
      priority: 'NORMAL',
      dueDate: new Date().toISOString().split('T')[0],
      timeString: null,
      durationMinutes: 30,
      isCompleted: false,
      completedAt: null,
      isPinned: false,
      isArchived: false,
      projectId: null,
      checklist: [],
      createdAt: now,
      updatedAt: now
    }));

    updateItems([...createdTasks, ...items]);
    showToast(`Extracted ${createdTasks.length} action tasks!`);
  };

  // Bulk organize inbox
  const handleBulkOrganize = () => {
    const unorganized = items.filter(i => !i.isArchived && !i.isCompleted && (i.category === 'General' || !i.dueDate));
    if (unorganized.length === 0) {
      showToast('Inbox is already well organized!');
      return;
    }
    const updated = items.map(item => {
      if (!item.isArchived && !item.isCompleted && (item.category === 'General' || !item.dueDate)) {
        const parsed = parseNaturalLanguageInput(item.title)[0];
        if (parsed) {
          return {
            ...item,
            category: parsed.category,
            priority: parsed.priority,
            durationMinutes: parsed.durationMinutes,
            updatedAt: Date.now()
          };
        }
      }
      return item;
    });
    updateItems(updated);
    showToast(`Auto-organized ${unorganized.length} items!`);
  };

  // Plan My Day
  const handleRebalanceSchedule = () => {
    const newSchedule = generateDailyPlanSchedule(items, preferences);
    setSchedule(newSchedule);
    StorageService.saveSchedule(newSchedule);
  };

  const handleAcceptSchedule = (acceptedBlocks: ScheduleBlock[]) => {
    setSchedule(acceptedBlocks);
    StorageService.saveSchedule(acceptedBlocks);
    showToast('Schedule accepted for today! 🚀');
  };

  // Metrics
  const activeItems = items.filter(i => !i.isArchived);
  const totalItemsCount = activeItems.length;
  const completedItemsCount = activeItems.filter(i => i.isCompleted).length;
  const progressPercent = totalItemsCount > 0 ? Math.round((completedItemsCount / totalItemsCount) * 100) : 0;
  const urgentTasks = activeItems.filter(i => !i.isCompleted && (i.priority === 'URGENT' || i.priority === 'HIGH'));
  const inboxItems = activeItems.filter(i => !i.isCompleted);

  // Filter items
  const filteredItems = activeItems.filter(item => {
    if (categoryFilter === 'Tasks') return item.type === 'TASK';
    if (categoryFilter === 'Reminders') return item.type === 'REMINDER';
    if (categoryFilter === 'Notes') return item.type === 'NOTE';
    if (categoryFilter === 'Shopping') return item.type === 'SHOPPING';
    if (categoryFilter === 'Ideas') return item.type === 'IDEA';
    return true;
  });

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans pb-20 md:pb-8">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed top-16 left-1/2 -translate-x-1/2 z-50 px-4 py-2.5 rounded-xl bg-violet-primary text-white text-xs font-semibold shadow-xl border border-violet-light/30 flex items-center space-x-2 animate-in fade-in slide-in-from-top-2">
          <Sparkles className="w-4 h-4 text-cyan-accent" />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Navigation */}
      <Navbar
        currentTab={currentTab}
        onSelectTab={setCurrentTab}
        onOpenQuickCapture={() => setIsQuickCaptureOpen(true)}
        inboxCount={inboxItems.length}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 lg:px-8 py-6">
        {/* ========================================================= */}
        {/* DASHBOARD TAB                                             */}
        {/* ========================================================= */}
        {currentTab === 'dashboard' && (
          <div className="space-y-6">
            {/* Greeting Header */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div>
                <span className="text-xs font-bold uppercase tracking-wider text-violet-light">
                  {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })}
                </span>
                <h1 className="text-2xl sm:text-3xl font-extrabold text-white mt-0.5">
                  Hello, {preferences.userName}
                </h1>
              </div>

              <div className="flex items-center space-x-2">
                <span className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full bg-violet-container/50 border border-violet-primary/30 text-xs font-semibold text-violet-light">
                  <span className="w-2 h-2 rounded-full bg-emerald-success animate-pulse" />
                  <span>AI Engine Online</span>
                </span>
              </div>
            </div>

            {/* Daily Trajectory Card */}
            <div className="p-6 rounded-3xl bg-slate-900 border border-slate-800 shadow-xl flex flex-col md:flex-row md:items-center md:justify-between gap-6">
              <div>
                <h2 className="text-lg font-bold text-white">Daily Trajectory</h2>
                <p className="text-sm text-slate-400 mt-1">
                  {completedItemsCount} of {totalItemsCount} actions completed
                </p>
                <div className="flex items-center space-x-2 text-xs font-semibold text-emerald-success mt-3">
                  <TrendingUp className="w-4 h-4" />
                  <span>
                    {totalItemsCount === 0 ? 'Ready to organize your day' : `${progressPercent}% on schedule`}
                  </span>
                </div>
              </div>

              <div className="flex items-center space-x-4">
                <div className="relative w-20 h-20 flex items-center justify-center shrink-0">
                  <svg className="w-full h-full -rotate-90" viewBox="0 0 36 36">
                    <path
                      className="text-slate-800"
                      strokeWidth="3.5"
                      stroke="currentColor"
                      fill="none"
                      d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                    />
                    <path
                      className="text-violet-primary transition-all duration-500 ease-out"
                      strokeDasharray={`${progressPercent}, 100`}
                      strokeWidth="3.5"
                      strokeLinecap="round"
                      stroke="currentColor"
                      fill="none"
                      d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                    />
                  </svg>
                  <span className="absolute text-sm font-bold text-white">{progressPercent}%</span>
                </div>
              </div>
            </div>

            {/* Plan My Day Action Banner */}
            <div 
              onClick={() => {
                handleRebalanceSchedule();
                setIsPlanMyDayOpen(true);
              }}
              className="group cursor-pointer p-5 rounded-2xl bg-gradient-to-r from-violet-primary/20 via-indigo-900/40 to-cyan-accent/15 border border-violet-primary/30 hover:border-violet-light/50 transition-all flex items-center justify-between shadow-lg"
            >
              <div className="flex items-center space-x-4">
                <div className="w-11 h-11 rounded-xl bg-violet-primary/30 border border-violet-primary/40 flex items-center justify-center text-cyan-accent shrink-0 group-hover:scale-105 transition-transform">
                  <Sparkles className="w-6 h-6" />
                </div>
                <div>
                  <span className="text-xs font-bold text-cyan-accent uppercase tracking-wider">AI Smart Daily Planner</span>
                  <h3 className="text-base font-bold text-white">
                    {schedule.length > 0 ? `Today's schedule active (${schedule.length} time blocks)` : 'Propose an optimized schedule for Today'}
                  </h3>
                  <p className="text-xs text-slate-400">Balances focus tasks with 15-minute breaks.</p>
                </div>
              </div>

              <button className="px-4 py-2 rounded-xl bg-violet-primary group-hover:bg-violet-light text-white font-semibold text-xs transition-colors shrink-0">
                {schedule.length > 0 ? 'View Plan' : 'Plan My Day'}
              </button>
            </div>

            {/* Urgent Items Alert */}
            {urgentTasks.length > 0 && (
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <h2 className="text-sm font-bold uppercase tracking-wider text-rose-urgent flex items-center space-x-1.5">
                    <AlertCircle className="w-4 h-4" />
                    <span>High Priority & Urgent ({urgentTasks.length})</span>
                  </h2>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  {urgentTasks.map(item => (
                    <ItemCard
                      key={item.id}
                      item={item}
                      onToggleComplete={handleToggleComplete}
                      onToggleChecklist={handleToggleChecklist}
                      onTogglePin={handleTogglePin}
                      onArchive={handleArchive}
                      onDelete={handleDelete}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Category Filter Pills & Items Feed */}
            <div className="space-y-4 pt-2">
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-bold text-white">Items & Notes</h2>
                <div className="flex items-center space-x-1.5 overflow-x-auto pb-1">
                  {['All', 'Tasks', 'Reminders', 'Notes', 'Shopping', 'Ideas'].map(cat => (
                    <button
                      key={cat}
                      onClick={() => setCategoryFilter(cat)}
                      className={`text-xs px-3 py-1.5 rounded-full font-medium transition-all ${
                        categoryFilter === cat 
                          ? 'bg-violet-primary text-white shadow-sm' 
                          : 'bg-slate-900 text-slate-400 hover:text-white border border-slate-800'
                      }`}
                    >
                      {cat}
                    </button>
                  ))}
                </div>
              </div>

              {filteredItems.length === 0 ? (
                <div className="text-center py-14 rounded-2xl bg-slate-900/50 border border-slate-800">
                  <CheckCircle2 className="w-10 h-10 text-violet-primary mx-auto mb-2 opacity-80" />
                  <p className="text-sm font-semibold text-white">No items found</p>
                  <p className="text-xs text-slate-400 mt-1">Use Quick Capture to add thoughts or tasks in seconds.</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
                  {filteredItems.map(item => (
                    <ItemCard
                      key={item.id}
                      item={item}
                      onToggleComplete={handleToggleComplete}
                      onToggleChecklist={handleToggleChecklist}
                      onTogglePin={handleTogglePin}
                      onArchive={handleArchive}
                      onDelete={handleDelete}
                      onExtractActions={item.type === 'NOTE' ? handleExtractActionsFromNote : undefined}
                    />
                  ))}
                </div>
              )}
            </div>
          </div>
        )}

        {/* ========================================================= */}
        {/* INBOX TAB                                                 */}
        {/* ========================================================= */}
        {currentTab === 'inbox' && (
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div>
                <h1 className="text-2xl font-bold text-white">Inbox</h1>
                <p className="text-xs text-slate-400 mt-0.5">
                  {inboxItems.length} active items waiting to be organized or completed
                </p>
              </div>

              <button
                onClick={handleBulkOrganize}
                className="flex items-center space-x-2 px-4 py-2 rounded-xl bg-violet-primary hover:bg-violet-light text-white text-xs font-semibold shadow-md transition-all self-start sm:self-auto"
              >
                <Sparkles className="w-4 h-4" />
                <span>Auto-Organize Inbox</span>
              </button>
            </div>

            {inboxItems.length === 0 ? (
              <div className="text-center py-20 rounded-3xl bg-slate-900 border border-slate-800">
                <Inbox className="w-12 h-12 text-violet-primary mx-auto mb-3" />
                <h2 className="text-lg font-bold text-white">Inbox Zero! 🎉</h2>
                <p className="text-xs text-slate-400 mt-1">Every thought, reminder, and task has been completed.</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
                {inboxItems.map(item => (
                  <ItemCard
                    key={item.id}
                    item={item}
                    onToggleComplete={handleToggleComplete}
                    onToggleChecklist={handleToggleChecklist}
                    onTogglePin={handleTogglePin}
                    onArchive={handleArchive}
                    onDelete={handleDelete}
                    onExtractActions={item.type === 'NOTE' ? handleExtractActionsFromNote : undefined}
                  />
                ))}
              </div>
            )}
          </div>
        )}

        {/* ========================================================= */}
        {/* TODAY TAB                                                 */}
        {/* ========================================================= */}
        {currentTab === 'today' && (
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div>
                <h1 className="text-2xl font-bold text-white">Today's Schedule & Action</h1>
                <p className="text-xs text-slate-400 mt-0.5">
                  {preferences.workStartHour}:00 - {preferences.workEndHour}:00 working window
                </p>
              </div>

              <button
                onClick={() => {
                  handleRebalanceSchedule();
                  setIsPlanMyDayOpen(true);
                }}
                className="flex items-center space-x-2 px-4 py-2 rounded-xl bg-cyan-accent hover:bg-cyan-light text-slate-950 text-xs font-bold shadow-md transition-all self-start sm:self-auto"
              >
                <Sparkles className="w-4 h-4" />
                <span>Plan My Day</span>
              </button>
            </div>

            {/* Time Blocks */}
            {schedule.length > 0 && (
              <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 space-y-3">
                <h2 className="text-sm font-bold text-white flex items-center space-x-2">
                  <Clock className="w-4 h-4 text-violet-light" />
                  <span>Time-Blocked Schedule</span>
                </h2>

                <div className="space-y-2">
                  {schedule.map(block => (
                    <div 
                      key={block.id}
                      className={`p-3 rounded-xl border flex items-center justify-between text-xs ${
                        block.isBreak 
                          ? 'bg-cyan-accent/5 border-cyan-accent/20 text-cyan-200' 
                          : 'bg-slate-950/70 border-slate-800 text-slate-200'
                      }`}
                    >
                      <div>
                        <span className="font-semibold text-white">{block.title}</span>
                        <span className="text-slate-400 ml-2">({block.timeSlot})</span>
                      </div>
                      <span className="text-[10px] font-semibold px-2 py-0.5 rounded bg-slate-800 text-slate-300">
                        {block.category}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Tasks for today */}
            <div className="space-y-3">
              <h2 className="text-base font-bold text-white">Action Items</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
                {activeItems
                  .filter(i => !i.isCompleted)
                  .map(item => (
                    <ItemCard
                      key={item.id}
                      item={item}
                      onToggleComplete={handleToggleComplete}
                      onToggleChecklist={handleToggleChecklist}
                      onTogglePin={handleTogglePin}
                      onArchive={handleArchive}
                      onDelete={handleDelete}
                    />
                  ))}
              </div>
            </div>
          </div>
        )}

        {/* ========================================================= */}
        {/* CALENDAR TAB                                              */}
        {/* ========================================================= */}
        {currentTab === 'calendar' && (
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div>
                <h1 className="text-2xl font-bold text-white">Calendar</h1>
                <p className="text-xs text-slate-400 mt-0.5">Timezone: {preferences.timezone}</p>
              </div>

              <div className="flex items-center space-x-1.5 bg-slate-900 p-1 rounded-xl border border-slate-800 self-start sm:self-auto">
                {(['Day', 'Week', 'Month'] as const).map(mode => (
                  <button
                    key={mode}
                    onClick={() => setCalendarViewMode(mode)}
                    className={`text-xs px-3 py-1.5 rounded-lg font-medium transition-all ${
                      calendarViewMode === mode 
                        ? 'bg-violet-primary text-white font-semibold' 
                        : 'text-slate-400 hover:text-white'
                    }`}
                  >
                    {mode}
                  </button>
                ))}
              </div>
            </div>

            {/* Date Navigator */}
            <div className="p-4 rounded-2xl bg-slate-900 border border-slate-800 flex items-center justify-between">
              <button
                onClick={() => setCalendarDayOffset(prev => prev - 1)}
                className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
              >
                <ChevronLeft className="w-5 h-5" />
              </button>

              <div className="text-center">
                <p className="text-base font-bold text-white">
                  {(() => {
                    const d = new Date();
                    d.setDate(d.getDate() + calendarDayOffset);
                    return d.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });
                  })()}
                </p>
                <p className="text-xs text-violet-light font-medium">
                  {calendarDayOffset === 0 ? 'Today' : calendarDayOffset === 1 ? 'Tomorrow' : calendarDayOffset === -1 ? 'Yesterday' : `${Math.abs(calendarDayOffset)} days ${calendarDayOffset > 0 ? 'ahead' : 'ago'}`}
                </p>
              </div>

              <button
                onClick={() => setCalendarDayOffset(prev => prev + 1)}
                className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
              >
                <ChevronRight className="w-5 h-5" />
              </button>
            </div>

            {/* Calendar Events List */}
            <div className="space-y-3">
              <h2 className="text-base font-bold text-white">Scheduled Tasks & Events</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
                {activeItems.map(item => (
                  <ItemCard
                    key={item.id}
                    item={item}
                    onToggleComplete={handleToggleComplete}
                    onToggleChecklist={handleToggleChecklist}
                    onTogglePin={handleTogglePin}
                    onArchive={handleArchive}
                    onDelete={handleDelete}
                  />
                ))}
              </div>
            </div>
          </div>
        )}

        {/* ========================================================= */}
        {/* NOTES TAB                                                 */}
        {/* ========================================================= */}
        {currentTab === 'notes' && (
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div>
                <h1 className="text-2xl font-bold text-white">Notes & Memory</h1>
                <p className="text-xs text-slate-400 mt-0.5">Private notes with 1-tap AI action extraction</p>
              </div>

              <button
                onClick={() => setIsCreateNoteOpen(true)}
                className="flex items-center space-x-1.5 px-4 py-2 rounded-xl bg-violet-primary hover:bg-violet-light text-white text-xs font-semibold shadow-md transition-all self-start sm:self-auto"
              >
                <Plus className="w-4 h-4" />
                <span>New Note</span>
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
              {activeItems
                .filter(i => i.type === 'NOTE')
                .map(note => (
                  <ItemCard
                    key={note.id}
                    item={note}
                    onToggleComplete={handleToggleComplete}
                    onToggleChecklist={handleToggleChecklist}
                    onTogglePin={handleTogglePin}
                    onArchive={handleArchive}
                    onDelete={handleDelete}
                    onExtractActions={handleExtractActionsFromNote}
                  />
                ))}
            </div>
          </div>
        )}

        {/* ========================================================= */}
        {/* PROJECTS TAB                                              */}
        {/* ========================================================= */}
        {currentTab === 'projects' && (
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div>
                <h1 className="text-2xl font-bold text-white">Projects & Goals</h1>
                <p className="text-xs text-slate-400 mt-0.5">Track milestones and decompose ambitious objectives</p>
              </div>

              <div className="flex items-center space-x-2 self-start sm:self-auto">
                <button
                  onClick={() => setIsGoalBreakdownOpen(true)}
                  className="flex items-center space-x-1.5 px-3.5 py-2 rounded-xl bg-cyan-accent hover:bg-cyan-light text-slate-950 text-xs font-bold shadow-md transition-all"
                >
                  <Sparkles className="w-4 h-4" />
                  <span>Break Down Goal</span>
                </button>

                <button
                  onClick={() => setIsCreateProjectOpen(true)}
                  className="flex items-center space-x-1 px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-white text-xs font-semibold transition-all"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {projects.map(proj => {
                const linkedTasks = items.filter(i => i.projectId === proj.id && !i.isArchived);
                const doneCount = linkedTasks.filter(i => i.isCompleted).length;
                const percent = linkedTasks.length > 0 ? Math.round((doneCount / linkedTasks.length) * 100) : 0;

                return (
                  <div key={proj.id} className="p-5 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
                    <div className="flex items-start justify-between">
                      <div>
                        <h3 className="text-base font-bold text-white">{proj.title}</h3>
                        {proj.description && <p className="text-xs text-slate-400 mt-1">{proj.description}</p>}
                      </div>
                      <span className="text-xs font-bold text-violet-light bg-violet-primary/20 px-2 py-1 rounded-lg">
                        {percent}%
                      </span>
                    </div>

                    <div className="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                      <div 
                        className="bg-violet-primary h-full transition-all duration-300"
                        style={{ width: `${percent}%` }}
                      />
                    </div>

                    <div className="space-y-1.5 pt-1">
                      {linkedTasks.slice(0, 3).map(task => (
                        <div key={task.id} className="flex items-center space-x-2 text-xs text-slate-300">
                          <span className={task.isCompleted ? 'text-emerald-success font-bold' : 'text-slate-500'}>
                            {task.isCompleted ? '✓' : '○'}
                          </span>
                          <span className={task.isCompleted ? 'line-through text-slate-500' : ''}>
                            {task.title}
                          </span>
                        </div>
                      ))}
                    </div>

                    <div className="flex items-center justify-between text-xs text-slate-500 pt-2 border-t border-slate-800/80">
                      <span>{doneCount} of {linkedTasks.length} milestones complete</span>
                      <button
                        onClick={() => {
                          const updated = projects.filter(p => p.id !== proj.id);
                          updateProjects(updated);
                          showToast('Project deleted');
                        }}
                        className="text-slate-500 hover:text-rose-urgent"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* ========================================================= */}
        {/* SEARCH TAB                                                */}
        {/* ========================================================= */}
        {currentTab === 'search' && (
          <div className="space-y-6">
            <div>
              <h1 className="text-2xl font-bold text-white">Search</h1>
              <p className="text-xs text-slate-400 mt-0.5">Instant natural search across tasks, notes, and projects</p>
            </div>

            <div className="relative">
              <input
                type="text"
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                placeholder="Type keywords e.g. physics, Wi-Fi, milk, ₹799..."
                className="w-full bg-slate-900 border border-slate-800 focus:border-violet-primary rounded-xl px-4 py-3 pl-11 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none"
              />
              <Search className="w-5 h-5 text-slate-500 absolute left-3.5 top-3.5" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
              {activeItems
                .filter(i => 
                  !searchQuery.trim() || 
                  i.title.toLowerCase().includes(searchQuery.toLowerCase()) || 
                  i.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
                  i.category.toLowerCase().includes(searchQuery.toLowerCase())
                )
                .map(item => (
                  <ItemCard
                    key={item.id}
                    item={item}
                    onToggleComplete={handleToggleComplete}
                    onToggleChecklist={handleToggleChecklist}
                    onTogglePin={handleTogglePin}
                    onArchive={handleArchive}
                    onDelete={handleDelete}
                  />
                ))}
            </div>
          </div>
        )}

        {/* ========================================================= */}
        {/* SETTINGS TAB                                              */}
        {/* ========================================================= */}
        {currentTab === 'settings' && (
          <div className="space-y-6 max-w-2xl">
            <div>
              <h1 className="text-2xl font-bold text-white">Settings</h1>
              <p className="text-xs text-slate-400 mt-0.5">Personalize your schedule, working hours, and browser storage</p>
            </div>

            <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
              <h2 className="text-sm font-bold text-white">Profile</h2>
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Preferred Name</label>
                <input
                  type="text"
                  value={preferences.userName}
                  onChange={e => updatePreferences({ ...preferences, userName: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-sm text-white focus:outline-none focus:border-violet-primary"
                />
              </div>
            </div>

            <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
              <h2 className="text-sm font-bold text-white">Schedule Pacing & Working Hours</h2>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Work Start Hour</label>
                  <select
                    value={preferences.workStartHour}
                    onChange={e => updatePreferences({ ...preferences, workStartHour: Number(e.target.value) })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none"
                  >
                    {[7, 8, 9, 10, 11].map(h => (
                      <option key={h} value={h}>{h}:00 AM</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Work End Hour</label>
                  <select
                    value={preferences.workEndHour}
                    onChange={e => updatePreferences({ ...preferences, workEndHour: Number(e.target.value) })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none"
                  >
                    {[16, 17, 18, 19, 20, 21].map(h => (
                      <option key={h} value={h}>{h > 12 ? h - 12 : h}:00 PM</option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Planning Style</label>
                <div className="flex space-x-2">
                  {(['Balanced', 'Focused', 'Relaxed'] as const).map(style => (
                    <button
                      key={style}
                      onClick={() => updatePreferences({ ...preferences, planningStyle: style })}
                      className={`flex-1 py-2 rounded-xl text-xs font-medium border transition-all ${
                        preferences.planningStyle === style 
                          ? 'bg-violet-primary text-white border-violet-primary' 
                          : 'bg-slate-950 text-slate-400 border-slate-800 hover:text-white'
                      }`}
                    >
                      {style}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
              <h2 className="text-sm font-bold text-white">Data & Backup</h2>
              <div className="flex space-x-3">
                <button
                  onClick={() => {
                    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(items, null, 2));
                    const downloadAnchor = document.createElement('a');
                    downloadAnchor.setAttribute("href", dataStr);
                    downloadAnchor.setAttribute("download", `quickpilot-export-${Date.now()}.json`);
                    document.body.appendChild(downloadAnchor);
                    downloadAnchor.click();
                    downloadAnchor.remove();
                    showToast('Exported JSON backup!');
                  }}
                  className="flex items-center space-x-2 px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-white text-xs font-semibold transition-all"
                >
                  <Share2 className="w-4 h-4" />
                  <span>Export JSON</span>
                </button>

                <button
                  onClick={() => {
                    if (confirm('Are you sure you want to delete all tasks, notes, and reset localStorage?')) {
                      StorageService.clearAll();
                      setItems([]);
                      setProjects([]);
                      setSchedule([]);
                      showToast('All data cleared.');
                    }
                  }}
                  className="flex items-center space-x-2 px-4 py-2.5 rounded-xl bg-rose-urgent/20 hover:bg-rose-urgent text-rose-urgent hover:text-white text-xs font-semibold transition-all border border-rose-urgent/30"
                >
                  <Trash2 className="w-4 h-4" />
                  <span>Reset All Data</span>
                </button>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Modals */}
      <QuickCaptureModal
        isOpen={isQuickCaptureOpen}
        onClose={() => setIsQuickCaptureOpen(false)}
        onSaveItems={handleSaveParsedItems}
      />

      <PlanMyDayModal
        isOpen={isPlanMyDayOpen}
        onClose={() => setIsPlanMyDayOpen(false)}
        schedule={schedule}
        preferences={preferences}
        onRebalance={handleRebalanceSchedule}
        onAcceptSchedule={handleAcceptSchedule}
      />

      <GoalBreakdownModal
        isOpen={isGoalBreakdownOpen}
        onClose={() => setIsGoalBreakdownOpen(false)}
        onCreateGoal={(title, milestones) => {
          const now = Date.now();
          const pId = `proj-${now}`;
          const newProj: Project = {
            id: pId,
            title,
            description: 'AI decomposed milestone roadmap',
            color: '#6366f1',
            priority: 'HIGH',
            createdAt: now
          };
          updateProjects([newProj, ...projects]);

          const newTasks: QuickPilotItem[] = milestones.map((step, idx) => ({
            id: `task-${now}-${idx}`,
            title: step,
            description: `Milestone for project: ${title}`,
            type: 'TASK',
            category: 'Work',
            priority: 'NORMAL',
            dueDate: null,
            timeString: null,
            durationMinutes: 45,
            isCompleted: false,
            completedAt: null,
            isPinned: false,
            isArchived: false,
            projectId: pId,
            checklist: [],
            createdAt: now,
            updatedAt: now
          }));

          updateItems([...newTasks, ...items]);
          showToast(`Created project with ${milestones.length} milestones!`);
        }}
      />

      {/* New Note Dialog */}
      {isCreateNoteOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-lg p-5 space-y-4">
            <h3 className="text-lg font-bold text-white">Create Note</h3>
            <input
              type="text"
              placeholder="Note title (e.g. Wi-Fi bill ₹799 / Team sync)"
              value={noteTitle}
              onChange={e => setNoteTitle(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-violet-primary"
            />
            <textarea
              rows={4}
              placeholder="Note content, bullet points, or thoughts..."
              value={noteContent}
              onChange={e => setNoteContent(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl p-4 text-sm text-white focus:outline-none focus:border-violet-primary resize-none"
            />
            <div className="flex justify-end space-x-2">
              <button
                onClick={() => setIsCreateNoteOpen(false)}
                className="px-4 py-2 text-xs font-semibold text-slate-400 hover:text-white"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  if (!noteTitle.trim()) return;
                  const now = Date.now();
                  const noteItem: QuickPilotItem = {
                    id: `note-${now}`,
                    title: noteTitle.trim(),
                    description: noteContent.trim(),
                    type: 'NOTE',
                    category: noteCategory,
                    priority: 'NORMAL',
                    dueDate: null,
                    timeString: null,
                    durationMinutes: 15,
                    isCompleted: false,
                    completedAt: null,
                    isPinned: false,
                    isArchived: false,
                    projectId: null,
                    checklist: [],
                    createdAt: now,
                    updatedAt: now
                  };
                  updateItems([noteItem, ...items]);
                  setNoteTitle('');
                  setNoteContent('');
                  setIsCreateNoteOpen(false);
                  showToast('Note saved!');
                }}
                className="px-4 py-2 rounded-xl bg-violet-primary hover:bg-violet-light text-white text-xs font-semibold"
              >
                Save Note
              </button>
            </div>
          </div>
        </div>
      )}

      {/* New Project Dialog */}
      {isCreateProjectOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-lg p-5 space-y-4">
            <h3 className="text-lg font-bold text-white">New Project</h3>
            <input
              type="text"
              placeholder="Project title (e.g. Personal Website Launch)"
              value={projectTitle}
              onChange={e => setProjectTitle(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-violet-primary"
            />
            <input
              type="text"
              placeholder="Description"
              value={projectDesc}
              onChange={e => setProjectDesc(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-violet-primary"
            />
            <div className="flex justify-end space-x-2">
              <button
                onClick={() => setIsCreateProjectOpen(false)}
                className="px-4 py-2 text-xs font-semibold text-slate-400 hover:text-white"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  if (!projectTitle.trim()) return;
                  const now = Date.now();
                  const newProj: Project = {
                    id: `proj-${now}`,
                    title: projectTitle.trim(),
                    description: projectDesc.trim(),
                    color: '#6366f1',
                    priority: 'NORMAL',
                    createdAt: now
                  };
                  updateProjects([...projects, newProj]);
                  setProjectTitle('');
                  setProjectDesc('');
                  setIsCreateProjectOpen(false);
                  showToast('Project created!');
                }}
                className="px-4 py-2 rounded-xl bg-violet-primary hover:bg-violet-light text-white text-xs font-semibold"
              >
                Create
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default App;
