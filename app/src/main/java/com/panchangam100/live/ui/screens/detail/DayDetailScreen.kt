package com.panchangam100.live.ui.screens.detail

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panchangam100.live.astronomy.PanchangamResult
import com.panchangam100.live.data.datastore.AppPreferences
import com.panchangam100.live.data.model.Language
import com.panchangam100.live.data.repository.PanchangamRepository
import com.panchangam100.live.ui.components.*
import com.panchangam100.live.ui.theme.*
import com.panchangam100.live.utils.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// ─── ViewModel ───
data class DetailUiState(
    val result: PanchangamResult? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val language: Language = Language.TELUGU
)

@HiltViewModel
class DayDetailViewModel @Inject constructor(
    private val repository: PanchangamRepository,
    private val prefs: AppPreferences
) : ViewModel() {
    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    fun load(dateStr: String) {
        viewModelScope.launch {
            combine(prefs.languageFlow, prefs.locationFlow) { lang, loc -> Pair(lang, loc) }
                .collect { (lang, loc) ->
                    _state.update { it.copy(language = lang, isLoading = true) }
                    try {
                        val date = LocalDate.parse(dateStr)
                        val result = repository.getPanchangam(date, loc)
                        _state.update { it.copy(result = result, isLoading = false) }
                    } catch (e: Exception) {
                        _state.update { it.copy(isLoading = false, error = e.message) }
                    }
                }
        }
    }
}

// ─── Screen ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    dateStr: String,
    prefs: AppPreferences,
    onBack: () -> Unit,
    viewModel: DayDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lang = state.language

    LaunchedEffect(dateStr) { viewModel.load(dateStr) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            dateStr,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        state.result?.let {
                            Text(
                                LanguageManager.getVara(it.vara, lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator(Modifier.padding(padding))
            state.error != null -> ErrorMessage(state.error!!, Modifier.padding(padding))
            state.result != null -> DetailContent(
                result = state.result!!,
                lang = lang,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun DetailContent(result: PanchangamResult, lang: Language, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Sunrise / Sunset ──
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard(
                title = LanguageManager.label("sunrise", lang),
                value = "%02d:%02d".format(result.sunrise.hour, result.sunrise.minute),
                icon = Icons.Default.WbSunny,
                iconTint = Color(0xFFFFB300),
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                title = LanguageManager.label("sunset", lang),
                value = "%02d:%02d".format(result.sunset.hour, result.sunset.minute),
                icon = Icons.Default.Brightness3,
                iconTint = Color(0xFF7986CB),
                modifier = Modifier.weight(1f)
            )
        }

        // ── Pancha Anga ──
        PanchangamCard {
            SectionHeader(LanguageManager.label("panchangam", lang))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(4.dp))

            DetailRow(LanguageManager.label("vara", lang), LanguageManager.getVara(result.vara, lang))
            DetailRow(LanguageManager.label("paksha", lang), LanguageManager.getPaksha(result.paksha, lang))

            DetailRowWithEnd(
                label = LanguageManager.label("tithi", lang),
                value = LanguageManager.getTithi(result.tithiIndex, lang),
                end = result.tithiEnd?.let { "%02d:%02d".format(it.hour, it.minute) }
            )
            DetailRowWithEnd(
                label = LanguageManager.label("nakshatra", lang),
                value = LanguageManager.getNakshatra(result.nakshatraIndex, lang),
                end = result.nakshatraEnd?.let { "%02d:%02d".format(it.hour, it.minute) }
            )
            DetailRowWithEnd(
                label = LanguageManager.label("yoga", lang),
                value = LanguageManager.getYoga(result.yogaIndex, lang),
                end = result.yogaEnd?.let { "%02d:%02d".format(it.hour, it.minute) }
            )
            DetailRow(LanguageManager.label("karana", lang), LanguageManager.getKarana(result.karanaIndex, lang))
        }

        // ── Rashi ──
        PanchangamCard {
            SectionHeader("Rashi / Zodiac")
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(4.dp))
            DetailRow(LanguageManager.label("moonRashi", lang), LanguageManager.getRashi(result.moonRashiIndex, lang))
            DetailRow(LanguageManager.label("sunRashi", lang), LanguageManager.getRashi(result.sunRashiIndex, lang))
        }

        // ── Inauspicious Times ──
        PanchangamCard {
            SectionHeader(LanguageManager.label("inauspicious", lang))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeChip(
                    label = LanguageManager.label("rahu", lang),
                    time = "${result.rahuKalam.first} – ${result.rahuKalam.second}",
                    isAuspicious = false,
                    modifier = Modifier.weight(1f)
                )
                TimeChip(
                    label = LanguageManager.label("yama", lang),
                    time = "${result.yamagandam.first} – ${result.yamagandam.second}",
                    isAuspicious = false,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            TimeChip(
                label = LanguageManager.label("gulika", lang),
                time = "${result.gulikaKalam.first} – ${result.gulikaKalam.second}",
                isAuspicious = false,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ── Auspicious Times ──
        PanchangamCard {
            SectionHeader(LanguageManager.label("auspicious", lang))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))
            TimeChip(
                label = LanguageManager.label("abhijit", lang),
                time = "${result.abhijitStart} – ${result.abhijitEnd}",
                isAuspicious = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DetailRowWithEnd(label: String, value: String, end: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            if (end != null) {
                Text(
                    "ends $end",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color, modifier: Modifier = Modifier) {
    PanchangamCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun Divider(color: Color) = HorizontalDivider(color = color)
