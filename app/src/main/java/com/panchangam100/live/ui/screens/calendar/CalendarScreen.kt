package com.panchangam100.live.ui.screens.calendar

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.panchangam100.live.astronomy.PanchangamResult
import com.panchangam100.live.data.datastore.AppPreferences
import com.panchangam100.live.data.model.Language
import com.panchangam100.live.ui.components.GradientHeader
import com.panchangam100.live.ui.components.LoadingIndicator
import com.panchangam100.live.ui.theme.*
import com.panchangam100.live.utils.LanguageManager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    prefs: AppPreferences,
    onDateClick: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val lang = state.language
    val today = LocalDate.now()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        GradientHeader {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.goToMonth(state.month.minusMonths(1)) }) {
                        Icon(Icons.Default.ChevronLeft, null, tint = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val monthLocale = when (lang) {
                            com.panchangam100.live.data.model.Language.HINDI     -> Locale("hi")
                            com.panchangam100.live.data.model.Language.TAMIL     -> Locale("ta")
                            com.panchangam100.live.data.model.Language.MALAYALAM -> Locale("ml")
                            com.panchangam100.live.data.model.Language.KANNADA   -> Locale("kn")
                            com.panchangam100.live.data.model.Language.TELUGU    -> Locale("te")
                            else -> Locale.ENGLISH
                        }
                        Text(
                            state.month.month.getDisplayName(TextStyle.FULL, monthLocale),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            state.month.year.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldLight
                        )
                    }
                    IconButton(onClick = { viewModel.goToMonth(state.month.plusMonths(1)) }) {
                        Icon(Icons.Default.ChevronRight, null, tint = Color.White)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Weekday headers
                Row(Modifier.fillMaxWidth()) {
                    DayOfWeek.values().let {
                        val ordered = it.toMutableList()
                        // Start from Sunday
                        listOf(
                            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
                        ).forEach { dow ->
                            Text(
                                dow.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(2),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (dow == DayOfWeek.SUNDAY) InauspiciousRedLight else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        if (state.isLoading) {
            LoadingIndicator()
        } else {
            CalendarGrid(
                month = state.month,
                monthData = state.monthData,
                today = today,
                lang = lang,
                onDateClick = onDateClick
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    monthData: Map<LocalDate, PanchangamResult>,
    today: LocalDate,
    lang: Language,
    onDateClick: (String) -> Unit
) {
    val firstDay = month.atDay(1)
    // Offset so week starts Sunday (DayOfWeek.SUNDAY = 7 in Java, we want index 0)
    val startOffset = (firstDay.dayOfWeek.value % 7)
    val daysInMonth = month.lengthOfMonth()
    val totalCells = startOffset + daysInMonth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        var dayCounter = 1
        val rows = (totalCells + 6) / 7
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    if (cellIndex < startOffset || dayCounter > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(72.dp))
                    } else {
                        val date = month.atDay(dayCounter)
                        val result = monthData[date]
                        CalendarCell(
                            date = date,
                            result = result,
                            isToday = date == today,
                            lang = lang,
                            isSunday = col == 0,
                            modifier = Modifier.weight(1f),
                            onClick = { onDateClick(date.toString()) }
                        )
                        dayCounter++
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CalendarCell(
    date: LocalDate,
    result: PanchangamResult?,
    isToday: Boolean,
    lang: Language,
    isSunday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    val dateColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimary
        isSunday -> InauspiciousRed
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        tonalElevation = if (isToday) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = dateColor
            )
            if (result != null) {
                Text(
                    LanguageManager.getTithi(result.tithiIndex, lang).take(6),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    LanguageManager.getNakshatra(result.nakshatraIndex, lang).take(6),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
