package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.VaultItem
import com.example.data.model.VaultMediaItem
import com.example.ui.glass.GlassButton
import com.example.ui.glass.GlassCard
import com.example.ui.glass.GlassTextField
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainUiState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VaultScreen(
    uiState: MainUiState,
    vaultItems: List<VaultItem>,
    vaultMediaItems: List<VaultMediaItem>,
    onUnlockVault: (String) -> Unit,
    onLockVault: () -> Unit,
    onUpdatePinInput: (String) -> Unit,
    onUpdateSavedPin: (String) -> Unit,
    onImportMediaUris: (List<Uri>) -> Unit,
    onDeleteMediaItem: (VaultMediaItem) -> Unit,
    onAddSecret: (String, String, String) -> Unit,
    onToggleStar: (VaultItem) -> Unit,
    onDeleteItem: (VaultItem) -> Unit,
    onShowToast: (String) -> Unit,
    accentColor: Color
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Encrypted Photos, 1: Encrypted Videos, 2: Key Vault
    var showAddSecretDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var previewMediaItem by remember { mutableStateOf<VaultMediaItem?>(null) }
    var itemToDelete by remember { mutableStateOf<VaultMediaItem?>(null) }

    // Media Picker Launcher strictly configured for Images and Videos only
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportMediaUris(uris)
        }
    }

    if (uiState.vaultPinLocked) {
        // LOCKED VAULT PIN SCREEN
        VaultPinLockView(
            uiState = uiState,
            onUpdatePinInput = onUpdatePinInput,
            onUnlock = { onUnlockVault(uiState.vaultPinInput) },
            accentColor = accentColor
        )
    } else {
        // UNLOCKED VAULT MAIN SCREEN
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .testTag("vault_screen_column")
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // VAULT HEADER & TOP CONTROLS
            GlassCard(
                cornerRadius = 24.dp,
                glowColor = ElectricViolet,
                borderBrush = Brush.linearGradient(
                    listOf(ElectricViolet.copy(alpha = 0.5f), GlassBorderLight)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = EmeraldGlow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SECURE MEDIA VAULT",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = EmeraldGlow,
                                    letterSpacing = 1.5.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val photoCount = vaultMediaItems.count { !it.isVideo }
                        val videoCount = vaultMediaItems.count { it.isVideo }
                        Text(
                            text = "$photoCount Photos • $videoCount Videos",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Change PIN Button
                        IconButton(
                            onClick = { showChangePinDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(GlassSurfaceDark)
                                .border(1.dp, GlassBorderLight, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Password,
                                contentDescription = "Change PIN",
                                tint = ElectricViolet,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Lock Vault Button
                        IconButton(
                            onClick = onLockVault,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(GlassSurfaceDark)
                                .border(1.dp, GlassBorderLight, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Vault",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // IMPORT MEDIA ACTION BUTTON
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        GlassButton(
                            text = "Import Media",
                            onClick = {
                                mediaPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            },
                            accentColor = ElectricViolet,
                            icon = Icons.Default.AddPhotoAlternate,
                            testTag = "import_vault_media_btn"
                        )
                    }

                    if (selectedSubTab == 2) {
                        IconButton(
                            onClick = { showAddSecretDialog = true },
                            modifier = Modifier
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ElectricViolet.copy(alpha = 0.2f))
                                .border(1.dp, ElectricViolet, RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Secret",
                                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectricViolet)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TAB SWITCHER: Photos | Videos | Keys & Certs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassSurfaceDark)
                    .border(1.dp, GlassBorderLight, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VaultTabChip(
                    title = "Photos",
                    icon = Icons.Default.Image,
                    isSelected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                VaultTabChip(
                    title = "Videos",
                    icon = Icons.Default.Videocam,
                    isSelected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                VaultTabChip(
                    title = "Keys",
                    icon = Icons.Default.Key,
                    isSelected = selectedSubTab == 2,
                    onClick = { selectedSubTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CONTENT GRID / LIST ACCORDING TO SELECTED TAB
            when (selectedSubTab) {
                0 -> {
                    // ENCRYPTED PHOTOS GRID
                    val photos = vaultMediaItems.filter { !it.isVideo }
                    if (photos.isEmpty()) {
                        EmptyVaultMediaView(
                            title = "No Encrypted Photos",
                            subtitle = "Tap 'Import Media' to securely encrypt and hide photos from public gallery.",
                            icon = Icons.Default.Image
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(photos, key = { it.id }) { item ->
                                EncryptedMediaGridCard(
                                    item = item,
                                    onClick = { previewMediaItem = item },
                                    onDelete = { itemToDelete = item }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // ENCRYPTED VIDEOS GRID
                    val videos = vaultMediaItems.filter { it.isVideo }
                    if (videos.isEmpty()) {
                        EmptyVaultMediaView(
                            title = "No Encrypted Videos",
                            subtitle = "Tap 'Import Media' to securely encrypt and hide video files.",
                            icon = Icons.Default.Videocam
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(videos, key = { it.id }) { item ->
                                EncryptedMediaGridCard(
                                    item = item,
                                    onClick = { previewMediaItem = item },
                                    onDelete = { itemToDelete = item }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // KEY & CERTIFICATE SECRETS LIST
                    if (vaultItems.isEmpty()) {
                        EmptyVaultMediaView(
                            title = "No Stored Keys",
                            subtitle = "Tap 'Secret' button above to store encrypted API keys & certificates.",
                            icon = Icons.Default.Key
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(vaultItems) { item ->
                                VaultItemCard(
                                    item = item,
                                    onToggleStar = { onToggleStar(item) },
                                    onDelete = { onDeleteItem(item) },
                                    onCopySecret = {
                                        copyToClipboard(context, item.title, item.encryptedValue)
                                        onShowToast("Secret copied to clipboard")
                                    },
                                    accentColor = accentColor
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    // HIGH-RES IMAGE & EMBEDDED VIDEO PLAYER VIEWER DIALOG
    previewMediaItem?.let { item ->
        MediaViewerDialog(
            item = item,
            onDismiss = { previewMediaItem = null },
            onDelete = {
                previewMediaItem = null
                itemToDelete = item
            }
        )
    }

    // DELETE CONFIRMATION DIALOG
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text("Delete Encrypted Media?", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary))
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently remove '${item.fileName}' from the vault? This cannot be undone.",
                    style = TextStyle(fontSize = 14.sp, color = TextSecondary)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteMediaItem(item)
                        itemToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ADD SECRET DIALOG
    if (showAddSecretDialog) {
        AddSecretDialog(
            onDismiss = { showAddSecretDialog = false },
            onConfirm = { title, type, secret ->
                onAddSecret(title, type, secret)
                showAddSecretDialog = false
            },
            accentColor = ElectricViolet
        )
    }

    // CHANGE PIN DIALOG
    if (showChangePinDialog) {
        ChangePinDialog(
            currentPin = uiState.savedPin,
            onDismiss = { showChangePinDialog = false },
            onConfirm = { newPin ->
                onUpdateSavedPin(newPin)
                showChangePinDialog = false
            },
            accentColor = ElectricViolet
        )
    }
}

@Composable
fun VaultTabChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) ElectricViolet else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TextMuted
                )
            )
        }
    }
}

@Composable
fun EncryptedMediaGridCard(
    item: VaultMediaItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val file = remember(item.filePath) { File(item.filePath) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(if (item.isVideo) 16f / 10f else 1f)
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurfaceDark)
            .border(1.dp, GlassBorderLight, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        // Thumbnail image preview loaded via Coil directly from the encrypted file
        AsyncImage(
            model = file,
            contentDescription = item.fileName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        // Video Play Badge Center
        if (item.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Bottom Info Row
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatFileSize(item.sizeBytes),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
            )

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

@Composable
fun MediaViewerDialog(
    item: VaultMediaItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val file = remember(item.filePath) { File(item.filePath) }
    var showDetails by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // MAIN MEDIA DISPLAY (HIGH-RES PHOTO / EMBEDDED VIDEO PLAYER)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (item.isVideo) {
                    // EMBEDDED HIGH-PERFORMANCE VIDEO PLAYER VIEW
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                val mediaController = MediaController(ctx)
                                mediaController.setAnchorView(this)
                                setMediaController(mediaController)
                                setVideoPath(file.absolutePath)
                                start()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    )
                } else {
                    // HIGH-RES PHOTO ZOOM VIEW
                    AsyncImage(
                        model = file,
                        contentDescription = item.fileName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // TOP NAVIGATION BAR
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                Text(
                    text = if (item.isVideo) "ENCRYPTED VIDEO" else "ENCRYPTED PHOTO",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGlow,
                        letterSpacing = 1.2.sp
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showDetails = !showDetails },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Details",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                }
            }

            // BOTTOM FILE DETAILS OVERLAY PANEL
            if (showDetails) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassSurfaceDark)
                        .border(1.dp, GlassBorderLight, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = item.fileName,
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Text(
                            text = "Size: ${formatFileSize(item.sizeBytes)} • Type: ${item.mimeType}",
                            style = TextStyle(fontSize = 12.sp, color = TextMuted)
                        )
                        Text(
                            text = "Encrypted File Path: ${item.filePath}",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextSecondary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Added: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(item.timestamp))}",
                            style = TextStyle(fontSize = 11.sp, color = TextMuted)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyVaultMediaView(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(GlassSurfaceDark)
            .border(1.dp, GlassBorderLight, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ElectricViolet.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricViolet,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
fun VaultPinLockView(
    uiState: MainUiState,
    onUpdatePinInput: (String) -> Unit,
    onUnlock: () -> Unit,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(0.92f),
            cornerRadius = 28.dp,
            glowColor = ElectricViolet,
            borderBrush = Brush.linearGradient(
                listOf(ElectricViolet.copy(alpha = 0.5f), GlassBorderLight)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Header Lock Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(ElectricViolet.copy(alpha = 0.15f))
                        .border(1.5.dp, ElectricViolet.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Vault Locked",
                        tint = ElectricViolet,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SECURE MEDIA VAULT",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextMuted,
                        letterSpacing = 2.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enter 4-Digit PIN to Access Vault",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = TextSecondary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Dots Display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..4) {
                        val filled = uiState.vaultPinInput.length >= i
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (filled) ElectricViolet else Color.White.copy(alpha = 0.08f))
                                .border(
                                    1.5.dp,
                                    if (filled) ElectricViolet else Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Keypad Grid - Clean Vector Icons, No Emojis
                val keyRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BACKSPACE", "0", "UNLOCK")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    keyRows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            row.forEach { key ->
                                val isUnlock = key == "UNLOCK"
                                val isBackspace = key == "BACKSPACE"

                                val bg = when {
                                    isUnlock -> EmeraldGlow.copy(alpha = 0.2f)
                                    isBackspace -> Color.White.copy(alpha = 0.08f)
                                    else -> GlassSurfaceDark
                                }

                                val borderColor = when {
                                    isUnlock -> EmeraldGlow.copy(alpha = 0.6f)
                                    else -> GlassBorderLight
                                }

                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(bg)
                                        .border(1.dp, borderColor, CircleShape)
                                        .clickable {
                                            when (key) {
                                                "BACKSPACE" -> {
                                                    if (uiState.vaultPinInput.isNotEmpty()) {
                                                        onUpdatePinInput(uiState.vaultPinInput.dropLast(1))
                                                    }
                                                }
                                                "UNLOCK" -> onUnlock()
                                                else -> {
                                                    if (uiState.vaultPinInput.length < 4) {
                                                        onUpdatePinInput(uiState.vaultPinInput + key)
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        isBackspace -> {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                contentDescription = "Backspace",
                                                tint = TextPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        isUnlock -> {
                                            Icon(
                                                imageVector = Icons.Default.LockOpen,
                                                contentDescription = "Unlock Vault",
                                                tint = EmeraldGlow,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = key,
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Default PIN: ${uiState.savedPin}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextMuted
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ChangePinDialog(
    currentPin: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    accentColor: Color
) {
    var newPin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Change Vault PIN", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Enter a new 4-digit numeric PIN:", style = TextStyle(fontSize = 13.sp, color = TextMuted))
                GlassTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) newPin = it },
                    placeholder = "4-Digit PIN (e.g. 5678)",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPin.length == 4) {
                        onConfirm(newPin)
                    }
                }
            ) {
                Text("Save PIN", color = accentColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun VaultItemCard(
    item: VaultItem,
    onToggleStar: () -> Unit,
    onDelete: () -> Unit,
    onCopySecret: () -> Unit,
    accentColor: Color
) {
    var isRevealed by remember { mutableStateOf(false) }

    GlassCard(cornerRadius = 18.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricViolet.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (item.secretType) {
                            "SSH_KEY" -> Icons.Default.VpnKey
                            "API_KEY" -> Icons.Default.Key
                            else -> Icons.Default.Note
                        },
                        contentDescription = null,
                        tint = ElectricViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.title,
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = item.secretType,
                        style = TextStyle(fontSize = 11.sp, color = TextMuted)
                    )
                }
            }

            Row {
                IconButton(onClick = onToggleStar) {
                    Icon(
                        imageVector = if (item.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Star",
                        tint = if (item.isStarred) EmeraldGlow else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .border(1.dp, GlassBorderLight, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRevealed) item.encryptedValue else "••••••••••••••••••••••••",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TextSecondary
                    ),
                    modifier = Modifier.weight(1f)
                )

                Row {
                    Text(
                        text = if (isRevealed) "Hide" else "Show",
                        style = TextStyle(fontSize = 11.sp, color = ElectricViolet, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .clickable { isRevealed = !isRevealed }
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onCopySecret() }
                    )
                }
            }
        }
    }
}

@Composable
fun AddSecretDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    accentColor: Color
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("API_KEY") }
    var secret by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Secret to Vault",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Title (e.g. Primary Node Key)",
                    modifier = Modifier.fillMaxWidth()
                )
                GlassTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    placeholder = "Encrypted Secret Payload...",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && secret.isNotBlank()) {
                        onConfirm(title, type, secret)
                    }
                }
            ) {
                Text("Store", color = accentColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp)
    )
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}
