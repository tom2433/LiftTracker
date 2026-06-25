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
 * ViewModel for AnalyticsScreen
 */
class AnalyticsViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _analyticsUiState = MutableStateFlow(AnalyticsUiState())
    val analyticsUiState: StateFlow<AnalyticsUiState> = _analyticsUiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.getAllProfilesStream().collect { profiles ->
                _analyticsUiState.update { currentState ->
                    currentState.copy(
                        profileList = profiles
                    )
                }
            }
        }
    }
}

/**
 * Ui State for AnalyticsScreen
 */
data class AnalyticsUiState(
    val profileList: List<Profile> = listOf()
)
