package com.example.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*

/**
 * Data structure representing daily learning stats over the past week.
 */
data class DayProgressStat(
    val day: String,
    val dayFullName: String,
    val completionRate: Int, // 0 to 100 percentage
    val milestonesCount: Int,
    val minutesSpent: Int,
    val xpEarned: Int
)

/**
 * Data structure representing individual milestone achievements over the past week.
 */
data class ChildMilestone(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val dayLabel: String,
    val xpAwarded: Int,
    val isCompleted: Boolean = true
)

/**
 * Sample milestone and completion data for the past week.
 */
val defaultWeeklyProgressStats = listOf(
    DayProgressStat("Mon", "Monday", 75, 2, 25, 120),
    DayProgressStat("Tue", "Tuesday", 85, 3, 35, 160),
    DayProgressStat("Wed", "Wednesday", 65, 2, 20, 95),
    DayProgressStat("Thu", "Thursday", 90, 4, 40, 190),
    DayProgressStat("Fri", "Friday", 100, 4, 45, 210),
    DayProgressStat("Sat", "Saturday", 80, 3, 30, 140),
    DayProgressStat("Sun", "Sunday", 95, 4, 38, 180)
)

val defaultWeeklyMilestones = listOf(
    ChildMilestone(
        id = "m1",
        title = "Magic Words Mastery",
        category = "MANNERS",
        description = "Consistently used 'Please' and 'Thank you' in 5 conversational roleplays.",
        dayLabel = "Today (Sun)",
        xpAwarded = 60
    ),
    ChildMilestone(
        id = "m2",
        title = "Number Quest Level 3",
        category = "EDUCATION",
        description = "Solved 10 addition and subtraction arithmetic puzzles flawlessly.",
        dayLabel = "Today (Sun)",
        xpAwarded = 75
    ),
    ChildMilestone(
        id = "m3",
        title = "Moral Choice Completed",
        category = "READING",
        description = "Listened to 'The Honest Woodcutter' and chose the truth pathway.",
        dayLabel = "Yesterday (Sat)",
        xpAwarded = 50
    ),
    ChildMilestone(
        id = "m4",
        title = "Critical Reasoning Detective",
        category = "THINKING",
        description = "Identified pattern sequences and logic anomalies in riddle challenge.",
        dayLabel = "Fri",
        xpAwarded = 65
    ),
    ChildMilestone(
        id = "m5",
        title = "Interstellar Storyteller",
        category = "CREATIVITY",
        description = "Co-created a space fantasy adventure with the AI companion.",
        dayLabel = "Thu",
        xpAwarded = 70
    ),
    ChildMilestone(
        id = "m6",
        title = "Empathy & Sharing Reflection",
        category = "REFLECTION",
        description = "Expressed gratitude and practiced calm reflection before bedtime.",
        dayLabel = "Wed",
        xpAwarded = 50
    )
)

/**
 * ProgressDashboard component that uses the Recharts library (embedded via Web platform)
 * to visualize a child's learning milestones and completion rates over the past week.
 */
@Composable
fun ProgressDashboard(
    modifier: Modifier = Modifier,
    childName: String = "Your Child",
    weeklyStats: List<DayProgressStat> = defaultWeeklyProgressStats,
    milestones: List<ChildMilestone> = defaultWeeklyMilestones
) {
    var selectedTab by remember { mutableStateOf("recharts") } // "recharts" | "milestones"
    val averageCompletion = remember(weeklyStats) {
        if (weeklyStats.isNotEmpty()) weeklyStats.map { it.completionRate }.average().toInt() else 0
    }
    val totalMilestones = remember(weeklyStats) {
        weeklyStats.sumOf { it.milestonesCount }
    }
    val totalMinutes = remember(weeklyStats) {
        weeklyStats.sumOf { it.minutesSpent }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .testTag("progress_dashboard")
            .drawBehind {
                val strokeWidth = 4.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = Color(0xFF38BDF8), // VibrantSky border
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row with Title & Child Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VibrantSkyLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "Progress Analytics",
                            tint = VibrantSky,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Weekly Progress Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$childName's Past 7 Days",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Recharts badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VibrantIndigoLight
                ) {
                    Text(
                        text = "Recharts Powered",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VibrantIndigoDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Summary Metric Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricSummaryCard(
                    title = "Avg Completion",
                    value = "$averageCompletion%",
                    icon = Icons.Default.CheckCircle,
                    color = VibrantSky,
                    bgColor = VibrantSkyLight,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "Milestones",
                    value = "$totalMilestones done",
                    icon = Icons.Default.EmojiEvents,
                    color = VibrantEmerald,
                    bgColor = VibrantEmeraldLight,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "Study Time",
                    value = "${totalMinutes}m",
                    icon = Icons.Default.Timer,
                    color = VibrantOrange,
                    bgColor = VibrantOrangeLight,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tab Selector: Recharts Visualization vs Milestone Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabButton(
                    title = "Recharts Visualization",
                    isSelected = selectedTab == "recharts",
                    onClick = { selectedTab = "recharts" },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Milestone Milestones (${milestones.size})",
                    isSelected = selectedTab == "milestones",
                    onClick = { selectedTab = "milestones" },
                    modifier = Modifier.weight(1f)
                )
            }

            // Animated tab content
            AnimatedVisibility(
                visible = selectedTab == "recharts",
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Interactive charts rendered with the Recharts library (AreaChart & BarChart):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // Recharts embedded interactive view
                    RechartsWebView(
                        weeklyStats = weeklyStats,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(310.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            AnimatedVisibility(
                visible = selectedTab == "milestones",
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    milestones.forEach { milestone ->
                        MilestoneItemCard(milestone = milestone)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricSummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            }
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun MilestoneItemCard(milestone: ChildMilestone) {
    val categoryColor = when (milestone.category.uppercase()) {
        "MANNERS" -> VibrantEmerald
        "EDUCATION" -> VibrantIndigo
        "READING", "CREATIVITY" -> VibrantOrange
        "THINKING", "REFLECTION" -> VibrantRose
        else -> VibrantSky
    }

    val categoryBg = when (milestone.category.uppercase()) {
        "MANNERS" -> VibrantEmeraldLight
        "EDUCATION" -> VibrantIndigoLight
        "READING", "CREATIVITY" -> VibrantOrangeLight
        "THINKING", "REFLECTION" -> VibrantRoseLight
        else -> VibrantSkyLight
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(categoryBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = milestone.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = categoryBg
                    ) {
                        Text(
                            text = milestone.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = milestone.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${milestone.xpAwarded} XP",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = VibrantSky
                )
                Text(
                    text = milestone.dayLabel,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Embedded Recharts Web component inside an Android WebView.
 * Renders Recharts AreaChart (completion rates) and BarChart (milestones)
 * with robust offline SVG fallback if external CDN is unreachable.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsWebView(
    weeklyStats: List<DayProgressStat>,
    modifier: Modifier = Modifier
) {
    val statsJson = remember(weeklyStats) {
        val items = weeklyStats.joinToString(separator = ",") { stat ->
            """{"day":"${stat.day}","completionRate":${stat.completionRate},"milestones":${stat.milestonesCount},"minutes":${stat.minutesSpent},"xp":${stat.xpEarned}}"""
        }
        "[$items]"
    }

    val htmlContent = remember(statsJson) {
        buildRechartsHtml(statsJson)
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                }
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://recharts.local", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://recharts.local", htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

/**
 * Generates the HTML5 bundle importing React, ReactDOM, and Recharts,
 * rendering interactive charts with tabs for Completion Rate and Milestones,
 * plus an instant SVG fallback if CDN is offline.
 */
private fun buildRechartsHtml(dataJson: String): String {
    return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <style>
    * { box-sizing: border-box; -webkit-tap-highlight-color: transparent; }
    body {
      margin: 0;
      padding: 8px 4px 4px 4px;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
      background-color: transparent;
      color: #1e293b;
      overflow-x: hidden;
    }
    .toggle-bar {
      display: flex;
      gap: 6px;
      margin-bottom: 12px;
    }
    .btn {
      flex: 1;
      padding: 7px 10px;
      font-size: 11px;
      font-weight: 700;
      border-radius: 8px;
      border: none;
      cursor: pointer;
      transition: all 0.2s ease;
    }
    .btn-active-sky {
      background: #0ea5e9;
      color: #ffffff;
      box-shadow: 0 2px 4px rgba(14,165,233,0.3);
    }
    .btn-inactive-sky {
      background: #e0f2fe;
      color: #0369a1;
    }
    .btn-active-emerald {
      background: #10b981;
      color: #ffffff;
      box-shadow: 0 2px 4px rgba(16,185,129,0.3);
    }
    .btn-inactive-emerald {
      background: #d1fae5;
      color: #064e3b;
    }
    .chart-container {
      width: 100%;
      height: 230px;
      position: relative;
    }
    .fallback-svg {
      width: 100%;
      height: 100%;
    }
  </style>
  <script src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
  <script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
  <script src="https://unpkg.com/recharts@2.12.7/umd/Recharts.min.js"></script>
</head>
<body>
  <div id="root">
    <!-- Instant Fallback Render in case Recharts is offline or loading -->
    <div id="fallback">
      <div class="toggle-bar">
        <button class="btn btn-active-sky">📈 Completion Rate (%)</button>
        <button class="btn btn-inactive-emerald">🏆 Milestones Achieved</button>
      </div>
      <div class="chart-container">
        <svg class="fallback-svg" viewBox="0 0 320 180">
          <defs>
            <linearGradient id="svgGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#0ea5e9" stop-opacity="0.6"/>
              <stop offset="100%" stop-color="#0ea5e9" stop-opacity="0.05"/>
            </linearGradient>
          </defs>
          <line x1="20" y1="20" x2="300" y2="20" stroke="#f1f5f9" stroke-width="1"/>
          <line x1="20" y1="70" x2="300" y2="70" stroke="#f1f5f9" stroke-width="1"/>
          <line x1="20" y1="120" x2="300" y2="120" stroke="#f1f5f9" stroke-width="1"/>
          <line x1="20" y1="150" x2="300" y2="150" stroke="#cbd5e1" stroke-width="1.5"/>
          <path d="M 30 65 L 75 48 L 120 82 L 165 40 L 210 25 L 255 58 L 290 32 L 290 150 L 30 150 Z" fill="url(#svgGrad)" />
          <path d="M 30 65 L 75 48 L 120 82 L 165 40 L 210 25 L 255 58 L 290 32" fill="none" stroke="#0ea5e9" stroke-width="3" stroke-linecap="round"/>
          <circle cx="30" cy="65" r="4" fill="#0ea5e9" stroke="#fff" stroke-width="2"/>
          <circle cx="75" cy="48" r="4" fill="#0ea5e9" stroke="#fff" stroke-width="2"/>
          <circle cx="120" cy="82" r="4" fill="#0ea5e9" stroke="#fff" stroke-width="2"/>
          <circle cx="165" cy="40" r="4" fill="#0ea5e9" stroke="#fff" stroke-width="2"/>
          <circle cx="210" cy="25" r="4" fill="#0ea5e9" stroke="#fff" stroke-width="2"/>
          <circle cx="255" cy="58" r="4" fill="#0ea5e9" stroke="#fff" stroke-width="2"/>
          <circle cx="290" cy="32" r="4" fill="#0ea5e9" stroke="#fff" stroke-width="2"/>
          <text x="30" y="165" font-size="9" fill="#64748b" text-anchor="middle">Mon</text>
          <text x="75" y="165" font-size="9" fill="#64748b" text-anchor="middle">Tue</text>
          <text x="120" y="165" font-size="9" fill="#64748b" text-anchor="middle">Wed</text>
          <text x="165" y="165" font-size="9" fill="#64748b" text-anchor="middle">Thu</text>
          <text x="210" y="165" font-size="9" fill="#64748b" text-anchor="middle">Fri</text>
          <text x="255" y="165" font-size="9" fill="#64748b" text-anchor="middle">Sat</text>
          <text x="290" y="165" font-size="9" fill="#64748b" text-anchor="middle">Sun</text>
        </svg>
      </div>
    </div>
  </div>

  <script>
    const chartData = $dataJson;

    function renderRecharts() {
      if (!window.React || !window.ReactDOM || !window.Recharts) {
        return;
      }

      const { useState, createElement: h } = window.React;
      const { ResponsiveContainer, AreaChart, Area, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid } = window.Recharts;

      function DashboardApp() {
        const [view, setView] = useState('completion');

        return h('div', null,
          h('div', { className: 'toggle-bar' },
            h('button', {
              className: 'btn ' + (view === 'completion' ? 'btn-active-sky' : 'btn-inactive-sky'),
              onClick: () => setView('completion')
            }, '📈 Completion Rate (%)'),
            h('button', {
              className: 'btn ' + (view === 'milestones' ? 'btn-active-emerald' : 'btn-inactive-emerald'),
              onClick: () => setView('milestones')
            }, '🏆 Milestones Achieved')
          ),
          h('div', { className: 'chart-container' },
            h(ResponsiveContainer, { width: '100%', height: '100%' },
              view === 'completion'
                ? h(AreaChart, { data: chartData, margin: { top: 10, right: 10, left: -22, bottom: 0 } },
                    h('defs', null,
                      h('linearGradient', { id: 'rateGrad', x1: '0', y1: '0', x2: '0', y2: '1' },
                        h('stop', { offset: '5%', stopColor: '#0ea5e9', stopOpacity: 0.8 }),
                        h('stop', { offset: '95%', stopColor: '#0ea5e9', stopOpacity: 0.05 })
                      )
                    ),
                    h(CartesianGrid, { strokeDasharray: '3 3', stroke: '#e2e8f0' }),
                    h(XAxis, { dataKey: 'day', stroke: '#64748b', fontSize: 11 }),
                    h(YAxis, { domain: [0, 100], stroke: '#64748b', fontSize: 11 }),
                    h(Tooltip, {
                      contentStyle: { backgroundColor: '#0f172a', borderRadius: '10px', color: '#ffffff', border: 'none', fontSize: '12px' },
                      formatter: (val) => [val + '%', 'Completion Rate']
                    }),
                    h(Area, {
                      type: 'monotone',
                      dataKey: 'completionRate',
                      stroke: '#0ea5e9',
                      strokeWidth: 3,
                      fillOpacity: 1,
                      fill: 'url(#rateGrad)'
                    })
                  )
                : h(BarChart, { data: chartData, margin: { top: 10, right: 10, left: -22, bottom: 0 } },
                    h(CartesianGrid, { strokeDasharray: '3 3', stroke: '#e2e8f0' }),
                    h(XAxis, { dataKey: 'day', stroke: '#64748b', fontSize: 11 }),
                    h(YAxis, { stroke: '#64748b', fontSize: 11 }),
                    h(Tooltip, {
                      contentStyle: { backgroundColor: '#0f172a', borderRadius: '10px', color: '#ffffff', border: 'none', fontSize: '12px' },
                      formatter: (val) => [val, 'Milestones Reached']
                    }),
                    h(Bar, {
                      dataKey: 'milestones',
                      fill: '#10b981',
                      radius: [6, 6, 0, 0]
                    })
                  )
            )
          )
        );
      }

      const rootElem = document.getElementById('root');
      if (window.ReactDOM.createRoot) {
        const root = window.ReactDOM.createRoot(rootElem);
        root.render(h(DashboardApp));
      } else {
        window.ReactDOM.render(h(DashboardApp), rootElem);
      }
    }

    // Try mounting Recharts immediately or on load
    if (window.Recharts) {
      renderRecharts();
    } else {
      window.addEventListener('load', renderRecharts);
      setTimeout(renderRecharts, 1200);
    }
  </script>
</body>
</html>
    """.trimIndent()
}
