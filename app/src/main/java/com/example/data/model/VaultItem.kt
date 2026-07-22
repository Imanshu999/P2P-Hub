package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val secretType: String, // "API_KEY", "SSH_KEY", "TOKEN", "ENCRYPTED_NOTE", "PEER_CERT"
    val encryptedValue: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStarred: Boolean = false
)
