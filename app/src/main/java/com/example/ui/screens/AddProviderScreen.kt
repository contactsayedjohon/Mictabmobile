package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.models.AiModelConfig
import com.example.ui.MainViewModel

enum class ProviderPreset {
    Groq, OpenRouter, Custom
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProviderScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var preset by remember { mutableStateOf(ProviderPreset.Groq) }
    var presetExpanded by remember { mutableStateOf(false) }

    var type by remember { mutableStateOf("STT") } // STT or LLM
    
    var name by remember { mutableStateOf("Groq Whisper") }
    var baseUrl by remember { mutableStateOf("https://api.groq.com/openai/v1/") }
    var apiKey by remember { mutableStateOf("") }
    
    var modelName by remember { mutableStateOf("whisper-large-v3") }
    var modelExpanded by remember { mutableStateOf(false) }
    var customModelInput by remember { mutableStateOf("") }

    val groqSttModels = listOf("whisper-large-v3", "whisper-large-v3-turbo")
    val groqLlmModels = listOf("llama3-8b-8192", "llama3-70b-8192", "mixtral-8x7b-32768", "gemma2-9b-it")
    val openRouterLlmModels = listOf(
        "meta-llama/llama-3-8b-instruct:free",
        "mistralai/mistral-7b-instruct:free",
        "google/gemma-2-9b-it:free",
        "microsoft/phi-3-mini-128k-instruct:free"
    )

    // Update fields when preset changes
    LaunchedEffect(preset, type) {
        when (preset) {
            ProviderPreset.Groq -> {
                baseUrl = "https://api.groq.com/openai/v1/"
                modelName = if (type == "STT") groqSttModels.first() else groqLlmModels.first()
                name = if (type == "STT") "Groq Whisper" else "Groq LLM"
            }
            ProviderPreset.OpenRouter -> {
                type = "LLM" // OpenRouter is primarily LLM
                baseUrl = "https://openrouter.ai/api/v1/"
                modelName = openRouterLlmModels.first()
                name = "OpenRouter AI"
            }
            ProviderPreset.Custom -> {
                name = "Custom Provider"
                if (baseUrl.contains("groq") || baseUrl.contains("openrouter")) {
                    baseUrl = ""
                }
                if (customModelInput.isBlank() && modelName.isNotBlank() && !groqSttModels.contains(modelName) && !groqLlmModels.contains(modelName) && !openRouterLlmModels.contains(modelName)) {
                     customModelInput = modelName
                } else {
                    modelName = customModelInput
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Preset Dropdown
        ExposedDropdownMenuBox(
            expanded = presetExpanded,
            onExpandedChange = { presetExpanded = it }
        ) {
            OutlinedTextField(
                value = preset.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Provider Setup") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = presetExpanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = presetExpanded,
                onDismissRequest = { presetExpanded = false }
            ) {
                ProviderPreset.values().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name) },
                        onClick = {
                            preset = option
                            presetExpanded = false
                        }
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = type == "STT",
                onClick = { if (preset != ProviderPreset.OpenRouter) type = "STT" },
                label = { Text("Dictation (STT)") },
                enabled = preset != ProviderPreset.OpenRouter
            )
            FilterChip(
                selected = type == "LLM",
                onClick = { type = "LLM" },
                label = { Text("AI Polish (LLM)") }
            )
        }
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth()
        )

        // Model Selection
        if (preset == ProviderPreset.Custom) {
             OutlinedTextField(
                value = customModelInput,
                onValueChange = { 
                    customModelInput = it 
                    modelName = it
                },
                label = { Text("Model ID (e.g. whisper-1)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Base URL") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            val availableModels = when (preset) {
                ProviderPreset.Groq -> if (type == "STT") groqSttModels else groqLlmModels
                ProviderPreset.OpenRouter -> openRouterLlmModels
                else -> emptyList()
            }

            ExposedDropdownMenuBox(
                expanded = modelExpanded,
                onExpandedChange = { modelExpanded = it }
            ) {
                OutlinedTextField(
                    value = modelName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Model") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = modelExpanded,
                    onDismissRequest = { modelExpanded = false }
                ) {
                    availableModels.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                modelName = option
                                modelExpanded = false
                            }
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                readOnly = true,
                label = { Text("Base URL (Auto-filled)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && apiKey.isNotBlank() && baseUrl.isNotBlank() && modelName.isNotBlank()) {
                    viewModel.addModel(
                        AiModelConfig(
                            name = name,
                            type = type,
                            baseUrl = baseUrl,
                            apiKey = apiKey,
                            modelName = modelName,
                            priority = 10,
                            isEnabled = true
                        )
                    )
                    onNavigateBack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Provider")
        }
    }
}
