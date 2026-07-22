package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_logs")
data class ConnectionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ipAddress: String,
    val connectionCode: String,
    val peerCode: String? = null,
    val status: String, // "CONNECTED", "DISCONNECTED", "GENERATED", "EXPIRED"
    val timestamp: Long = System.currentTimeMillis(),
    val latencyMs: Int = 14,
    val encryptionType: String = "AES-256-GCM"
)
