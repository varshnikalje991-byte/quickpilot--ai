export type ItemType = 
  | 'TASK' 
  | 'REMINDER' 
  | 'NOTE' 
  | 'EVENT' 
  | 'SHOPPING' 
  | 'IDEA' 
  | 'FOLLOW_UP';

export type Priority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';

export interface ChecklistItem {
  id: string;
  text: string;
  isDone: boolean;
}

export interface QuickPilotItem {
  id: string;
  title: string;
  description: string;
  type: ItemType;
  category: string;
  priority: Priority;
  dueDate: string | null; // ISO Date string (YYYY-MM-DD)
  timeString: string | null; // e.g. "06:00 PM"
  durationMinutes: number;
  isCompleted: boolean;
  completedAt: number | null;
  isPinned: boolean;
  isArchived: boolean;
  projectId: string | null;
  checklist: ChecklistItem[];
  clarificationQuestion?: string | null;
  scheduledTimeBlock?: string | null;
  createdAt: number;
  updatedAt: number;
}

export interface Project {
  id: string;
  title: string;
  description: string;
  color: string;
  priority: Priority;
  createdAt: number;
}

export interface ScheduleBlock {
  id: string;
  timeSlot: string; // e.g. "09:00 AM - 09:30 AM"
  title: string;
  durationMinutes: number;
  isBreak: boolean;
  itemId?: string;
  category: string;
}

export interface UserPreferences {
  userName: string;
  timezone: string;
  language: string;
  wakingHour: number;
  workStartHour: number;
  workEndHour: number;
  breakIntervalMinutes: number;
  breakDurationMinutes: number;
  planningStyle: 'Balanced' | 'Focused' | 'Relaxed';
  reminderBehavior: 'Gentle' | 'Standard' | 'Persistent';
  isPro: boolean;
}

export type AppTab = 
  | 'dashboard' 
  | 'inbox' 
  | 'today' 
  | 'calendar' 
  | 'notes' 
  | 'projects' 
  | 'search' 
  | 'settings';
