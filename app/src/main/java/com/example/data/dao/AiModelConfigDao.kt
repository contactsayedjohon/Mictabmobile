package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.AiModelConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface AiModelConfigDao {
    @Query("SELECT * FROM ai_models ORDER BY priority ASC")
    fun getAllModels(): Flow<List<AiModelConfig>>
    
    @Query("SELECT * FROM ai_models WHERE type = :type AND isEnabled = 1 ORDER BY priority ASC")
    fun getEnabledModelsByType(type: String): Flow<List<AiModelConfig>>

    @Query("SELECT * FROM ai_models WHERE type = :type AND isEnabled = 1 ORDER BY priority ASC")
    suspend fun getEnabledModelsByTypeSync(type: String): List<AiModelConfig>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: AiModelConfig)

    @Update
    suspend fun update(model: AiModelConfig)

    @Delete
    suspend fun delete(model: AiModelConfig)
}
