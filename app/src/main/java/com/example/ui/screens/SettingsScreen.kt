package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("mictab_prefs", Context.MODE_PRIVATE) }
    val scrollState = rememberScrollState()

    var selectedLanguageName by remember {
        mutableStateOf(sharedPref.getString("stt_language_name", "Auto (Detect)") ?: "Auto (Detect)")
    }
    var selectedLanguageCode by remember {
        mutableStateOf(sharedPref.getString("stt_language_code", "") ?: "")
    }

    val languages = listOf(
        Pair("Auto (Detect)", ""),
        Pair("Bengali (Bangla)", "bn"),
        Pair("Hindi", "hi"),
        Pair("English", "en"),
        Pair("Spanish", "es"),
        Pair("French", "fr"),
        Pair("German", "de"),
        Pair("Chinese", "zh"),
        Pair("Japanese", "ja"),
        Pair("Russian", "ru"),
        Pair("Arabic", "ar"),
        Pair("Portuguese", "pt"),
        Pair("Korean", "ko"),
        Pair("Italian", "it"),
        Pair("Dutch", "nl"),
        Pair("Turkish", "tr"),
        Pair("Polish", "pl"),
        Pair("Swedish", "sv"),
        Pair("Indonesian", "id"),
        Pair("Vietnamese", "vi")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Preferences", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        SettingsToggle(
            title = "Triple-Tap to Polish",
            description = "Triple tapping a text field will automatically use the default AI Polish model on the selected text.",
            prefKey = "pref_triple_tap_polish",
            defaultChecked = true,
            context = context
        )

        SettingsToggle(
            title = "Hide Floating Button when typing",
            description = "Temporarily hides the mic button when the software keyboard is active, showing it when focused typing is idle.",
            prefKey = "pref_hide_floating_typing",
            defaultChecked = false,
            context = context
        )

        SettingsToggle(
            title = "Auto-fallback",
            description = "If the primary AI provider fails or times out, seamlessly switch to the next priority provider.",
            prefKey = "pref_auto_fallback",
            defaultChecked = true,
            context = context
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Speech-to-Text Language",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        var expanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Transcription Language",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Currently: $selectedLanguageName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown Arrow"
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 280.dp)
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(language.first)
                                if (language.second.isNotEmpty()) {
                                    Text(
                                        text = language.second,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = {
                            sharedPref.edit().apply {
                                putString("stt_language_name", language.first)
                                putString("stt_language_code", language.second)
                                apply()
                            }
                            selectedLanguageName = language.first
                            selectedLanguageCode = language.second
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Branding Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MicTab.com",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Developed by Sayed Johon",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/@junoverseai"))
                            context.startActivity(intent)
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "YouTube channel",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Youtube.com/@junoverseai",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsToggle(
    title: String,
    description: String,
    prefKey: String,
    defaultChecked: Boolean,
    context: Context
) {
    val sharedPref = remember { context.getSharedPreferences("mictab_prefs", Context.MODE_PRIVATE) }
    var checked by remember { mutableStateOf(sharedPref.getBoolean(prefKey, defaultChecked)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = {
                    checked = it
                    sharedPref.edit().putBoolean(prefKey, it).apply()
                }
            )
        }
    }
}

