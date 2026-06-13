package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.AiModelConfig
import com.example.ui.MainViewModel

@Composable
fun ProvidersScreen(
    viewModel: MainViewModel,
    onNavigateToAddProvider: () -> Unit
) {
    val aiModels by viewModel.aiModels.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddProvider) {
                Icon(Icons.Default.Add, contentDescription = "Add Provider")
            }
        }
    ) { innerPadding ->
        if (aiModels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(text = "No AI Providers configured.\nAdd one to get started.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(aiModels, key = { it.id }) { model ->
                    ProviderCard(
                        model = model,
                        onDelete = { viewModel.deleteModel(model) },
                        onToggle = { viewModel.updateModel(model.copy(isEnabled = it)) },
                        onMoveUp = { viewModel.moveModelUp(model) },
                        onMoveDown = { viewModel.moveModelDown(model) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProviderCard(
    model: AiModelConfig,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onMoveUp) { Icon(Icons.Default.KeyboardArrowUp, null) }
                Text(text = "${model.priority}", style = MaterialTheme.typography.bodySmall)
                IconButton(onClick = onMoveDown) { Icon(Icons.Default.KeyboardArrowDown, null) }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = model.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = "Type: ${model.type} | Model: ${model.modelName}", style = MaterialTheme.typography.bodyMedium)
                Text(text = model.baseUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Switch(checked = model.isEnabled, onCheckedChange = onToggle)
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
