package com.example.data.repository

import com.example.data.local.ConnectionDao
import com.example.data.local.VaultDao
import com.example.data.local.VaultMediaDao
import com.example.data.model.ConnectionLog
import com.example.data.model.VaultItem
import com.example.data.model.VaultMediaItem
import kotlinx.coroutines.flow.Flow

class AetherRepository(
    private val connectionDao: ConnectionDao,
    private val vaultDao: VaultDao,
    private val vaultMediaDao: VaultMediaDao
) {
    val allLogs: Flow<List<ConnectionLog>> = connectionDao.getAllLogs()
    val allVaultItems: Flow<List<VaultItem>> = vaultDao.getAllVaultItems()
    val allVaultMediaItems: Flow<List<VaultMediaItem>> = vaultMediaDao.getAllMediaItems()

    suspend fun insertLog(log: ConnectionLog) = connectionDao.insertLog(log)
    suspend fun clearLogs() = connectionDao.clearLogs()

    suspend fun insertVaultItem(item: VaultItem) = vaultDao.insertVaultItem(item)
    suspend fun updateVaultItem(item: VaultItem) = vaultDao.updateVaultItem(item)
    suspend fun deleteVaultItem(item: VaultItem) = vaultDao.deleteVaultItem(item)

    suspend fun insertVaultMediaItem(item: VaultMediaItem) = vaultMediaDao.insertMediaItem(item)
    suspend fun deleteVaultMediaItem(item: VaultMediaItem) = vaultMediaDao.deleteMediaItem(item)
}
