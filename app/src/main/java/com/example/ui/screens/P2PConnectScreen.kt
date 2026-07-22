package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionLog
import com.example.ui.glass.GlassButton
import com.example.ui.glass.GlassCard
import com.example.ui.glass.GlassStatusBadge
import com.example.ui.glass.GlassTextField
import com.example.ui.theme.AmberAlert
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
import com.example.util.NetworkUtils
import com.example.viewmodel.MainUiState

@Composable
fun P2PConnectScreen(
    uiState: MainUiState,
    logs: List<ConnectionLog>,
    onGenerateCode: () -> Unit,
    onInputChange: (String) -> Unit,
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit,
    onShowToast: (String) -> Unit,
    accentColor: Color
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("p2p_connect_screen_column"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // HEADER CARD: DEVICE IP & NETWORK ADDRESS
        item {
            DeviceNetworkCard(
                ipAddress = uiState.localIp,
                accentColor = accentColor
            )
        }

        // SECTION 1: YOUR GENERATED CONNECTION CODE
        item {
            CodeGeneratorCard(
                uiState = uiState,
                onGenerateCode = onGenerateCode,
                onCopyCode = { code ->
                    copyToClipboard(context, "Connection Code", code)
                    onShowToast("Connection code copied to clipboard!")
                },
                accentColor = accentColor
            )
        }

        // SECTION 2: PASTE PEER CODE & CONNECT HUB
        item {
            PeerConnectInputCard(
                uiState = uiState,
                onInputChange = onInputChange,
                onConnect = { onConnect(uiState.inputPeerCode) },
                onDisconnect = onDisconnect,
                onPasteFromClipboard = {
                    val pasted = getFromClipboard(context)
                    if (pasted.isNotEmpty()) {
                        onInputChange(pasted)
                        onShowToast("Pasted code from clipboard")
                    } else {
                        onShowToast("Clipboard is empty")
                    }
                },
                accentColor = accentColor
            )
        }

        // SECTION 3: SESSION LOGS & HISTORY
        item {
            Text(
                text = "CONNECTION HANDSHAKE HISTORY",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextMuted,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
            )
        }

        if (logs.isEmpty()) {
            item {
                GlassCard(cornerRadius = 16.dp) {
                    Text(
                        text = "No prior connection history recorded.",
                        style = TextStyle(fontSize = 13.sp, color = TextMuted),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        } else {
            items(logs) { log ->
                HandshakeLogCard(log = log, accentColor = accentColor)
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun DeviceNetworkCard(
    ipAddress: String,
    accentColor: Color
) {
    val fingerprint = NetworkUtils.generateFingerprint(ipAddress)

    GlassCard(
        cornerRadius = 20.dp,
        borderBrush = Brush.horizontalGradient(
            listOf(accentColor.copy(alpha = 0.4f), GlassBorderLight)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "LOCAL DEVICE INTERFACE",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = ipAddress,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldGlow.copy(alpha = 0.15f))
                    .border(1.dp, EmeraldGlow.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "WLAN0 / ONLINE",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGlow
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Digest: $fingerprint",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            )
        }
    }
}

@Composable
fun CodeGeneratorCard(
    uiState: MainUiState,
    onGenerateCode: () -> Unit,
    onCopyCode: (String) -> Unit,
    accentColor: Color
) {
    var isRotating by remember { mutableStateOf(false) }
    val rotationDegrees by animateFloatAsState(
        targetValue = if (uiState.isGeneratingCode) 360f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        label = "BtnRotation"
    )

    GlassCard(
        cornerRadius = 24.dp,
        glowColor = accentColor,
        borderBrush = Brush.linearGradient(
            listOf(accentColor.copy(alpha = 0.5f), ElectricViolet.copy(alpha = 0.3f))
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YOUR HOST CONNECTION CODE",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextMuted,
                    letterSpacing = 1.5.sp
                )
            )

            Text(
                text = "Valid: 05:00",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = accentColor
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Monospace Code Container Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(accentColor.copy(alpha = 0.6f), GlassBorderLight)
                        )
                    ),
                    RoundedCornerShape(16.dp)
                )
                .padding(vertical = 20.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (uiState.isGeneratingCode) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = accentColor,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = uiState.connectionCode.ifEmpty { "AETH-8F92-4C10" },
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            letterSpacing = 3.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Derived from ${uiState.localIp} + SHA-256 Hash",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Generate Code Button
            GlassButton(
                text = if (uiState.isGeneratingCode) "Generating..." else "Generate Code",
                onClick = onGenerateCode,
                accentColor = accentColor,
                icon = Icons.Default.Refresh,
                isPulsing = true,
                modifier = Modifier.weight(1.3f),
                testTag = "generate_code_button"
            )

            // Copy Code Button
            IconButton(
                onClick = { onCopyCode(uiState.connectionCode) },
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassSurfaceDark)
                    .border(1.dp, GlassBorderLight, RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Code",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PeerConnectInputCard(
    uiState: MainUiState,
    onInputChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onPasteFromClipboard: () -> Unit,
    accentColor: Color
) {
    GlassCard(
        cornerRadius = 24.dp,
        glowColor = if (uiState.connectionStatus == "CONNECTED") EmeraldGlow else null,
        borderBrush = Brush.linearGradient(
            listOf(
                if (uiState.connectionStatus == "CONNECTED") EmeraldGlow.copy(alpha = 0.5f) else GlassBorderCyan,
                GlassBorderLight
            )
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PASTE & CONNECT TO PEER",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextMuted,
                    letterSpacing = 1.5.sp
                )
            )

            GlassStatusBadge(status = uiState.connectionStatus)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Field
        GlassTextField(
            value = uiState.inputPeerCode,
            onValueChange = onInputChange,
            placeholder = "Enter Peer Code (e.g. AETH-7940-2018)",
            onPasteClick = onPasteFromClipboard,
            onClearClick = { onInputChange("") },
            modifier = Modifier.fillMaxWidth(),
            testTag = "peer_code_input_field"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // CONNECT / DISCONNECT CONTROLS & VISUAL FEEDBACK
        if (uiState.connectionStatus == "CONNECTING") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.handshakeStepMessage,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = NeonCyan
                        )
                    )
                    Text(
                        text = "${(uiState.handshakeProgress * 100).toInt()}%",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { uiState.handshakeProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NeonCyan,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        } else if (uiState.connectionStatus == "CONNECTED") {
            // Connected Active Peer Summary Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EmeraldGlow.copy(alpha = 0.1f))
                    .border(1.dp, EmeraldGlow.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGlow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tunnel Active",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGlow
                                )
                            )
                        }

                        Text(
                            text = "${uiState.latencyMs} ms",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGlow
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Connected Peer: ${uiState.connectedPeerCode}",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    Text(
                        text = "Peer Endpoint: ${uiState.connectedPeerIp} • AES-256-GCM",
                        style = TextStyle(fontSize = 12.sp, color = TextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            GlassButton(
                text = "Disconnect Tunnel",
                onClick = onDisconnect,
                accentColor = AmberAlert,
                icon = Icons.Default.LinkOff,
                testTag = "disconnect_button"
            )
        } else {
            GlassButton(
                text = "Paste & Connect",
                onClick = onConnect,
                accentColor = accentColor,
                icon = Icons.Default.Link,
                testTag = "paste_and_connect_button"
            )
        }
    }
}

@Composable
fun HandshakeLogCard(
    log: ConnectionLog,
    accentColor: Color
) {
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
                            if (log.status == "CONNECTED") EmeraldGlow.copy(alpha = 0.15f)
                            else accentColor.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = if (log.status == "CONNECTED") EmeraldGlow else accentColor,
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
                        text = "Peer: ${log.peerCode ?: "Self Generated"} • ${log.latencyMs}ms",
                        style = TextStyle(fontSize = 11.sp, color = TextMuted)
                    )
                }
            }

            Text(
                text = log.status,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (log.status == "CONNECTED") EmeraldGlow else TextMuted
                )
            )
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

private fun getFromClipboard(context: Context): String {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = clipboard.primaryClip
    if (clip != null && clip.itemCount > 0) {
        return clip.getItemAt(0).text.toString()
    }
    return ""
}
