import { ItemType, Priority, QuickPilotItem, ScheduleBlock, UserPreferences } from './types';

export interface ParsedItemCandidate {
  title: string;
  description: string;
  type: ItemType;
  category: string;
  priority: Priority;
  dueDate: string | null;
  timeString: string | null;
  durationMinutes: number;
  checklist: string[];
  clarificationQuestion?: string | null;
}

export function parseNaturalLanguageInput(rawInput: string): ParsedItemCandidate[] {
  const trimmed = rawInput.trim();
  if (!trimmed) return [];

  // 1. Check for shopping list (e.g. "Milk, bread, eggs")
  if (isLikelyShoppingList(trimmed)) {
    const listItems = trimmed
      .split(/[,;\n]+|\band\b/i)
      .map(s => s.trim().replace(/^[-*•]\s*/, ''))
      .filter(s => s.length > 0);

    if (listItems.length >= 2) {
      const firstUpper = listItems[0].charAt(0).toUpperCase() + listItems[0].slice(1);
      return [
        {
          title: `Shopping: ${firstUpper} & items`,
          description: listItems.join(', '),
          type: 'SHOPPING',
          category: 'Home',
          priority: 'NORMAL',
          dueDate: getTodayDateString(),
          timeString: 'Today',
          durationMinutes: 20,
          checklist: listItems,
          clarificationQuestion: null
        }
      ];
    }
  }

  // 2. Check for Financial / Bill Memo (e.g. "Remember that my Wi-Fi bill is ₹799")
  if (/(?:₹|\$|€|£|INR|USD|bill|fee|rent|subscription)\s*\d+/i.test(trimmed)) {
    const cleanTitle = trimmed
      .replace(/^(remember that|note that|note:|remember:|remind me that)\s*/i, '')
      .trim();
    return [
      {
        title: cleanTitle.charAt(0).toUpperCase() + cleanTitle.slice(1),
        description: 'Captured financial reminder / note',
        type: 'NOTE',
        category: 'Finance',
        priority: 'NORMAL',
        dueDate: null,
        timeString: null,
        durationMinutes: 15,
        checklist: [],
        clarificationQuestion: 'Would you like to schedule a monthly recurring payment reminder?'
      }
    ];
  }

  // 3. Multi-item input segmentation:
  // e.g. "Remind me to finish my physics assignment tomorrow at 6 PM, buy milk after college, and call Rahul on Sunday."
  const clauses = splitCompoundInput(trimmed);
  return clauses.map(clause => parseSingleClause(clause));
}

function isLikelyShoppingList(text: string): boolean {
  const lower = text.toLowerCase();
  const foodKeywords = ['milk', 'bread', 'eggs', 'butter', 'cheese', 'apples', 'bananas', 'rice', 'coffee', 'sugar', 'groceries', 'vegetables', 'chicken', 'flour', 'oil', 'snacks'];
  const matchCount = foodKeywords.filter(k => lower.includes(k)).length;
  const hasPunctuation = text.includes(',') || text.includes('\n');
  return (matchCount >= 2) || (matchCount >= 1 && hasPunctuation && !lower.includes('tomorrow') && !lower.includes('remind me'));
}

function splitCompoundInput(text: string): string[] {
  const splitRegex = /(?:;\s*)|(?:,\s*and\s+)|(?:\s+and\s+(?=(?:remind|call|buy|check|finish|send|meet|review|schedule|pay)\b))|(?:,\s*(?=(?:buy|call|pick up|meet|finish|remember|check|send|pay)\b))/i;
  const parts = text.split(splitRegex)
    .map(p => p.trim().replace(/\.$/, ''))
    .filter(p => p.length > 3);

  return parts.length >= 2 ? parts : [text];
}

function parseSingleClause(clause: string): ParsedItemCandidate {
  let text = clause.trim();
  const lower = text.toLowerCase();

  // Detect Type
  let type: ItemType = 'TASK';
  if (lower.startsWith('remind') || lower.includes('call ') || lower.includes('meet ')) {
    type = 'REMINDER';
  } else if (lower.startsWith('buy ') || lower.startsWith('groceries') || lower.includes('milk') || lower.includes('market')) {
    type = 'SHOPPING';
  } else if (lower.startsWith('idea:') || lower.startsWith('thought:')) {
    type = 'IDEA';
  } else if (lower.startsWith('note:') || lower.includes('note that') || lower.includes('password') || lower.includes('code')) {
    type = 'NOTE';
  } else if (lower.includes('meeting') || lower.includes('doctor') || lower.includes('appointment')) {
    type = 'EVENT';
  } else if (lower.startsWith('follow up') || lower.includes('follow-up')) {
    type = 'FOLLOW_UP';
  }

  // Detect Category
  let category = 'General';
  if (/physics|chemistry|college|assignment|study|exam|homework/i.test(lower)) {
    category = 'Study';
  } else if (/milk|groceries|clean|apartment|house|room/i.test(lower)) {
    category = 'Home';
  } else if (/rahul|mom|dad|friend|party|call /i.test(lower)) {
    category = 'Personal';
  } else if (/bill|pay|tax|bank|₹|\$/i.test(lower)) {
    category = 'Finance';
  } else if (/workout|gym|run|medicine|doctor|health/i.test(lower)) {
    category = 'Health';
  } else if (/client|presentation|deploy|website|code|bug|project/i.test(lower)) {
    category = 'Work';
  }

  // Detect Priority
  let priority: Priority = 'NORMAL';
  if (/urgent|asap|immediately/i.test(lower)) {
    priority = 'URGENT';
  } else if (/important|high priority|exam|assignment/i.test(lower)) {
    priority = 'HIGH';
  } else if (/someday|maybe|low priority/i.test(lower)) {
    priority = 'LOW';
  }

  // Duration
  let durationMinutes = 30;
  const durMatch = text.match(/for\s+(\d+)\s*(min|minute|minutes|hour|hours|hr|hrs)?/i);
  if (durMatch) {
    const num = parseInt(durMatch[1], 10) || 30;
    const unit = (durMatch[2] || 'min').toLowerCase();
    durationMinutes = unit.startsWith('h') ? num * 60 : num;
    text = text.replace(durMatch[0], '').trim();
  }

  // Date and Time
  let dueDate: string | null = null;
  let timeString: string | null = null;
  let clarificationQuestion: string | null = null;

  const now = new Date();

  if (lower.includes('tomorrow')) {
    const tomorrow = new Date(now);
    tomorrow.setDate(tomorrow.getDate() + 1);
    dueDate = tomorrow.toISOString().split('T')[0];
  } else if (lower.includes('sunday')) {
    dueDate = getNextDayOfWeekDateString(0); // 0 is Sunday
    timeString = '11:00 AM';
    clarificationQuestion = 'Scheduled for Sunday. Proposed time 11:00 AM. Tap to reschedule if desired.';
  } else if (lower.includes('monday')) {
    dueDate = getNextDayOfWeekDateString(1);
    timeString = '10:00 AM';
  } else if (lower.includes('friday')) {
    dueDate = getNextDayOfWeekDateString(5);
    timeString = '05:00 PM';
  } else if (lower.includes('after college') || lower.includes('after work') || lower.includes('after school')) {
    dueDate = getTodayDateString();
    timeString = '05:00 PM';
    clarificationQuestion = 'Assumed after college schedule (5:00 PM). Confirm or adjust time.';
  } else if (lower.includes('tonight')) {
    dueDate = getTodayDateString();
    timeString = '08:00 PM';
  }

  // Specific Time Detection (e.g. "at 6 PM", "at 6:30 PM", "morning", "evening")
  const timeMatch = text.match(/at\s+(\d{1,2})(?::(\d{2}))?\s*(am|pm)?/i);
  if (timeMatch) {
    let hour = parseInt(timeMatch[1], 10);
    const min = timeMatch[2] || '00';
    let ampm = (timeMatch[3] || (hour < 8 ? 'PM' : 'AM')).toUpperCase();
    if (hour > 12) {
      hour -= 12;
      ampm = 'PM';
    }
    timeString = `${hour.toString().padStart(2, '0')}:${min} ${ampm}`;
  } else if (lower.includes('morning')) {
    if (!timeString) timeString = '09:00 AM';
  } else if (lower.includes('afternoon')) {
    if (!timeString) timeString = '02:00 PM';
  } else if (lower.includes('evening')) {
    if (!timeString) timeString = '06:30 PM';
  }

  // Clean title
  let title = text
    .replace(/^(remind me to|remember to|don't forget to|make sure to|please)\s*/i, '')
    .replace(/\b(tomorrow|morning|evening|afternoon|after college|after work|on sunday|on monday)\b/gi, '')
    .replace(/\bat\s+\d{1,2}(?::\d{2})?\s*(?:am|pm)?\b/gi, '')
    .trim();

  if (!title) {
    title = clause.trim();
  }
  title = title.charAt(0).toUpperCase() + title.slice(1);

  return {
    title,
    description: clause.trim(),
    type,
    category,
    priority,
    dueDate,
    timeString,
    durationMinutes,
    checklist: [],
    clarificationQuestion
  };
}

function getTodayDateString(): string {
  return new Date().toISOString().split('T')[0];
}

function getNextDayOfWeekDateString(targetDay: number): string {
  const d = new Date();
  const currentDay = d.getDay();
  let daysToAdd = targetDay - currentDay;
  if (daysToAdd <= 0) daysToAdd += 7;
  d.setDate(d.getDate() + daysToAdd);
  return d.toISOString().split('T')[0];
}

export function generateDailyPlanSchedule(
  tasks: QuickPilotItem[],
  preferences: UserPreferences
): ScheduleBlock[] {
  const schedule: ScheduleBlock[] = [];
  let currentHour = preferences.workStartHour;
  let currentMinute = 0;
  const endHour = preferences.workEndHour;

  // Filter pending tasks and sort by Priority & due
  const sorted = tasks
    .filter(t => !t.isCompleted && t.type === 'TASK')
    .sort((a, b) => {
      const pMap: Record<Priority, number> = { URGENT: 4, HIGH: 3, NORMAL: 2, LOW: 1 };
      return pMap[b.priority] - pMap[a.priority];
    });

  let minutesSinceLastBreak = 0;

  for (const task of sorted) {
    const totalMinutesNow = currentHour * 60 + currentMinute;
    const totalEndMinutes = endHour * 60;
    if (totalMinutesNow >= totalEndMinutes) break;

    // Mindful break check
    if (minutesSinceLastBreak >= preferences.breakIntervalMinutes) {
      const breakStart = formatTime(currentHour, currentMinute);
      const breakDuration = preferences.breakDurationMinutes;
      const [nextH, nextM] = addMinutes(currentHour, currentMinute, breakDuration);
      const breakEnd = formatTime(nextH, nextM);

      schedule.push({
        id: `break-${Date.now()}-${Math.random()}`,
        timeSlot: `${breakStart} - ${breakEnd}`,
        title: 'Mindful Break & Hydration',
        durationMinutes: breakDuration,
        isBreak: true,
        category: 'Health'
      });

      currentHour = nextH;
      currentMinute = nextM;
      minutesSinceLastBreak = 0;
    }

    const duration = Math.min(Math.max(task.durationMinutes || 30, 15), 90);
    const slotStart = formatTime(currentHour, currentMinute);
    const [endH, endM] = addMinutes(currentHour, currentMinute, duration);
    const slotEnd = formatTime(endH, endM);

    schedule.push({
      id: `task-${task.id}`,
      timeSlot: `${slotStart} - ${slotEnd}`,
      title: task.title,
      durationMinutes: duration,
      isBreak: false,
      itemId: task.id,
      category: task.category
    });

    currentHour = endH;
    currentMinute = endM;
    minutesSinceLastBreak += duration;
  }

  return schedule;
}

function addMinutes(hour: Int, min: Int, add: Int): [number, number] {
  const total = hour * 60 + min + add;
  const newH = Math.floor(total / 60) % 24;
  const newM = total % 60;
  return [newH, newM];
}

type Int = number;

function formatTime(hour: number, minute: number): string {
  const ampm = hour >= 12 ? 'PM' : 'AM';
  const h12 = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour;
  return `${h12.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} ${ampm}`;
}

export function breakDownGoalIntoMilestones(goalTitle: string): string[] {
  const lower = goalTitle.toLowerCase();
  if (/website|app|portfolio|saas/i.test(lower)) {
    return [
      'Choose and register custom domain name',
      'Design responsive homepage layout and hero section',
      'Add core project case studies and contact form',
      'Test cross-device responsiveness and load speed',
      'Publish live site and announce to network'
    ];
  }
  if (/exam|study|physics|assignment|chemistry/i.test(lower)) {
    return [
      'Review syllabus and prioritize hardest chapters',
      'Summarize key formulas, diagrams, and definitions',
      'Solve past 3 years question papers under timed conditions',
      'Discuss doubt questions with professor or study group',
      'Final revision flashcard session before exam day'
    ];
  }
  if (/fitness|workout|gym|health/i.test(lower)) {
    return [
      'Define weekly workout schedule (Mon/Wed/Fri)',
      'Prepare balanced high-protein grocery meal plan',
      'Set up gym clothes and water bottle night before',
      'Track daily sets, reps, and hydration in app',
      'Review weekly progress and reward consistency'
    ];
  }
  return [
    `Outline scope and define required milestones for '${goalTitle}'`,
    'Gather necessary tools, resources, and reference materials',
    'Complete initial draft or prototype step',
    'Review quality and test with real-world feedback',
    'Finalize and mark goal successfully achieved'
  ];
}

export function extractActionsFromNoteText(title: string, content: string): string[] {
  const actions: string[] = [];
  const lines = content.split('\n');
  const actionVerbs = ['review', 'call', 'send', 'email', 'fix', 'buy', 'write', 'finish', 'submit', 'prepare', 'schedule', 'ask', 'follow up'];

  for (const line of lines) {
    const trimmed = line.trim().replace(/^[-*•]\s*/, '').trim();
    if (trimmed.length > 5) {
      const firstWord = trimmed.split(' ')[0].toLowerCase();
      if (actionVerbs.includes(firstWord) || /TODO|\[ \]/i.test(line)) {
        actions.push(trimmed.replace(/^TODO:?\s*|\[ \]\s*/i, '').trim());
      }
    }
  }

  if (actions.length === 0 && content.trim().length > 0) {
    actions.push(`Review and organize action items for '${title}'`);
  }

  return Array.from(new Set(actions));
}
