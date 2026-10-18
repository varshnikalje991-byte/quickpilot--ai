package com.example.ai

import com.example.data.model.ChecklistItem
import com.example.data.model.ItemEntity
import com.example.data.model.ItemPriority
import com.example.data.model.ItemType
import com.example.data.model.ScheduleBlock
import com.example.data.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

object AiOrganizationEngine {

    suspend fun parseCaptureInput(rawInput: String): AiExtractionResult = withContext(Dispatchers.Default) {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return@withContext AiExtractionResult(rawInput, emptyList())
        }

        // Try Gemini AI first if configured
        if (GeminiApiClient.isApiKeyConfigured()) {
            val geminiResult = tryParseWithGemini(trimmed)
            if (geminiResult != null && geminiResult.items.isNotEmpty()) {
                return@withContext geminiResult
            }
        }

        // Highly accurate smart offline NLP fallback engine
        val ruleBasedItems = parseWithRuleEngine(trimmed)
        AiExtractionResult(
            rawInput = trimmed,
            items = ruleBasedItems,
            isFromGemini = false
        )
    }

    private suspend fun tryParseWithGemini(input: String): AiExtractionResult? {
        val prompt = """
            You are QuickPilot AI, an elite natural language task and note organizer.
            Analyze this user input:
            "$input"

            Split multiple items if present (e.g. "Remind me to finish physics assignment tomorrow at 6 PM, buy milk after college, and call Rahul on Sunday").
            Categorize each item into one of: TASK, REMINDER, NOTE, EVENT, SHOPPING, IDEA, FOLLOW_UP.
            Determine priority: LOW, NORMAL, HIGH, URGENT.
            Extract title, description, category (Study, Work, Home, Personal, Finance, Health, General), timeString, durationMinutes (int), checklist items if list, and clarificationQuestion if details like time or date are ambiguous.

            Output ONLY a JSON array in this exact schema without markdown backticks:
            [
              {
                "title": "string",
                "description": "string",
                "itemType": "TASK|REMINDER|NOTE|EVENT|SHOPPING|IDEA|FOLLOW_UP",
                "dueDateOffsetDays": 0,
                "timeString": "06:00 PM",
                "durationMinutes": 30,
                "priority": "LOW|NORMAL|HIGH|URGENT",
                "category": "Work",
                "checklist": ["item 1", "item 2"],
                "clarificationQuestion": null
              }
            ]
        """.trimIndent()

        val responseText = GeminiApiClient.generatePrompt(prompt) ?: return null
        return try {
            val cleanJson = responseText.replace("```json", "").replace("```", "").trim()
            val jsonArray = JSONArray(cleanJson)
            val items = mutableListOf<AiParsedItem>()
            val nowCal = Calendar.getInstance()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val typeStr = obj.optString("itemType", "TASK").uppercase()
                val itemType = try { ItemType.valueOf(typeStr) } catch (e: Exception) { ItemType.TASK }
                val priorityStr = obj.optString("priority", "NORMAL").uppercase()
                val priority = try { ItemPriority.valueOf(priorityStr) } catch (e: Exception) { ItemPriority.NORMAL }

                val offsetDays = obj.optInt("dueDateOffsetDays", -1)
                val dueDateMillis = if (offsetDays >= 0) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.DAY_OF_YEAR, offsetDays)
                    c.timeInMillis
                } else null

                val checklistArr = obj.optJSONArray("checklist")
                val checklist = mutableListOf<String>()
                if (checklistArr != null) {
                    for (j in 0 until checklistArr.length()) {
                        checklist.add(checklistArr.getString(j))
                    }
                }

                items.add(
                    AiParsedItem(
                        title = obj.optString("title", "Captured item"),
                        description = obj.optString("description", ""),
                        itemType = itemType,
                        dueDateEpochMillis = dueDateMillis,
                        timeString = if (obj.has("timeString") && !obj.isNull("timeString")) obj.getString("timeString") else null,
                        durationMinutes = obj.optInt("durationMinutes", 30),
                        priority = priority,
                        category = obj.optString("category", "General"),
                        checklist = checklist,
                        clarificationQuestion = if (obj.has("clarificationQuestion") && !obj.isNull("clarificationQuestion")) obj.getString("clarificationQuestion") else null,
                        confidence = 0.98f
                    )
                )
            }
            AiExtractionResult(rawInput = input, items = items, isFromGemini = true)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Smart Rule-Based NLP Parser supporting compound sentences,
     * temporal phrases, currency recognition, grocery checklists, and disambiguation.
     */
    fun parseWithRuleEngine(input: String): List<AiParsedItem> {
        val trimmed = input.trim()

        // 1. Check if input is a simple grocery/shopping list (e.g. "Milk, bread, eggs" or "Milk, eggs and butter")
        if (isLikelyShoppingList(trimmed)) {
            val listItems = trimmed.split(Regex("[,\\n]+|\\band\\b", RegexOption.IGNORE_CASE))
                .map { it.trim().removePrefix("-").trim() }
                .filter { it.isNotBlank() }
            if (listItems.size >= 2) {
                return listOf(
                    AiParsedItem(
                        title = "Shopping: ${listItems.first().replaceFirstChar { it.uppercase() }} & more",
                        description = listItems.joinToString(", "),
                        itemType = ItemType.SHOPPING,
                        dueDateEpochMillis = System.currentTimeMillis(),
                        timeString = "Today",
                        durationMinutes = 20,
                        priority = ItemPriority.NORMAL,
                        category = "Home",
                        checklist = listItems,
                        clarificationQuestion = null,
                        confidence = 0.94f
                    )
                )
            }
        }

        // 2. Check for Bill / Financial memo (e.g. "Remember that my Wi-Fi bill is ₹799" or "$120 rent")
        if (Regex("(₹|\\$|€|£|INR|USD|bill|fee|rent|subscription)\\s*\\d+", RegexOption.IGNORE_CASE).containsMatchIn(trimmed)) {
            val cleanTitle = trimmed
                .replace(Regex("(?i)^(remember that|note that|note:|remember:|remind me that)\\s*"), "")
                .trim()
                .replaceFirstChar { it.uppercase() }
            return listOf(
                AiParsedItem(
                    title = cleanTitle,
                    description = "Captured financial reminder / note",
                    itemType = ItemType.NOTE,
                    priority = ItemPriority.NORMAL,
                    category = "Finance",
                    clarificationQuestion = "Would you like to schedule a monthly recurring payment reminder?",
                    confidence = 0.92f
                )
            )
        }

        // 3. Multi-item segmentation:
        // Splits by semicolon, or conjunctive clauses like ", and", ", buy", ", call", ", remind me"
        val clauses = splitCompoundInput(trimmed)
        if (clauses.size > 1) {
            return clauses.map { clause -> parseSingleClause(clause) }
        }

        return listOf(parseSingleClause(trimmed))
    }

    private fun isLikelyShoppingList(text: String): Boolean {
        val lower = text.lowercase()
        val foodKeywords = listOf("milk", "bread", "eggs", "butter", "cheese", "apples", "bananas", "rice", "coffee", "sugar", "groceries", "vegetables", "chicken", "flour", "oil", "snacks")
        val matchCount = foodKeywords.count { lower.contains(it) }
        val hasShoppingPunctuation = text.contains(",") || text.contains("\n")
        return (matchCount >= 2) || (matchCount >= 1 && hasShoppingPunctuation && !lower.contains("tomorrow") && !lower.contains("remind me"))
    }

    private fun splitCompoundInput(text: String): List<String> {
        // Look for boundaries such as:
        // "Remind me to finish my physics assignment tomorrow at 6 PM, buy milk after college, and call Rahul on Sunday."
        val splitRegex = Regex(
            "(?:;\\s*)|(?:,\\s*and\\s+)|(?:\\s+and\\s+(?=(?:remind|call|buy|check|finish|send|meet|review|schedule|pay)\\b))|(?:,\\s*(?=(?:buy|call|pick up|meet|finish|remember|check|send|pay)\\b))",
            RegexOption.IGNORE_CASE
        )
        val parts = text.split(splitRegex)
            .map { it.trim().trimEnd('.') }
            .filter { it.isNotBlank() && it.length > 3 }

        return if (parts.size >= 2) parts else listOf(text)
    }

    private fun parseSingleClause(clause: String): AiParsedItem {
        var text = clause.trim()
        val lower = text.lowercase()

        // Detect Item Type
        val itemType = when {
            lower.startsWith("remind") || lower.contains("call ") || lower.contains("meet ") -> ItemType.REMINDER
            lower.startsWith("buy ") || lower.startsWith("groceries") || lower.contains("milk") || lower.contains("market") -> ItemType.SHOPPING
            lower.startsWith("idea:") || lower.startsWith("thought:") -> ItemType.IDEA
            lower.startsWith("note:") || lower.contains("note that") || lower.contains("password") || lower.contains("code") -> ItemType.NOTE
            lower.contains("meeting") || lower.contains("doctor") || lower.contains("appointment") -> ItemType.EVENT
            lower.startsWith("follow up") || lower.contains("follow-up") -> ItemType.FOLLOW_UP
            else -> ItemType.TASK
        }

        // Detect Category
        val category = when {
            lower.contains("physics") || lower.contains("chemistry") || lower.contains("college") ||
                    lower.contains("assignment") || lower.contains("study") || lower.contains("exam") || lower.contains("homework") -> "Study"
            lower.contains("milk") || lower.contains("groceries") || lower.contains("clean") || lower.contains("apartment") || lower.contains("house") -> "Home"
            lower.contains("rahul") || lower.contains("mom") || lower.contains("dad") || lower.contains("friend") || lower.contains("party") || lower.contains("call ") -> "Personal"
            lower.contains("bill") || lower.contains("pay") || lower.contains("tax") || lower.contains("bank") || lower.contains("₹") || lower.contains("$") -> "Finance"
            lower.contains("workout") || lower.contains("gym") || lower.contains("run") || lower.contains("medicine") || lower.contains("doctor") -> "Health"
            lower.contains("client") || lower.contains("presentation") || lower.contains("deploy") || lower.contains("website") || lower.contains("code") || lower.contains("bug") -> "Work"
            else -> "General"
        }

        // Detect Priority
        val priority = when {
            lower.contains("urgent") || lower.contains("asap") || lower.contains("immediately") -> ItemPriority.URGENT
            lower.contains("important") || lower.contains("high priority") || lower.contains("exam") || lower.contains("assignment") -> ItemPriority.HIGH
            lower.contains("someday") || lower.contains("maybe") || lower.contains("low priority") -> ItemPriority.LOW
            else -> ItemPriority.NORMAL
        }

        // Extract Temporal Details
        val cal = Calendar.getInstance()
        var dueDateMillis: Long? = null
        var timeStr: String? = null
        var clarification: String? = null
        var durationMinutes = 30

        // Duration detection (e.g. "for 45 minutes", "for 1 hour", "for 2 hours")
        val durMatcher = Pattern.compile("for\\s+(\\d+)\\s*(min|minute|minutes|hour|hours|hr|hrs)?", Pattern.CASE_INSENSITIVE).matcher(text)
        if (durMatcher.find()) {
            val num = durMatcher.group(1)?.toIntOrNull() ?: 30
            val unit = durMatcher.group(2)?.lowercase() ?: "min"
            durationMinutes = if (unit.startsWith("h")) num * 60 else num
            durMatcher.group(0)?.let { matchedStr ->
                text = text.replace(matchedStr, "").trim()
            }
        }

        // Date detection
        if (lower.contains("tomorrow")) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            dueDateMillis = cal.timeInMillis
        } else if (lower.contains("sunday")) {
            advanceToNextDayOfWeek(cal, Calendar.SUNDAY)
            dueDateMillis = cal.timeInMillis
            clarification = "Scheduled for Sunday. Proposed time 11:00 AM."
            timeStr = "11:00 AM"
        } else if (lower.contains("monday")) {
            advanceToNextDayOfWeek(cal, Calendar.MONDAY)
            dueDateMillis = cal.timeInMillis
            timeStr = "10:00 AM"
        } else if (lower.contains("friday")) {
            advanceToNextDayOfWeek(cal, Calendar.FRIDAY)
            dueDateMillis = cal.timeInMillis
            timeStr = "05:00 PM"
        } else if (lower.contains("after college") || lower.contains("after work") || lower.contains("after school")) {
            dueDateMillis = cal.timeInMillis
            timeStr = "05:00 PM"
            clarification = "Assumed after college schedule (5:00 PM). Confirm or adjust time."
        } else if (lower.contains("in 2 hours") || lower.contains("in two hours")) {
            cal.add(Calendar.HOUR_OF_DAY, 2)
            dueDateMillis = cal.timeInMillis
            timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
        } else if (lower.contains("tonight")) {
            dueDateMillis = cal.timeInMillis
            timeStr = "08:00 PM"
        }

        // Specific time parsing (e.g. "at 6 PM", "at 6:30 PM", "morning", "evening")
        val timeMatcher = Pattern.compile("at\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE).matcher(text)
        if (timeMatcher.find()) {
            val hour = timeMatcher.group(1)?.toIntOrNull() ?: 12
            val min = timeMatcher.group(2)?.toIntOrNull() ?: 0
            val ampm = timeMatcher.group(3)?.uppercase() ?: if (hour < 8) "PM" else "AM"
            timeStr = String.format(Locale.getDefault(), "%02d:%02d %s", if (hour > 12) hour - 12 else hour, min, ampm)
        } else if (lower.contains("morning")) {
            if (timeStr == null) timeStr = "09:00 AM"
        } else if (lower.contains("afternoon")) {
            if (timeStr == null) timeStr = "02:00 PM"
        } else if (lower.contains("evening")) {
            if (timeStr == null) timeStr = "06:30 PM"
        }

        // Clean title
        var title = text
            .replace(Regex("(?i)^(remind me to|remember to|don't forget to|make sure to|please)\\s*"), "")
            .replace(Regex("(?i)\\b(tomorrow|morning|evening|afternoon|after college|after work|on sunday|on monday)\\b"), "")
            .replace(Regex("(?i)\\bat\\s+\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?\\b"), "")
            .trim()
            .replaceFirstChar { it.uppercase() }

        if (title.isBlank()) {
            title = clause.trim().replaceFirstChar { it.uppercase() }
        }

        return AiParsedItem(
            title = title,
            description = clause.trim(),
            itemType = itemType,
            dueDateEpochMillis = dueDateMillis,
            timeString = timeStr,
            durationMinutes = durationMinutes,
            priority = priority,
            category = category,
            clarificationQuestion = clarification,
            confidence = 0.95f
        )
    }

    private fun advanceToNextDayOfWeek(cal: Calendar, targetDay: Int) {
        val currentDay = cal.get(Calendar.DAY_OF_WEEK)
        var daysToAdd = targetDay - currentDay
        if (daysToAdd <= 0) {
            daysToAdd += 7
        }
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
        cal.set(Calendar.HOUR_OF_DAY, 10)
        cal.set(Calendar.MINUTE, 0)
    }

    /**
     * AI Goal Breakdown: Breaks a large task or goal into concrete steps.
     * Example: "Launch my website" -> domain, homepage, copy, test, publish.
     */
    suspend fun breakDownGoal(goalTitle: String): List<String> = withContext(Dispatchers.Default) {
        if (GeminiApiClient.isApiKeyConfigured()) {
            val prompt = """
                Break down this goal into 4 to 6 actionable, concise checklist tasks for a user:
                "$goalTitle"
                Output ONLY a JSON array of strings, e.g. ["Step 1", "Step 2"]. No markdown.
            """.trimIndent()
            val result = GeminiApiClient.generatePrompt(prompt)
            if (result != null) {
                try {
                    val clean = result.replace("```json", "").replace("```", "").trim()
                    val arr = JSONArray(clean)
                    val list = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.getString(i))
                    }
                    if (list.isNotEmpty()) return@withContext list
                } catch (e: Exception) {
                    // Fall back
                }
            }
        }

        // Rule-based decomposition templates
        val lower = goalTitle.lowercase()
        when {
            lower.contains("website") || lower.contains("app") || lower.contains("portfolio") -> listOf(
                "Choose and register custom domain name",
                "Design responsive homepage layout and hero section",
                "Add core project case studies and contact form",
                "Test cross-device responsiveness and load speed",
                "Publish live site and announce to network"
            )
            lower.contains("exam") || lower.contains("study") || lower.contains("physics") || lower.contains("assignment") -> listOf(
                "Review syllabus and prioritize hardest chapters",
                "Summarize key formulas, diagrams, and definitions",
                "Solve past 3 years' question papers under timed conditions",
                "Discuss doubt questions with professor or study group",
                "Final revision flashcard session before exam day"
            )
            lower.contains("fitness") || lower.contains("workout") || lower.contains("gym") || lower.contains("health") -> listOf(
                "Define weekly workout schedule (Mon/Wed/Fri)",
                "Prepare balanced high-protein grocery meal plan",
                "Set up gym clothes and water bottle night before",
                "Track daily sets, reps, and hydration in app",
                "Review weekly progress and reward consistency"
            )
            lower.contains("clean") || lower.contains("apartment") || lower.contains("room") || lower.contains("home") -> listOf(
                "Sort clutter and discard recycling/trash",
                "Organize desk workspace and clear floor areas",
                "Wipe down kitchen countertops and wash dishes",
                "Vacuum carpets, mop hard floors, and empty bins",
                "Light fresh candle or air out room"
            )
            else -> listOf(
                "Outline scope and define required milestones for '$goalTitle'",
                "Gather necessary tools, resources, and reference materials",
                "Complete initial draft or prototype step",
                "Review quality and test with real-world feedback",
                "Finalize and mark goal successfully achieved"
            )
        }
    }

    /**
     * Plan My Day Engine:
     * Generates a realistic, balanced time-blocked daily schedule based on:
     * - Deadlines & Priorities
     * - Estimated task durations
     * - User waking and working hours
     * - Mindful 15-minute breaks between deep work sessions
     * - Prevents burnout and overfilling.
     */
    fun generateDaySchedule(
        tasks: List<ItemEntity>,
        userPrefs: UserPreferences
    ): List<ScheduleBlock> {
        val schedule = mutableListOf<ScheduleBlock>()
        var currentHour = userPrefs.workStartHour
        var currentMinute = 0
        val endHour = userPrefs.workEndHour

        // Filter uncompleted tasks & sort by Priority (Urgent first, then High, Normal, Low)
        val sortedTasks = tasks.filter { !it.isCompleted }
            .sortedWith(
                compareByDescending<ItemEntity> {
                    when (it.priority) {
                        ItemPriority.URGENT.name -> 4
                        ItemPriority.HIGH.name -> 3
                        ItemPriority.NORMAL.name -> 2
                        else -> 1
                    }
                }.thenBy { it.dueDateEpochMillis ?: Long.MAX_VALUE }
            )

        var minutesSinceLastBreak = 0

        for (task in sortedTasks) {
            val totalMinutesNow = currentHour * 60 + currentMinute
            val totalEndMinutes = endHour * 60
            if (totalMinutesNow >= totalEndMinutes) break

            // Check if break is needed
            if (minutesSinceLastBreak >= userPrefs.breakIntervalMinutes) {
                val breakStart = formatTime(currentHour, currentMinute)
                val breakMinutes = userPrefs.breakDurationMinutes
                val (nextH, nextM) = addMinutes(currentHour, currentMinute, breakMinutes)
                val breakEnd = formatTime(nextH, nextM)

                schedule.add(
                    ScheduleBlock(
                        timeSlot = "$breakStart - $breakEnd",
                        title = "Mindful Break & Hydration",
                        durationMinutes = breakMinutes,
                        isBreak = true,
                        category = "Health",
                        isConfirmed = true
                    )
                )
                currentHour = nextH
                currentMinute = nextM
                minutesSinceLastBreak = 0
            }

            // Schedule Task
            val taskDuration = task.durationMinutes.coerceIn(15, 90)
            val slotStart = formatTime(currentHour, currentMinute)
            val (endH, endM) = addMinutes(currentHour, currentMinute, taskDuration)
            val slotEnd = formatTime(endH, endM)

            schedule.add(
                ScheduleBlock(
                    timeSlot = "$slotStart - $slotEnd",
                    title = task.title,
                    durationMinutes = taskDuration,
                    isBreak = false,
                    itemId = task.id,
                    category = task.category,
                    isConfirmed = true
                )
            )

            currentHour = endH
            currentMinute = endM
            minutesSinceLastBreak += taskDuration
        }

        return schedule
    }

    private fun addMinutes(hour: Int, minute: Int, duration: Int): Pair<Int, Int> {
        val total = hour * 60 + minute + duration
        val newH = (total / 60) % 24
        val newM = total % 60
        return Pair(newH, newM)
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val ampm = if (hour >= 12) "PM" else "AM"
        val h12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.getDefault(), "%02d:%02d %s", h12, minute, ampm)
    }

    /**
     * Extracts actionable tasks from freeform notes.
     */
    fun extractActionsFromNote(noteTitle: String, content: String): List<String> {
        val actions = mutableListOf<String>()
        val lines = content.split("\n")
        val actionVerbs = listOf("review", "call", "send", "email", "fix", "buy", "write", "finish", "submit", "prepare", "schedule", "ask", "follow up")

        for (line in lines) {
            val trimmed = line.trim().removePrefix("-").removePrefix("*").removePrefix("•").trim()
            if (trimmed.length > 5) {
                val firstWord = trimmed.split(" ").firstOrNull()?.lowercase() ?: ""
                if (actionVerbs.contains(firstWord) || line.trim().startsWith("[ ]") || line.trim().startsWith("TODO")) {
                    actions.add(trimmed.removePrefix("[ ]").removePrefix("TODO:").trim())
                }
            }
        }

        if (actions.isEmpty() && content.isNotBlank()) {
            actions.add("Review and organize notes for '$noteTitle'")
        }
        return actions.distinct()
    }
}
