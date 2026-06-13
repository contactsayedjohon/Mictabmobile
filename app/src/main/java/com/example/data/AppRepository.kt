package com.example.data

import com.example.data.dao.AiModelConfigDao
import com.example.data.models.AiModelConfig
import kotlinx.coroutines.flow.Flow

class AppRepository(private val aiModelConfigDao: AiModelConfigDao) {
    val allModels: Flow<List<AiModelConfig>> = aiModelConfigDao.getAllModels()
    
    fun getEnabledModelsByType(type: String): Flow<List<AiModelConfig>> {
        return aiModelConfigDao.getEnabledModelsByType(type)
    }

    suspend fun getEnabledModelsByTypeSync(type: String): List<AiModelConfig> {
        return aiModelConfigDao.getEnabledModelsByTypeSync(type)
    }

    suspend fun insertModel(model: AiModelConfig) {
        aiModelConfigDao.insert(model)
    }

    suspend fun updateModel(model: AiModelConfig) {
        aiModelConfigDao.update(model)
    }

    suspend fun deleteModel(model: AiModelConfig) {
        aiModelConfigDao.delete(model)
    }
}
