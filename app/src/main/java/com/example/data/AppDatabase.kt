package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AiModelConfigDao
import com.example.data.models.AiModelConfig

@Database(entities = [AiModelConfig::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun aiModelConfigDao(): AiModelConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mictab_database"
                )
                // Add prepopulation if needed
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
