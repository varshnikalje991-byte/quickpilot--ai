package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiExtractionResult
import com.example.ai.AiOrganizationEngine
import com.example.ai.AiParsedItem
import com.example.data.db.AppDatabase
import com.example.data.model.ChecklistItem
import com.example.data.model.ItemEntity
import com.example.data.model.ItemPriority
import com.example.data.model.ItemType
import com.example.data.model.ProjectEntity
import com.example.data.model.ScheduleBlock
import com.example.data.model.UserPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

enum class AppNavDestination {
    HOME,
    INBOX,
    TODAY,
    CALENDAR,
    NOTES,
    PROJECTS,
    SEARCH,
    SETTINGS
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class QuickPilotViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val itemDao = database.itemDao()
    private val projectDao = database.projectDao()
    private val userPrefsDao = database.userPreferencesDao()

    val currentScreen = MutableStateFlow(AppNavDestination.HOME)

    val activeItems: StateFlow<List<ItemEntity>> = itemDao.getAllActiveItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inboxItems: StateFlow<List<ItemEntity>> = itemDao.getInboxItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<ItemEntity>> = itemDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<ProjectEntity>> = projectDao.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences: StateFlow<UserPreferences> = userPrefsDao.getUserPreferences()
        .combine(MutableStateFlow(UserPreferences())) { stored, default ->
            stored ?: default
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    // Search
    val searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<ItemEntity>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) activeItems
            else itemDao.searchItems(query.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Capture state
    private val _isAiParsing = MutableStateFlow(false)
    val isAiParsing: StateFlow<Boolean> = _isAiParsing.asStateFlow()

    private val _parsedResult = MutableStateFlow<AiExtractionResult?>(null)
    val parsedResult: StateFlow<AiExtractionResult?> = _parsedResult.asStateFlow()

    private val _isCaptureModalOpen = MutableStateFlow(false)
    val isCaptureModalOpen: StateFlow<Boolean> = _isCaptureModalOpen.asStateFlow()

    // Plan My Day state
    private val _todaySchedule = MutableStateFlow<List<ScheduleBlock>>(emptyList())
    val todaySchedule: StateFlow<List<ScheduleBlock>> = _todaySchedule.asStateFlow()

    private val _isPlanningDay = MutableStateFlow(false)
    val isPlanningDay: StateFlow<Boolean> = _isPlanningDay.asStateFlow()

    private val _isPlanMyDayModalOpen = MutableStateFlow(false)
    val isPlanMyDayModalOpen: StateFlow<Boolean> = _isPlanMyDayModalOpen.asStateFlow()

    // Toast / Feedback channel
    private val _snackMessage = MutableSharedFlow<String>()
    val snackMessage = _snackMessage.asSharedFlow()

    init {
        // Pre-generate initial schedule based on active items
        viewModelScope.launch {
            activeItems.collect { items ->
                if (_todaySchedule.value.isEmpty() && items.isNotEmpty()) {
                    val prefs = userPreferences.value
                    _todaySchedule.value = AiOrganizationEngine.generateDaySchedule(items, prefs)
                }
            }
        }
    }

    fun openCaptureModal() {
        _isCaptureModalOpen.value = true
    }

    fun closeCaptureModal() {
        _isCaptureModalOpen.value = false
        _parsedResult.value = null
    }

    fun openPlanMyDayModal() {
        _isPlanMyDayModalOpen.value = true
        generatePlanMyDayProposal()
    }

    fun closePlanMyDayModal() {
        _isPlanMyDayModalOpen.value = false
    }

    fun processCaptureInput(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            _isAiParsing.value = true
            val result = AiOrganizationEngine.parseCaptureInput(input)
            _parsedResult.value = result
            _isAiParsing.value = false
        }
    }

    fun clearParsedResult() {
        _parsedResult.value = null
    }

    fun confirmAndSaveParsedItems(items: List<AiParsedItem>) {
        viewModelScope.launch {
            val entities = items.map { parsed ->
                ItemEntity(
                    title = parsed.title,
                    description = parsed.description,
                    itemType = parsed.itemType.name,
                    dueDateEpochMillis = parsed.dueDateEpochMillis,
                    timeString = parsed.timeString,
                    durationMinutes = parsed.durationMinutes,
                    priority = parsed.priority.name,
                    category = parsed.category,
                    checklistJson = if (parsed.checklist.isNotEmpty()) {
                        ItemEntity.serializeChecklist(parsed.checklist.map { ChecklistItem(text = it) })
                    } else "[]",
                    clarificationQuestion = parsed.clarificationQuestion
                )
            }
            itemDao.insertItems(entities)
            closeCaptureModal()
            _snackMessage.emit("Organized ${entities.size} items into QuickPilot!")
        }
    }

    fun createDirectItem(
        title: String,
        type: ItemType,
        category: String = "General",
        priority: ItemPriority = ItemPriority.NORMAL,
        dueDateMillis: Long? = null,
        timeStr: String? = null
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val item = ItemEntity(
                title = title.trim(),
                itemType = type.name,
                category = category,
                priority = priority.name,
                dueDateEpochMillis = dueDateMillis,
                timeString = timeStr
            )
            itemDao.insertItem(item)
            _snackMessage.emit("Added to ${type.name.lowercase()}s")
        }
    }

    fun toggleItemCompleted(item: ItemEntity) {
        viewModelScope.launch {
            val updated = item.copy(
                isCompleted = !item.isCompleted,
                completedAtEpochMillis = if (!item.isCompleted) System.currentTimeMillis() else null,
                updatedAtEpochMillis = System.currentTimeMillis()
            )
            itemDao.updateItem(updated)
        }
    }

    fun toggleChecklistItem(item: ItemEntity, checkItemId: String) {
        viewModelScope.launch {
            val currentList = item.getChecklistItems()
            val updatedList = currentList.map {
                if (it.id == checkItemId) it.copy(isDone = !it.isDone) else it
            }
            val updated = item.copy(
                checklistJson = ItemEntity.serializeChecklist(updatedList),
                updatedAtEpochMillis = System.currentTimeMillis()
            )
            itemDao.updateItem(updated)
        }
    }

    fun togglePin(item: ItemEntity) {
        viewModelScope.launch {
            itemDao.updateItem(item.copy(isPinned = !item.isPinned))
        }
    }

    fun archiveItem(item: ItemEntity) {
        viewModelScope.launch {
            itemDao.updateItem(item.copy(isArchived = true))
            _snackMessage.emit("Item archived")
        }
    }

    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            itemDao.deleteItem(item)
            _snackMessage.emit("Item removed")
        }
    }

    fun updateItem(item: ItemEntity) {
        viewModelScope.launch {
            itemDao.updateItem(item.copy(updatedAtEpochMillis = System.currentTimeMillis()))
        }
    }

    // Projects & Goals
    fun createProject(title: String, description: String, colorHex: String = "#6366F1", priority: String = "NORMAL") {
        if (title.isBlank()) return
        viewModelScope.launch {
            projectDao.insertProject(
                ProjectEntity(
                    title = title.trim(),
                    description = description.trim(),
                    colorHex = colorHex,
                    priority = priority
                )
            )
            _snackMessage.emit("Project created")
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectDao.deleteProject(project)
            _snackMessage.emit("Project deleted")
        }
    }

    fun breakDownGoalAndCreateTasks(goalTitle: String, projectId: Long?) {
        viewModelScope.launch {
            _snackMessage.emit("AI breaking down '$goalTitle'...")
            val steps = AiOrganizationEngine.breakDownGoal(goalTitle)
            val checklists = steps.map { ChecklistItem(text = it) }
            val item = ItemEntity(
                title = goalTitle,
                description = "AI-generated actionable goal roadmap",
                itemType = ItemType.TASK.name,
                category = "Work",
                projectId = projectId,
                checklistJson = ItemEntity.serializeChecklist(checklists)
            )
            itemDao.insertItem(item)
            _snackMessage.emit("Created goal with ${steps.size} milestones!")
        }
    }

    // Daily Planner
    fun generatePlanMyDayProposal() {
        viewModelScope.launch {
            _isPlanningDay.value = true
            val items = activeItems.value
            val prefs = userPreferences.value
            val proposed = AiOrganizationEngine.generateDaySchedule(items, prefs)
            _todaySchedule.value = proposed
            _isPlanningDay.value = false
        }
    }

    fun acceptDaySchedule(blocks: List<ScheduleBlock>) {
        _todaySchedule.value = blocks
        closePlanMyDayModal()
        viewModelScope.launch {
            _snackMessage.emit("Schedule confirmed for today! 🚀")
        }
    }

    // Bulk Organize
    fun bulkOrganizeInbox() {
        viewModelScope.launch {
            val inbox = inboxItems.value
            if (inbox.isEmpty()) {
                _snackMessage.emit("Inbox is already clean!")
                return@launch
            }
            var count = 0
            inbox.forEach { item ->
                if (item.category == "General" || item.dueDateEpochMillis == null) {
                    val parsed = AiOrganizationEngine.parseWithRuleEngine(item.title).firstOrNull()
                    if (parsed != null) {
                        itemDao.updateItem(
                            item.copy(
                                category = parsed.category,
                                priority = parsed.priority.name,
                                durationMinutes = parsed.durationMinutes,
                                updatedAtEpochMillis = System.currentTimeMillis()
                            )
                        )
                        count++
                    }
                }
            }
            _snackMessage.emit("Auto-organized $count items")
        }
    }

    // Extract Action Items from Note
    fun extractActionsFromNote(note: ItemEntity) {
        viewModelScope.launch {
            val actions = AiOrganizationEngine.extractActionsFromNote(note.title, note.description)
            val now = System.currentTimeMillis()
            val newTasks = actions.map { actionText ->
                ItemEntity(
                    title = actionText,
                    description = "Extracted from note: ${note.title}",
                    itemType = ItemType.TASK.name,
                    category = note.category,
                    createdAtEpochMillis = now
                )
            }
            itemDao.insertItems(newTasks)
            _snackMessage.emit("Extracted ${actions.size} action tasks from note!")
        }
    }

    // Settings & Personalization
    fun saveUserPreferences(prefs: UserPreferences) {
        viewModelScope.launch {
            userPrefsDao.insertOrUpdate(prefs)
            _snackMessage.emit("Preferences updated")
        }
    }

    // Export & Reset
    fun exportDataAsJson(): String {
        val root = JSONObject()
        val itemsArray = JSONArray()
        activeItems.value.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("description", item.description)
                put("itemType", item.itemType)
                put("category", item.category)
                put("priority", item.priority)
                put("timeString", item.timeString)
                put("isCompleted", item.isCompleted)
            }
            itemsArray.put(obj)
        }
        root.put("items", itemsArray)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("version", "QuickPilot AI 1.0")
        return root.toString(2)
    }

    fun clearAllData() {
        viewModelScope.launch {
            itemDao.clearAll()
            projectDao.clearAll()
            _todaySchedule.value = emptyList()
            _snackMessage.emit("All data cleared")
        }
    }
}
