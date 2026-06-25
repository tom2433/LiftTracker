package com.example.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifttracker.data.Profile
import com.example.lifttracker.data.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel to retrieve all profiles in the Room database
 */
class MuscleGroupsViewModel(
    private val profileRepository: ProfileRepository
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

    fun isValidProfileName(): Boolean {
        return muscleGroupsUiState.value.newProfileName.isNotBlank()
    }

    // making this a suspend function so that the database updates with the new profile before the
    // navigation drawer is shown.
    suspend fun createProfile() {
        profileRepository.insertProfile(
            Profile(
                name = muscleGroupsUiState.value.newProfileName,
                active = true,
                note = muscleGroupsUiState.value.newProfileNote
            )
        )
    }
}

/**
 * Ui State for MuscleGroupsScreen
 */
data class MuscleGroupsUiState(
    val profileList: List<Profile> = listOf(),
    var welcomeDialogVisible: Boolean = false,
    var newProfileName: String = "",
    var newProfileNote: String = ""
)