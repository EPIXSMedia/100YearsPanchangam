package com.panchangam100.live.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.panchangam100.live.astronomy.PanchangamResult
import com.panchangam100.live.data.datastore.AppPreferences
import com.panchangam100.live.data.model.Language
import com.panchangam100.live.ui.components.*
import com.panchangam100.live.ui.theme.*
import com.panchangam100.live.utils.LanguageManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    prefs: AppPreferences,
    onDateClick: (String) -> Unit,
    onLocationClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val lang = state.language
    val today = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ─── Header ───
        GradientHeader {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            LanguageManager.label("panchangam", lang),
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldLight.copy(alpha = 0.8f),
                            letterSpacing = 2.sp
                        )
                        Text(
                            "100 Years Panchangam",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Location button
                    IconButton(
                        onClick = onLocationClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = GoldLight)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Location name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, null, tint = GoldLight, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        state.location.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Date selector strip (±7 days)
                DateStrip(
                    selectedDate = state.selectedDate,
                    today = today,
                    lang = lang,
                    onDateSelect = viewModel::selectDate
                )
            }
        }

        // ─── Content ───
        AnimatedContent(
            targetState = state.isLoading,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
            label = "content"
        ) { loading ->
            if (loading) {
                LoadingIndicator(Modifier.fillMaxSize())
            } else if (state.error != null) {
                ErrorMessage(state.error!!, Modifier.fillMaxSize())
            } else {
                state.result?.let { result ->
                    HomeContent(
                        result = result,
                        lang = lang,
                        onDetailClick = { onDateClick(state.selectedDate.toString()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateStrip(
    selectedDate: LocalDate,
    today: LocalDate,
    lang: Language,
    onDateSelect: (LocalDate) -> Unit
) {
    val dates = remember { (-7..7).map { today.plusDays(it.toLong()) } }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(dates.size) { i ->
            val date = dates[i]
            val isSelected = date == selectedDate
            val isToday = date == today

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isSelected -> Gold
                            isToday -> Color.White.copy(alpha = 0.2f)
                            else -> Color.Transparent
                        }
                    )
                    .clickable { onDateSelect(date) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).first().toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color(0xFF3A0000) else Color.White.copy(alpha = 0.7f)
                )
                Text(
                    date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF3A0000) else Color.White
                )
                if (isToday && !isSelected) {
                    Box(
                        Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(GoldLight)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    result: PanchangamResult,
    lang: Language,
    onDetailClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ─── Sunrise/Sunset row ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SunTimeCard(
                label = LanguageManager.label("sunrise", lang),
                time = "%02d:%02d".format(result.sunrise.hour, result.sunrise.minute),
                icon = Icons.Default.WbSunny,
                modifier = Modifier.weight(1f)
            )
            SunTimeCard(
                label = LanguageManager.label("sunset", lang),
                time = "%02d:%02d".format(result.sunset.hour, result.sunset.minute),
                icon = Icons.Default.Brightness3,
                modifier = Modifier.weight(1f)
            )
        }

        // ─── Pancha Anga Card ───
        PanchangamCard {
            SectionHeader(LanguageManager.label("panchangam", lang))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(Modifier.height(8.dp))

            PanchangaElement(
                label = LanguageManager.label("vara", lang),
                value = LanguageManager.getVara(result.vara, lang),
                icon = Icons.Default.CalendarToday
            )
            PanchangaElement(
                label = LanguageManager.label("paksha", lang),
                value = LanguageManager.getPaksha(result.paksha, lang),
                icon = Icons.Default.Brightness6
            )
            PanchangaElement(
                label = LanguageManager.label("tithi", lang),
                value = LanguageManager.getTithi(result.tithiIndex, lang),
                icon = Icons.Default.Circle,
                endTime = result.tithiEnd?.let { "↓ %02d:%02d".format(it.hour, it.minute) }
            )
            PanchangaElement(
                label = LanguageManager.label("nakshatra", lang),
                value = LanguageManager.getNakshatra(result.nakshatraIndex, lang),
                icon = Icons.Default.Star,
                endTime = result.nakshatraEnd?.let { "↓ %02d:%02d".format(it.hour, it.minute) }
            )
            PanchangaElement(
                label = LanguageManager.label("yoga", lang),
                value = LanguageManager.getYoga(result.yogaIndex, lang),
                icon = Icons.Default.AutoAwesome,
                endTime = result.yogaEnd?.let { "↓ %02d:%02d".format(it.hour, it.minute) }
            )
            PanchangaElement(
                label = LanguageManager.label("karana", lang),
                value = LanguageManager.getKarana(result.karanaIndex, lang),
                icon = Icons.Default.PanoramaFishEye
            )

            Spacer(Modifier.height(8.dp))

            // Moon & Sun Rashi
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RashiChip(
                    label = LanguageManager.label("moonRashi", lang),
                    rashi = LanguageManager.getRashi(result.moonRashiIndex, lang),
                    modifier = Modifier.weight(1f)
                )
                RashiChip(
                    label = LanguageManager.label("sunRashi", lang),
                    rashi = LanguageManager.getRashi(result.sunRashiIndex, lang),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onDetailClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Full Details")
            }
        }

        // ─── Inauspicious Times ───
        PanchangamCard {
            SectionHeader(LanguageManager.label("inauspicious", lang))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeChip(
                    label = LanguageManager.label("rahu", lang),
                    time = "${result.rahuKalam.first}\n${result.rahuKalam.second}",
                    isAuspicious = false,
                    modifier = Modifier.weight(1f)
                )
                TimeChip(
                    label = LanguageManager.label("yama", lang),
                    time = "${result.yamagandam.first}\n${result.yamagandam.second}",
                    isAuspicious = false,
                    modifier = Modifier.weight(1f)
                )
                TimeChip(
                    label = LanguageManager.label("gulika", lang),
                    time = "${result.gulikaKalam.first}\n${result.gulikaKalam.second}",
                    isAuspicious = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ─── Auspicious Times ───
        PanchangamCard {
            SectionHeader(LanguageManager.label("auspicious", lang))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(Modifier.height(12.dp))
            TimeChip(
                label = LanguageManager.label("abhijit", lang),
                time = "${result.abhijitStart} – ${result.abhijitEnd}",
                isAuspicious = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SunTimeCard(
    label: String,
    time: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    PanchangamCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Gold, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(time, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun RashiChip(label: String, rashi: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(rashi, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

