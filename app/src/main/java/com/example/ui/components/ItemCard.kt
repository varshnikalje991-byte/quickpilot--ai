package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ItemEntity
import com.example.data.model.ItemPriority
import com.example.data.model.ItemType
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityNormal
import com.example.ui.theme.PriorityUrgent
import com.example.ui.theme.RoseUrgent
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.VioletPrimary
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemCard(
    item: ItemEntity,
    onToggleComplete: () -> Unit,
    onToggleChecklist: (String) -> Unit,
    onTogglePin: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onExtractActions: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isDone = item.isCompleted

    val priorityColor = when (item.priority) {
        ItemPriority.URGENT.name -> PriorityUrgent
        ItemPriority.HIGH.name -> PriorityHigh
        ItemPriority.LOW.name -> PriorityLow
        else -> PriorityNormal
    }

    val typeIcon: ImageVector = when (item.itemType) {
        ItemType.REMINDER.name -> Icons.Default.Notifications
        ItemType.SHOPPING.name -> Icons.Default.ShoppingCart
        ItemType.NOTE.name -> Icons.Default.Bookmark
        ItemType.IDEA.name -> Icons.Default.Lightbulb
        ItemType.EVENT.name -> Icons.Default.Schedule
        else -> Icons.Default.CheckCircle
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { isExpanded = !isExpanded }
            .testTag("item_card_${item.id}"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (item.isPinned) VioletPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
        ),
        tonalElevation = if (item.isPinned) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Complete checkbox / icon
                if (item.itemType != ItemType.NOTE.name && item.itemType != ItemType.IDEA.name) {
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("toggle_complete_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (isDone) "Mark Incomplete" else "Mark Complete",
                            tint = if (isDone) EmeraldSuccess else priorityColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(VioletPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = item.itemType,
                            modifier = Modifier.size(18.dp),
                            tint = VioletPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isDone) Slate400 else MaterialTheme.colorScheme.onSurface,
                        maxLines = if (isExpanded) 10 else 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.description.isNotBlank() && item.description != item.title) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (isExpanded) 20 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Pin indicator/action
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (item.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin item",
                        tint = if (item.isPinned) VioletPrimary else Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Clarification Warning Banner if present
            if (!item.clarificationQuestion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = AmberWarning.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Clarification",
                            tint = AmberWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.clarificationQuestion,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Checklist section (e.g. for shopping or broken down tasks)
            val checklists = item.getChecklistItems()
            if (checklists.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val visibleList = if (isExpanded) checklists else checklists.take(3)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    visibleList.forEach { check ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleChecklist(check.id) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = check.isDone,
                                onCheckedChange = { onToggleChecklist(check.id) },
                                modifier = Modifier.size(28.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = EmeraldSuccess,
                                    uncheckedColor = Slate400
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = check.text,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = if (check.isDone) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (check.isDone) Slate400 else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (!isExpanded && checklists.size > 3) {
                        Text(
                            text = "+ ${checklists.size - 3} more items...",
                            style = MaterialTheme.typography.labelSmall,
                            color = VioletPrimary,
                            modifier = Modifier.padding(start = 34.dp, bottom = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Category Chip
                TagChip(text = item.category, color = VioletPrimary)

                // Type Chip
                TagChip(text = item.itemType.lowercase().replaceFirstChar { it.uppercase() }, color = CyanAccent)

                // Priority Chip
                if (item.priority != ItemPriority.NORMAL.name) {
                    TagChip(text = item.priority, color = priorityColor)
                }

                // Date & Time
                if (item.dueDateEpochMillis != null || !item.timeString.isNullOrBlank()) {
                    val dateText = DateUtils.formatFriendlyDate(item.dueDateEpochMillis)
                    val timeText = DateUtils.formatFriendlyTime(item.dueDateEpochMillis, item.timeString)
                    val isOverdue = DateUtils.isOverdue(item.dueDateEpochMillis, item.isCompleted)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isOverdue) RoseUrgent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isOverdue) BorderStroke(1.dp, RoseUrgent) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isOverdue) RoseUrgent else Slate400
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = listOf(dateText, timeText).filter { it.isNotBlank() && it != "No date" }.joinToString(" • "),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverdue) RoseUrgent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Duration
                if (item.durationMinutes > 0 && item.itemType == ItemType.TASK.name) {
                    TagChip(text = "${item.durationMinutes}m", color = Slate400)
                }
            }

            // Expanded Actions
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.itemType == ItemType.NOTE.name && onExtractActions != null) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onExtractActions() },
                                color = VioletPrimary.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Extract Tasks",
                                        tint = VioletPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Extract Action Items",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = VioletPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        IconButton(
                            onClick = onArchive,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = "Archive",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = RoseUrgent.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
