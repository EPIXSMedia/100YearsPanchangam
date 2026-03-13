package com.panchangam100.live.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panchangam100.live.astronomy.PanchangamResult
import com.panchangam100.live.data.datastore.AppPreferences
import com.panchangam100.live.data.model.AppLocation
import com.panchangam100.live.data.model.Language
import com.panchangam100.live.data.repository.PanchangamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val monthData: Map<LocalDate, PanchangamResult> = emptyMap(),
    val isLoading: Boolean = false,
    val location: AppLocation = AppPreferences.DEFAULT_LOCATION,
    val language: Language = Language.TELUGU
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: PanchangamRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(prefs.locationFlow, prefs.languageFlow) { loc, lang -> Pair(loc, lang) }
                .collect { (loc, lang) ->
                    _uiState.update { it.copy(location = loc, language = lang) }
                    loadMonth(_uiState.value.month, loc)
                }
        }
    }

    fun goToMonth(month: YearMonth) {
        _uiState.update { it.copy(month = month) }
        loadMonth(month, _uiState.value.location)
    }

    private fun loadMonth(month: YearMonth, location: AppLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val start = month.atDay(1)
                val end = month.atEndOfMonth()
                val results = repository.getPanchangamRange(start, end, location)
                val map = results.associateBy { it.date }
                _uiState.update { it.copy(isLoading = false, monthData = map) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
