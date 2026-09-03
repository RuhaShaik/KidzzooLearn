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

    suspend fun unlockBadge(title: String, description: String, iconName: String): Boolean = withContext(Dispatchers.IO) {
        val existing = badgeDao.getBadgeByTitle(title)
        if (existing == null) {
            badgeDao.insertBadge(
                Badge(
                    title = title,
                    description = description,
                    iconName = iconName
                )
            )
            true
        } else {
            false
        }
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
     * Call the Gemini API directly for child companion conversation with multi-turn memory and safety instructions.
     */
    suspend fun getAIResponse(
        userMessage: String,
        category: String = "General",
        companionPersonaId: String = "puppy"
    ): String = withContext(Dispatchers.IO) {
        val profile = profileDao.getProfile() ?: ChildProfile(name = "Friend", age = 6, interests = "Stories")
        val apiKey = BuildConfig.GEMINI_API_KEY

        val systemPrompt = getSystemInstructionForCompanion(profile, category, companionPersonaId)

        // Retrieve recent messages for multi-turn conversation memory
        val recentMessages = try {
            messageDao.getRecentMessages(8).reversed()
        } catch (e: Exception) {
            emptyList()
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalFallbackResponse(userMessage, profile, companionPersonaId, recentMessages)
        }

        try {
            val contentsList = mutableListOf<Content>()

            // Append historical turns so Gemini understands conversational flow
            for (msg in recentMessages) {
                val role = if (msg.sender == "USER") "user" else "model"
                contentsList.add(Content(role = role, parts = listOf(Part(text = msg.text))))
            }

            // Append current user message
            contentsList.add(Content(role = "user", parts = listOf(Part(text = userMessage))))

            val request = GenerateContentRequest(
                contents = contentsList,
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: getLocalFallbackResponse(userMessage, profile, companionPersonaId, recentMessages)
        } catch (e: Exception) {
            getLocalFallbackResponse(userMessage, profile, companionPersonaId, recentMessages)
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

    private fun getSystemInstructionForCompanion(
        profile: ChildProfile,
        category: String,
        personaId: String
    ): String {
        val personaIdentity = when (personaId) {
            "astronaut" -> "You are 'Captain Curie', an enthusiastic cosmic spaceship explorer and learning partner who loves space, science, and curiosity."
            "fox" -> "You are 'Spark the Fox', a clever, energetic, and playful learning companion who loves riddles, nature, and puzzles."
            "owl" -> "You are 'Luna the Star Owl', a gentle, wise, and soothing companion who loves fascinating facts, reading, and thoughtful questions."
            else -> "You are 'Buddy the Puppy', a super cheerful, affectionate, and playful puppy learning companion who loves making learning fun and rewarding."
        }

        return """
            $personaIdentity
            You are talking live with a child named "${profile.name}", age ${profile.age} (interests: "${profile.interests}").
            
            CRITICAL GOALS:
            1. You are a living animated companion and learning partner, NOT a cold chatbot. Speak directly, warmly, and enthusiastically to ${profile.name}.
            2. Greet the child warmly when starting or meeting.
            3. Ask the child questions to spark curiosity and test understanding.
            4. Listen to and understand what the child says.
            5. Respond intelligently and dynamically to their exact input.
            6. Ask follow-up questions to keep the conversation flowing naturally.
            7. Explain concepts in a simple, child-friendly way using everyday examples and analogies appropriate for age ${profile.age}.
            8. GIVE HINTS: If the child says "I don't know", asks for a hint, or seems stuck, give a fun, gentle clue without immediately spoiling the answer.
            9. GENTLY CORRECT MISTAKES: Never say "No", "Wrong", or "That's incorrect". Always validate their effort first! Example:
               Child: "Elephant."
               Companion: "Good try! Elephants are the biggest land animals. But the biggest animal on Earth is actually the blue whale! 🐋 Want to learn something interesting about it?"
            10. ENCOURAGE & CELEBRATE: When the child gets something right or shares a great thought, praise them enthusiastically!
            11. VISUAL EXPRESSIONS: Start your response with exactly one of these visual emotion tags:
                [MOOD: HAPPY] - for cheerful greetings, general chatting, or happy remarks.
                [MOOD: CURIOUS] - when asking a question or pondering an interesting puzzle.
                [MOOD: THINKING] - when calculating, reflecting, or searching memory.
                [MOOD: ENCOURAGING] - when giving a gentle hint, soft correction, or comforting words.
                [MOOD: CELEBRATING] - when the child gets an answer right or reaches a breakthrough!
                [MOOD: TALKING] - when explaining a fascinating fact.

            CHILD SAFETY & CONTENT BOUNDARIES:
            - Strictly child-safe at all times. Zero profanity, violence, scary themes, adult topics, or negative self-talk.
            - Never ask for or store private personal information (home address, phone number, passwords, school address). If the child offers this, gently redirect: "Let's keep your private details safe! What fun topic shall we explore next?"
            - Keep each response concise (2 to 4 friendly sentences) so it fits in a conversation bubble and can be easily spoken aloud with text-to-speech.
        """.trimIndent()
    }

    // --- LOCAL FALLBACK ENGINE FOR OFFLINE / CRASH-PROOF PERFORMANCE ---

    private fun getLocalFallbackResponse(
        userMessage: String,
        profile: ChildProfile,
        personaId: String,
        recentMessages: List<ChatMessage> = emptyList()
    ): String {
        val lower = userMessage.trim().lowercase()

        // 1. Child Safety Redirection
        if (lower.contains("password") || lower.contains("phone") || lower.contains("address") || lower.contains("where do you live")) {
            return "[MOOD: ENCOURAGING] Remember ${profile.name}, we always keep our private details safe! Let's explore something fun instead—what's your favorite animal or superpower?"
        }

        // 2. Greetings
        if (lower.matches(Regex(".*\\b(hi|hello|hey|greetings|good morning|good afternoon)\\b.*"))) {
            return when (personaId) {
                "astronaut" -> "[MOOD: HAPPY] Greetings, Space Cadet ${profile.name}! 🚀 Captain Curie here! What stellar topic are we exploring today?"
                "fox" -> "[MOOD: HAPPY] Hi ${profile.name}! 🦊 Spark here! I'm ready to solve some super fun riddles with you. What are you learning today?"
                "owl" -> "[MOOD: HAPPY] Hoo-hoo! Hello dear ${profile.name}! 🦉 Luna is here to learn with you. What wonder of the world shall we discover?"
                else -> "[MOOD: HAPPY] Hi ${profile.name}! 👋 Woof! I'm Buddy, your learning buddy! What fun thing are you learning today?"
            }
        }

        // 3. Animal topic starter
        if (lower.contains("animal") || lower.contains("animals") || lower.contains("zoo")) {
            return "[MOOD: CURIOUS] Awesome! 🐘 Can you tell me which animal is the biggest animal on Earth?"
        }

        // 4. Animal answer: Elephant (from user prompt example)
        if (lower.contains("elephant")) {
            return "[MOOD: ENCOURAGING] Good try! Elephants are the biggest land animals. But the biggest animal on Earth is actually the blue whale! 🐋 Want to learn something interesting about it?"
        }

        // 5. Whale reaction
        if (lower.contains("whale") || lower.contains("blue whale")) {
            return "[MOOD: CELEBRATING] You got it! 🐋 A blue whale's tongue weighs as much as an entire elephant, and its heart is the size of a small car! Isn't nature amazing?"
        }

        // 6. Hints requested
        if (lower.contains("hint") || lower.contains("clue") || lower.contains("i don't know") || lower.contains("dont know") || lower.contains("help")) {
            return "[MOOD: CURIOUS] Here's a clue! 💡 Think about creatures that swim in the deep blue ocean! It breathes through a blowhole at the top of its head. Want to guess now?"
        }

        // 7. Math & Numbers
        if (lower.contains("math") || lower.contains("count") || lower.contains("calculate")) {
            return "[MOOD: CURIOUS] Let's try a fun math puzzle! 🔢 If you have 3 magic stars 🌟 and you find 2 more in the galaxy, how many stars do you have in all?"
        }
        if (lower == "5" || lower == "five") {
            return "[MOOD: CELEBRATING] High five! ✋ That is 5 stars! You solved it so fast, ${profile.name}! You have true math superpowers!"
        }

        // 8. Jokes
        if (lower.contains("joke") || lower.contains("funny")) {
            return "[MOOD: HAPPY] Why did the dinosaur cross the road? Because chickens didn't exist yet! 🦖 😂 Did that make you giggle?"
        }

        // 9. Riddles
        if (lower.contains("riddle")) {
            return "[MOOD: CURIOUS] Riddle time! 🧩 What has hands, but cannot clap? Take your time and think!"
        }
        if (lower.contains("clock") || lower.contains("watch")) {
            return "[MOOD: CELEBRATING] Hooray! You got it right! 🎉 A clock! You're a super detective!"
        }

        // 10. Space / Stars
        if (lower.contains("space") || lower.contains("planet") || lower.contains("star") || lower.contains("moon")) {
            return "[MOOD: TALKING] 🚀 Did you know that footprints on the Moon will stay there for millions of years because there is no wind to blow them away? What planet would you love to visit?"
        }

        // 11. Why questions
        if (lower.startsWith("why") || lower.contains("why is") || lower.contains("how does")) {
            return "[MOOD: THINKING] What an inquisitive thinker you are, ${profile.name}! When we look closely at things, everything has a cool scientific reason. Would you like me to explain step-by-step?"
        }

        // Default natural conversational companion response
        return when (personaId) {
            "astronaut" -> "[MOOD: HAPPY] That is an extraordinary observation, Explorer ${profile.name}! 🚀 Tell me more about that, or ask me anything you want to explore!"
            "fox" -> "[MOOD: HAPPY] You're so clever, ${profile.name}! 🦊 That sparks my curiosity! What else should we investigate together?"
            "owl" -> "[MOOD: HAPPY] How wonderful, dear ${profile.name}! 🦉 Every conversation teaches us something new. What would you like to know next?"
            else -> "[MOOD: HAPPY] That sounds so cool, ${profile.name}! 🐶 Buddy loves learning with you! What's your next big idea?"
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
