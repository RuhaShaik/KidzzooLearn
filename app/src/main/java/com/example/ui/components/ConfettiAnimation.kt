package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Shape variants for particle-based confetti.
 */
enum class ConfettiShape {
    RECTANGLE,
    CIRCLE,
    STAR,
    DIAMOND
}

/**
 * Particle representation with 2D/3D physics properties.
 */
data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    var tilt: Float,
    var tiltSpeed: Float,
    val color: Color,
    val shape: ConfettiShape,
    val width: Float,
    val height: Float,
    var alpha: Float = 1f,
    val totalLife: Float,
    var remainingLife: Float
)

/**
 * Palette of vibrant celebratory colors for confetti particles.
 */
private val ConfettiColors = listOf(
    VibrantSky,
    VibrantEmerald,
    BubbleYellow,
    BubblePink,
    VibrantOrange,
    VibrantIndigo,
    VibrantRose,
    VibrantAmber,
    Color(0xFFFFD700), // Pure Gold
    Color(0xFF8B5CF6), // Royal Violet
    Color(0xFF06B6D4)  // Cyan
)

/**
 * Particle-based confetti canvas and celebratory overlay.
 * Renders high-performance 60fps physics simulation with bursting cannons,
 * gravity, air drag, flutter sway, and 3D tumbling ribbons.
 */
@Composable
fun ConfettiCelebrationOverlay(
    visible: Boolean,
    title: String = "Lesson Completed! 🎉",
    message: String = "You did an amazing job!",
    xpBonus: Int = 50,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("confetti_celebration_overlay")
    ) {
        val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(400f)
        val heightPx = constraints.maxHeight.toFloat().coerceAtLeast(600f)

        val particles = remember(visible) {
            mutableStateListOf<ConfettiParticle>().apply {
                addAll(generateConfettiBurst(widthPx, heightPx, particleCount = 110))
            }
        }

        var isAnimationRunning by remember(visible) { mutableStateOf(visible) }

        // Animation frame loop using withFrameNanos
        LaunchedEffect(visible) {
            if (!visible) return@LaunchedEffect
            var lastTimeNanos = 0L

            while (isAnimationRunning && particles.isNotEmpty()) {
                withFrameNanos { frameTimeNanos ->
                    if (lastTimeNanos != 0L) {
                        val dt = ((frameTimeNanos - lastTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)

                        val iterator = particles.listIterator()
                        while (iterator.hasNext()) {
                            val p = iterator.next()
                            // Physics step
                            p.vy += 750f * dt // Gravity pulling downward
                            p.vx *= 0.985f    // Air drag resistance

                            // Flutter sway
                            p.x += (p.vx + sin(p.tilt) * 35f) * dt
                            p.y += p.vy * dt
                            p.rotation += p.rotationSpeed * dt
                            p.tilt += p.tiltSpeed * dt

                            p.remainingLife -= dt

                            // Fade out gracefully towards the end
                            val lifeProgress = (p.remainingLife / p.totalLife).coerceIn(0f, 1f)
                            p.alpha = if (lifeProgress < 0.35f) lifeProgress / 0.35f else 1f

                            // Remove dead or off-screen particles
                            if (p.remainingLife <= 0f || p.y > heightPx + 100f) {
                                iterator.remove()
                            }
                        }
                    }
                    lastTimeNanos = frameTimeNanos
                }
            }
        }

        // Auto-dismiss after 4.5 seconds of joyful celebration
        LaunchedEffect(visible) {
            delay(4500)
            onDismiss()
        }

        // Particle Canvas drawing layer
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("confetti_canvas")
        ) {
            for (p in particles) {
                drawConfettiParticle(p)
            }
        }

        // Celebratory Dialog / Banner Modal
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                initialScale = 0.6f
            ) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CelebrationModalCard(
                title = title,
                message = message,
                xpBonus = xpBonus,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * Draws a single particle with 3D rotation, scaling, and specific geometry.
 */
private fun DrawScope.drawConfettiParticle(p: ConfettiParticle) {
    val alphaColor = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f))
    // 3D perspective tumbling factor
    val perspectiveWidth = (p.width * abs(cos(p.tilt))).coerceAtLeast(1.5f)

    rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
        when (p.shape) {
            ConfettiShape.RECTANGLE -> {
                drawRect(
                    color = alphaColor,
                    topLeft = Offset(p.x - perspectiveWidth / 2f, p.y - p.height / 2f),
                    size = Size(perspectiveWidth, p.height)
                )
            }
            ConfettiShape.CIRCLE -> {
                drawCircle(
                    color = alphaColor,
                    radius = (p.width / 2f) * abs(cos(p.tilt * 0.5f)).coerceAtLeast(0.4f),
                    center = Offset(p.x, p.y)
                )
            }
            ConfettiShape.DIAMOND -> {
                val path = Path().apply {
                    moveTo(p.x, p.y - p.height / 2f)
                    lineTo(p.x + perspectiveWidth / 2f, p.y)
                    lineTo(p.x, p.y + p.height / 2f)
                    lineTo(p.x - perspectiveWidth / 2f, p.y)
                    close()
                }
                drawPath(path, color = alphaColor)
            }
            ConfettiShape.STAR -> {
                val starPath = createStarPath(p.x, p.y, p.width, 5)
                drawPath(starPath, color = alphaColor)
            }
        }
    }
}

/**
 * Helper to generate a 5-pointed star path centered at (cx, cy).
 */
private fun createStarPath(cx: Float, cy: Float, radius: Float, points: Int): Path {
    val path = Path()
    val outerRadius = radius
    val innerRadius = radius * 0.45f
    val step = Math.PI / points

    for (i in 0 until (points * 2)) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * step - (Math.PI / 2.0)
        val x = (cx + r * cos(angle)).toFloat()
        val y = (cy + r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

/**
 * Generates an explosive burst of confetti particles originating from cannons
 * positioned at bottom corners and center screen.
 */
private fun generateConfettiBurst(width: Float, height: Float, particleCount: Int): List<ConfettiParticle> {
    val list = mutableListOf<ConfettiParticle>()
    val random = Random(System.currentTimeMillis())

    for (i in 0 until particleCount) {
        // Distribute origins: Left Cannon (40%), Right Cannon (40%), Center Cannon (20%)
        val (startX, startY, angleRad, speed) = when {
            i % 5 in 0..1 -> {
                // Left cannon shooting up-right (45° to 75°)
                val ang = Math.toRadians((45.0 + random.nextDouble() * 35.0))
                val spd = 650f + random.nextFloat() * 750f
                Quad(width * 0.1f, height * 0.85f, ang, spd)
            }
            i % 5 in 2..3 -> {
                // Right cannon shooting up-left (105° to 135°)
                val ang = Math.toRadians((100.0 + random.nextDouble() * 35.0))
                val spd = 650f + random.nextFloat() * 750f
                Quad(width * 0.9f, height * 0.85f, ang, spd)
            }
            else -> {
                // Center burst shooting straight up with wide spread (65° to 115°)
                val ang = Math.toRadians((65.0 + random.nextDouble() * 50.0))
                val spd = 700f + random.nextFloat() * 850f
                Quad(width * 0.5f, height * 0.80f, ang, spd)
            }
        }

        val vx = (speed * cos(angleRad)).toFloat() * if (startX > width * 0.5f) -1f else 1f
        val vy = -(speed * sin(angleRad)).toFloat()

        val shape = when (random.nextInt(10)) {
            0, 1 -> ConfettiShape.STAR
            2, 3 -> ConfettiShape.CIRCLE
            4, 5 -> ConfettiShape.DIAMOND
            else -> ConfettiShape.RECTANGLE
        }

        val size = 10f + random.nextFloat() * 12f
        val length = if (shape == ConfettiShape.RECTANGLE) size * (1.6f + random.nextFloat() * 1.4f) else size
        val life = 2.8f + random.nextFloat() * 1.4f

        list.add(
            ConfettiParticle(
                x = startX,
                y = startY,
                vx = vx,
                vy = vy,
                rotation = random.nextFloat() * 360f,
                rotationSpeed = (random.nextFloat() - 0.5f) * 600f,
                tilt = random.nextFloat() * 6.28f,
                tiltSpeed = 4f + random.nextFloat() * 8f,
                color = ConfettiColors[random.nextInt(ConfettiColors.size)],
                shape = shape,
                width = size,
                height = length,
                alpha = 1f,
                totalLife = life,
                remainingLife = life
            )
        )
    }

    return list
}

private data class Quad(val x: Float, val y: Float, val angle: Double, val speed: Float)

/**
 * Centered celebratory milestone badge card.
 */
@Composable
private fun CelebrationModalCard(
    title: String,
    message: String,
    xpBonus: Int,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier
            .padding(horizontal = 28.dp)
            .fillMaxWidth()
            .testTag("celebration_card")
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trophy / Star Glowing Badge
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                BubbleYellow,
                                VibrantOrange
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Achievement Trophy",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            // Milestone / Lesson Completed Title
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp,
                modifier = Modifier.testTag("celebration_title")
            )

            // Message Body
            Text(
                text = message,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                modifier = Modifier.testTag("celebration_message")
            )

            // XP Reward Pill
            if (xpBonus > 0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = VibrantSkyLight,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, VibrantSky)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = VibrantSky,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "+$xpBonus XP Earned!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantSkyDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Awesome Button to continue and dismiss
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantEmerald,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("dismiss_celebration_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Awesome, Keep Going! ✨",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
