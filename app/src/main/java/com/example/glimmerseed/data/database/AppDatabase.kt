package com.example.glimmerseed.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.glimmerseed.data.asset.AssetDao
import com.example.glimmerseed.data.asset.AssetEntity
import com.example.glimmerseed.data.dao.CommandHistoryDao
import com.example.glimmerseed.data.dao.UserPrefDao
import com.example.glimmerseed.data.entity.CommandHistoryEntity
import com.example.glimmerseed.data.entity.UserPref

@Database(
    entities = [UserPref::class, CommandHistoryEntity::class, AssetEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userPrefDao(): UserPrefDao
    abstract fun commandHistoryDao(): CommandHistoryDao
    abstract fun assetDao(): AssetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glimmerseed_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}