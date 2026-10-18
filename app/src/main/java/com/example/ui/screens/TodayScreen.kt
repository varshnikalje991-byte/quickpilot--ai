package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.QuickPilotViewModel
import com.example.ui.components.ItemCard
import com.example.ui.components.ScheduleTimelineRow
import com.example.ui.theme.RoseUrgent
import com.example.ui.theme.Slate400
import com.example.ui.theme.VioletPrimary
import com.example.util.DateUtils

@Composable
fun TodayScreen(viewModel: QuickPilotViewModel) {
    val items by viewModel.activeItems.collectAsState()
    val schedule by viewModel.todaySchedule.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()

    val overdueItems = items.filter { DateUtils.isOverdue(it.dueDateEpochMillis, it.isCompleted) }
    val todayItems = items.filter { DateUtils.isToday(it.dueDateEpochMillis) || it.dueDateEpochMillis == null }
    val completedToday = todayItems.filter { it.isCompleted }
    val pendingToday = todayItems.filter { !it.isCompleted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${pendingToday.size} pending tasks • ${completedToday.size} done",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }

                Button(
                    onClick = { viewModel.openPlanMyDayModal() },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("today_plan_day_button")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Plan My Day", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Overdue Alert
        if (overdueItems.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = RoseUrgent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, RoseUrgent.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = RoseUrgent, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${overdueItems.size} Overdue Items",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = RoseUrgent
                            )
                            Text(
                                text = "Tasks from previous days that need immediate attention.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Scheduled Timeline section
        if (schedule.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "AI Scheduled Time Blocks",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Realistic pacing with mindful breaks based on your work preferences",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
            }

            items(schedule, key = { it.id }) { block ->
                ScheduleTimelineRow(block = block)
            }
        }

        // Pending Tasks
        item {
            Text(
                text = "Today's Action Items (${pendingToday.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (pendingToday.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "No pending tasks for today. You're completely caught up!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(pendingToday, key = { it.id }) { item ->
                ItemCard(
                    item = item,
                    onToggleComplete = { viewModel.toggleItemCompleted(item) },
                    onToggleChecklist = { checkId -> viewModel.toggleChecklistItem(item, checkId) },
                    onTogglePin = { viewModel.togglePin(item) },
                    onArchive = { viewModel.archiveItem(item) },
                    onDelete = { viewModel.deleteItem(item) }
                )
            }
        }

        // Completed Tasks
        if (completedToday.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Completed (${completedToday.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate400
                )
            }

            items(completedToday, key = { it.id }) { item ->
                ItemCard(
                    item = item,
                    onToggleComplete = { viewModel.toggleItemCompleted(item) },
                    onToggleChecklist = { checkId -> viewModel.toggleChecklistItem(item, checkId) },
                    onTogglePin = { viewModel.togglePin(item) },
                    onArchive = { viewModel.archiveItem(item) },
                    onDelete = { viewModel.deleteItem(item) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
