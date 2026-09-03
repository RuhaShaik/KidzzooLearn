package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.ui.screens.ChildProfileSetupScreen
import com.example.ui.screens.KidsMainScreen
import com.example.ui.screens.ParentDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LearningViewModel

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var viewModel: LearningViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Instantiate local Room Database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "smart_kids_companion.db"
        )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

        // Create the companion ViewModel
        viewModel = LearningViewModel(application, database)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val profile by viewModel.childProfile.collectAsState()
                    var navigationRoute by remember { mutableStateOf("kids_main") }

                    // Elegant Crossfade navigation for smooth visual transitions
                    Crossfade(
                        targetState = profile,
                        label = "main_navigation"
                    ) { currentProfile ->
                        if (currentProfile == null) {
                            ChildProfileSetupScreen(
                                onSetupComplete = { name, age, interests ->
                                    viewModel.setupProfile(name, age, interests)
                                }
                            )
                        } else {
                            when (navigationRoute) {
                                "kids_main" -> {
                                    KidsMainScreen(
                                        viewModel = viewModel,
                                        onNavigateToParent = {
                                            navigationRoute = "parent_dashboard"
                                        }
                                    )
                                }
                                "parent_dashboard" -> {
                                    ParentDashboardScreen(
                                        viewModel = viewModel,
                                        onBack = {
                                            navigationRoute = "kids_main"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
