package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Badge
import com.example.data.model.ChatMessage
import com.example.data.model.ChildProfile
import com.example.data.model.DailyMission
import com.example.ui.components.AnimatedCharacter
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsMainScreen(
    viewModel: com.example.ui.viewmodel.LearningViewModel,
    onNavigateToParent: () -> Unit
) {
    val profile by viewModel.childProfile.collectAsState()
    val allBadges by viewModel.allBadges.collectAsState()
    val dailyMissions by viewModel.dailyMissions.collectAsState()

    var activeTab by remember { mutableStateOf("chat") }
    var showParentGate by remember { mutableStateOf(false) }

    val localProfile = profile ?: ChildProfile(name = "Learner", age = 6, interests = "Stories")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(colors = listOf(BubbleYellow, BubblePink))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = localProfile.name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Column {
                            Text(
                                text = "Hi, ${localProfile.name}! 👋",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Level ${localProfile.level} Explorer",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    // Star Streak Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Streak",
                            tint = BubbleYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${localProfile.streak} day streak!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Parent Dashboard Secure Entrance Button
                    IconButton(
                        onClick = { showParentGate = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            .testTag("parent_dashboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Parent Area",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "chat",
                    onClick = { activeTab = "chat" },
                    icon = { Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Companion Chat") },
                    label = { Text("AI Teacher", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_chat")
                )
                NavigationBarItem(
                    selected = activeTab == "stories",
                    onClick = { activeTab = "stories" },
                    icon = { Icon(imageVector = Icons.Default.MenuBook, contentDescription = "AI Stories") },
                    label = { Text("Stories", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_stories")
                )
                NavigationBarItem(
                    selected = activeTab == "games",
                    onClick = { activeTab = "games" },
                    icon = { Icon(imageVector = Icons.Default.Casino, contentDescription = "Learning Games") },
                    label = { Text("Games", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_games")
                )
                NavigationBarItem(
                    selected = activeTab == "badges",
                    onClick = { activeTab = "badges" },
                    icon = { Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "My Badges") },
                    label = { Text("Badges", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_badges")
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when (activeTab) {
                "chat" -> ChatTab(viewModel, localProfile)
                "stories" -> StoriesTab(viewModel, localProfile)
                "games" -> GamesTab(viewModel, localProfile, dailyMissions)
                "badges" -> BadgesTab(localProfile, allBadges)
            }

            // Secure Parent Gate Challenge Dialog
            if (showParentGate) {
                ParentGateChallengeDialog(
                    onDismiss = { showParentGate = false },
                    onSuccess = {
                        showParentGate = false
                        onNavigateToParent()
                    }
                )
            }
        }
    }
}

// --- SECURE COPPA-FRIENDLY PARENT GATE CHALLENGE ---

@Composable
fun ParentGateChallengeDialog(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    // Generates a random multiplication question to challenge the adult
    val num1 = remember { (4..9).random() }
    val num2 = remember { (3..8).random() }
    val correctAnswer = num1 * num2

    var inputAnswer by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Parental Verification",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "To access parenting reports, analytical metrics, and screen time locks, solve this math challenge to confirm you are an adult:",
                    fontSize = 14.sp
                )
                Text(
                    text = "What is $num1 multiplied by $num2?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                OutlinedTextField(
                    value = inputAnswer,
                    onValueChange = { inputAnswer = it; showError = false },
                    placeholder = { Text("Your answer...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("parent_gate_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                if (showError) {
                    Text(
                        text = "Oops! That's not correct. Please try again.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputAnswer.trim().toIntOrNull() == correctAnswer) {
                        onSuccess()
                    } else {
                        showError = true
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("parent_gate_submit")
            ) {
                Text("Confirm Parent Status", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// --- TAB 1: AI TEACHER COMPANION CHAT ---

@Composable
fun ChatTab(viewModel: com.example.ui.viewmodel.LearningViewModel, profile: ChildProfile) {
    val messages by viewModel.chatMessages.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isAILoading by viewModel.isAILoading.collectAsState()
    val sttText by viewModel.sttText.collectAsState()

    var keyboardInput by remember { mutableStateOf("") }
    var useVoiceMode by remember { mutableStateOf(true) }

    val lazyListState = rememberLazyListState()
    val context = LocalContext.current

    // STT Audio Permission Handler
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Visual Hero: Animated Companion Character (Buddy, Captain Curie, Spark, or Atlas)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bobbing vector Canvas animation
                AnimatedCharacter(
                    isSpeaking = isSpeaking,
                    isListening = isListening,
                    ageGroup = profile.getAgeGroup(),
                    modifier = Modifier.size(110.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    val characterName = when (profile.getAgeGroup()) {
                        "3-5" -> "Buddy the Puppy 🐶"
                        "6-8" -> "Captain Curie 🚀"
                        "9-11" -> "Professor Spark ⚡"
                        else -> "Atlas the Mentor 🪐"
                    }
                    Text(
                        text = characterName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isListening) "Listening carefully..." else if (isSpeaking) "Talking to you out loud!" else "Ready to play! Tap mic or type!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                if (isSpeaking) {
                                    viewModel.stopSpeaking()
                                } else {
                                    if (messages.isNotEmpty()) {
                                        viewModel.speak(messages.last().text)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Mute/Unmute",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Chat Bubble Logs Area
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                val isAI = message.sender == "AI"
                val alignment = if (isAI) Alignment.Start else Alignment.End
                val bubbleColor = if (isAI) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
                val textColor = if (isAI) MaterialTheme.colorScheme.onSurface else Color.White
                val shape = if (isAI) {
                    RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
                } else {
                    RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
                }

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
                    Card(
                        shape = shape,
                        colors = CardDefaults.cardColors(containerColor = bubbleColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = message.text,
                                fontSize = 15.sp,
                                color = textColor,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isAI) "AI Teacher" else profile.name,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }

            if (isAILoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thinking...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Input Controls Layer (Switch between voice pulsing & keyboard)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (useVoiceMode) {
                // Interactive STT Voice Mode
                Text(
                    text = if (isListening) "Speak now, I'm listening!" else if (sttText.isNotBlank() && sttText != "Listening...") sttText else "Tap the microphone to talk!",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { useVoiceMode = false },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Keyboard, contentDescription = "Type instead", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Pulse Voice Mic Button
                    val micBgColor by animateColorAsState(
                        targetValue = if (isListening) BubblePink else MaterialTheme.colorScheme.primary
                    )
                    val micScale = if (isListening) 1.2f else 1.0f

                    Box(
                        modifier = Modifier
                            .scale(micScale)
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(micBgColor)
                            .clickable {
                                if (isListening) {
                                    viewModel.stopListening()
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                    if (hasPermission) {
                                        viewModel.startListening()
                                    } else {
                                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                            .testTag("microphone_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Speech Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(72.dp)) // Equal spacing balance
                }
            } else {
                // Conventional Fallback Keyboard Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { useVoiceMode = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice mode", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = keyboardInput,
                        onValueChange = { keyboardInput = it },
                        placeholder = { Text("Ask your AI companion...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("text_input_field"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (keyboardInput.isNotBlank()) {
                                viewModel.sendMessage(keyboardInput)
                                keyboardInput = ""
                            }
                        },
                        enabled = keyboardInput.isNotBlank(),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("send_message_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

// --- TAB 2: INTERACTIVE AI STORYBOOK ---

@Composable
fun StoriesTab(viewModel: com.example.ui.viewmodel.LearningViewModel, profile: ChildProfile) {
    val storyText by viewModel.storyText.collectAsState()
    val storyMoral by viewModel.storyMoral.collectAsState()
    val storyOptions by viewModel.storyOptions.collectAsState()
    val isStoryLoading by viewModel.isStoryLoading.collectAsState()

    val storyTopics = listOf(
        "Empathy & Kindness Forest 🌸",
        "The Magic of Sharing Toys 🧸",
        "Honesty: The Golden Compass 🗺️",
        "Space Adventure with Dinosaur Friends 🦖",
        "The Kingdom of Numbers & Addition 👑"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "AI Interactive Storybook 📚",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (storyText.isBlank() && !isStoryLoading) {
            // Pick a starting topic list
            Text(
                text = "Pick a magical topic to generate a personalized interactive story where YOU are the main hero! 🚀",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(storyTopics) { topic ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.loadStory(topic) }
                            .testTag("story_topic_${topic.take(5)}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = BubbleYellow)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = topic, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        } else {
            // Interactive reading section
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    if (isStoryLoading) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Writing magical story just for you...", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(4.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Chapter Choices",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = storyText,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (storyMoral.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Moral Lesson:",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = storyMoral,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // Branch choices
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "What will you do next, ${profile.name}?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                storyOptions.forEachIndexed { index, option ->
                                    Button(
                                        onClick = { viewModel.loadStory("Topic", option) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("story_option_$index")
                                    ) {
                                        Text(text = "👉 $option", fontSize = 12.sp, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                                    }
                                }

                                TextButton(
                                    onClick = { viewModel.loadStory("") },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Reset Story Topics", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 3: LEARNING GAMES & MISSIONS ---

@Composable
fun GamesTab(
    viewModel: com.example.ui.viewmodel.LearningViewModel,
    profile: ChildProfile,
    missions: List<DailyMission>
) {
    val gameQuestion by viewModel.gameQuestion.collectAsState()
    val gameOptions by viewModel.gameOptions.collectAsState()
    val gameSelectedAnswer by viewModel.gameSelectedAnswer.collectAsState()
    val gameCorrectAnswer by viewModel.gameCorrectAnswer.collectAsState()
    val gameExplanation by viewModel.gameExplanation.collectAsState()
    val isGameLoading by viewModel.isGameLoading.collectAsState()

    val subjects = listOf("Science 🔬", "Mathematics 🔢", "English Words 📚", "Geography 🌍")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Daily Learning Quests 🎯",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Active Daily Missions List
        items(missions) { mission ->
            val isCompleted = mission.isCompleted
            val bgColor = if (isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                when (mission.category.uppercase()) {
                    "MANNERS" -> VibrantEmeraldLight
                    "EDUCATION" -> VibrantIndigoLight
                    "READING", "CREATIVITY" -> VibrantOrangeLight
                    "THINKING", "REFLECTION" -> VibrantRoseLight
                    else -> VibrantSkyLight
                }
            }

            val bottomBorderColor = if (isCompleted) {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            } else {
                when (mission.category.uppercase()) {
                    "MANNERS" -> Color(0xFFA7F3D0) // Emerald-300
                    "EDUCATION" -> Color(0xFFC7D2FE) // Indigo-300
                    "READING", "CREATIVITY" -> Color(0xFFFED7AA) // Orange-300
                    "THINKING", "REFLECTION" -> Color(0xFFFECDD3) // Rose-300
                    else -> Color(0xFFBAE6FD) // Sky-300
                }
            }

            val iconBgColor = when (mission.category.uppercase()) {
                "MANNERS" -> VibrantEmerald
                "EDUCATION" -> VibrantIndigo
                "READING", "CREATIVITY" -> VibrantOrange
                "THINKING", "REFLECTION" -> VibrantRose
                else -> VibrantSky
            }

            val textColor = if (isCompleted) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            } else {
                when (mission.category.uppercase()) {
                    "MANNERS" -> VibrantEmeraldDark
                    "EDUCATION" -> VibrantIndigoDark
                    "READING", "CREATIVITY" -> VibrantOrangeDark
                    "THINKING", "REFLECTION" -> VibrantRoseDark
                    else -> VibrantSkyDark
                }
            }

            val categoryIconStr = when (mission.category.uppercase()) {
                "MANNERS" -> "♥"
                "EDUCATION" -> "+"
                "READING", "CREATIVITY" -> "✎"
                "THINKING", "REFLECTION" -> "◆"
                else -> "★"
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val strokeWidth = 4.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = bottomBorderColor,
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(iconBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = categoryIconStr,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Column {
                            Text(
                                text = mission.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${mission.durationMinutes} mins • Reward: ${mission.xpReward} XP",
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (!mission.isCompleted) {
                                viewModel.completeDailyMission(mission.id, mission.xpReward, mission.category)
                            }
                        },
                        modifier = Modifier.testTag("mission_check_${mission.id}")
                    ) {
                        Icon(
                            imageVector = if (mission.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Complete",
                            tint = if (mission.isCompleted) iconBgColor else textColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "AI Adaptive Mini Games 🎮",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        if (gameQuestion.isBlank() && !isGameLoading) {
            item {
                Text(
                    text = "Pick an educational subject to generate an instant, age-appropriate interactive game riddle challenge! Earn extra XP for correct answers!",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }

            items(subjects) { subject ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.loadGameChallenge(subject) }
                        .testTag("game_subject_$subject"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = BubbleYellow)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = subject, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        if (isGameLoading) {
                            Column(
                                modifier = Modifier.fillMaxWidth().height(180.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Assembling customized challenge...", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Game Challenge Question:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = gameQuestion,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Multiple Choices Option buttons
                                gameOptions.forEach { opt ->
                                    val letter = opt.take(1)
                                    val isSelected = gameSelectedAnswer == letter
                                    val isCorrectChoice = letter.equals(gameCorrectAnswer, ignoreCase = true)

                                    val btnColor = if (gameSelectedAnswer.isNotBlank()) {
                                        if (isCorrectChoice) MaterialTheme.colorScheme.tertiary else if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    }

                                    Button(
                                        onClick = {
                                            if (gameSelectedAnswer.isBlank()) {
                                                viewModel.submitAnswer(letter)
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = btnColor,
                                            contentColor = if (gameSelectedAnswer.isNotBlank() && (isCorrectChoice || isSelected)) Color.White else MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("game_option_$letter")
                                    ) {
                                        Text(text = opt, fontSize = 13.sp, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                                    }
                                }

                                // Explanation feedback
                                if (gameSelectedAnswer.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = if (gameSelectedAnswer.equals(gameCorrectAnswer, ignoreCase = true)) "🎉 Well done!" else "💡 Learning Tip!",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = gameExplanation,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.loadGameChallenge("") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text("Play Another Game Challenge")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 4: ACHIEVEMENTS & BADGES ---

@Composable
fun BadgesTab(profile: ChildProfile, badges: List<Badge>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "My Achievements & Badges 🏆",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Stats Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Total XP", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(text = "${profile.xp} Points", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Level", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(text = "${profile.level}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Next level calculation
                    val currentLevelBaseXp = (profile.level - 1) * 200
                    val nextLevelTargetXp = profile.level * 200
                    val levelProgress = if (nextLevelTargetXp > currentLevelBaseXp) {
                        (profile.xp - currentLevelBaseXp).toFloat() / (nextLevelTargetXp - currentLevelBaseXp).toFloat()
                    } else 0f

                    LinearProgressIndicator(
                        progress = { levelProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${(nextLevelTargetXp - profile.xp).coerceAtLeast(0)} XP until Level ${profile.level + 1}! Keep learning!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Badges Grid
        item {
            Text(
                text = "Unlocked Badges (${badges.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        if (badges.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No badges unlocked yet. Complete daily quests, read interactive stories, and talk with your AI Teacher to unlock beautiful achievements!",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(badges) { badge ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(colors = listOf(BubbleYellow, BubblePink))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(text = badge.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Text(text = badge.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}
