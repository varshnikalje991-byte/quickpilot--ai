package com.example.ai

import com.example.data.model.ItemPriority
import com.example.data.model.ItemType

data class AiParsedItem(
    val title: String,
    val description: String = "",
    val itemType: ItemType = ItemType.TASK,
    val dueDateEpochMillis: Long? = null,
    val timeString: String? = null,
    val durationMinutes: Int = 30,
    val priority: ItemPriority = ItemPriority.NORMAL,
    val category: String = "General",
    val checklist: List<String> = emptyList(),
    val clarificationQuestion: String? = null,
    val confidence: Float = 0.95f
)

data class AiExtractionResult(
    val rawInput: String,
    val items: List<AiParsedItem>,
    val isFromGemini: Boolean = false,
    val notes: String? = null
)
