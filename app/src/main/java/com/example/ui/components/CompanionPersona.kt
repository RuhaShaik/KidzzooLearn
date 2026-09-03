package com.example.ui.components

enum class CharacterEmotion {
    HAPPY,        // Default cheerful, smiling, twinkling eyes
    LISTENING,    // Attentive, listening glow, ears perked
    TALKING,      // Animated mouth movement, synchronized bob
    THINKING,     // Eyes looking up with curiosity, thoughtful float
    CURIOUS,      // Playful head tilt, wide inquisitive eyes
    ENCOURAGING,  // Warm reassuring eye smile, gentle affirmation
    CELEBRATING   // Joyful hopping bounce, star sparkles
}

data class CompanionPersona(
    val id: String,
    val name: String,
    val title: String,
    val avatarEmoji: String,
    val greeting: String,
    val description: String,
    val voicePitch: Float = 1.2f,
    val speechRate: Float = 0.95f
) {
    val displayName: String get() = title
    val subtitle: String get() = description
}

val AvailableCompanionPersonas = listOf(
    CompanionPersona(
        id = "puppy",
        name = "Buddy",
        title = "Buddy the Puppy 🐶",
        avatarEmoji = "🐶",
        greeting = "Woof-woof! Hi there! I'm Buddy! What exciting thing are we learning today?",
        description = "Super warm, playful, and cheerful puppy buddy.",
        voicePitch = 1.35f,
        speechRate = 0.95f
    ),
    CompanionPersona(
        id = "astronaut",
        name = "Captain Curie",
        title = "Captain Curie 🚀",
        avatarEmoji = "🚀",
        greeting = "Greetings, Space Cadet! Captain Curie ready for launch! What stellar topic shall we explore?",
        description = "Adventurous cosmic explorer who loves science, space, and discovery.",
        voicePitch = 1.15f,
        speechRate = 1.0f
    ),
    CompanionPersona(
        id = "fox",
        name = "Spark",
        title = "Spark the Fox 🦊",
        avatarEmoji = "🦊",
        greeting = "Hi friend! I'm Spark! I love puzzles, riddles, and tricky questions! Ready for a brain teaser?",
        description = "Clever, energetic fox who loves riddles, nature, and creative thinking.",
        voicePitch = 1.2f,
        speechRate = 1.05f
    ),
    CompanionPersona(
        id = "owl",
        name = "Luna",
        title = "Luna the Star Owl 🦉",
        avatarEmoji = "🦉",
        greeting = "Hoo-hoo! Hello little thinker! I'm Luna. What wonderful story or mystery shall we uncover?",
        description = "Gentle, wise owl who loves reading, calming stories, and fascinating facts.",
        voicePitch = 1.1f,
        speechRate = 0.9f
    )
)
