package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.glass.GlassButton
import com.example.ui.glass.GlassCard
import com.example.ui.glass.GlassTextField
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SunsetOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.MainUiState

data class ChatMessage(val sender: String, val text: String, val time: String)

@Composable
fun LiveStreamScreen(
    uiState: MainUiState,
    onToggleMute: () -> Unit,
    onToggleCameraFacing: () -> Unit,
    onToggleRecording: () -> Unit,
    onDisconnect: () -> Unit,
    onUpdatePermissions: (Boolean, Boolean) -> Unit,
    onShowToast: (String) -> Unit,
    accentColor: Color
) {
    val context = LocalContext.current
    var chatInput by remember { mutableStateOf("") }
    val chatMessages = remember {
        mutableStateListOf<ChatMessage>()
    }

    // Permission launcher for Camera & Microphone
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        onUpdatePermissions(cameraGranted, audioGranted)
        if (cameraGranted && audioGranted) {
            onShowToast("Camera & Microphone Granted")
        } else {
            onShowToast("Permissions required for live stream")
        }
    }

    // Check permissions on enter
    LaunchedEffect(Unit) {
        val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        onUpdatePermissions(hasCam, hasMic)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("live_stream_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // PERMISSION CARD BANNER IF NOT GRANTED
        if (!uiState.hasCameraPermission || !uiState.hasAudioPermission) {
            item {
                PermissionRequestGlassCard(
                    onRequestPermissions = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            )
                        )
                    },
                    accentColor = accentColor
                )
            }
        }

        // CAMERA / PEER VIDEO PREVIEW CANVAS FRAME WITH WEBRTC HUD OVERLAY
        item {
            LiveVideoCanvasBox(
                uiState = uiState,
                onToggleMute = onToggleMute,
                onToggleCameraFacing = onToggleCameraFacing,
                onToggleRecording = onToggleRecording,
                accentColor = accentColor
            )
        }

        // CONTROL BUTTONS TOOLBAR (MUTE, FLIP, RECORD, HD, DISCONNECT)
        item {
            StreamControlToolbar(
                uiState = uiState,
                onToggleMute = onToggleMute,
                onToggleCameraFacing = onToggleCameraFacing,
                onToggleRecording = onToggleRecording,
                onDisconnect = onDisconnect,
                onShowToast = onShowToast,
                accentColor = accentColor
            )
        }

        // REAL-TIME SIGNALING & ENCRYPTED CHAT
        item {
            GlassCard(cornerRadius = 20.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WEBRTC SIGNALING CHANNEL",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TextMuted,
                            letterSpacing = 1.2.sp
                        )
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldGlow.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "STUN CONNECTED",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGlow
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chat Messages List
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    chatMessages.forEach { msg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${msg.sender}: ${msg.text}",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = msg.time,
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Send Message Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = "Send signal text...",
                        modifier = Modifier.weight(1f),
                        testTag = "chat_signal_input"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                chatMessages.add(ChatMessage("You", chatInput.trim(), "NOW"))
                                chatInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun PermissionRequestGlassCard(
    onRequestPermissions: () -> Unit,
    accentColor: Color
) {
    GlassCard(
        cornerRadius = 20.dp,
        glowColor = SunsetOrange,
        borderBrush = Brush.linearGradient(listOf(SunsetOrange.copy(alpha = 0.5f), GlassBorderLight))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SunsetOrange.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = SunsetOrange,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Permissions Required",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Grant Camera & Audio access for live WebRTC video streaming",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        GlassButton(
            text = "Grant Camera & Microphone",
            onClick = onRequestPermissions,
            icon = Icons.Default.Videocam,
            accentColor = SunsetOrange,
            testTag = "btn_grant_permissions"
        )
    }
}

@Composable
fun LiveVideoCanvasBox(
    uiState: MainUiState,
    onToggleMute: () -> Unit,
    onToggleCameraFacing: () -> Unit,
    onToggleRecording: () -> Unit,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ScanlineAnimation")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ScanY"
    )

    GlassCard(
        cornerRadius = 24.dp,
        glowColor = accentColor,
        borderBrush = Brush.linearGradient(
            listOf(accentColor.copy(alpha = 0.5f), GlassBorderLight)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
        ) {
            // MAIN BACKGROUND VIEW: WebRTC Remote Stream Video Renderer
            if (uiState.hasCameraPermission) {
                // Active WebRTC Remote Peer Stream Container
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraXPreviewView(
                        isFrontCamera = false, // Remote view feed
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // Fallback Dark Canvas when waiting for camera permissions
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF020617)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Awaiting WebRTC MediaStream Permission",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }

            // PICTURE-IN-PICTURE (PiP): Local Camera Preview View
            if (uiState.hasCameraPermission) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .width(110.dp)
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .border(1.5.dp, accentColor, RoundedCornerShape(12.dp))
                        .clickable { onToggleCameraFacing() }
                ) {
                    CameraXPreviewView(
                        isFrontCamera = uiState.isFrontCamera,
                        modifier = Modifier.fillMaxSize()
                    )

                    // PiP Overlay Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "YOU",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    // PiP Flip Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Flip",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // HUD OVERLAY BADGES & CONTROLS
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Top Left: Live Status Badge & Recording Timer
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (uiState.liveStreamRecording) SunsetOrange.copy(alpha = 0.9f)
                                else EmeraldGlow.copy(alpha = 0.85f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val seconds = uiState.recordingDurationSeconds
                        val timerText = if (uiState.liveStreamRecording) {
                            String.format("REC %02d:%02d", seconds / 60, seconds % 60)
                        } else {
                            "LIVE STREAM"
                        }
                        Text(
                            text = timerText,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = uiState.liveStreamQuality,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                // Top Right: Latency & Bitrate
                Column(modifier = Modifier.align(Alignment.TopEnd), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${uiState.latencyMs}ms • 6.4 Mbps",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )
                    Text(
                        text = "STUN: ${uiState.stunServerActive.take(18)}...",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }

                // Bottom Center: Stream Lens & Security Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmeraldGlow,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "E2EE DTLS-SRTP",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CameraXPreviewView(
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = if (isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (e: Exception) {
                    Log.e("CameraXPreview", "Camera preview binding failed", e)
                }
            }, ContextCompat.getMainExecutor(previewView.context))
        },
        modifier = modifier
    )
}

@Composable
fun StreamControlToolbar(
    uiState: MainUiState,
    onToggleMute: () -> Unit,
    onToggleCameraFacing: () -> Unit,
    onToggleRecording: () -> Unit,
    onDisconnect: () -> Unit,
    onShowToast: (String) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mute / Unmute
        IconButton(
            onClick = onToggleMute,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (uiState.liveStreamMuted) SunsetOrange.copy(alpha = 0.2f) else GlassSurfaceDark)
                .border(
                    1.dp,
                    if (uiState.liveStreamMuted) SunsetOrange else GlassBorderLight,
                    CircleShape
                )
                .testTag("btn_toggle_mute")
        ) {
            Icon(
                imageVector = if (uiState.liveStreamMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mute",
                tint = if (uiState.liveStreamMuted) SunsetOrange else TextPrimary
            )
        }

        // Camera Flip (Front/Rear)
        IconButton(
            onClick = onToggleCameraFacing,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(GlassSurfaceDark)
                .border(1.dp, GlassBorderLight, CircleShape)
                .testTag("btn_flip_camera")
        ) {
            Icon(
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = "Flip Camera",
                tint = TextPrimary
            )
        }

        // Record Stream Button
        IconButton(
            onClick = onToggleRecording,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (uiState.liveStreamRecording) SunsetOrange else accentColor)
                .testTag("btn_toggle_recording")
        ) {
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                contentDescription = "Record",
                tint = Color.Black,
                modifier = Modifier.size(26.dp)
            )
        }

        // Quality HD Badge
        IconButton(
            onClick = { onShowToast("Stream Quality locked at 1080p60 E2EE") },
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(GlassSurfaceDark)
                .border(1.dp, GlassBorderLight, CircleShape)
                .testTag("btn_hd_quality")
        ) {
            Icon(
                imageVector = Icons.Default.Hd,
                contentDescription = "HD Quality",
                tint = accentColor
            )
        }

        // Disconnect P2P Stream Button
        IconButton(
            onClick = onDisconnect,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(SunsetOrange.copy(alpha = 0.2f))
                .border(1.dp, SunsetOrange, CircleShape)
                .testTag("btn_disconnect_stream")
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "Disconnect Stream",
                tint = SunsetOrange
            )
        }
    }
}
