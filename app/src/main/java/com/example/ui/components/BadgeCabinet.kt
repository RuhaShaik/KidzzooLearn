package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Badge
import com.example.data.model.ChildProfile
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Definition of all badge milestones with their themes, icons, and requirements.
 */
data class BadgeMilestoneDef(
    val id: String,
    val title: String,
    val category: String, // "DISCOVERY", "EXPLORATION", "CREATIVITY", "HABITS"
    val description: String,
    val unlockCriteria: String,
    val iconVector: ImageVector,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color,
    val ribbonColor: Color,
    val badgeShape: BadgeVectorShape = BadgeVectorShape.SUNBURST
)

enum class BadgeVectorShape {
    SUNBURST,
    SHIELD,
    STAR_OCTAGON,
    HEXAGON,
    ROUND_MEDAL
}

/**
 * Registry of available collectible badges for children.
 */
val ALL_BADGE_MILESTONES = listOf(
    BadgeMilestoneDef(
        id = "early_bird",
        title = "Early Bird",
        category = "HABITS",
        description = "Greeted your companion and started learning with bright morning energy!",
        unlockCriteria = "Start a morning learning chat or complete a quest before noon",
        iconVector = Icons.Default.WbSunny,
        primaryColor = Color(0xFFF59E0B),
        secondaryColor = Color(0xFFFFD54F),
        glowColor = Color(0xFFFFF9C4),
        ribbonColor = Color(0xFFEF4444),
        badgeShape = BadgeVectorShape.SUNBURST
    ),
    BadgeMilestoneDef(
        id = "science_whiz",
        title = "Science Whiz",
        category = "DISCOVERY",
        description = "Explored fascinating nature, space, animals, and science mysteries!",
        unlockCriteria = "Complete 3 science challenges or ask science questions",
        iconVector = Icons.Default.Science,
        primaryColor = Color(0xFF0EA5E9),
        secondaryColor = Color(0xFF38BDF8),
        glowColor = Color(0xFFBAE6FD),
        ribbonColor = Color(0xFF6366F1),
        badgeShape = BadgeVectorShape.HEXAGON
    ),
    BadgeMilestoneDef(
        id = "storyteller",
        title = "Storyteller",
        category = "CREATIVITY",
        description = "Listened to adventures and made your own creative interactive story choices!",
        unlockCriteria = "Read or choose adventures in the interactive storybook",
        iconVector = Icons.Default.MenuBook,
        primaryColor = Color(0xFFEC407A),
        secondaryColor = Color(0xFFF48FB1),
        glowColor = Color(0xFFFCE4EC),
        ribbonColor = Color(0xFF8B5CF6),
        badgeShape = BadgeVectorShape.ROUND_MEDAL
    ),
    BadgeMilestoneDef(
        id = "cosmic_explorer",
        title = "Cosmic Explorer",
        category = "EXPLORATION",
        description = "Blasted off on space adventures with Captain Curie among the stars!",
        unlockCriteria = "Chat with Captain Curie or complete space missions",
        iconVector = Icons.Default.RocketLaunch,
        primaryColor = Color(0xFF6366F1),
        secondaryColor = Color(0xFF818CF8),
        glowColor = Color(0xFFC7D2FE),
        ribbonColor = Color(0xFFEC407A),
        badgeShape = BadgeVectorShape.SHIELD
    ),
    BadgeMilestoneDef(
        id = "riddle_master",
        title = "Riddle Master",
        category = "DISCOVERY",
        description = "Cracked brain-twisting riddles and thought like a true detective!",
        unlockCriteria = "Solve riddles with Spark the Fox or answer game puzzles",
        iconVector = Icons.Default.Psychology,
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF34D399),
        glowColor = Color(0xFFA7F3D0),
        ribbonColor = Color(0xFFF59E0B),
        badgeShape = BadgeVectorShape.STAR_OCTAGON
    ),
    BadgeMilestoneDef(
        id = "curious_mind",
        title = "Curious Mind",
        category = "EXPLORATION",
        description = "Asked big questions and learned awesome new facts about our world!",
        unlockCriteria = "Ask questions or have multi-turn conversations with your companion",
        iconVector = Icons.Default.Lightbulb,
        primaryColor = Color(0xFFF97316),
        secondaryColor = Color(0xFFFB923C),
        glowColor = Color(0xFFFED7AA),
        ribbonColor = Color(0xFF3B82F6),
        badgeShape = BadgeVectorShape.SUNBURST
    ),
    BadgeMilestoneDef(
        id = "kindness_champ",
        title = "Kindness Champ",
        category = "HABITS",
        description = "Practiced good manners, empathy, and saying kind words to others!",
        unlockCriteria = "Complete Manners and Emotional kindness daily quests",
        iconVector = Icons.Default.Favorite,
        primaryColor = Color(0xFFE11D48),
        secondaryColor = Color(0xFFFB7185),
        glowColor = Color(0xFFFECDD3),
        ribbonColor = Color(0xFF10B981),
        badgeShape = BadgeVectorShape.ROUND_MEDAL
    ),
    BadgeMilestoneDef(
        id = "math_wizard",
        title = "Math Wizard",
        category = "DISCOVERY",
        description = "Solved arithmetic puzzles and showed off real number superpowers!",
        unlockCriteria = "Solve counting and math challenges in the Games arena",
        iconVector = Icons.Default.Casino,
        primaryColor = Color(0xFF8B5CF6),
        secondaryColor = Color(0xFFA78BFA),
        glowColor = Color(0xFFDDD6FE),
        ribbonColor = Color(0xFFF97316),
        badgeShape = BadgeVectorShape.HEXAGON
    ),
    BadgeMilestoneDef(
        id = "voice_explorer",
        title = "Voice Explorer",
        category = "EXPLORATION",
        description = "Spoke directly with your companion using the magic microphone!",
        unlockCriteria = "Use voice speech recognition in the AI Teacher chat",
        iconVector = Icons.Default.Mic,
        primaryColor = Color(0xFF06B6D4),
        secondaryColor = Color(0xFF22D3EE),
        glowColor = Color(0xFFCFFAFE),
        ribbonColor = Color(0xFFE11D48),
        badgeShape = BadgeVectorShape.SHIELD
    ),
    BadgeMilestoneDef(
        id = "super_streak",
        title = "Streak Champion",
        category = "HABITS",
        description = "Kept your learning fire burning bright by returning every day!",
        unlockCriteria = "Maintain a daily learning streak of 3 or more days",
        iconVector = Icons.Default.EmojiEvents,
        primaryColor = Color(0xFFD97706),
        secondaryColor = Color(0xFFFBBF24),
        glowColor = Color(0xFFFEF3C7),
        ribbonColor = Color(0xFF2563EB),
        badgeShape = BadgeVectorShape.STAR_OCTAGON
    )
)

/**
 * Animated Colorful Badge Emblem rendered on Canvas with sparkling stars,
 * shimmering light rays, multi-layer gradient depth, and ribbon tails.
 */
@Composable
fun AnimatedBadgeEmblem(
    def: BadgeMilestoneDef,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 100.dp,
    showAnimation: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_shimmer")
    
    // Smooth pulsing scale
    val pulseScale by if (isUnlocked && showAnimation) {
        infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    // Continuous celestial rotation for background rays
    val rayRotation by if (isUnlocked && showAnimation) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rayRotation"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // Shimmer highlight sweep
    val shimmerPhase by if (isUnlocked && showAnimation) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerPhase"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(sizeDp)
            .scale(pulseScale)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.42f

            if (isUnlocked) {
                // 1. Aura / Glow halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            def.glowColor.copy(alpha = 0.7f),
                            def.primaryColor.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 1.35f
                    ),
                    center = center,
                    radius = radius * 1.35f
                )

                // 2. Rotating Starburst Sunrays
                rotate(rayRotation, pivot = center) {
                    val rayCount = 12
                    for (i in 0 until rayCount) {
                        val angle = (i * 360f / rayCount) * (PI.toFloat() / 180f)
                        val rayLength = radius * 1.18f
                        val rayEnd = Offset(
                            center.x + cos(angle) * rayLength,
                            center.y + sin(angle) * rayLength
                        )
                        drawLine(
                            color = def.glowColor.copy(alpha = 0.45f),
                            start = center,
                            end = rayEnd,
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }

                // 3. Hanging Award Ribbon Tails behind the medal
                val ribbonWidth = radius * 0.45f
                val ribbonLength = radius * 1.15f

                // Left Ribbon
                val leftRibbonPath = Path().apply {
                    moveTo(center.x - radius * 0.35f, center.y + radius * 0.2f)
                    lineTo(center.x - radius * 0.65f, center.y + ribbonLength)
                    lineTo(center.x - radius * 0.45f, center.y + ribbonLength * 0.82f)
                    lineTo(center.x - radius * 0.20f, center.y + ribbonLength)
                    lineTo(center.x - radius * 0.05f, center.y + radius * 0.2f)
                    close()
                }
                drawPath(leftRibbonPath, color = def.ribbonColor.copy(alpha = 0.9f))
                drawPath(
                    leftRibbonPath,
                    color = Color.White.copy(alpha = 0.3f),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Right Ribbon
                val rightRibbonPath = Path().apply {
                    moveTo(center.x + radius * 0.05f, center.y + radius * 0.2f)
                    lineTo(center.x + radius * 0.20f, center.y + ribbonLength)
                    lineTo(center.x + radius * 0.45f, center.y + ribbonLength * 0.82f)
                    lineTo(center.x + radius * 0.65f, center.y + ribbonLength)
                    lineTo(center.x + radius * 0.35f, center.y + radius * 0.2f)
                    close()
                }
                drawPath(rightRibbonPath, color = def.ribbonColor.copy(alpha = 0.95f))
                drawPath(
                    rightRibbonPath,
                    color = Color.White.copy(alpha = 0.3f),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // 4. Badge Outer Medallion Base
                drawBadgeShape(
                    shape = def.badgeShape,
                    center = center,
                    radius = radius,
                    brush = Brush.radialGradient(
                        colors = listOf(def.secondaryColor, def.primaryColor),
                        center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                        radius = radius * 1.3f
                    )
                )

                // 5. Golden Rim Ring
                drawCircle(
                    color = Color(0xFFFFD54F),
                    center = center,
                    radius = radius * 0.82f,
                    style = Stroke(width = 3.5.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFFFFF9C4),
                    center = center,
                    radius = radius * 0.80f,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // 6. Inner Core Medallion
                drawCircle(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            def.primaryColor.copy(alpha = 0.95f),
                            def.secondaryColor
                        )
                    ),
                    center = center,
                    radius = radius * 0.72f
                )

                // 7. Dynamic Shimmer Streak across medallion
                val shimmerX = (center.x - radius) + (radius * 2 * shimmerPhase)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.65f),
                            Color.Transparent
                        ),
                        startX = shimmerX - 20.dp.toPx(),
                        endX = shimmerX + 20.dp.toPx()
                    ),
                    start = Offset(shimmerX, center.y - radius * 0.7f),
                    end = Offset(shimmerX, center.y + radius * 0.7f),
                    strokeWidth = 14.dp.toPx()
                )

                // 8. Tiny Sparkling Stars
                drawTinySparkle(Offset(center.x - radius * 0.5f, center.y - radius * 0.5f), Color.White, 3.5.dp.toPx())
                drawTinySparkle(Offset(center.x + radius * 0.55f, center.y - radius * 0.35f), Color(0xFFFFEE58), 4.dp.toPx())
                drawTinySparkle(Offset(center.x + radius * 0.45f, center.y + radius * 0.45f), Color.White, 3.dp.toPx())

            } else {
                // --- LOCKED STATE EMBLEM ---
                // Subtle gray stone/pedestal badge with locked metallic trim
                drawCircle(
                    color = Color(0xFFE2E8F0),
                    center = center,
                    radius = radius * 1.05f
                )
                drawCircle(
                    color = Color(0xFFCBD5E1),
                    center = center,
                    radius = radius * 0.85f,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF94A3B8), Color(0xFF64748B))
                    ),
                    center = center,
                    radius = radius * 0.75f
                )
            }
        }

        // Center Icon Vector
        if (isUnlocked) {
            Icon(
                imageVector = def.iconVector,
                contentDescription = def.title,
                tint = Color.White,
                modifier = Modifier
                    .size(sizeDp * 0.40f)
                    .shadow(4.dp, CircleShape)
            )
        } else {
            // Metallic Lock Icon
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color(0xFFE2E8F0),
                modifier = Modifier.size(sizeDp * 0.34f)
            )
        }
    }
}

private fun DrawScope.drawBadgeShape(
    shape: BadgeVectorShape,
    center: Offset,
    radius: Float,
    brush: Brush
) {
    when (shape) {
        BadgeVectorShape.SUNBURST -> {
            val path = Path()
            val points = 16
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) radius else radius * 0.88f
                val angle = (i * PI / points).toFloat()
                val x = center.x + cos(angle) * r
                val y = center.y + sin(angle) * r
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, brush = brush)
        }
        BadgeVectorShape.HEXAGON -> {
            val path = Path()
            for (i in 0 until 6) {
                val angle = (i * PI / 3 - PI / 6).toFloat()
                val x = center.x + cos(angle) * radius
                val y = center.y + sin(angle) * radius
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, brush = brush)
        }
        BadgeVectorShape.STAR_OCTAGON -> {
            val path = Path()
            val points = 8
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) radius else radius * 0.82f
                val angle = (i * PI / points).toFloat()
                val x = center.x + cos(angle) * r
                val y = center.y + sin(angle) * r
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, brush = brush)
        }
        BadgeVectorShape.SHIELD -> {
            val path = Path().apply {
                moveTo(center.x - radius * 0.85f, center.y - radius * 0.75f)
                lineTo(center.x + radius * 0.85f, center.y - radius * 0.75f)
                lineTo(center.x + radius * 0.85f, center.y + radius * 0.1f)
                cubicTo(
                    center.x + radius * 0.85f, center.y + radius * 0.7f,
                    center.x + radius * 0.3f, center.y + radius * 0.95f,
                    center.x, center.y + radius
                )
                cubicTo(
                    center.x - radius * 0.3f, center.y + radius * 0.95f,
                    center.x - radius * 0.85f, center.y + radius * 0.7f,
                    center.x - radius * 0.85f, center.y + radius * 0.1f
                )
                close()
            }
            drawPath(path, brush = brush)
        }
        BadgeVectorShape.ROUND_MEDAL -> {
            drawCircle(brush = brush, center = center, radius = radius)
        }
    }
}

private fun DrawScope.drawTinySparkle(center: Offset, color: Color, size: Float) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        lineTo(center.x + size * 0.3f, center.y)
        lineTo(center.x + size, center.y)
        lineTo(center.x + size * 0.3f, center.y + size * 0.3f)
        lineTo(center.x, center.y + size)
        lineTo(center.x - size * 0.3f, center.y + size * 0.3f)
        lineTo(center.x - size, center.y)
        lineTo(center.x - size * 0.3f, center.y)
        close()
    }
    drawPath(path, color)
}

/**
 * Full Virtual Badge Cabinet viewable directly from the main interface or dedicated tab.
 */
@Composable
fun VirtualBadgeCabinet(
    profile: ChildProfile,
    unlockedBadges: List<Badge>,
    onBadgeSelected: (BadgeMilestoneDef, Boolean, Badge?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("ALL") }

    // Map unlocked badges by title matching or id matching
    val unlockedTitles = remember(unlockedBadges) {
        unlockedBadges.map { it.title.lowercase().trim() }.toSet()
    }

    val unlockedCount = remember(unlockedBadges, ALL_BADGE_MILESTONES) {
        ALL_BADGE_MILESTONES.count { def ->
            unlockedTitles.any { it.contains(def.title.lowercase()) || def.title.lowercase().contains(it) }
        }
    }

    val filteredMilestones = remember(selectedCategory) {
        if (selectedCategory == "ALL") {
            ALL_BADGE_MILESTONES
        } else {
            ALL_BADGE_MILESTONES.filter { it.category == selectedCategory }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("virtual_badge_cabinet"),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabinet Header & Wooden Trophy Showcase Banner
        item(span = { GridItemSpan(maxLineSpan) }) {
            CabinetHeroHeader(
                profile = profile,
                unlockedCount = unlockedCount,
                totalCount = ALL_BADGE_MILESTONES.size
            )
        }

        // Category Filter Chips
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "All Badges 🌟",
                    "DISCOVERY" to "Discovery 🔬",
                    "EXPLORATION" to "Exploration 🚀",
                    "CREATIVITY" to "Creativity 🎨",
                    "HABITS" to "Habits 🏆"
                ).forEach { (catKey, label) ->
                    val isSelected = selectedCategory == catKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = catKey },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Badges Collection Grid Items
        items(filteredMilestones, key = { it.id }) { milestone ->
            val unlockedBadge = unlockedBadges.find {
                it.title.lowercase().contains(milestone.title.lowercase()) ||
                milestone.title.lowercase().contains(it.title.lowercase())
            }
            val isUnlocked = unlockedBadge != null

            BadgeCabinetCard(
                def = milestone,
                isUnlocked = isUnlocked,
                onClick = {
                    onBadgeSelected(milestone, isUnlocked, unlockedBadge)
                }
            )
        }

        // Motivational Bottom Footer
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tip: Talk with your AI buddy, solve fun riddles, and complete daily quests to collect every badge in your cabinet!",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Top Trophy Cabinet Showcase Banner with wooden texture look and glass shelf reflection.
 */
@Composable
fun CabinetHeroHeader(
    profile: ChildProfile,
    unlockedCount: Int,
    totalCount: Int
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2E1C10) // Warm mahogany cabinet wood aesthetic
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cabinet_hero_header")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "My Badge Cabinet",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color(0xFFFFD54F)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "🏆", fontSize = 20.sp)
                    }
                    Text(
                        text = "Showcase of ${profile.name}'s Big Milestones",
                        fontSize = 13.sp,
                        color = Color(0xFFE2D7CC)
                    )
                }

                // Level / Star Badge in Cabinet Corner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF4A2E1A))
                        .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Level ${profile.level}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFFFD54F)
                        )
                        Text(
                            text = "${profile.xp} XP",
                            fontSize = 11.sp,
                            color = Color(0xFFFFF9C4)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cabinet Shelf Progress Indicator
            val progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount.toFloat() else 0f
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$unlockedCount of $totalCount Badges Collected",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${(progress * 100).toInt()}% Complete",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFD54F)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = Color(0xFFFFD54F),
                trackColor = Color(0xFF4A2E1A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Wooden Shelf Glass Bar Visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0x33FFFFFF),
                                Color(0x99FFFFFF),
                                Color(0x33FFFFFF)
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Individual Badge Card in the Cabinet Grid.
 */
@Composable
fun BadgeCabinetCard(
    def: BadgeMilestoneDef,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("badge_item_${def.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Emblem Vector
            AnimatedBadgeEmblem(
                def = def,
                isUnlocked = isUnlocked,
                sizeDp = 88.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = def.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Status Tag
            if (isUnlocked) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BubbleGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Unlocked",
                        tint = BubbleGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Unlocked!",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BubbleGreen
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Locked",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Rich Detail Dialog shown when a child taps any badge in their cabinet.
 */
@Composable
fun BadgeDetailDialog(
    milestone: BadgeMilestoneDef,
    isUnlocked: Boolean,
    unlockedBadge: Badge?,
    onDismiss: () -> Unit,
    onCelebrateAgain: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("badge_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Large Glowing Animated Emblem
                AnimatedBadgeEmblem(
                    def = milestone,
                    isUnlocked = isUnlocked,
                    sizeDp = 130.dp,
                    showAnimation = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = milestone.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = if (isUnlocked) milestone.primaryColor else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = milestone.description,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // How to Earn / Unlocked date Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) BubbleGreen.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isUnlocked) "🎉 Milestone Reached!" else "🎯 How to Unlock:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isUnlocked) BubbleGreen else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isUnlocked) {
                                val dateStr = unlockedBadge?.unlockedAt?.let {
                                    SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(it))
                                } ?: "Today"
                                "Awarded to you on $dateStr! Keep shining!"
                            } else {
                                milestone.unlockCriteria
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isUnlocked && onCelebrateAgain != null) {
                    Button(
                        onClick = {
                            onCelebrateAgain()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = milestone.primaryColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Celebrate This Badge! 🎉", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Awesome!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Quick Badge Cabinet Preview bar on top of screen / main tabs.
 */
@Composable
fun QuickBadgeCabinetStrip(
    unlockedBadges: List<Badge>,
    onOpenCabinet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenCabinet)
            .testTag("quick_badge_strip")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(colors = listOf(BubbleYellow, BubbleOrange))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Badge Cabinet",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Badge Cabinet 🏆",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val count = ALL_BADGE_MILESTONES.count { def ->
                        unlockedBadges.any { it.title.lowercase().contains(def.title.lowercase()) }
                    }
                    Text(
                        text = "$count of ${ALL_BADGE_MILESTONES.size} Badges Earned",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Small Row of miniature unlocked badge icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((-6).dp)
            ) {
                val previewList = ALL_BADGE_MILESTONES.take(4)
                previewList.forEach { milestone ->
                    val isUnlocked = unlockedBadges.any { it.title.lowercase().contains(milestone.title.lowercase()) }
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (isUnlocked) milestone.primaryColor else Color(0xFFCBD5E1))
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = milestone.iconVector,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "View >",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
