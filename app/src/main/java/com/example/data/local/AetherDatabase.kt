package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ConnectionLog
import com.example.data.model.VaultItem
import com.example.data.model.VaultMediaItem

@Database(entities = [ConnectionLog::class, VaultItem::class, VaultMediaItem::class], version = 2, exportSchema = false)
abstract class AetherDatabase : RoomDatabase() {
    abstract fun connectionDao(): ConnectionDao
    abstract fun vaultDao(): VaultDao
    abstract fun vaultMediaDao(): VaultMediaDao

    companion object {
        @Volatile
        private var INSTANCE: AetherDatabase? = null

        fun getDatabase(context: Context): AetherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AetherDatabase::class.java,
                    "aether_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
