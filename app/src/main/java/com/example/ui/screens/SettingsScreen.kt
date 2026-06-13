package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        SettingsToggle(
            title = "Triple-Tap to Polish",
            description = "Triple tapping a text field will automatically use the default AI Polish model on the selected text.",
            defaultChecked = true
        )

        SettingsToggle(
            title = "Hide Floating Button when typing",
            description = "Temporarily hides the mic button when the software keyboard is active.",
            defaultChecked = false
        )

        SettingsToggle(
            title = "Auto-fallback",
            description = "If the primary AI provider fails or times out, seamlessly switch to the next priority provider.",
            defaultChecked = true
        )

        Spacer(modifier = Modifier.weight(1f))
        
        OutlinedButton(
            onClick = { /* TODO: Implement Backup */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Backup Configurations")
        }

        OutlinedButton(
            onClick = { /* TODO: Implement Restore */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restore Configurations")
        }
    }
}

@Composable
fun SettingsToggle(title: String, description: String, defaultChecked: Boolean) {
    var checked by remember { mutableStateOf(defaultChecked) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = checked, onCheckedChange = { checked = it })
        }
    }
}
