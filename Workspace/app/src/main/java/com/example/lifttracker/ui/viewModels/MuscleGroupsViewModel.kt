package com.example.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifttracker.data.Profile
import com.example.lifttracker.data.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel to retrieve all profiles in the Room database
 */
class MuscleGroupsViewModel(
    profileRepository: ProfileRepository
) : ViewModel() {
    val muscleGroupsUiState: StateFlow<MuscleGroupsUiState> = profileRepository.getAllProfilesStream()
        .map { MuscleGroupsUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MuscleGroupsUiState()
        )

    fun showWelcomeDialog() {
        muscleGroupsUiState.value.welcomeDialogVisible = true
    }

    fun dismissWelcomeDialog() {
        muscleGroupsUiState.value.welcomeDialogVisible = false
    }
}

/**
 * Ui State for MuscleGroupsScreen
 */
data class MuscleGroupsUiState(
    val profileList: List<Profile> = listOf(),
    var welcomeDialogVisible: Boolean = false
)