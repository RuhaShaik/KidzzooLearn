package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Badge
import com.example.data.model.ChildProfile
import com.example.data.model.DailyMission
import com.example.data.model.LearningProgress
import com.example.data.repository.LearningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class LearningViewModel(application: Application, val database: AppDatabase) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val repository = LearningRepository(
        profileDao = database.childProfileDao(),
        progressDao = database.learningProgressDao(),
        messageDao = database.chatMessageDao(),
        missionDao = database.dailyMissionDao(),
        badgeDao = database.badgeDao()
    )

    // Exposed Flows
    val childProfile: StateFlow<ChildProfile?> = repository.childProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allProgress: StateFlow<List<LearningProgress>> = repository.allProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyMissions = repository.dailyMissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBadges = repository.allBadges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _sttText = MutableStateFlow("")
    val sttText: StateFlow<String> = _sttText.asStateFlow()

    // Interactive Story State
    private val _storyText = MutableStateFlow("")
    val storyText: StateFlow<String> = _storyText.asStateFlow()

    private val _storyMoral = MutableStateFlow("")
    val storyMoral: StateFlow<String> = _storyMoral.asStateFlow()

    private val _storyOptions = MutableStateFlow<List<String>>(emptyList())
    val storyOptions: StateFlow<List<String>> = _storyOptions.asStateFlow()

    private val _isStoryLoading = MutableStateFlow(false)
    val isStoryLoading: StateFlow<Boolean> = _isStoryLoading.asStateFlow()

    // Game Challenge State
    private val _gameQuestion = MutableStateFlow("")
    val gameQuestion: StateFlow<String> = _gameQuestion.asStateFlow()

    private val _gameOptions = MutableStateFlow<List<String>>(emptyList())
    val gameOptions: StateFlow<List<String>> = _gameOptions.asStateFlow()

    private val _gameCorrectAnswer = MutableStateFlow("")
    val gameCorrectAnswer: StateFlow<String> = _gameCorrectAnswer.asStateFlow()

    private val _gameExplanation = MutableStateFlow("")
    val gameExplanation: StateFlow<String> = _gameExplanation.asStateFlow()

    private val _gameSelectedAnswer = MutableStateFlow("")
    val gameSelectedAnswer: StateFlow<String> = _gameSelectedAnswer.asStateFlow()

    private val _isGameLoading = MutableStateFlow(false)
    val isGameLoading: StateFlow<Boolean> = _isGameLoading.asStateFlow()

    // Parent Dashboard States
    private val _parentReport = MutableStateFlow("")
    val parentReport: StateFlow<String> = _parentReport.asStateFlow()

    private val _isReportLoading = MutableStateFlow(false)
    val isReportLoading: StateFlow<Boolean> = _isReportLoading.asStateFlow()

    // Text To Speech & Speech Recognizer Objects
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechIntent: Intent? = null

    init {
        // Initialize Android TTS
        try {
            tts = TextToSpeech(application, this)
        } catch (e: Exception) {
            Log.e("LearningViewModel", "Error creating TTS: ${e.message}")
        }

        // Initialize Android Speech Recognizer (if available)
        try {
            if (SpeechRecognizer.isRecognitionAvailable(application)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(application)
                speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                setupSpeechListener()
            }
        } catch (e: Exception) {
            Log.e("LearningViewModel", "Error creating SpeechRecognizer: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setPitch(1.2f) // Higher pitch to sound friendly like a cartoon character!
            tts?.setSpeechRate(0.95f) // Slightly slower rate for clarity
        }
    }

    fun speak(text: String) {
        if (tts != null) {
            _isSpeaking.value = true
            val cleanText = text
                .replace(Regex("\\[STORY_TEXT\\]|\\[MORAL\\]|\\[OPTION_\\d\\]"), "")
                .replace(Regex("\\[QUESTION\\]|\\[A\\]|\\[B\\]|\\[C\\]|\\[D\\]|\\[CORRECT\\]|\\[EXPLANATION\\]"), "")
                .trim()
            
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_ID")
            // Periodically check speech status or stop speaking animation after a short delay
            viewModelScope.launch {
                kotlinx.coroutines.delay(cleanText.length * 60L) // Estimate duration based on text length
                _isSpeaking.value = false
            }
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    // --- Speech To Text (Microphone Input) ---

    private fun setupSpeechListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                _sttText.value = "Listening..."
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                _isListening.value = false
            }

            override fun onError(error: Int) {
                _isListening.value = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions missing"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    else -> "Speech error"
                }
                _sttText.value = errorMsg
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    _sttText.value = recognizedText
                    // Send recognized message to AI
                    sendMessage(recognizedText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _sttText.value = matches[0]
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening() {
        if (speechRecognizer != null && speechIntent != null) {
            stopSpeaking()
            speechRecognizer?.startListening(speechIntent)
        } else {
            _sttText.value = "Speech recognition not supported on this device"
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    // --- Profile & Database Updates ---

    fun setupProfile(name: String, age: Int, interests: String) {
        viewModelScope.launch {
            val initialProfile = ChildProfile(
                name = name,
                age = age,
                interests = interests,
                xp = 0,
                level = 1,
                streak = 1,
                lastLearningDate = System.currentTimeMillis()
            )
            repository.saveProfile(initialProfile)
            
            // Welcome Greeting
            val welcomeText = "Hi $name! I am so excited to be your learning companion. We are going to have so much fun! Ask me any questions, or click on the buttons below to hear a story, solve a puzzle, or start a daily quest!"
            repository.addChatMessage("AI", welcomeText, false)
            speak(welcomeText)
        }
    }

    fun addXp(amount: Int) {
        viewModelScope.launch {
            repository.addXp(amount)
        }
    }

    fun completeDailyMission(id: Int, xp: Int, category: String) {
        viewModelScope.launch {
            repository.completeMission(id, xp)
            repository.addChatMessage("AI", "🌟 Amazing! You completed your ${category.lowercase()} mission and earned $xp XP! Keep up the brilliant work!", false)
            speak("Amazing! You completed your daily mission and earned $xp points!")
            
            // Update lesson completed in learning progress database
            val list = allProgress.value
            val targetProgress = list.find { it.category.lowercase() == category.lowercase() }
            if (targetProgress != null) {
                val newCompleted = targetProgress.completedLessons + 1
                val newScore = targetProgress.maxScore + 10
                val newMastery = (newCompleted * 0.15f).coerceAtMost(1.0f)
                repository.saveProgress(targetProgress.copy(
                    completedLessons = newCompleted,
                    maxScore = newScore,
                    masteryLevel = newMastery
                ))
            }
        }
    }

    fun updateScreenTime(minutes: Int) {
        viewModelScope.launch {
            repository.updateScreenTime(minutes)
        }
    }

    // --- AI Chat Conversations ---

    fun sendMessage(text: String, isVoice: Boolean = false) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addChatMessage("USER", text, isVoice)
            _isAILoading.value = true
            
            // Get AI Response based on current context
            val response = repository.getAIResponse(text)
            repository.addChatMessage("AI", response, false)
            _isAILoading.value = false
            speak(response)
            
            // Track learning conversation for progress and XP!
            addXp(10)
            updateScreenTime(2) // Estimate active screen time
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // --- AI Interactive Storyteller ---

    fun loadStory(topic: String, choice: String? = null) {
        viewModelScope.launch {
            _isStoryLoading.value = true
            stopSpeaking()
            
            val response = repository.generateStoryPrompt(topic, choice)
            parseStoryResponse(response)
            _isStoryLoading.value = false
            
            speak(_storyText.value + " " + _storyMoral.value)
            addXp(15) // XP for reading/listening
            updateScreenTime(3)
        }
    }

    private fun parseStoryResponse(response: String) {
        try {
            var story = ""
            var moral = ""
            val options = mutableListOf<String>()

            val lines = response.split("\n")
            var currentSection = "STORY"

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("[STORY_TEXT]")) {
                    currentSection = "STORY"
                    continue
                } else if (trimmed.startsWith("[MORAL]")) {
                    currentSection = "MORAL"
                    continue
                } else if (trimmed.startsWith("[OPTION_1]")) {
                    options.add(trimmed.replace("[OPTION_1]", "").trim())
                    continue
                } else if (trimmed.startsWith("[OPTION_2]")) {
                    options.add(trimmed.replace("[OPTION_2]", "").trim())
                    continue
                } else if (trimmed.startsWith("[OPTION_3]")) {
                    options.add(trimmed.replace("[OPTION_3]", "").trim())
                    continue
                }

                when (currentSection) {
                    "STORY" -> story += line + "\n"
                    "MORAL" -> moral += line + "\n"
                }
            }

            _storyText.value = story.trim()
            _storyMoral.value = moral.trim()
            if (options.isNotEmpty()) {
                _storyOptions.value = options
            } else {
                // Fallback options
                _storyOptions.value = listOf(
                    "Explore the glowing path further into the magical woods.",
                    "Ask your animal friend if there are other secrets around.",
                    "Go back home and share this wonderful adventure with parents."
                )
            }
        } catch (e: Exception) {
            _storyText.value = "An exciting story is unfolding..."
            _storyOptions.value = emptyList()
        }
    }

    // --- AI Game Challenges ---

    fun loadGameChallenge(subject: String) {
        viewModelScope.launch {
            _isGameLoading.value = true
            _gameSelectedAnswer.value = ""
            stopSpeaking()
            
            val response = repository.generateGameChallenge(subject)
            parseGameResponse(response)
            _isGameLoading.value = false
            
            speak(_gameQuestion.value)
            updateScreenTime(2)
        }
    }

    private fun parseGameResponse(response: String) {
        try {
            var question = ""
            val options = mutableListOf<String>()
            var correct = ""
            var explanation = ""

            val lines = response.split("\n")
            for (line in lines) {
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("[QUESTION]") -> {
                        question = trimmed.replace("[QUESTION]", "").trim()
                    }
                    trimmed.startsWith("[A]") -> {
                        options.add("A: " + trimmed.replace("[A]", "").trim())
                    }
                    trimmed.startsWith("[B]") -> {
                        options.add("B: " + trimmed.replace("[B]", "").trim())
                    }
                    trimmed.startsWith("[C]") -> {
                        options.add("C: " + trimmed.replace("[C]", "").trim())
                    }
                    trimmed.startsWith("[D]") -> {
                        options.add("D: " + trimmed.replace("[D]", "").trim())
                    }
                    trimmed.startsWith("[CORRECT]") -> {
                        correct = trimmed.replace("[CORRECT]", "").trim()
                    }
                    trimmed.startsWith("[EXPLANATION]") -> {
                        explanation = trimmed.replace("[EXPLANATION]", "").trim()
                    }
                }
            }

            _gameQuestion.value = question.ifBlank { "Here is a fun challenge for you! Let's solve it together!" }
            _gameOptions.value = options
            _gameCorrectAnswer.value = correct
            _gameExplanation.value = explanation
        } catch (e: Exception) {
            _gameQuestion.value = "Let's play a fun game! Choose your favorite topic!"
        }
    }

    fun submitAnswer(answerLetter: String) {
        _gameSelectedAnswer.value = answerLetter
        val isCorrect = answerLetter.equals(_gameCorrectAnswer.value, ignoreCase = true)
        
        viewModelScope.launch {
            if (isCorrect) {
                addXp(30) // Bonus XP for correct answers!
                speak("Whoop-de-doo! That is correct! Brilliant job!")
            } else {
                addXp(10) // Participation XP
                speak("Nice try! The correct answer was " + _gameCorrectAnswer.value + ". Let's read why!")
            }
        }
    }

    // --- Parent Dashboard Analytics ---

    fun loadParentReport() {
        viewModelScope.launch {
            _isReportLoading.value = true
            val response = repository.generateParentReport(allProgress.value)
            _parentReport.value = response
            _isReportLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tts?.shutdown()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("LearningViewModel", "Error shutting down engines: ${e.message}")
        }
    }
}
