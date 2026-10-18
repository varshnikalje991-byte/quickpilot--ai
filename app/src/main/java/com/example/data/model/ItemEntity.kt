package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

enum class ItemType {
    TASK,
    REMINDER,
    NOTE,
    EVENT,
    SHOPPING,
    IDEA,
    FOLLOW_UP
}

enum class ItemPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

data class ChecklistItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isDone: Boolean = false
)

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val itemType: String = ItemType.TASK.name,
    val dueDateEpochMillis: Long? = null,
    val timeString: String? = null, // e.g. "18:00", "09:30 AM", "Morning"
    val durationMinutes: Int = 30,
    val priority: String = ItemPriority.NORMAL.name,
    val category: String = "General", // Work, Study, Personal, Home, Health, Finance, General
    val isCompleted: Boolean = false,
    val completedAtEpochMillis: Long? = null,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val projectId: Long? = null,
    val checklistJson: String = "[]",
    val recurrence: String = "NONE", // NONE, DAILY, WEEKLY, MONTHLY
    val scheduledTimeBlock: String? = null, // e.g. "09:00 - 09:30"
    val clarificationQuestion: String? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
) {
    fun getChecklistItems(): List<ChecklistItem> {
        if (checklistJson.isBlank() || checklistJson == "[]") return emptyList()
        return try {
            val jsonArray = JSONArray(checklistJson)
            val list = mutableListOf<ChecklistItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ChecklistItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        text = obj.optString("text", ""),
                        isDone = obj.optBoolean("isDone", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun serializeChecklist(items: List<ChecklistItem>): String {
            val jsonArray = JSONArray()
            items.forEach { item ->
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("text", item.text)
                obj.put("isDone", item.isDone)
                jsonArray.put(obj)
            }
            return jsonArray.toString()
        }
    }
}
