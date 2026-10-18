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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ItemType
import com.example.ui.QuickPilotViewModel
import com.example.ui.components.ItemCard
import com.example.ui.theme.Slate400
import com.example.ui.theme.VioletPrimary

@Composable
fun InboxScreen(viewModel: QuickPilotViewModel) {
    val inboxItems by viewModel.inboxItems.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredItems = when (selectedCategory) {
        "Tasks" -> inboxItems.filter { it.itemType == ItemType.TASK.name }
        "Notes" -> inboxItems.filter { it.itemType == ItemType.NOTE.name }
        "Reminders" -> inboxItems.filter { it.itemType == ItemType.REMINDER.name }
        "Events" -> inboxItems.filter { it.itemType == ItemType.EVENT.name }
        "Shopping" -> inboxItems.filter { it.itemType == ItemType.SHOPPING.name }
        "Ideas" -> inboxItems.filter { it.itemType == ItemType.IDEA.name }
        else -> inboxItems
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        text = "Inbox",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${inboxItems.size} items waiting to be organized",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }

                // Bulk Organize Button
                Button(
                    onClick = { viewModel.bulkOrganizeInbox() },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("bulk_organize_button")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto Organize", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Filter tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val tabs = listOf("All", "Tasks", "Notes", "Reminders", "Events", "Shopping", "Ideas")
                items(tabs) { tab ->
                    FilterChip(
                        selected = selectedCategory == tab,
                        onClick = { selectedCategory = tab },
                        label = { Text(tab) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VioletPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (filteredItems.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = VioletPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Inbox Zero! 🎉",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Every thought and task is organized.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                ItemCard(
                    item = item,
                    onToggleComplete = { viewModel.toggleItemCompleted(item) },
                    onToggleChecklist = { checkId -> viewModel.toggleChecklistItem(item, checkId) },
                    onTogglePin = { viewModel.togglePin(item) },
                    onArchive = { viewModel.archiveItem(item) },
                    onDelete = { viewModel.deleteItem(item) },
                    onExtractActions = if (item.itemType == ItemType.NOTE.name) {
                        { viewModel.extractActionsFromNote(item) }
                    } else null
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
