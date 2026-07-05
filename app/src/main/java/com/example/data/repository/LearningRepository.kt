package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.local.BadgeDao
import com.example.data.local.ChatMessageDao
import com.example.data.local.ChildProfileDao
import com.example.data.local.DailyMissionDao
import com.example.data.local.LearningProgressDao
import com.example.data.model.Badge
import com.example.data.model.ChatMessage
import com.example.data.model.ChildProfile
import com.example.data.model.DailyMission
import com.example.data.model.LearningProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LearningRepository(
    private val profileDao: ChildProfileDao,
    private val progressDao: LearningProgressDao,
    private val messageDao: ChatMessageDao,
    private val missionDao: DailyMissionDao,
    private val badgeDao: BadgeDao
) {
    val childProfile: Flow<ChildProfile?> = profileDao.getProfileFlow()
    val allProgress: Flow<List<LearningProgress>> = progressDao.getAllProgressFlow()
    val chatMessages: Flow<List<ChatMessage>> = messageDao.getAllMessagesFlow()
    val dailyMissions: Flow<List<DailyMission>> = missionDao.getMissionsFlow()
    val allBadges: Flow<List<Badge>> = badgeDao.getAllBadgesFlow()

    suspend fun saveProfile(profile: ChildProfile) = withContext(Dispatchers.IO) {
        profileDao.insertProfile(profile)
        // Auto-generate some initial progress records if they do not exist
        initializeProgress()
        // Auto-generate daily missions
        initializeMissions(profile.age)
    }

    suspend fun addXp(amount: Int) = withContext(Dispatchers.IO) {
        val currentProfile = profileDao.getProfile() ?: return@withContext
        val newXp = currentProfile.xp + amount
        // Level up formula: Level = (XP / 200) + 1
        val newLevel = (newXp / 200) + 1
        profileDao.insertProfile(currentProfile.copy(xp = newXp, level = newLevel))

        // Check and unlock level-based badges
        if (newLevel > currentProfile.level) {
            badgeDao.insertBadge(
                Badge(
                    title = "Level $newLevel Explorer",
                    description = "Reached Level $newLevel! Super Learner!",
                    iconName = "rocket"
                )
            )
        }
    }

    suspend fun completeMission(missionId: Int, xpReward: Int) = withContext(Dispatchers.IO) {
        missionDao.updateMissionStatus(missionId, true)
        addXp(xpReward)
    }

    suspend fun addChatMessage(sender: String, text: String, isVoice: Boolean = false) = withContext(Dispatchers.IO) {
        messageDao.insertMessage(ChatMessage(sender = sender, text = text, isVoice = isVoice))
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        messageDao.clearChat()
    }

    suspend fun updateScreenTime(minutes: Int) = withContext(Dispatchers.IO) {
        val currentProfile = profileDao.getProfile() ?: return@withContext
        val newUsed = (currentProfile.screenTimeUsedTodayMinutes + minutes).coerceAtLeast(0)
        profileDao.updateScreenTimeUsed(newUsed)
    }

    suspend fun saveProgress(progress: LearningProgress) = withContext(Dispatchers.IO) {
        progressDao.insertProgress(progress)
    }

    private suspend fun initializeProgress() {
        val categories = listOf(
            "Manners" to "Sharing & Gratitude",
            "School" to "Mathematics & Science",
            "Critical Thinking" to "Problem Solving",
            "Communication" to "Vocabulary Building",
            "Creativity" to "Story Creation",
            "Emotional" to "Empathy & Kindness"
        )
        for ((cat, sub) in categories) {
            val existing = progressDao.getProgress(cat, sub)
            if (existing == null) {
                progressDao.insertProgress(
                    LearningProgress(
                        category = cat,
                        subject = sub,
                        completedLessons = 0,
                        maxScore = 0,
                        masteryLevel = 0.0f
                    )
                )
            }
        }
    }

    suspend fun initializeMissions(age: Int) {
        missionDao.clearMissions()
        val list = listOf(
            DailyMission(category = "MANNERS", title = "Complete a 10-min Manners lesson", durationMinutes = 10, xpReward = 50),
            DailyMission(category = "EDUCATION", title = "Play an interactive Science/Math game", durationMinutes = 15, xpReward = 75),
            DailyMission(category = "READING", title = "Listen to one moral story with choices", durationMinutes = 10, xpReward = 50),
            DailyMission(category = "THINKING", title = "Solve a fun critical thinking puzzle", durationMinutes = 10, xpReward = 60),
            DailyMission(category = "CREATIVITY", title = "Do a quick imagination roleplay", durationMinutes = 10, xpReward = 60),
            DailyMission(category = "REFLECTION", title = "Reflect with your AI companion for 5 mins", durationMinutes = 5, xpReward = 40)
        )
        missionDao.insertMissions(list)
    }

    /**
     * Call the Gemini API directly for kid conversation with dynamic parenting safety system instructions.
     */
    suspend fun getAIResponse(userMessage: String, category: String = "General"): String = withContext(Dispatchers.IO) {
        val profile = profileDao.getProfile() ?: ChildProfile(name = "Friend", age = 6, interests = "Stories")
        val apiKey = BuildConfig.GEMINI_API_KEY

        val systemPrompt = getSystemInstructionForAge(profile, category)

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalFallbackResponse(userMessage, profile, category)
        }

        try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = userMessage)))),
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I am listening to you, my friend! Let's talk about something exciting."
        } catch (e: Exception) {
            getLocalFallbackResponse(userMessage, profile, category)
        }
    }

    /**
     * Generate interactive moral story where the child is the hero.
     */
    suspend fun generateStoryPrompt(topic: String, selectedOption: String? = null): String = withContext(Dispatchers.IO) {
        val profile = profileDao.getProfile() ?: ChildProfile(name = "Friend", age = 6, interests = "Stories")
        val apiKey = BuildConfig.GEMINI_API_KEY

        val optionPrompt = if (selectedOption != null) {
            "The child chose option: \"$selectedOption\". Continue the story, then offer 3 new branches."
        } else {
            "Start a new, exciting interactive moral story. Topic is \"$topic\"."
        }

        val systemPrompt = """
            You are a whimsical, friendly master storyteller for children.
            Write an interactive, child-safe moral story of exactly 3-4 paragraphs.
            The child's name is "${profile.name}", age ${profile.age}. Integrate their interests ("${profile.interests}") if possible.
            At the end of the story paragraph, write a clear, moral lesson, then provide exactly three options of what ${profile.name} can do next.
            Format your output strictly as:
            [STORY_TEXT]
            ...
            [MORAL]
            ...
            [OPTION_1] ...
            [OPTION_2] ...
            [OPTION_3] ...
        """.trimIndent()

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalFallbackStory(profile, topic, selectedOption)
        }

        try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = optionPrompt)))),
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: getLocalFallbackStory(profile, topic, selectedOption)
        } catch (e: Exception) {
            getLocalFallbackStory(profile, topic, selectedOption)
        }
    }

    /**
     * Generates a kid-friendly quiz/game question based on the subject.
     */
    suspend fun generateGameChallenge(subject: String): String = withContext(Dispatchers.IO) {
        val profile = profileDao.getProfile() ?: ChildProfile(name = "Friend", age = 7, interests = "Science")
        val apiKey = BuildConfig.GEMINI_API_KEY

        val prompt = "Create a single-question, age-appropriate interactive game challenge/riddle for a ${profile.age}-year-old child in $subject. Provide 4 multiple-choice answers, with a brief explanation of why the correct one is right."

        val systemPrompt = """
            You are an interactive, fun mini-game host.
            Generate a single-question challenge for a child named "${profile.name}", age ${profile.age}.
            Format your output strictly as:
            [QUESTION] Write the question here.
            [A] Option A
            [B] Option B
            [C] Option C
            [D] Option D
            [CORRECT] A, B, C, or D
            [EXPLANATION] Explain simply and cheerfully why that is the correct answer.
        """.trimIndent()

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalFallbackChallenge(profile.age, subject)
        }

        try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: getLocalFallbackChallenge(profile.age, subject)
        } catch (e: Exception) {
            getLocalFallbackChallenge(profile.age, subject)
        }
    }

    /**
     * Compiles parent analytical summary report.
     */
    suspend fun generateParentReport(progressList: List<LearningProgress>): String = withContext(Dispatchers.IO) {
        val profile = profileDao.getProfile() ?: ChildProfile(name = "Your Kid", age = 6, interests = "Stories")
        val apiKey = BuildConfig.GEMINI_API_KEY

        val progressString = progressList.joinToString("\n") { 
            "- ${it.category} (${it.subject}): Completed ${it.completedLessons} lessons, Mastery Level ${it.masteryLevel * 100}%"
        }

        val prompt = """
            Generate a detailed parent analytical summary report for a child:
            Name: ${profile.name}
            Age: ${profile.age}
            Current Level: ${profile.level}
            Interests: ${profile.interests}
            Progress:
            $progressString
        """.trimIndent()

        val systemPrompt = """
            You are an expert child development psychologist, friendly school teacher, and parenting advisor.
            Write a detailed, warm, and highly structured analytical parenting report.
            Do not list files or technical internals.
            Include:
            1. Executive summary of weekly progress.
            2. Strength analysis based on the child's age (${profile.age}) and interest ("${profile.interests}").
            3. Areas for development (personalized).
            4. Suggested daily routine adjustments and specific conversation starter topics to talk about at dinner.
            5. Recommended screen time guidelines (current is ${profile.screenTimeLimitMinutes} mins).
        """.trimIndent()

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalFallbackParentReport(profile, progressList)
        }

        try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: getLocalFallbackParentReport(profile, progressList)
        } catch (e: Exception) {
            getLocalFallbackParentReport(profile, progressList)
        }
    }

    private fun getSystemInstructionForAge(profile: ChildProfile, category: String): String {
        return when {
            profile.age <= 5 -> """
                You are a super sweet, warm, animated cartoon puppy-teacher named 'Buddy' for preschool children (ages 3-5).
                Use very simple, short sentences (maximum 8 words). Speak slowly, with high energy and pure cheerfulness.
                Introduce happy sound effects like '*Woof-Woof!*', '*Happy Bounce!*', or '*Sparkle!*' at the start of sentences.
                Explain things by color, shapes, and friendly animal characters.
                Focus on fundamental manners: sharing, saying thank you, washing hands, and being nice to friends.
                Current Topic / Context: $category. Always encourage the child and give them a big visual high-five!
            """.trimIndent()

            profile.age <= 8 -> """
                You are 'Captain Curie', a fun, enthusiastic science and story explorer buddy for primary school children (ages 6-8).
                Keep sentences clear, playful, and action-oriented. Use simple, exciting real-world analogies (e.g., 'Gravity is like invisible tape!').
                Praise effort and answer questions with great curiosity: 'Wow, what an incredible question!' or 'You're thinking like a scientist!'.
                Focus on: Math games, amazing nature and science, basic geography (exploring mountains and oceans), sharing, and understanding empathy.
                Current Topic / Context: $category. Keep the conversation extremely natural and hold a friendly dialogues instead of standard boring quizzes.
            """.trimIndent()

            profile.age <= 11 -> """
                You are 'Professor Spark', a friendly, encouraging mentor and co-explorer for intermediate school kids (ages 9-11).
                Speak naturally, like an inspiring, highly knowledgeable camp counselor.
                Incorporate interesting trivia, brainteasers, and critical thinking questions (e.g., 'What would happen if we didn't have plants on Earth?').
                Encourage self-reflection and emotional confidence. Ask for their opinion: 'What do you think about that?' or 'How would you solve this challenge?'.
                Current Topic / Context: $category. Explain things logically with clear, simple facts.
            """.trimIndent()

            else -> """
                You are 'Atlas', a cool, modern, highly supportive coach and academic tutor for teens/advanced children (ages 12-14).
                Speak like an intellectual older sibling or mentor. Keep it authentic, engaging, and respectful of their growing maturity.
                Provide deeply interesting context, logical brainteasers, and real-life problem solving.
                Prompt them to think about leadership, responsibility, science ethics, history, and advanced communication confidence.
                Current Topic / Context: $category. Keep content sophisticated yet extremely accessible, friendly, and completely child-safe.
            """.trimIndent()
        }
    }

    // --- LOCAL FALLBACK ENGINE FOR OFFLINE / CRASH-PROOF PERFORMANCE ---

    private fun getLocalFallbackResponse(userMessage: String, profile: ChildProfile, category: String): String {
        val lower = userMessage.lowercase()
        return when {
            profile.age <= 5 -> {
                if (lower.contains("hello") || lower.contains("hi")) {
                    "Hello ${profile.name}! *Woof!* I'm Buddy, your happy learning companion! 🐶 Are you ready to play and learn today?"
                } else if (lower.contains("math") || lower.contains("count")) {
                    "Yippee! Let's count! Can you count 1, 2, 3 stars in the sky? 🌟 You are doing so great, *Sparkle!*"
                } else {
                    "That is so wonderful, ${profile.name}! *Happy Bounce!* Buddy loves learning with you. Tell me, what's your favorite animal? 🦁"
                }
            }
            profile.age <= 8 -> {
                if (lower.contains("hello") || lower.contains("hi")) {
                    "Hey there, Explorer ${profile.name}! Captain Curie here! 🚀 What amazing thing should we explore today? Science, math, or a cool story?"
                } else if (lower.contains("why")) {
                    "Wow, that's a stellar question! You're thinking like a true scientist! Did you know that things fall down because of gravity, which acts like a giant invisible magnet? Isn't that awesome?"
                } else {
                    "High-five, ${profile.name}! That's really cool. Let's practice some math or tell a story together. What do you think?"
                }
            }
            else -> {
                "Hello ${profile.name}! I'm Atlas, your learning companion. It's great to connect. Whether you want to solve some logic puzzles, learn about geography, practice English, or just talk about your day, I'm here to support you. What's on your mind?"
            }
        }
    }

    private fun getLocalFallbackStory(profile: ChildProfile, topic: String, selectedOption: String?): String {
        return if (selectedOption != null) {
            """
                [STORY_TEXT]
                With a brave heart, ${profile.name} decided to: "$selectedOption". 
                As soon as they took this step, a magical pathway of glowing mushrooms lit up before them! 
                It led straight to the ancient tree of wisdom. There, a friendly owl named Barnaby was waiting. 
                "You made a very wise and helpful choice!" Barnaby hooted happily, offering a map of the golden castle.
                
                [MORAL]
                Being helpful and following the glowing path shows that taking constructive, safe steps always leads to exciting learning adventures.
                
                [OPTION_1] Look at Barnaby's magical map to find the secret castle door.
                [OPTION_2] Ask Barnaby to tell you a riddle about the stars.
                [OPTION_3] Share some of your favorite snacks with Barnaby to say thank you.
            """.trimIndent()
        } else {
            """
                [STORY_TEXT]
                Once upon a time in a colorful forest filled with laughter, ${profile.name} went out for a morning walk.
                Suddenly, they noticed a tiny puppy that was stuck behind a wooden fence, whimpering for help.
                ${profile.name} knew that helping others is a wonderful manner and builds a kinder world. 
                Their interests in "${profile.interests}" gave them an exciting idea to solve the problem!
                
                [MORAL]
                Kindness and sharing our help with those in need makes our neighborhood a beautiful place for everyone.
                
                [OPTION_1] Gently reach over the fence and lift the puppy out safely.
                [OPTION_2] Run and call a helpful neighbor or parent for extra hands.
                [OPTION_3] Sing a cheerful, happy song to keep the puppy calm while figuring out a plan.
            """.trimIndent()
        }
    }

    private fun getLocalFallbackChallenge(age: Int, subject: String): String {
        return if (subject.lowercase().contains("math") || age <= 5) {
            """
                [QUESTION]
                If you have 3 sweet red apples and your friendly classmate shares 2 more green apples with you, how many delicious apples do you have in total?
                [A] 3 apples
                [B] 4 apples
                [C] 5 apples
                [D] 6 apples
                [CORRECT] C
                [EXPLANATION]
                Excellent! When we add 3 and 2 together (3 + 2), we get 5 apples! Sharing and counting are double the fun!
            """.trimIndent()
        } else {
            """
                [QUESTION]
                Which beautiful planet in our solar system is known as the "Red Planet" because of its rusty iron-rich soil?
                [A] Venus
                [B] Mars
                [C] Jupiter
                [D] Saturn
                [CORRECT] B
                [EXPLANATION]
                Spot on! Mars is famously called the Red Planet! It has giant volcanoes and deep canyons, and space robots are exploring it right now!
            """.trimIndent()
        }
    }

    private fun getLocalFallbackParentReport(profile: ChildProfile, progressList: List<LearningProgress>): String {
        return """
            ### 📊 Parent Analytics Summary
            
            **Weekly Progress Overview**
            Your child, **${profile.name}** (Age ${profile.age}), has been learning brilliantly this week!
            - **Current Level**: ${profile.level}
            - **XP Accumulated**: ${profile.xp} XP
            - **Current Streak**: ${profile.streak} Days
            
            ---
            
            ### 🌟 Strength & Interest Analysis
            - **Active Interests**: "${profile.interests}" are being utilized successfully to contextualize lessons.
            - **Top Performing Skill**: Manners & Social Skills. ${profile.name} demonstrates high empathy, understanding, and excellent sharing behaviors during interactive scenarios.
            - **Critical Thinking**: Solves logic puzzles with active curiosity and shows high determination in multi-choice games.
            
            ---
            
            ### 📈 Growth Recommendations
            - **Vocabulary & Communication**: Practice speaking out loud with the voice companion. This will boost pronunciation accuracy and public-speaking confidence.
            - **Mathematics**: Introduce intermediate reasoning games to challenge their logic capacity.
            
            ---
            
            ### 💡 Screen Time & Parenting Tips
            - **Limit Set**: ${profile.screenTimeLimitMinutes} minutes.
            - **Active Screen Time Today**: ${profile.screenTimeUsedTodayMinutes} minutes.
            - **Dinner Conversation Starter**: Ask ${profile.name}: *"If you could explore Mars with a space robot today, what would you name the robot and why?"*
        """.trimIndent()
    }
}
