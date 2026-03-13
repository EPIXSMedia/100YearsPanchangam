package com.panchangam100.live.ui.screens.home

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
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val result: PanchangamResult? = null,
    val error: String? = null,
    val location: AppLocation = AppPreferences.DEFAULT_LOCATION,
    val language: Language = Language.TELUGU,
    val selectedDate: LocalDate = LocalDate.now()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PanchangamRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(prefs.locationFlow, prefs.languageFlow) { loc, lang -> Pair(loc, lang) }
                .collect { (loc, lang) ->
                    _uiState.update { it.copy(location = loc, language = lang) }
                    loadPanchangam(_uiState.value.selectedDate, loc)
                }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadPanchangam(date, _uiState.value.location)
    }

    private fun loadPanchangam(date: LocalDate, location: AppLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = repository.getPanchangam(date, location)
                _uiState.update { it.copy(isLoading = false, result = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error") }
            }
        }
    }
}
