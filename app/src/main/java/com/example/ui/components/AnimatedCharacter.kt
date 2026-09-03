package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BubblePink
import com.example.ui.theme.BubbleYellow
import com.example.ui.theme.VibrantAmber
import com.example.ui.theme.VibrantEmerald
import com.example.ui.theme.VibrantSky
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Highly expressive animated visual companion for children.
 * Reacts visually to conversation with facial expressions, head tilts, eye sparkles,
 * talking mouth oscillations, listening halos, thinking thought bubbles, and tap reactions.
 */
@Composable
fun AnimatedCharacter(
    isSpeaking: Boolean = false,
    isListening: Boolean = false,
    ageGroup: String = "6-8",
    emotion: CharacterEmotion = CharacterEmotion.HAPPY,
    characterType: String? = null,
    modifier: Modifier = Modifier,
    onCharacterTap: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val tapScale = remember { Animatable(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "character_anim")

    // Dynamic bob/hop animation
    val bobDuration = when (emotion) {
        CharacterEmotion.CELEBRATING -> 450
        CharacterEmotion.TALKING -> 700
        CharacterEmotion.THINKING -> 1800
        else -> 1200
    }
    val bobAmplitude = if (emotion == CharacterEmotion.CELEBRATING) 18f else 10f

    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -bobAmplitude,
        targetValue = bobAmplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(bobDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    // Eye blinking animation
    val blinkScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    // Talking mouth oscillation
    val talkMouthSize by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(160, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "talk"
    )

    // Listening glow oscillation
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 35f,
        targetValue = 65f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Thought bubble float for THINKING emotion
    val thoughtFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thought_float"
    )

    // Ear / Antenna playful wiggle
    val earWiggle by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ear_wiggle"
    )

    // Effective character type resolution
    val effectiveType = characterType ?: when (ageGroup) {
        "3-5" -> "puppy"
        "6-8" -> "astronaut"
        "9-11" -> "fox"
        else -> "owl"
    }

    // Color palette per persona
    val (primaryColor, accentColor, eyeColor) = when (effectiveType) {
        "puppy" -> Triple(Color(0xFFFFF176), Color(0xFFFF8A65), Color(0xFF4E342E)) // Warm Golden Puppy
        "astronaut" -> Triple(Color(0xFF4FC3F7), Color(0xFF7E57C2), Color(0xFF1A237E)) // Cosmic Spaceship Cyan & Purple
        "fox" -> Triple(Color(0xFFFF9800), Color(0xFFFFCC80), Color(0xFF3E2723)) // Playful Amber Fox
        "owl" -> Triple(Color(0xFFB39DDB), Color(0xFFFFD54F), Color(0xFF311B92)) // Wise Starry Lavender Owl
        else -> Triple(Color(0xFF80DEEA), Color(0xFFFF80AB), Color(0xFF263238)) // Tech Mentor Cyan
    }

    // Interactive Box wrapper
    Box(
        modifier = modifier
            .size(160.dp)
            .scale(tapScale.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                coroutineScope.launch {
                    tapScale.animateTo(1.22f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f))
                    tapScale.animateTo(1.0f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f))
                }
                onCharacterTap?.invoke()
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val headRotation = when (emotion) {
                CharacterEmotion.CURIOUS -> 9f
                CharacterEmotion.LISTENING -> -6f
                CharacterEmotion.THINKING -> 5f
                CharacterEmotion.CELEBRATING -> earWiggle * 1.5f
                else -> 0f
            }

            rotate(headRotation, pivot = Offset(size.width / 2, size.height / 2 + bobOffset)) {
                val center = Offset(size.width / 2, size.height / 2 + bobOffset)
                val radius = 56.dp.toPx()

                // 1. Ambient Glow Halo (Listening or Speaking or Celebrating)
                if (isListening || emotion == CharacterEmotion.LISTENING) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(VibrantAmber.copy(alpha = 0.45f), Color.Transparent),
                            center = center,
                            radius = radius + glowRadius
                        ),
                        center = center,
                        radius = radius + glowRadius
                    )
                } else if (isSpeaking || emotion == CharacterEmotion.TALKING) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(VibrantSky.copy(alpha = 0.4f), Color.Transparent),
                            center = center,
                            radius = radius + 32f
                        ),
                        center = center,
                        radius = radius + 32f
                    )
                } else if (emotion == CharacterEmotion.CELEBRATING) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(BubbleYellow.copy(alpha = 0.5f), Color.Transparent),
                            center = center,
                            radius = radius + 38f
                        ),
                        center = center,
                        radius = radius + 38f
                    )
                }

                // 2. Ears, Horns, Helmet Antenna, or Feather Tufts
                when (effectiveType) {
                    "puppy" -> {
                        // Floppy soft puppy ears with bounce
                        val earWiggleOffset = if (emotion == CharacterEmotion.CELEBRATING) earWiggle * 3f else earWiggle
                        drawOval(
                            color = accentColor,
                            topLeft = Offset(center.x - radius - 16f, center.y - radius + 12f + earWiggleOffset),
                            size = Size(38f, 92f)
                        )
                        drawOval(
                            color = accentColor,
                            topLeft = Offset(center.x + radius - 22f, center.y - radius + 12f - earWiggleOffset),
                            size = Size(38f, 92f)
                        )
                    }
                    "astronaut" -> {
                        // Space Cadet Antenna & Beacon Light
                        drawLine(
                            color = accentColor,
                            start = Offset(center.x, center.y - radius),
                            end = Offset(center.x, center.y - radius - 28f),
                            strokeWidth = 9f
                        )
                        drawCircle(
                            color = if (isSpeaking || isListening) VibrantEmerald else BubbleYellow,
                            center = Offset(center.x, center.y - radius - 33f),
                            radius = 13f
                        )
                        // Side comm pads
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(center.x - radius - 12f, center.y - 20f),
                            size = Size(20f, 44f),
                            cornerRadius = CornerRadius(10f)
                        )
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(center.x + radius - 8f, center.y - 20f),
                            size = Size(20f, 44f),
                            cornerRadius = CornerRadius(10f)
                        )
                    }
                    "fox" -> {
                        // Pointed energetic fox ears
                        val leftEarPath = Path().apply {
                            moveTo(center.x - radius + 10f, center.y - radius + 35f)
                            lineTo(center.x - radius - 12f, center.y - radius - 28f + earWiggle)
                            lineTo(center.x - 18f, center.y - radius + 10f)
                            close()
                        }
                        drawPath(leftEarPath, color = primaryColor)
                        // Inner pink ear
                        val leftInnerPath = Path().apply {
                            moveTo(center.x - radius + 14f, center.y - radius + 28f)
                            lineTo(center.x - radius - 4f, center.y - radius - 16f + earWiggle)
                            lineTo(center.x - 22f, center.y - radius + 12f)
                            close()
                        }
                        drawPath(leftInnerPath, color = BubblePink)

                        val rightEarPath = Path().apply {
                            moveTo(center.x + radius - 10f, center.y - radius + 35f)
                            lineTo(center.x + radius + 12f, center.y - radius - 28f - earWiggle)
                            lineTo(center.x + 18f, center.y - radius + 10f)
                            close()
                        }
                        drawPath(rightEarPath, color = primaryColor)
                        val rightInnerPath = Path().apply {
                            moveTo(center.x + radius - 14f, center.y - radius + 28f)
                            lineTo(center.x + radius + 4f, center.y - radius - 16f - earWiggle)
                            lineTo(center.x + 22f, center.y - radius + 12f)
                            close()
                        }
                        drawPath(rightInnerPath, color = BubblePink)
                    }
                    "owl" -> {
                        // Wise Owl Feather Ear Tufts
                        val leftTuft = Path().apply {
                            moveTo(center.x - radius + 15f, center.y - radius + 25f)
                            lineTo(center.x - radius + 5f, center.y - radius - 20f)
                            lineTo(center.x - 20f, center.y - radius + 5f)
                            close()
                        }
                        drawPath(leftTuft, color = accentColor)
                        val rightTuft = Path().apply {
                            moveTo(center.x + radius - 15f, center.y - radius + 25f)
                            lineTo(center.x + radius - 5f, center.y - radius - 20f)
                            lineTo(center.x + 20f, center.y - radius + 5f)
                            close()
                        }
                        drawPath(rightTuft, color = accentColor)
                    }
                    else -> {
                        // High-tech sleek side ear pads
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(center.x - radius - 10f, center.y - 25f),
                            size = Size(20f, 50f),
                            cornerRadius = CornerRadius(10f)
                        )
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(center.x + radius - 10f, center.y - 25f),
                            size = Size(20f, 50f),
                            cornerRadius = CornerRadius(10f)
                        )
                    }
                }

                // 3. Main Head Sphere
                drawCircle(
                    color = primaryColor,
                    center = center,
                    radius = radius
                )

                // 4. Cheerful Rosy Cheeks
                val cheekGlow = if (emotion == CharacterEmotion.CELEBRATING || emotion == CharacterEmotion.HAPPY) 0.75f else 0.45f
                drawCircle(
                    color = BubblePink.copy(alpha = cheekGlow),
                    center = Offset(center.x - 38f, center.y + 14f),
                    radius = 12f
                )
                drawCircle(
                    color = BubblePink.copy(alpha = cheekGlow),
                    center = Offset(center.x + 38f, center.y + 14f),
                    radius = 12f
                )

                // 5. Distinctive Snout / Beak / Glasses per character
                when (effectiveType) {
                    "puppy" -> {
                        // Cute puppy nose
                        drawOval(
                            color = Color(0xFF3E2723),
                            topLeft = Offset(center.x - 8f, center.y + 5f),
                            size = Size(16f, 10f)
                        )
                    }
                    "fox" -> {
                        // Fox nose tip & cream muzzle
                        drawOval(
                            color = Color.White.copy(alpha = 0.85f),
                            topLeft = Offset(center.x - 18f, center.y + 2f),
                            size = Size(36f, 22f)
                        )
                        drawCircle(
                            color = Color(0xFF212121),
                            center = Offset(center.x, center.y + 6f),
                            radius = 5f
                        )
                    }
                    "owl" -> {
                        // Owl Spectacles & small golden beak
                        drawCircle(
                            color = Color(0xFFFFD54F),
                            center = Offset(center.x - 26f, center.y - 12f),
                            radius = 20f,
                            style = Stroke(width = 4f)
                        )
                        drawCircle(
                            color = Color(0xFFFFD54F),
                            center = Offset(center.x + 26f, center.y - 12f),
                            radius = 20f,
                            style = Stroke(width = 4f)
                        )
                        drawLine(
                            color = Color(0xFFFFD54F),
                            start = Offset(center.x - 6f, center.y - 12f),
                            end = Offset(center.x + 6f, center.y - 12f),
                            strokeWidth = 4f
                        )
                        // Beak
                        val beak = Path().apply {
                            moveTo(center.x - 7f, center.y + 2f)
                            lineTo(center.x + 7f, center.y + 2f)
                            lineTo(center.x, center.y + 14f)
                            close()
                        }
                        drawPath(beak, color = Color(0xFFFF8F00))
                    }
                    "astronaut" -> {
                        // Sleek helmet visor shine
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                                center = Offset(center.x - 25f, center.y - 25f),
                                radius = 28f
                            ),
                            topLeft = Offset(center.x - 42f, center.y - 35f),
                            size = Size(40f, 20f)
                        )
                    }
                }

                // 6. Facial Expressions (Eyes & Mouth)
                val eyeY = center.y - 14f

                when (emotion) {
                    CharacterEmotion.CELEBRATING -> {
                        // Starry joyful eyes (* *)
                        drawStar(center = Offset(center.x - 28f, eyeY), radius = 10f, color = eyeColor)
                        drawStar(center = Offset(center.x + 28f, eyeY), radius = 10f, color = eyeColor)

                        // Confetti sparkles floating around head
                        drawCircle(color = BubblePink, center = Offset(center.x - radius - 6f, center.y - 20f + earWiggle), radius = 4f)
                        drawCircle(color = BubbleYellow, center = Offset(center.x + radius + 10f, center.y - 15f - earWiggle), radius = 5f)
                        drawCircle(color = VibrantSky, center = Offset(center.x - 25f, center.y - radius - 15f), radius = 4f)
                    }
                    CharacterEmotion.ENCOURAGING -> {
                        // Gentle happy crescent eye-smile (^ ^)
                        drawArc(
                            color = eyeColor,
                            startAngle = 190f,
                            sweepAngle = 160f,
                            useCenter = false,
                            topLeft = Offset(center.x - 36f, eyeY - 8f),
                            size = Size(18f, 16f),
                            style = Stroke(width = 5f)
                        )
                        drawArc(
                            color = eyeColor,
                            startAngle = 190f,
                            sweepAngle = 160f,
                            useCenter = false,
                            topLeft = Offset(center.x + 18f, eyeY - 8f),
                            size = Size(18f, 16f),
                            style = Stroke(width = 5f)
                        )
                    }
                    CharacterEmotion.THINKING -> {
                        // Eyes looking up & right thoughtfully
                        drawOval(
                            color = eyeColor,
                            topLeft = Offset(center.x - 28f, eyeY - 8f),
                            size = Size(13f, 13f)
                        )
                        drawOval(
                            color = eyeColor,
                            topLeft = Offset(center.x + 20f, eyeY - 8f),
                            size = Size(13f, 13f)
                        )
                        // Floating thought bubbles
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.8f),
                            center = Offset(center.x + radius + 4f, center.y - radius + 15f - thoughtFloat),
                            radius = 6f
                        )
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.85f),
                            center = Offset(center.x + radius + 16f, center.y - radius - 5f - thoughtFloat),
                            radius = 10f
                        )
                    }
                    CharacterEmotion.CURIOUS -> {
                        // One eye wider, curious tilt
                        drawOval(
                            color = eyeColor,
                            topLeft = Offset(center.x - 30f, eyeY - 9f),
                            size = Size(15f, 18f)
                        )
                        drawOval(
                            color = eyeColor,
                            topLeft = Offset(center.x + 18f, eyeY - 5f),
                            size = Size(12f, 13f)
                        )
                    }
                    else -> {
                        // Standard Natural Blinking Eyes
                        val effectiveEyeHeight = (16f * blinkScale).coerceAtLeast(2.5f)
                        drawOval(
                            color = eyeColor,
                            topLeft = Offset(center.x - 29f, eyeY - (effectiveEyeHeight / 2)),
                            size = Size(12f, effectiveEyeHeight)
                        )
                        drawOval(
                            color = eyeColor,
                            topLeft = Offset(center.x + 17f, eyeY - (effectiveEyeHeight / 2)),
                            size = Size(12f, effectiveEyeHeight)
                        )

                        // Twinkling white spark in eyes when open
                        if (blinkScale > 0.35f) {
                            drawCircle(
                                color = Color.White,
                                center = Offset(center.x - 26f, eyeY - 4f),
                                radius = 3.2f
                            )
                            drawCircle(
                                color = Color.White,
                                center = Offset(center.x + 20f, eyeY - 4f),
                                radius = 3.2f
                            )
                        }
                    }
                }

                // 7. Mouth Animation (Talking vs Smiling vs Celebrating)
                if (isSpeaking || emotion == CharacterEmotion.TALKING) {
                    // Dynamic talking mouth oval
                    drawOval(
                        color = Color(0xFFD81B60),
                        topLeft = Offset(center.x - 11f, center.y + 15f),
                        size = Size(22f, talkMouthSize)
                    )
                } else if (emotion == CharacterEmotion.CELEBRATING) {
                    // Big open happy smile
                    drawArc(
                        color = Color(0xFFD81B60),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(center.x - 14f, center.y + 12f),
                        size = Size(28f, 22f)
                    )
                } else if (emotion == CharacterEmotion.THINKING) {
                    // Small inquisitive 'o' mouth
                    drawCircle(
                        color = Color(0xFF4E342E),
                        center = Offset(center.x, center.y + 18f),
                        radius = 4f
                    )
                } else {
                    // Sweet curved friendly smile
                    drawArc(
                        color = Color(0xFF4E342E),
                        startAngle = 10f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(center.x - 13f, center.y + 14f),
                        size = Size(26f, 16f),
                        style = Stroke(width = 5f)
                    )
                }
            }
        }
    }
}

// Utility to draw a cute 5-point star on Canvas
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStar(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path()
    val points = 5
    val innerRadius = radius * 0.45f
    val angleStep = Math.PI / points
    var angle = -Math.PI / 2.0

    path.moveTo(
        (center.x + radius * cos(angle)).toFloat(),
        (center.y + radius * sin(angle)).toFloat()
    )

    for (i in 1 until points * 2) {
        angle += angleStep
        val r = if (i % 2 == 0) radius else innerRadius
        path.lineTo(
            (center.x + r * cos(angle)).toFloat(),
            (center.y + r * sin(angle)).toFloat()
        )
    }
    path.close()
    drawPath(path, color)
}

/**
 * Primary Composable using CompanionPersona directly.
 */
@Composable
fun AnimatedCharacter(
    persona: CompanionPersona,
    emotion: CharacterEmotion = CharacterEmotion.HAPPY,
    isSpeaking: Boolean = false,
    isListening: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedCharacter(
        isSpeaking = isSpeaking,
        isListening = isListening,
        emotion = emotion,
        characterType = persona.id,
        modifier = modifier,
        onCharacterTap = onClick
    )
}

/**
 * Overloaded backward-compatible AnimatedCharacter Composable using ageGroup.
 */
@Composable
fun AnimatedCharacter(
    isSpeaking: Boolean = false,
    isListening: Boolean = false,
    ageGroup: String = "6-8",
    modifier: Modifier = Modifier
) {
    val emotion = when {
        isSpeaking -> CharacterEmotion.TALKING
        isListening -> CharacterEmotion.LISTENING
        else -> CharacterEmotion.HAPPY
    }
    AnimatedCharacter(
        isSpeaking = isSpeaking,
        isListening = isListening,
        ageGroup = ageGroup,
        emotion = emotion,
        modifier = modifier
    )
}

