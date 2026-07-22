package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.glass.GlassBackground
import com.example.ui.glass.GlassBottomNavigation
import com.example.ui.glass.GlassTopBar
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveStreamScreen
import com.example.ui.screens.P2PConnectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(mainViewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val logs by mainViewModel.connectionLogs.collectAsStateWithLifecycle()
    val vaultItems by mainViewModel.vaultItems.collectAsStateWithLifecycle()
    val vaultMediaItems by mainViewModel.vaultMediaItems.collectAsStateWithLifecycle()

    // Determine current accent color
    val accentColor: Color = when (uiState.accentColorIndex) {
        1 -> ElectricViolet
        2 -> EmeraldGlow
        else -> NeonCyan
    }

    // Toast listener
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            mainViewModel.clearToast()
        }
    }

    GlassBackground(accentColor = accentColor) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    uiState = uiState,
                    accentColor = accentColor,
                    onNavigateTab = { mainViewModel.selectTab(it) }
                )
            },
            bottomBar = {
                GlassBottomNavigation(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { mainViewModel.selectTab(it) },
                    accentColor = accentColor
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = uiState.selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeScreen(
                            uiState = uiState,
                            logs = logs,
                            onNavigateTab = { mainViewModel.selectTab(it) },
                            onGenerateCodeClick = { mainViewModel.fetchLocalIpAndGenerateCode() },
                            accentColor = accentColor
                        )

                        1 -> P2PConnectScreen(
                            uiState = uiState,
                            logs = logs,
                            onGenerateCode = { mainViewModel.fetchLocalIpAndGenerateCode() },
                            onInputChange = { mainViewModel.updateInputPeerCode(it) },
                            onConnect = { mainViewModel.connectToPeer(it) },
                            onDisconnect = { mainViewModel.disconnect() },
                            onShowToast = { mainViewModel.showToast(it) },
                            accentColor = accentColor
                        )

                        2 -> LiveStreamScreen(
                            uiState = uiState,
                            onToggleMute = { mainViewModel.toggleMute() },
                            onToggleCameraFacing = { mainViewModel.toggleCameraFacing() },
                            onToggleRecording = { mainViewModel.toggleRecording() },
                            onDisconnect = { mainViewModel.disconnect() },
                            onUpdatePermissions = { camera, audio ->
                                mainViewModel.updatePermissionsGranted(camera, audio)
                            },
                            onShowToast = { mainViewModel.showToast(it) },
                            accentColor = accentColor
                        )

                        3 -> VaultScreen(
                            uiState = uiState,
                            vaultItems = vaultItems,
                            vaultMediaItems = vaultMediaItems,
                            onUnlockVault = { mainViewModel.unlockVault(it) },
                            onLockVault = { mainViewModel.lockVault() },
                            onUpdatePinInput = { mainViewModel.updateVaultPinInput(it) },
                            onUpdateSavedPin = { mainViewModel.updateSavedPin(it) },
                            onImportMediaUris = { uris -> mainViewModel.importVaultMediaUris(uris, context) },
                            onDeleteMediaItem = { mainViewModel.deleteVaultMediaItem(it) },
                            onAddSecret = { title, type, secret ->
                                mainViewModel.addVaultSecret(title, type, secret)
                            },
                            onToggleStar = { mainViewModel.toggleVaultStar(it) },
                            onDeleteItem = { mainViewModel.deleteVaultItem(it) },
                            onShowToast = { mainViewModel.showToast(it) },
                            accentColor = accentColor
                        )

                        4 -> SettingsScreen(
                            uiState = uiState,
                            onSetAccentColor = { mainViewModel.setAccentColor(it) },
                            onShowToast = { mainViewModel.showToast(it) },
                            accentColor = accentColor
                        )
                    }
                }
            }
        }
    }
}
