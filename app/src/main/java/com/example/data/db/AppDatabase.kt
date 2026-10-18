package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ItemEntity
import com.example.data.model.ItemPriority
import com.example.data.model.ItemType
import com.example.data.model.ProjectEntity
import com.example.data.model.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [ItemEntity::class, ProjectEntity::class, UserPreferences::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun projectDao(): ProjectDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quickpilot_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val userPrefDao = database.userPreferencesDao()
                val itemDao = database.itemDao()
                val projectDao = database.projectDao()

                // Default preferences
                userPrefDao.insertOrUpdate(UserPreferences())

                // Default Projects
                val proj1Id = projectDao.insertProject(
                    ProjectEntity(
                        title = "College & Academics",
                        description = "Assignments, exam prep, and research milestones",
                        colorHex = "#6366F1",
                        priority = "HIGH"
                    )
                )

                val proj2Id = projectDao.insertProject(
                    ProjectEntity(
                        title = "Personal Website Launch",
                        description = "Portfolio redesign and project write-ups",
                        colorHex = "#06B6D4",
                        priority = "NORMAL"
                    )
                )

                val cal = Calendar.getInstance()
                val now = System.currentTimeMillis()

                // Seed Initial Items demonstrating QuickPilot's multi-category capture
                cal.set(Calendar.HOUR_OF_DAY, 18)
                cal.set(Calendar.MINUTE, 0)
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val tomorrow6pm = cal.timeInMillis

                val seedItems = listOf(
                    ItemEntity(
                        title = "Finish physics assignment",
                        description = "Solve mechanics questions 4 to 12 and verify diagrams",
                        itemType = ItemType.TASK.name,
                        dueDateEpochMillis = tomorrow6pm,
                        timeString = "06:00 PM",
                        durationMinutes = 60,
                        priority = ItemPriority.HIGH.name,
                        category = "Study",
                        projectId = proj1Id,
                        scheduledTimeBlock = "09:30 AM - 10:30 AM",
                        createdAtEpochMillis = now
                    ),
                    ItemEntity(
                        title = "Buy fresh groceries & almond milk",
                        description = "After college stop by organic market",
                        itemType = ItemType.SHOPPING.name,
                        dueDateEpochMillis = now,
                        timeString = "Evening",
                        durationMinutes = 20,
                        priority = ItemPriority.NORMAL.name,
                        category = "Home",
                        checklistJson = """[{"id":"1","text":"Almond milk","isDone":false},{"id":"2","text":"Whole wheat bread","isDone":false},{"id":"3","text":"Eggs & bananas","isDone":true}]""",
                        createdAtEpochMillis = now - 3600000
                    ),
                    ItemEntity(
                        title = "Call Rahul to discuss group presentation",
                        description = "Clarify who presents the intro and architecture slides",
                        itemType = ItemType.REMINDER.name,
                        dueDateEpochMillis = now + (24 * 3600000 * 2), // Sunday
                        timeString = "11:00 AM",
                        durationMinutes = 15,
                        priority = ItemPriority.NORMAL.name,
                        category = "Personal",
                        clarificationQuestion = "Proposed time: 11:00 AM on Sunday. Tap to reschedule.",
                        createdAtEpochMillis = now - 7200000
                    ),
                    ItemEntity(
                        title = "Wi-Fi monthly fiber bill is ₹799",
                        description = "Account #492817 — Due on the 25th of every month via UPI",
                        itemType = ItemType.NOTE.name,
                        category = "Finance",
                        isPinned = true,
                        createdAtEpochMillis = now - 86400000
                    ),
                    ItemEntity(
                        title = "Launch portfolio website checklist",
                        description = "Key steps required before sharing domain on LinkedIn",
                        itemType = ItemType.TASK.name,
                        durationMinutes = 90,
                        priority = ItemPriority.NORMAL.name,
                        category = "Work",
                        projectId = proj2Id,
                        checklistJson = """[{"id":"c1","text":"Select custom domain name","isDone":true},{"id":"c2","text":"Design responsive homepage & hero","isDone":true},{"id":"c3","text":"Add project case studies & demo links","isDone":false},{"id":"c4","text":"Cross-browser testing on mobile","isDone":false},{"id":"c5","text":"Publish and announce on social","isDone":false}]""",
                        createdAtEpochMillis = now - 120000000
                    ),
                    ItemEntity(
                        title = "Idea: Smart voice-to-schedule sync widget",
                        description = "Quick widget on home screen for 1-tap voice dictation while walking",
                        itemType = ItemType.IDEA.name,
                        category = "Work",
                        createdAtEpochMillis = now - 180000000
                    )
                )

                itemDao.insertItems(seedItems)
            }
        }
    }
}
