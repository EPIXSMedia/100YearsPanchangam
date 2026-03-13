package com.panchangam100.live.ui.screens.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.panchangam100.live.data.datastore.AppPreferences
import com.panchangam100.live.data.model.Language
import com.panchangam100.live.utils.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(val prefs: AppPreferences) : ViewModel() {
    val language = prefs.languageFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Language.TELUGU)
    val location = prefs.locationFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences.DEFAULT_LOCATION)
    val darkMode = prefs.darkModeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setLanguage(lang: Language) { viewModelScope.launch { prefs.setLanguage(lang) } }
    fun setDarkMode(dark: Boolean?) { viewModelScope.launch { prefs.setDarkMode(dark) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: AppPreferences,
    onLocationClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val lang by viewModel.language.collectAsState()
    val location by viewModel.location.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()

    var showLangDialog by remember { mutableStateOf(false) }
    var showDarkDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                LanguageManager.label("settings", lang),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Language ──
        SettingSection(title = LanguageManager.label("language", lang)) {
            SettingRow(
                icon = Icons.Default.Language,
                title = LanguageManager.label("language", lang),
                subtitle = "${lang.nativeName} (${lang.displayName})",
                onClick = { showLangDialog = true }
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Location ──
        SettingSection(title = LanguageManager.label("location", lang)) {
            SettingRow(
                icon = Icons.Default.LocationOn,
                title = LanguageManager.label("location", lang),
                subtitle = location.displayName,
                onClick = onLocationClick
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Appearance ──
        SettingSection(title = LanguageManager.label("appearance", lang)) {
            SettingRow(
                icon = Icons.Default.DarkMode,
                title = LanguageManager.label("darkMode", lang),
                subtitle = when (darkMode) {
                    true -> LanguageManager.label("dark", lang)
                    false -> LanguageManager.label("light", lang)
                    null -> LanguageManager.label("followSystem", lang)
                },
                onClick = { showDarkDialog = true }
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── About ──
        SettingSection(title = LanguageManager.label("about", lang)) {
            SettingRow(
                icon = Icons.Default.Info,
                title = "100 Years Panchangam",
                subtitle = "Version 1.0.0 • 2020–2120",
                onClick = {}
            )
            SettingRow(
                icon = Icons.Default.Star,
                title = "Accuracy",
                subtitle = "Astronomy Engine v2 • Lahiri Ayanamsa • Udaya Rule",
                onClick = {}
            )
        }

        Spacer(Modifier.height(24.dp))
    }

    // Language picker dialog
    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = { Text(LanguageManager.label("selectLang", lang), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Language.entries.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(language)
                                    showLangDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = lang == language, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(language.nativeName, fontWeight = FontWeight.SemiBold)
                                Text(language.displayName, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLangDialog = false }) { Text(LanguageManager.label("close", lang)) }
            }
        )
    }

    // Dark mode dialog
    if (showDarkDialog) {
        AlertDialog(
            onDismissRequest = { showDarkDialog = false },
            title = { Text(LanguageManager.label("darkMode", lang), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(
                        Pair(null, LanguageManager.label("followSystem", lang)),
                        Pair(false, LanguageManager.label("light", lang)),
                        Pair(true, LanguageManager.label("dark", lang))
                    ).forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDarkMode(value)
                                    showDarkDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = darkMode == value, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDarkDialog = false }) { Text(LanguageManager.label("close", lang)) }
            }
        )
    }
}

@Composable
private fun SettingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 1.dp
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
