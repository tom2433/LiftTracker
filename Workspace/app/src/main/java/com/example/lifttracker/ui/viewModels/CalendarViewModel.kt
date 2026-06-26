package com.example.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifttracker.data.Profile
import com.example.lifttracker.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for CalendarScreen
 */
class CalendarViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _calendarUiState = MutableStateFlow(CalendarUiState())
    val calendarUiState: StateFlow<CalendarUiState> = _calendarUiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.getAllProfilesStream().collect { profiles ->
                _calendarUiState.update { currentState ->
                    currentState.copy(
                        profileList = profiles
                    )
                }
            }
        }
    }
}

/**
 * Ui State for CalendarScreen
 */
data class CalendarUiState(
    val profileList: List<Profile> = listOf()
)
