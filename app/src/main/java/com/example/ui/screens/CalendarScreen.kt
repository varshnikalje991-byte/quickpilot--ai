package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.QuickPilotViewModel
import com.example.ui.components.ItemCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Slate400
import com.example.ui.theme.VioletPrimary
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: QuickPilotViewModel) {
    val items by viewModel.activeItems.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()

    var viewMode by remember { mutableStateOf("Day") } // Day, Week, Month
    var dayOffset by remember { mutableIntStateOf(0) }

    val currentCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
    val displayDateStr = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(currentCal.time)

    // Filter items matching the selected day
    val targetStart = Calendar.getInstance().apply {
        timeInMillis = currentCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis

    val targetEnd = Calendar.getInstance().apply {
        timeInMillis = currentCal.timeInMillis
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }.timeInMillis

    val selectedDayItems = items.filter { item ->
        val due = item.dueDateEpochMillis
        if (due != null) {
            due in targetStart..targetEnd
        } else {
            dayOffset == 0 // Show unscheduled items on today's view
        }
    }

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
                        text = "Calendar",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Timezone: ${prefs.timezone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyanAccent
                        )
                    }
                }

                // View Mode Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Day", "Week", "Month").forEach { mode ->
                        FilterChip(
                            selected = viewMode == mode,
                            onClick = { viewMode = mode },
                            label = { Text(mode, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Date Navigator
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { dayOffset-- }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = Slate400)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = displayDateStr,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (dayOffset == 0) {
                            Text(text = "Today", style = MaterialTheme.typography.labelSmall, color = VioletPrimary)
                        } else if (dayOffset == 1) {
                            Text(text = "Tomorrow", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                        } else if (dayOffset == -1) {
                            Text(text = "Yesterday", style = MaterialTheme.typography.labelSmall, color = Slate400)
                        }
                    }

                    IconButton(onClick = { dayOffset++ }) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Day", tint = Slate400)
                    }
                }
            }
        }

        // Week Strip selector if in Week or Month view
        if (viewMode == "Week" || viewMode == "Month") {
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = (-3..3).toList()
                    items(days) { offset ->
                        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
                        val isSelected = offset == dayOffset
                        val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
                        val dayNum = SimpleDateFormat("d", Locale.getDefault()).format(cal.time)

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { dayOffset = offset }
                                .padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) VioletPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else Slate400
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dayNum,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scheduled Items on date
        item {
            Text(
                text = "Scheduled Items (${selectedDayItems.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (selectedDayItems.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No scheduled events on this date",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Capture tasks with dates to populate your calendar timeline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
            }
        } else {
            items(selectedDayItems, key = { it.id }) { item ->
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
