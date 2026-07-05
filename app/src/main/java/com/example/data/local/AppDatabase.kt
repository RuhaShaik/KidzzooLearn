package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.data.model.Badge
import com.example.data.model.ChatMessage
import com.example.data.model.ChildProfile
import com.example.data.model.DailyMission
import com.example.data.model.LearningProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildProfileDao {
    @Query("SELECT * FROM child_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<ChildProfile?>

    @Query("SELECT * FROM child_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): ChildProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ChildProfile)

    @Query("UPDATE child_profile SET xp = :xp, level = :level WHERE id = 1")
    suspend fun updateXpAndLevel(xp: Int, level: Int)

    @Query("UPDATE child_profile SET streak = :streak, lastLearningDate = :lastDate WHERE id = 1")
    suspend fun updateStreak(streak: Int, lastDate: Long)

    @Query("UPDATE child_profile SET screenTimeUsedTodayMinutes = :minutes WHERE id = 1")
    suspend fun updateScreenTimeUsed(minutes: Int)
}

@Dao
interface LearningProgressDao {
    @Query("SELECT * FROM learning_progress")
    fun getAllProgressFlow(): Flow<List<LearningProgress>>

    @Query("SELECT * FROM learning_progress WHERE category = :category AND subject = :subject LIMIT 1")
    suspend fun getProgress(category: String, subject: String): LearningProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: LearningProgress)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_message ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_message")
    suspend fun clearChat()
}

@Dao
interface DailyMissionDao {
    @Query("SELECT * FROM daily_mission")
    fun getMissionsFlow(): Flow<List<DailyMission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<DailyMission>)

    @Query("UPDATE daily_mission SET isCompleted = :completed WHERE id = :id")
    suspend fun updateMissionStatus(id: Int, completed: Boolean)

    @Query("DELETE FROM daily_mission")
    suspend fun clearMissions()
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badge")
    fun getAllBadgesFlow(): Flow<List<Badge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: Badge)
}

@Database(
    entities = [
        ChildProfile::class,
        LearningProgress::class,
        ChatMessage::class,
        DailyMission::class,
        Badge::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun childProfileDao(): ChildProfileDao
    abstract fun learningProgressDao(): LearningProgressDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun dailyMissionDao(): DailyMissionDao
    abstract fun badgeDao(): BadgeDao
}
