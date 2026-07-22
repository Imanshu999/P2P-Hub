package com.example.ui.glass

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyberBackgroundEnd
import com.example.ui.theme.CyberBackgroundStart
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * High-tech Glassmorphism Background with glowing animated floating gradient Orbs
 * matching the Immersive UI design spec (#050505 canvas with top-left cyan & bottom-right purple glows).
 */
@Composable
fun GlassBackground(
    accentColor: Color = NeonCyan,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundAnimation")
    
    // Floating Orb 1 translation
    val orb1Offset by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 50f,
        animationSpec = infiniteTransitionSpec(7000),
        label = "Orb1"
    )

    // Floating Orb 2 scale / alpha
    val orb2Scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteTransitionSpec(5000),
        label = "Orb2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050505),
                        Color(0xFF090D16)
                    )
                )
            )
    ) {
        // Glowing Ambient Radial Blobs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-Left Blue/Cyan Glowing Radial Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyberBlue.copy(alpha = 0.22f), NeonCyan.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(-width * 0.1f + orb1Offset, -height * 0.05f),
                    radius = width * 0.85f
                ),
                radius = width * 0.85f,
                center = Offset(-width * 0.1f + orb1Offset, -height * 0.05f)
            )

            // Bottom-Right Purple/Violet Glowing Radial Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ElectricViolet.copy(alpha = 0.22f), NeonPurple.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(width * 1.1f, height * 0.85f + orb1Offset * 0.5f),
                    radius = width * 0.8f * orb2Scale
                ),
                radius = width * 0.8f * orb2Scale,
                center = Offset(width * 1.1f, height * 0.85f + orb1Offset * 0.5f)
            )

            // Subtle Grid Overlay
            val gridStep = 90f
            var x = 0f
            while (x < width) {
                drawLine(
                    color = Color.White.copy(alpha = 0.015f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridStep
            }
            var y = 0f
            while (y < height) {
                drawLine(
                    color = Color.White.copy(alpha = 0.015f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }
        }

        content()
    }
}

private fun infiniteTransitionSpec(durationMs: Int) = tween<Float>(
    durationMillis = durationMs,
    easing = LinearEasing
).let {
    androidx.compose.animation.core.infiniteRepeatable(
        animation = it,
        repeatMode = RepeatMode.Reverse
    )
}

/**
 * Translucent Frosted Glass Card with subtle gradient outline border
 * and optional glowing backdrop reflection.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    glowColor: Color? = null,
    borderBrush: Brush = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.25f),
            Color.White.copy(alpha = 0.05f)
        )
    ),
    backgroundColor: Color = GlassSurfaceDark,
    onClick: (() -> Unit)? = null,
    testTag: String = "glass_card",
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = androidx.compose.material3.ripple(color = NeonCyan),
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .testTag(testTag)
            .then(
                if (glowColor != null) {
                    Modifier.drawBehind {
                        drawCircle(
                            color = glowColor.copy(alpha = 0.15f),
                            radius = size.maxDimension * 0.6f,
                            center = center
                        )
                    }
                } else Modifier
            )
            .clip(shape)
            .background(backgroundColor)
            .border(BorderStroke(1.dp, borderBrush), shape)
            .then(clickableModifier)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

/**
 * High-tech Neon Glass Button with smooth glow and press states.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = NeonCyan,
    enabled: Boolean = true,
    isPulsing: Boolean = false,
    testTag: String = "glass_button"
) {
    val shape = RoundedCornerShape(16.dp)

    val infiniteTransition = rememberInfiniteTransition(label = "ButtonPulse")
    val pulseScale by if (isPulsing) {
        infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.03f,
            animationSpec = infiniteTransitionSpec(1000),
            label = "ButtonPulseScale"
        )
    } else {
        remember { androidx.compose.runtime.mutableStateOf(1f) }
    }

    Surface(
        modifier = modifier
            .testTag(testTag)
            .scale(pulseScale)
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (enabled) {
                        Brush.horizontalGradient(
                            listOf(
                                accentColor.copy(alpha = 0.85f),
                                ElectricViolet.copy(alpha = 0.85f)
                            )
                        )
                    } else {
                        SolidColor(Color.White.copy(alpha = 0.1f))
                    }
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color.White.copy(alpha = 0.4f), Color.White.copy(alpha = 0.1f))
                    ),
                    shape = shape
                )
                .padding(vertical = 14.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) Color.White else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text.uppercase(),
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (enabled) Color.White else TextMuted,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

/**
 * Translucent Glass TextField for inputting connection codes or peer endpoints.
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onPasteClick: (() -> Unit)? = null,
    onClearClick: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    testTag: String = "glass_text_field"
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(shape)
            .background(GlassSurfaceDark)
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(GlassBorderCyan, GlassBorderLight)
                    )
                ),
                shape
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(NeonCyan),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (value.isNotEmpty() && onClearClick != null) {
                Text(
                    text = "Clear",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    ),
                    modifier = Modifier
                        .clickable { onClearClick() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }

            if (onPasteClick != null) {
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onPasteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GlassBorderCyan.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Visual Status Badge with glowing pulse dot indicator.
 */
@Composable
fun GlassStatusBadge(
    status: String, // "CONNECTED", "CONNECTING", "DISCONNECTED"
    modifier: Modifier = Modifier
) {
    val (color, text, isPulsing) = when (status.uppercase()) {
        "CONNECTED" -> Triple(EmeraldGlow, "Connected", true)
        "CONNECTING" -> Triple(NeonCyan, "Connecting...", true)
        else -> Triple(AmberAlert, "Disconnected", false)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "StatusDotPulse")
    val dotAlpha by if (isPulsing) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteTransitionSpec(800),
            label = "DotAlpha"
        )
    } else {
        remember { androidx.compose.runtime.mutableStateOf(1f) }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.4f)), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = dotAlpha))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = color,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

/**
 * Glassmorphic Metric Display Card
 */
@Composable
fun GlassMetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color = NeonCyan,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        glowColor = accentColor,
        borderBrush = Brush.linearGradient(
            listOf(accentColor.copy(alpha = 0.4f), Color.White.copy(alpha = 0.05f))
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtext,
            style = TextStyle(
                fontSize = 12.sp,
                color = accentColor.copy(alpha = 0.85f)
            )
        )
    }
}
