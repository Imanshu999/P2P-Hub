package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.glass.GlassCard
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainUiState

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onSetAccentColor: (Int) -> Unit,
    onShowToast: (String) -> Unit,
    accentColor: Color
) {
    var autoRotate by remember { mutableStateOf(true) }
    var encryptedDns by remember { mutableStateOf(true) }
    var strictHandshake by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("settings_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // SECTION 1: GLASS ACCENT COLOR PICKER
        item {
            GlassCard(
                cornerRadius = 24.dp,
                glowColor = accentColor,
                borderBrush = Brush.linearGradient(
                    listOf(accentColor.copy(alpha = 0.5f), GlassBorderLight)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "NEON GLASS ACCENT THEME",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = accentColor,
                            letterSpacing = 1.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val colors = listOf(
                        Triple(0, NeonCyan, "Cyber Cyan"),
                        Triple(1, ElectricViolet, "Electric Violet"),
                        Triple(2, EmeraldGlow, "Emerald Glow")
                    )

                    colors.forEach { (index, col, name) ->
                        val isSelected = uiState.accentColorIndex == index
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                onSetAccentColor(index)
                                onShowToast("Theme updated to $name")
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        2.dp,
                                        if (isSelected) Color.White else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = name,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextPrimary else TextMuted
                                )
                            )
                        }
                    }
                }
            }
        }

        // SECTION 2: SECURITY PREFERENCES
        item {
            Text(
                text = "SECURITY & PROTOCOL PREFERENCES",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextMuted,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        item {
            GlassCard(cornerRadius = 18.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SettingToggleRow(
                        title = "Auto-Rotate Connection Code",
                        subtitle = "Generates new SHA-256 code every 5 minutes",
                        icon = Icons.Default.Security,
                        checked = autoRotate,
                        onCheckedChange = { autoRotate = it },
                        accentColor = accentColor
                    )

                    SettingToggleRow(
                        title = "Encrypted DNS-over-HTTPS",
                        subtitle = "Prevents ISP socket leaking",
                        icon = Icons.Default.Shield,
                        checked = encryptedDns,
                        onCheckedChange = { encryptedDns = it },
                        accentColor = accentColor
                    )

                    SettingToggleRow(
                        title = "Aggressive ECDH Handshake",
                        subtitle = "Requires double signature verification",
                        icon = Icons.Default.Router,
                        checked = strictHandshake,
                        onCheckedChange = { strictHandshake = it },
                        accentColor = accentColor
                    )
                }
            }
        }

        // SECTION 3: SYSTEM DIAGNOSTICS & ABOUT
        item {
            GlassCard(cornerRadius = 18.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SYSTEM INFORMATION",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TextMuted,
                            letterSpacing = 1.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SystemInfoRow(label = "Application Version", value = "AetherLink v1.0.0-PROD")
                    SystemInfoRow(label = "Encryption Engine", value = "AES-256-GCM / Curve25519")
                    SystemInfoRow(label = "Local IP Interface", value = uiState.localIp)
                    SystemInfoRow(label = "Network Socket Port", value = "TCP 8443 (Secure)")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Text(
                    text = subtitle,
                    style = TextStyle(fontSize = 11.sp, color = TextMuted)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun SystemInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = TextStyle(fontSize = 12.sp, color = TextMuted))
        Text(text = value, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextPrimary))
    }
}
