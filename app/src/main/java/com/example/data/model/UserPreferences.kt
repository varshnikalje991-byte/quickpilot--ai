package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Pilot",
    val timezone: String = java.util.TimeZone.getDefault().id,
    val language: String = "English",
    val wakingHour: Int = 7,
    val workStartHour: Int = 9,
    val workEndHour: Int = 18,
    val breakIntervalMinutes: Int = 90,
    val breakDurationMinutes: Int = 15,
    val planningStyle: String = "Balanced", // Balanced, Focused, Relaxed
    val reminderBehavior: String = "Standard", // Gentle, Standard, Persistent
    val isPro: Boolean = true,
    val hasCompletedOnboarding: Boolean = true
)

data class ScheduleBlock(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timeSlot: String, // e.g. "09:00 AM - 09:30 AM"
    val title: String,
    val durationMinutes: Int,
    val isBreak: Boolean = false,
    val itemId: Long? = null,
    val category: String = "Work",
    val isConfirmed: Boolean = true
)
