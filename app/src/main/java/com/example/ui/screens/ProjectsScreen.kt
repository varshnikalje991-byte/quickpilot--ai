package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.ProjectEntity
import com.example.ui.QuickPilotViewModel
import com.example.ui.components.ItemCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.RoseUrgent
import com.example.ui.theme.Slate400
import com.example.ui.theme.VioletPrimary

@Composable
fun ProjectsScreen(viewModel: QuickPilotViewModel) {
    val projects by viewModel.projects.collectAsState()
    val items by viewModel.activeItems.collectAsState()

    var showCreateProjectDialog by remember { mutableStateOf(false) }
    var showGoalBreakdownDialog by remember { mutableStateOf(false) }

    var projectTitle by remember { mutableStateOf("") }
    var projectDesc by remember { mutableStateOf("") }
    var goalTitleToBreakDown by remember { mutableStateOf("") }

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
                        text = "Projects & Goals",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Milestones and AI-decomposed goal roadmaps",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showGoalBreakdownDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("ai_breakdown_goal_button")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Break Down", color = Color.Black, style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { showCreateProjectDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        if (projects.isEmpty()) {
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
                        Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No projects yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Group tasks into goals like 'Launch Website' or 'College Assignments'.", style = MaterialTheme.typography.bodySmall, color = Slate400)
                    }
                }
            }
        } else {
            items(projects, key = { it.id }) { project ->
                val projectTasks = items.filter { it.projectId == project.id }
                val completed = projectTasks.count { it.isCompleted }
                val total = projectTasks.size
                val progress = if (total > 0) completed.toFloat() / total else 0f

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (project.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = project.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate400
                                    )
                                }
                            }

                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxSize(),
                                    color = VioletPrimary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeWidth = 4.dp
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tasks list preview
                        if (projectTasks.isEmpty()) {
                            Text(
                                text = "No tasks linked to this project yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        } else {
                            projectTasks.take(3).forEach { task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (task.isCompleted) "✓ " else "○ ",
                                        color = if (task.isCompleted) CyanAccent else Slate400,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$completed of $total milestones complete",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )

                            IconButton(
                                onClick = { viewModel.deleteProject(project) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Project",
                                    tint = RoseUrgent.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Create Project Dialog
    if (showCreateProjectDialog) {
        AlertDialog(
            onDismissRequest = { showCreateProjectDialog = false },
            title = { Text("New Project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = projectTitle,
                        onValueChange = { projectTitle = it },
                        label = { Text("Project Title") },
                        placeholder = { Text("e.g. Launch my website") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = projectDesc,
                        onValueChange = { projectDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projectTitle.isNotBlank()) {
                            viewModel.createProject(projectTitle, projectDesc)
                            showCreateProjectDialog = false
                            projectTitle = ""
                            projectDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateProjectDialog = false }) { Text("Cancel") }
            }
        )
    }

    // AI Goal Breakdown Dialog
    if (showGoalBreakdownDialog) {
        AlertDialog(
            onDismissRequest = { showGoalBreakdownDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Goal Breakdown")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter a large goal or objective. QuickPilot AI will break it down into an actionable step-by-step checklist.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                    OutlinedTextField(
                        value = goalTitleToBreakDown,
                        onValueChange = { goalTitleToBreakDown = it },
                        label = { Text("Goal Title") },
                        placeholder = { Text("e.g. Launch my website / Prepare for chemistry exam") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (goalTitleToBreakDown.isNotBlank()) {
                            viewModel.breakDownGoalAndCreateTasks(goalTitleToBreakDown, null)
                            showGoalBreakdownDialog = false
                            goalTitleToBreakDown = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Break Down with AI", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalBreakdownDialog = false }) { Text("Cancel") }
            }
        )
    }
}
