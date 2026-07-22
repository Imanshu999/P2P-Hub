package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_media_items")
data class VaultMediaItem(
    @PrimaryKey val id: String,
    val filePath: String,
    val fileName: String,
    val mimeType: String,
    val isVideo: Boolean,
    val sizeBytes: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val durationFormatted: String = "00:00"
)
