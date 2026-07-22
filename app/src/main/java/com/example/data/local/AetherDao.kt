package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ConnectionLog
import com.example.data.model.VaultItem
import com.example.data.model.VaultMediaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connection_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ConnectionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ConnectionLog): Long

    @Query("DELETE FROM connection_logs")
    suspend fun clearLogs()
}

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items ORDER BY isStarred DESC, timestamp DESC")
    fun getAllVaultItems(): Flow<List<VaultItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItem): Long

    @Update
    suspend fun updateVaultItem(item: VaultItem)

    @Delete
    suspend fun deleteVaultItem(item: VaultItem)
}

@Dao
interface VaultMediaDao {
    @Query("SELECT * FROM vault_media_items ORDER BY timestamp DESC")
    fun getAllMediaItems(): Flow<List<VaultMediaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(item: VaultMediaItem)

    @Delete
    suspend fun deleteMediaItem(item: VaultMediaItem)
}
