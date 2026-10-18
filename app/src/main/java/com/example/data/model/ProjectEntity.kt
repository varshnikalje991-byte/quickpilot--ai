package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val colorHex: String = "#6366F1",
    val deadlineEpochMillis: Long? = null,
    val priority: String = "NORMAL",
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
