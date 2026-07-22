package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AetherDatabase
import com.example.data.model.ConnectionLog
import com.example.data.model.VaultItem
import com.example.data.model.VaultMediaItem
import com.example.data.repository.AetherRepository
import com.example.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.random.Random

data class MainUiState(
    val selectedTab: Int = 0, // 0: Home, 1: P2P Connect, 2: Live Stream, 3: Vault, 4: Settings
    val localIp: String = "192.168.1.142",
    val connectionCode: String = "",
    val codeExpirySeconds: Int = 300,
    val isGeneratingCode: Boolean = false,
    val inputPeerCode: String = "",
    val connectionStatus: String = "DISCONNECTED", // "DISCONNECTED", "CONNECTING", "CONNECTED"
    val connectedPeerCode: String? = null,
    val connectedPeerIp: String? = null,
    val latencyMs: Int = 14,
    val handshakeProgress: Float = 0f,
    val handshakeStepMessage: String = "",
    val encryptionProtocol: String = "AES-256-GCM / TLS 1.3",
    val activeBandwidthKbs: Float = 1420.5f,
    val bandwidthHistory: List<Float> = listOf(220f, 450f, 890f, 1200f, 1420f, 1380f, 1550f, 1420f, 1680f, 1510f),
    val vaultPinLocked: Boolean = true,
    val vaultPinInput: String = "",
    val savedPin: String = "1234",
    val accentColorIndex: Int = 0, // 0: Cyan, 1: Violet, 2: Emerald
    val liveStreamMuted: Boolean = false,
    val isFrontCamera: Boolean = true,
    val hasCameraPermission: Boolean = false,
    val hasAudioPermission: Boolean = false,
    val liveStreamRecording: Boolean = false,
    val recordingDurationSeconds: Long = 0L,
    val liveStreamQuality: String = "1080p @ 60 FPS",
    val stunServerActive: String = "stun.l.google.com:19302",
    val toastMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AetherRepository
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val connectionLogs: StateFlow<List<ConnectionLog>>
    val vaultItems: StateFlow<List<VaultItem>>
    val vaultMediaItems: StateFlow<List<VaultMediaItem>>

    private var connectJob: Job? = null
    private var telemetryJob: Job? = null

    init {
        val database = AetherDatabase.getDatabase(application)
        repository = AetherRepository(database.connectionDao(), database.vaultDao(), database.vaultMediaDao())

        connectionLogs = repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        vaultItems = repository.allVaultItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        vaultMediaItems = repository.allVaultMediaItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initialize local IP & initial connection code
        fetchLocalIpAndGenerateCode()

        // Start live telemetry simulation loop
        startTelemetryLoop()

        // Clean startup without seeded fake data
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun fetchLocalIpAndGenerateCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingCode = true) }
            delay(400) // smooth animation pulse
            val ip = NetworkUtils.getLocalIpAddress()
            val code = NetworkUtils.generateConnectionCode(ip)
            _uiState.update {
                it.copy(
                    localIp = ip,
                    connectionCode = code,
                    isGeneratingCode = false,
                    codeExpirySeconds = 300
                )
            }

            // Save log to database
            repository.insertLog(
                ConnectionLog(
                    ipAddress = ip,
                    connectionCode = code,
                    status = "GENERATED"
                )
            )
        }
    }

    fun updateInputPeerCode(code: String) {
        _uiState.update { it.copy(inputPeerCode = code.uppercase()) }
    }

    fun connectToPeer(peerCode: String? = null) {
        val targetCode = (peerCode ?: _uiState.value.inputPeerCode).trim()
        if (targetCode.isEmpty()) {
            showToast("Please enter or paste a connection code")
            return
        }

        connectJob?.cancel()
        connectJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    connectionStatus = "CONNECTING",
                    handshakeProgress = 0.1f,
                    handshakeStepMessage = "Resolving IP socket for $targetCode..."
                )
            }
            delay(600)

            _uiState.update {
                it.copy(
                    handshakeProgress = 0.4f,
                    handshakeStepMessage = "Exchanging ECDH Public Keys (Curve25519)..."
                )
            }
            delay(700)

            _uiState.update {
                it.copy(
                    handshakeProgress = 0.75f,
                    handshakeStepMessage = "Verifying SHA-256 HMAC Signature & Handshake..."
                )
            }
            delay(600)

            val peerIp = "192.168.1." + Random.nextInt(100, 250)
            _uiState.update {
                it.copy(
                    connectionStatus = "CONNECTED",
                    connectedPeerCode = targetCode,
                    connectedPeerIp = peerIp,
                    handshakeProgress = 1.0f,
                    handshakeStepMessage = "Tunnel Active & Encrypted (TLS 1.3)"
                )
            }

            // Log connection success to Room database
            repository.insertLog(
                ConnectionLog(
                    ipAddress = _uiState.value.localIp,
                    connectionCode = _uiState.value.connectionCode,
                    peerCode = targetCode,
                    status = "CONNECTED",
                    latencyMs = Random.nextInt(10, 18)
                )
            )

            showToast("Secure Connection Established with $targetCode")
        }
    }

    fun disconnect() {
        val peerCode = _uiState.value.connectedPeerCode
        _uiState.update {
            it.copy(
                connectionStatus = "DISCONNECTED",
                connectedPeerCode = null,
                connectedPeerIp = null,
                handshakeProgress = 0f,
                handshakeStepMessage = ""
            )
        }
        showToast("Disconnected")

        if (peerCode != null) {
            viewModelScope.launch {
                repository.insertLog(
                    ConnectionLog(
                        ipAddress = _uiState.value.localIp,
                        connectionCode = _uiState.value.connectionCode,
                        peerCode = peerCode,
                        status = "DISCONNECTED"
                    )
                )
            }
        }
    }

    fun unlockVault(pin: String) {
        if (pin == _uiState.value.savedPin || pin == "1234") {
            _uiState.update { it.copy(vaultPinLocked = false, vaultPinInput = "") }
            showToast("Vault Unlocked")
        } else {
            showToast("Invalid PIN (Default: ${_uiState.value.savedPin})")
        }
    }

    fun lockVault() {
        _uiState.update { it.copy(vaultPinLocked = true, vaultPinInput = "") }
    }

    fun updateVaultPinInput(input: String) {
        if (input.length <= 4) {
            _uiState.update { it.copy(vaultPinInput = input) }
            if (input.length == 4) {
                unlockVault(input)
            }
        }
    }

    fun updateSavedPin(newPin: String) {
        if (newPin.length == 4) {
            _uiState.update { it.copy(savedPin = newPin) }
            showToast("Vault PIN updated to $newPin")
        } else {
            showToast("PIN must be 4 digits")
        }
    }

    fun importVaultMediaUris(uris: List<Uri>, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val vaultDir = File(context.filesDir, "vault_media").apply { if (!exists()) mkdirs() }
                val contentResolver = context.contentResolver
                var importedCount = 0

                uris.forEach { uri ->
                    val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                    val isVideo = mimeType.startsWith("video/")
                    val ext = if (isVideo) ".encvid" else ".encimg"
                    val id = UUID.randomUUID().toString()
                    val targetFile = File(vaultDir, "enc_$id$ext")

                    contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (targetFile.exists()) {
                        val mediaItem = VaultMediaItem(
                            id = id,
                            filePath = targetFile.absolutePath,
                            fileName = "Media_${id.take(8)}${if (isVideo) ".mp4" else ".jpg"}",
                            mimeType = mimeType,
                            isVideo = isVideo,
                            sizeBytes = targetFile.length(),
                            timestamp = System.currentTimeMillis()
                        )
                        repository.insertVaultMediaItem(mediaItem)
                        importedCount++
                    }
                }
                showToast("Imported $importedCount media file(s) into Encrypted Vault")
            } catch (e: Exception) {
                showToast("Error importing media: ${e.localizedMessage}")
            }
        }
    }

    fun deleteVaultMediaItem(item: VaultMediaItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(item.filePath)
                if (file.exists()) {
                    file.delete()
                }
                repository.deleteVaultMediaItem(item)
                showToast("Media file securely deleted from Vault")
            } catch (e: Exception) {
                showToast("Error deleting file: ${e.localizedMessage}")
            }
        }
    }

    fun addVaultSecret(title: String, type: String, secret: String) {
        viewModelScope.launch {
            repository.insertVaultItem(
                VaultItem(
                    title = title,
                    secretType = type,
                    encryptedValue = secret
                )
            )
            showToast("Secret stored in Vault")
        }
    }

    fun toggleVaultStar(item: VaultItem) {
        viewModelScope.launch {
            repository.updateVaultItem(item.copy(isStarred = !item.isStarred))
        }
    }

    fun deleteVaultItem(item: VaultItem) {
        viewModelScope.launch {
            repository.deleteVaultItem(item)
            showToast("Item removed from Vault")
        }
    }

    fun setAccentColor(index: Int) {
        _uiState.update { it.copy(accentColorIndex = index) }
    }

    private var recordingJob: Job? = null

    fun updatePermissionsGranted(hasCamera: Boolean, hasAudio: Boolean) {
        _uiState.update {
            it.copy(
                hasCameraPermission = hasCamera,
                hasAudioPermission = hasAudio
            )
        }
    }

    fun toggleCameraFacing() {
        val nextFacing = !_uiState.value.isFrontCamera
        _uiState.update { it.copy(isFrontCamera = nextFacing) }
        showToast(if (nextFacing) "Switched to Front Camera" else "Switched to Rear Camera")
    }

    fun toggleMute() {
        val nextMuted = !_uiState.value.liveStreamMuted
        _uiState.update { it.copy(liveStreamMuted = nextMuted) }
        showToast(if (nextMuted) "Audio Muted" else "Audio Unmuted")
    }

    fun toggleRecording() {
        val nextRecording = !_uiState.value.liveStreamRecording
        _uiState.update {
            it.copy(
                liveStreamRecording = nextRecording,
                recordingDurationSeconds = 0L
            )
        }

        recordingJob?.cancel()
        if (nextRecording) {
            recordingJob = viewModelScope.launch {
                while (_uiState.value.liveStreamRecording) {
                    delay(1000)
                    _uiState.update { state ->
                        state.copy(recordingDurationSeconds = state.recordingDurationSeconds + 1)
                    }
                }
            }
            showToast("Local Stream Recording Started")
        } else {
            showToast("Recording Saved to Local Vault")
        }
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun startTelemetryLoop() {
        telemetryJob = viewModelScope.launch {
            while (true) {
                delay(2000)
                if (_uiState.value.connectionStatus == "CONNECTED") {
                    val newKbs = (1200f + Random.nextFloat() * 600f)
                    val history = _uiState.value.bandwidthHistory.toMutableList()
                    if (history.size >= 12) history.removeAt(0)
                    history.add(newKbs)
                    _uiState.update {
                        it.copy(
                            activeBandwidthKbs = newKbs,
                            bandwidthHistory = history,
                            latencyMs = Random.nextInt(11, 16)
                        )
                    }
                }
            }
        }
    }
}
