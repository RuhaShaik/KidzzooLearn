package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BubblePink
import com.example.ui.theme.BubbleYellow
import com.example.ui.theme.VibrantSky
import com.example.ui.theme.VibrantAmber
import com.example.ui.theme.VibrantEmerald

@Composable
fun AnimatedCharacter(
    isSpeaking: Boolean,
    isListening: Boolean,
    ageGroup: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "character")
    
    // Float/bob animation
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    // Eye blinking animation
    val blinkScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    // Talking mouth oscillation
    val talkMouthSize by infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(180, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "talk"
    )

    // Listening glow oscillation
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Colors depending on age group / character persona
    val (primaryColor, accentColor, eyeColor) = when (ageGroup) {
        "3-5" -> Triple(Color(0xFFFFF176), Color(0xFFFF8A65), Color(0xFF5D4037)) // Buddy the Puppy
        "6-8" -> Triple(Color(0xFF4FC3F7), Color(0xFFBA68C8), Color(0xFF1565C0)) // Captain Curie (Spaceship blue)
        "9-11" -> Triple(Color(0xFF81C784), Color(0xFFFFD54F), Color(0xFF2E7D32)) // Professor Spark (Lime green)
        else -> Triple(Color(0xFFFF8A80), Color(0xFF26C6DA), Color(0xFF37474F)) // Atlas (Modern coral)
    }

    Box(modifier = modifier.size(160.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2 + bobOffset)
            val radius = 60.dp.toPx()

            // 1. Draw glowing background when listening or speaking
            if (isListening) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(PlayfulSecondaryAlpha, Color.Transparent),
                        center = center,
                        radius = radius + glowRadius
                    ),
                    center = center,
                    radius = radius + glowRadius
                )
            } else if (isSpeaking) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(PlayfulPrimaryAlpha, Color.Transparent),
                        center = center,
                        radius = radius + 30f
                    ),
                    center = center,
                    radius = radius + 30f
                )
            }

            // 2. Draw Ears or Antennae based on ageGroup
            when (ageGroup) {
                "3-5" -> {
                    // Puppy Ears
                    drawOval(
                        color = accentColor,
                        topLeft = Offset(center.x - radius - 15f, center.y - radius + 10f),
                        size = Size(40f, 90f)
                    )
                    drawOval(
                        color = accentColor,
                        topLeft = Offset(center.x + radius - 25f, center.y - radius + 10f),
                        size = Size(40f, 90f)
                    )
                }
                "6-8" -> {
                    // Spaceship Captain Helmet Antenna
                    drawLine(
                        color = accentColor,
                        start = Offset(center.x, center.y - radius),
                        end = Offset(center.x, center.y - radius - 30f),
                        strokeWidth = 8f
                    )
                    drawCircle(
                        color = BubbleYellow,
                        center = Offset(center.x, center.y - radius - 35f),
                        radius = 12f
                    )
                }
                "9-11" -> {
                    // Smart Professor glasses bridge & ears
                    drawCircle(
                        color = accentColor,
                        center = Offset(center.x - radius + 5f, center.y - 10f),
                        radius = 15f
                    )
                    drawCircle(
                        color = accentColor,
                        center = Offset(center.x + radius - 5f, center.y - 10f),
                        radius = 15f
                    )
                }
                else -> {
                    // Atlas Sleek Futuristic Side Audio Cushions
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(center.x - radius - 10f, center.y - 25f),
                        size = Size(20f, 50f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(center.x + radius - 10f, center.y - 25f),
                        size = Size(20f, 50f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
                    )
                }
            }

            // 3. Draw Main Head Body
            drawCircle(
                color = primaryColor,
                center = center,
                radius = radius
            )

            // 4. Draw Cute Rosy Cheeks
            drawCircle(
                color = BubblePink.copy(alpha = 0.6f),
                center = Offset(center.x - 40f, center.y + 15f),
                radius = 12f
            )
            drawCircle(
                color = BubblePink.copy(alpha = 0.6f),
                center = Offset(center.x + 40f, center.y + 15f),
                radius = 12f
            )

            // 5. Draw Eyes (Blinking)
            val eyeHeight = 16f * blinkScale
            drawOval(
                color = eyeColor,
                topLeft = Offset(center.x - 30f, center.y - 15f - (eyeHeight / 2)),
                size = Size(12f, eyeHeight.coerceAtLeast(2f))
            )
            drawOval(
                color = eyeColor,
                topLeft = Offset(center.x + 18f, center.y - 15f - (eyeHeight / 2)),
                size = Size(12f, eyeHeight.coerceAtLeast(2f))
            )

            // Cute white spark in eyes when open
            if (blinkScale > 0.3f) {
                drawCircle(
                    color = Color.White,
                    center = Offset(center.x - 27f, center.y - 17f),
                    radius = 3f
                )
                drawCircle(
                    color = Color.White,
                    center = Offset(center.x + 21f, center.y - 17f),
                    radius = 3f
                )
            }

            // 6. Draw Mouth (Talking vs Smile)
            if (isSpeaking) {
                // Moving mouth oval
                drawOval(
                    color = Color(0xFFD81B60),
                    topLeft = Offset(center.x - 10f, center.y + 15f),
                    size = Size(20f, talkMouthSize)
                )
            } else {
                // Smiling crescent line
                drawArc(
                    color = Color(0xFF4E342E),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(center.x - 12f, center.y + 12f),
                    size = Size(24f, 16f),
                    style = Stroke(width = 5f)
                )
            }
        }
    }
}

private val PlayfulPrimaryAlpha = VibrantSky.copy(alpha = 0.3f)
private val PlayfulSecondaryAlpha = VibrantAmber.copy(alpha = 0.3f)
