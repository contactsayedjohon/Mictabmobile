package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.models.AiModelConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    val aiModels: StateFlow<List<AiModelConfig>> = repository.allModels
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addModel(model: AiModelConfig) {
        viewModelScope.launch {
            val currentList = aiModels.value
            val nextPriority = (currentList.maxOfOrNull { it.priority } ?: 0) + 1
            repository.insertModel(model.copy(priority = nextPriority))
        }
    }

    fun updateModel(model: AiModelConfig) {
        viewModelScope.launch {
            repository.updateModel(model)
        }
    }

    fun deleteModel(model: AiModelConfig) {
        viewModelScope.launch {
            repository.deleteModel(model)
        }
    }

    fun moveModelUp(model: AiModelConfig) {
        viewModelScope.launch {
            val currentList = aiModels.value
            val index = currentList.indexOfFirst { it.id == model.id }
            if (index > 0) {
                val previousModel = currentList[index - 1]
                val currentPriority = model.priority
                repository.updateModel(model.copy(priority = previousModel.priority))
                repository.updateModel(previousModel.copy(priority = currentPriority))
            }
        }
    }

    fun moveModelDown(model: AiModelConfig) {
        viewModelScope.launch {
            val currentList = aiModels.value
            val index = currentList.indexOfFirst { it.id == model.id }
            if (index >= 0 && index < currentList.size - 1) {
                val nextModel = currentList[index + 1]
                val currentPriority = model.priority
                repository.updateModel(model.copy(priority = nextModel.priority))
                repository.updateModel(nextModel.copy(priority = currentPriority))
            }
        }
    }
}

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
