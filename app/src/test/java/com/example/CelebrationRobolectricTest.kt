package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.ui.viewmodel.LearningViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CelebrationRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var viewModel: LearningViewModel

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = LearningViewModel(app, database)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testTriggerAndDismissCelebration() {
        assertNull(viewModel.celebrationEvent.value)

        viewModel.triggerCelebration(
            title = "Lesson Completed! 🎉",
            message = "Great work on your lesson!",
            xpBonus = 40
        )

        val event = viewModel.celebrationEvent.value
        assertNotNull(event)
        assertEquals("Lesson Completed! 🎉", event?.title)
        assertEquals("Great work on your lesson!", event?.message)
        assertEquals(40, event?.xpBonus)

        viewModel.dismissCelebration()
        assertNull(viewModel.celebrationEvent.value)
    }

    @Test
    fun testCompleteDailyMissionCelebration() = runBlocking {
        // Setup a child profile
        viewModel.setupProfile("Leo", 7, "Robots")
        
        // Complete a Manners mission
        viewModel.completeDailyMission(1, 40, "MANNERS")

        // Wait for background IO dispatchers and main looper to update state
        var attempts = 0
        while (viewModel.celebrationEvent.value == null && attempts < 40) {
            kotlinx.coroutines.delay(50)
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            attempts++
        }

        // Celebration event should be triggered with lesson completion title & XP
        val event = viewModel.celebrationEvent.value
        assertNotNull(event)
        assertTrue(event!!.title.contains("Lesson Completed!"))
        assertEquals(40, event.xpBonus)
    }
}
