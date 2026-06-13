package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "ai_models")
data class AiModelConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String, // "STT" or "LLM"
    val baseUrl: String,
    val apiKey: String,
    val modelName: String,
    val priority: Int = 0,
    val isEnabled: Boolean = true
)
