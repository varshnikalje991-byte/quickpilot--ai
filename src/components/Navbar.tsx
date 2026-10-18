import React from 'react';
import { 
  LayoutDashboard, 
  Inbox, 
  CalendarCheck2, 
  Calendar, 
  FileText, 
  FolderKanban, 
  Search, 
  Settings, 
  Plus, 
  Sparkles 
} from 'lucide-react';
import { AppTab } from '../types';

interface NavbarProps {
  currentTab: AppTab;
  onSelectTab: (tab: AppTab) => void;
  onOpenQuickCapture: () => void;
  inboxCount: number;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentTab,
  onSelectTab,
  onOpenQuickCapture,
  inboxCount
}) => {
  const navItems = [
    { id: 'dashboard' as AppTab, label: 'Dashboard', icon: LayoutDashboard },
    { id: 'inbox' as AppTab, label: 'Inbox', icon: Inbox, badge: inboxCount },
    { id: 'today' as AppTab, label: 'Today', icon: CalendarCheck2 },
    { id: 'calendar' as AppTab, label: 'Calendar', icon: Calendar },
    { id: 'notes' as AppTab, label: 'Notes', icon: FileText },
    { id: 'projects' as AppTab, label: 'Projects', icon: FolderKanban },
  ];

  return (
    <>
      {/* Top Header Bar */}
      <header className="sticky top-0 z-30 bg-slate-900/80 backdrop-blur-md border-b border-slate-800 px-4 lg:px-8 py-3.5 transition-colors">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center space-x-3 cursor-pointer" onClick={() => onSelectTab('dashboard')}>
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-violet-primary via-indigo-600 to-cyan-accent flex items-center justify-center shadow-lg shadow-violet-primary/20">
              <Sparkles className="w-5 h-5 text-white" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-bold text-lg tracking-tight text-white">QuickPilot AI</span>
                <span className="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded-full bg-violet-container/70 text-violet-light border border-violet-primary/30">
                  Web App
                </span>
              </div>
              <p className="text-xs text-slate-400 hidden sm:block">Tell your device once. It organizes everything.</p>
            </div>
          </div>

          <div className="flex items-center space-x-2 sm:space-x-3">
            {/* Quick Capture Floating Action */}
            <button
              onClick={onOpenQuickCapture}
              className="flex items-center space-x-2 bg-gradient-to-r from-violet-primary to-indigo-600 hover:from-violet-light hover:to-indigo-500 text-white font-medium px-4 py-2 rounded-xl shadow-lg shadow-violet-primary/25 active:scale-95 transition-all text-sm"
              title="Quick Capture"
            >
              <Plus className="w-4 h-4 stroke-[2.5]" />
              <span className="hidden sm:inline">Quick Capture</span>
            </button>

            <button
              onClick={() => onSelectTab('search')}
              className={`p-2.5 rounded-xl border transition-all ${
                currentTab === 'search' 
                  ? 'bg-violet-primary/20 border-violet-primary text-violet-light' 
                  : 'bg-slate-850/80 border-slate-800 text-slate-400 hover:text-slate-200 hover:bg-slate-800'
              }`}
              title="Search"
            >
              <Search className="w-4 h-4" />
            </button>

            <button
              onClick={() => onSelectTab('settings')}
              className={`p-2.5 rounded-xl border transition-all ${
                currentTab === 'settings' 
                  ? 'bg-violet-primary/20 border-violet-primary text-violet-light' 
                  : 'bg-slate-850/80 border-slate-800 text-slate-400 hover:text-slate-200 hover:bg-slate-800'
              }`}
              title="Settings"
            >
              <Settings className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      {/* Main Nav Tabs for Tablet/Desktop */}
      <nav className="hidden md:block bg-slate-900 border-b border-slate-800 px-4 lg:px-8">
        <div className="max-w-7xl mx-auto flex items-center space-x-1 py-1.5 overflow-x-auto">
          {navItems.map(item => {
            const IconComponent = item.icon;
            const isActive = currentTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => onSelectTab(item.id)}
                className={`flex items-center space-x-2 px-3.5 py-2 rounded-lg text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-violet-primary/15 text-violet-light border border-violet-primary/30'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-850'
                }`}
              >
                <IconComponent className={`w-4 h-4 ${isActive ? 'text-violet-primary' : ''}`} />
                <span>{item.label}</span>
                {item.badge !== undefined && item.badge > 0 && (
                  <span className="ml-1 text-xs px-1.5 py-0.2 rounded-full bg-violet-primary text-white font-semibold">
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </div>
      </nav>

      {/* Bottom Navigation for Mobile */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 z-30 bg-slate-900/95 backdrop-blur-md border-t border-slate-800 py-1.5 px-2">
        <div className="flex items-center justify-around">
          {navItems.slice(0, 5).map(item => {
            const IconComponent = item.icon;
            const isActive = currentTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => onSelectTab(item.id)}
                className={`flex flex-col items-center py-1 px-2.5 rounded-lg text-xs transition-colors relative ${
                  isActive ? 'text-violet-light font-semibold' : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                <IconComponent className={`w-5 h-5 mb-0.5 ${isActive ? 'text-violet-primary' : ''}`} />
                <span>{item.label}</span>
                {item.badge !== undefined && item.badge > 0 && (
                  <span className="absolute top-0.5 right-2 w-4 h-4 flex items-center justify-center rounded-full bg-violet-primary text-white text-[10px] font-bold">
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </div>
      </nav>
    </>
  );
};
