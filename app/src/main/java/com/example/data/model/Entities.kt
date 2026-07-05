package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_profile")
data class ChildProfile(
    @PrimaryKey val id: Int = 1, // Single profile for simplicity
    val name: String,
    val age: Int,
    val interests: String, // Comma-separated
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 1,
    val lastLearningDate: Long = System.currentTimeMillis(),
    val screenTimeLimitMinutes: Int = 60,
    val screenTimeUsedTodayMinutes: Int = 0
) {
    fun getAgeGroup(): String {
        return when {
            age <= 5 -> "3-5"
            age <= 8 -> "6-8"
            age <= 11 -> "9-11"
            else -> "12-14"
        }
    }
}

@Entity(tableName = "learning_progress")
data class LearningProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // e.g., "Manners", "School", "Critical Thinking", "Communication", "Creativity", "Emotional"
    val subject: String,  // e.g., "Math", "Science", "Sharing", "Problem Solving"
    val completedLessons: Int = 0,
    val maxScore: Int = 0,
    val masteryLevel: Float = 0.0f // 0.0 to 1.0
)

@Entity(tableName = "chat_message")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isVoice: Boolean = false
)

@Entity(tableName = "daily_mission")
data class DailyMission(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "MANNERS", "EDUCATION", "READING", "THINKING", "CREATIVITY", "REFLECTION"
    val title: String,
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val xpReward: Int = 50
)

@Entity(tableName = "badge")
data class Badge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val iconName: String, // e.g., "star", "shield", "rocket", "palette"
    val unlockedAt: Long = System.currentTimeMillis()
)
