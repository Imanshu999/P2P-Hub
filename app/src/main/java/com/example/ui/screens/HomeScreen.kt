package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionLog
import com.example.ui.glass.GlassButton
import com.example.ui.glass.GlassCard
import com.example.ui.glass.GlassMetricCard
import com.example.ui.glass.GlassStatusBadge
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: MainUiState,
    logs: List<ConnectionLog>,
    onNavigateTab: (Int) -> Unit,
    onGenerateCodeClick: () -> Unit,
    accentColor: Color
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("home_screen_column"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // 1. QUICK ACTIONS GRID (STREAM, VAULT, PANEL)
        item {
            QuickActionsGrid(onNavigateTab = onNavigateTab)
        }

        // 2. IMMERSIVE CONNECTION HUB CARD
        item {
            ImmersiveConnectionHubCard(
                uiState = uiState,
                onGenerateCodeClick = onGenerateCodeClick,
                onNavigateTab = onNavigateTab,
                accentColor = accentColor
            )
        }

        // 3. DASHBOARD METRIC CARDS GRID
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassMetricCard(
                    title = "Latency",
                    value = "${uiState.latencyMs} ms",
                    subtext = "P2P Direct Socket",
                    icon = Icons.Default.Speed,
                    accentColor = EmeraldGlow,
                    modifier = Modifier.weight(1f)
                )

                GlassMetricCard(
                    title = "Bandwidth",
                    value = "${"%.1f".format(uiState.activeBandwidthKbs / 1000f)} MB/s",
                    subtext = "Encrypted TLS",
                    icon = Icons.Default.NetworkCheck,
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. LIVE BANDWIDTH TELEMETRY WAVEFORM GRAPH
        item {
            LiveTelemetryGraphCard(
                history = uiState.bandwidthHistory,
                accentColor = accentColor
            )
        }

        // 4. QUICK ACCESS CORE NAVIGATION HUB
        item {
            Text(
                text = "FEATURE HUBS",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextMuted,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureHubTile(
                    title = "P2P Connect & Code Generator",
                    subtitle = "IP-based code matching, handshake hub, session logs",
                    icon = Icons.Default.Router,
                    badgeText = "Core",
                    accentColor = accentColor,
                    onClick = { onNavigateTab(1) },
                    testTag = "tile_p2p_connect"
                )

                FeatureHubTile(
                    title = "Live Stream Preview",
                    subtitle = "Real-time HD video/audio stream with glass controls",
                    icon = Icons.Default.Videocam,
                    badgeText = "Live",
                    accentColor = EmeraldGlow,
                    onClick = { onNavigateTab(2) },
                    testTag = "tile_live_stream"
                )

                FeatureHubTile(
                    title = "Secure Vault",
                    subtitle = "Encrypted key storage, certificates & notes",
                    icon = Icons.Default.Lock,
                    badgeText = "Encrypted",
                    accentColor = ElectricViolet,
                    onClick = { onNavigateTab(3) },
                    testTag = "tile_secure_vault"
                )
            }
        }

        // 5. RECENT NETWORK ACTIVITY LOGS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ACTIVITY",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextMuted,
                        letterSpacing = 1.5.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )

                Text(
                    text = "View Hub",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    ),
                    modifier = Modifier
                        .clickable { onNavigateTab(1) }
                        .padding(4.dp)
                )
            }
        }

        if (logs.isEmpty()) {
            item {
                GlassCard(cornerRadius = 16.dp) {
                    Text(
                        text = "No recent connection logs. Generate a connection code in the P2P Hub to get started.",
                        style = TextStyle(fontSize = 13.sp, color = TextSecondary),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        } else {
            items(logs.take(3)) { log ->
                ConnectionActivityItem(log = log, accentColor = accentColor)
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun HeroStatusBanner(
    uiState: MainUiState,
    onNavigateTab: (Int) -> Unit,
    accentColor: Color
) {
    GlassCard(
        cornerRadius = 24.dp,
        glowColor = accentColor,
        borderBrush = Brush.linearGradient(
            listOf(accentColor.copy(alpha = 0.5f), Color.White.copy(alpha = 0.1f))
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AETHERLINK NODE",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = accentColor,
                        letterSpacing = 1.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.localIp,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            GlassStatusBadge(status = uiState.connectionStatus)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Protocol Info Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = EmeraldGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.encryptionProtocol,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    )
                }

                Text(
                    text = if (uiState.connectionStatus == "CONNECTED") "TUNNEL ONLINE" else "IDLE / READY",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.connectionStatus == "CONNECTED") EmeraldGlow else TextMuted
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Action Button
        GlassButton(
            text = if (uiState.connectionStatus == "CONNECTED") "Open Connection Hub" else "Generate Connection Code",
            onClick = { onNavigateTab(1) },
            accentColor = accentColor,
            icon = Icons.Default.VpnKey,
            testTag = "home_hero_action_btn"
        )
    }
}

@Composable
fun LiveTelemetryGraphCard(
    history: List<Float>,
    accentColor: Color
) {
    GlassCard(cornerRadius = 20.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LIVE BANDWIDTH TELEMETRY",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp
                    )
                )
            }

            Text(
                text = "${"%.1f".format(history.lastOrNull() ?: 0f)} KB/s",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Canvas Line Graph with Smooth Curves
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (history.size < 2) return@Canvas
                val width = size.width
                val height = size.height
                val maxVal = (history.maxOrNull() ?: 2000f).coerceAtLeast(1000f)
                val minVal = 0f

                val points = history.mapIndexed { index, valF ->
                    val x = (index.toFloat() / (history.size - 1)) * width
                    val y = height - ((valF - minVal) / (maxVal - minVal)) * height
                    Offset(x, y)
                }

                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val cx = (p1.x + p2.x) / 2f
                        cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                    }
                }

                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                // Fill Gradient
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent)
                    )
                )

                // Line Stroke
                drawPath(
                    path = strokePath,
                    color = accentColor,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun FeatureHubTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    GlassCard(
        onClick = onClick,
        cornerRadius = 18.dp,
        borderBrush = Brush.horizontalGradient(
            listOf(accentColor.copy(alpha = 0.35f), GlassBorderLight)
        ),
        testTag = testTag
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText.uppercase(),
                            style = TextStyle(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(fontSize = 12.sp, color = TextMuted)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ConnectionActivityItem(
    log: ConnectionLog,
    accentColor: Color
) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = dateFormat.format(Date(log.timestamp))

    GlassCard(cornerRadius = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when (log.status) {
                                "CONNECTED" -> EmeraldGlow.copy(alpha = 0.15f)
                                "GENERATED" -> accentColor.copy(alpha = 0.15f)
                                else -> Color.White.copy(alpha = 0.08f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = when (log.status) {
                            "CONNECTED" -> EmeraldGlow
                            "GENERATED" -> accentColor
                            else -> TextMuted
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = log.connectionCode,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "IP: ${log.ipAddress} • $timeStr",
                        style = TextStyle(fontSize = 11.sp, color = TextMuted)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (log.status) {
                            "CONNECTED" -> EmeraldGlow.copy(alpha = 0.15f)
                            "GENERATED" -> accentColor.copy(alpha = 0.15f)
                            else -> Color.White.copy(alpha = 0.08f)
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = log.status,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (log.status) {
                            "CONNECTED" -> EmeraldGlow
                            "GENERATED" -> accentColor
                            else -> TextMuted
                        }
                    )
                )
            }
        }
    }
}

@Composable
fun QuickActionsGrid(onNavigateTab: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // STREAM CARD
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .clickable { onNavigateTab(2) }
                .padding(vertical = 16.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Stream",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "STREAM",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }

        // VAULT CARD
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .clickable { onNavigateTab(3) }
                .padding(vertical = 16.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ElectricViolet.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Vault",
                        tint = ElectricViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "VAULT",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }

        // PANEL CARD
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .clickable { onNavigateTab(4) }
                .padding(vertical = 16.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFB923C).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = "Panel",
                        tint = Color(0xFFFB923C),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PANEL",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@Composable
fun ImmersiveConnectionHubCard(
    uiState: MainUiState,
    onGenerateCodeClick: () -> Unit,
    onNavigateTab: (Int) -> Unit,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Connection Hub",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "IP: ${uiState.localIp} • Active",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "P2P HUB",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Private Node Key Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    .clickable { onNavigateTab(1) }
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PRIVATE NODE KEY",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = NeonCyan,
                            letterSpacing = 2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.connectionCode.ifEmpty { "AETH-8F92" },
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = Color.White,
                            letterSpacing = 3.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val fingerprint = com.example.util.NetworkUtils.generateFingerprint(uiState.localIp)
                    Text(
                        text = "HASH: ${fingerprint.take(16)}",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Button
            GlassButton(
                text = "Generate New Code",
                onClick = onGenerateCodeClick,
                icon = Icons.Default.VpnKey,
                accentColor = CyberBlue,
                testTag = "btn_generate_hub_code"
            )
        }
    }
}
