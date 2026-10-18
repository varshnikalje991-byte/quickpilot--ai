import { QuickPilotItem, Project, UserPreferences, ScheduleBlock } from './types';

const STORAGE_KEYS = {
  ITEMS: 'quickpilot_items',
  PROJECTS: 'quickpilot_projects',
  PREFERENCES: 'quickpilot_preferences',
  SCHEDULE: 'quickpilot_schedule'
};

const DEFAULT_PREFERENCES: UserPreferences = {
  userName: 'Pilot',
  timezone: Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC',
  language: 'English',
  wakingHour: 7,
  workStartHour: 9,
  workEndHour: 18,
  breakIntervalMinutes: 90,
  breakDurationMinutes: 15,
  planningStyle: 'Balanced',
  reminderBehavior: 'Standard',
  isPro: true
};

function getSeedData(): { items: QuickPilotItem[]; projects: Project[] } {
  const now = Date.now();
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const tomorrowStr = tomorrow.toISOString().split('T')[0];
  const todayStr = new Date().toISOString().split('T')[0];

  const projects: Project[] = [
    {
      id: 'proj-1',
      title: 'College & Academics',
      description: 'Assignments, exam prep, and research milestones',
      color: '#6366f1',
      priority: 'HIGH',
      createdAt: now - 86400000
    },
    {
      id: 'proj-2',
      title: 'Personal Website Launch',
      description: 'Portfolio redesign and project write-ups',
      color: '#06b6d4',
      priority: 'NORMAL',
      createdAt: now - 172800000
    }
  ];

  const items: QuickPilotItem[] = [
    {
      id: 'item-1',
      title: 'Finish physics assignment',
      description: 'Solve mechanics questions 4 to 12 and verify diagrams',
      type: 'TASK',
      category: 'Study',
      priority: 'HIGH',
      dueDate: tomorrowStr,
      timeString: '06:00 PM',
      durationMinutes: 60,
      isCompleted: false,
      completedAt: null,
      isPinned: true,
      isArchived: false,
      projectId: 'proj-1',
      checklist: [],
      scheduledTimeBlock: '09:30 AM - 10:30 AM',
      createdAt: now - 3600000,
      updatedAt: now - 3600000
    },
    {
      id: 'item-2',
      title: 'Buy fresh groceries & almond milk',
      description: 'After college stop by organic market',
      type: 'SHOPPING',
      category: 'Home',
      priority: 'NORMAL',
      dueDate: todayStr,
      timeString: 'Evening',
      durationMinutes: 20,
      isCompleted: false,
      completedAt: null,
      isPinned: false,
      isArchived: false,
      projectId: null,
      checklist: [
        { id: 'c1', text: 'Almond milk', isDone: false },
        { id: 'c2', text: 'Whole wheat bread', isDone: false },
        { id: 'c3', text: 'Eggs & bananas', isDone: true }
      ],
      createdAt: now - 7200000,
      updatedAt: now - 7200000
    },
    {
      id: 'item-3',
      title: 'Call Rahul to discuss presentation',
      description: 'Clarify who presents the intro and architecture slides',
      type: 'REMINDER',
      category: 'Personal',
      priority: 'NORMAL',
      dueDate: tomorrowStr,
      timeString: '11:00 AM',
      durationMinutes: 15,
      isCompleted: false,
      completedAt: null,
      isPinned: false,
      isArchived: false,
      projectId: null,
      checklist: [],
      clarificationQuestion: 'Proposed time: 11:00 AM on Sunday. Tap to reschedule.',
      createdAt: now - 10800000,
      updatedAt: now - 10800000
    },
    {
      id: 'item-4',
      title: 'Wi-Fi monthly fiber bill is ₹799',
      description: 'Account #492817 — Due on the 25th of every month via UPI',
      type: 'NOTE',
      category: 'Finance',
      priority: 'NORMAL',
      dueDate: null,
      timeString: null,
      durationMinutes: 10,
      isCompleted: false,
      completedAt: null,
      isPinned: true,
      isArchived: false,
      projectId: null,
      checklist: [],
      createdAt: now - 14400000,
      updatedAt: now - 14400000
    },
    {
      id: 'item-5',
      title: 'Launch portfolio website checklist',
      description: 'Key steps required before sharing domain on LinkedIn',
      type: 'TASK',
      category: 'Work',
      priority: 'NORMAL',
      dueDate: null,
      timeString: null,
      durationMinutes: 90,
      isCompleted: false,
      completedAt: null,
      isPinned: false,
      isArchived: false,
      projectId: 'proj-2',
      checklist: [
        { id: 'w1', text: 'Select custom domain name', isDone: true },
        { id: 'w2', text: 'Design responsive homepage & hero', isDone: true },
        { id: 'w3', text: 'Add project case studies & demo links', isDone: false },
        { id: 'w4', text: 'Cross-browser testing on mobile', isDone: false },
        { id: 'w5', text: 'Publish and announce on social', isDone: false }
      ],
      createdAt: now - 18000000,
      updatedAt: now - 18000000
    },
    {
      id: 'item-6',
      title: 'Idea: Smart voice-to-schedule sync widget',
      description: 'One-tap voice capture while commuting with automatic calendar time-blocking',
      type: 'IDEA',
      category: 'Work',
      priority: 'LOW',
      dueDate: null,
      timeString: null,
      durationMinutes: 10,
      isCompleted: false,
      completedAt: null,
      isPinned: false,
      isArchived: false,
      projectId: null,
      checklist: [],
      createdAt: now - 21600000,
      updatedAt: now - 21600000
    }
  ];

  return { items, projects };
}

export const StorageService = {
  loadItems(): QuickPilotItem[] {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.ITEMS);
      if (data) {
        return JSON.parse(data);
      }
      const seed = getSeedData();
      localStorage.setItem(STORAGE_KEYS.ITEMS, JSON.stringify(seed.items));
      localStorage.setItem(STORAGE_KEYS.PROJECTS, JSON.stringify(seed.projects));
      return seed.items;
    } catch {
      return getSeedData().items;
    }
  },

  saveItems(items: QuickPilotItem[]): void {
    try {
      localStorage.setItem(STORAGE_KEYS.ITEMS, JSON.stringify(items));
    } catch (e) {
      console.error('Error saving items to localStorage', e);
    }
  },

  loadProjects(): Project[] {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.PROJECTS);
      if (data) return JSON.parse(data);
      const seed = getSeedData();
      return seed.projects;
    } catch {
      return getSeedData().projects;
    }
  },

  saveProjects(projects: Project[]): void {
    try {
      localStorage.setItem(STORAGE_KEYS.PROJECTS, JSON.stringify(projects));
    } catch (e) {
      console.error('Error saving projects to localStorage', e);
    }
  },

  loadPreferences(): UserPreferences {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.PREFERENCES);
      return data ? { ...DEFAULT_PREFERENCES, ...JSON.parse(data) } : DEFAULT_PREFERENCES;
    } catch {
      return DEFAULT_PREFERENCES;
    }
  },

  savePreferences(prefs: UserPreferences): void {
    try {
      localStorage.setItem(STORAGE_KEYS.PREFERENCES, JSON.stringify(prefs));
    } catch (e) {
      console.error('Error saving preferences to localStorage', e);
    }
  },

  loadSchedule(): ScheduleBlock[] {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.SCHEDULE);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  },

  saveSchedule(blocks: ScheduleBlock[]): void {
    try {
      localStorage.setItem(STORAGE_KEYS.SCHEDULE, JSON.stringify(blocks));
    } catch (e) {
      console.error('Error saving schedule to localStorage', e);
    }
  },

  clearAll(): void {
    localStorage.removeItem(STORAGE_KEYS.ITEMS);
    localStorage.removeItem(STORAGE_KEYS.PROJECTS);
    localStorage.removeItem(STORAGE_KEYS.SCHEDULE);
  }
};
