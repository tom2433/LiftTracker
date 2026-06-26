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
 * ViewModel to retrieve all profiles in the Room database
 */
class MuscleGroupsViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _muscleGroupsUiState = MutableStateFlow(MuscleGroupsUiState())
    val muscleGroupsUiState: StateFlow<MuscleGroupsUiState> = _muscleGroupsUiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.getAllProfilesStream().collect { profiles ->
                _muscleGroupsUiState.update { currentState ->
                    currentState.copy(
                        profileList = profiles,
                        welcomeDialogVisible = profiles.isEmpty()
                    )
                }
            }
        }
    }

    fun isValidProfileName(): Boolean {
        return _muscleGroupsUiState.value.newProfileName.isNotBlank()
    }

    fun updateNewProfileName(newName: String) {
        _muscleGroupsUiState.update { currentState ->
            currentState.copy(
                newProfileName = newName
            )
        }
    }

    fun updateNewProfileNote(newNote: String) {
        _muscleGroupsUiState.update { currentState ->
            currentState.copy(
                newProfileNote = newNote
            )
        }
    }

    // making this a suspend function so that the database updates with the new profile before the
    // navigation drawer is shown.
    fun createProfile() {
        if (isValidProfileName()) {
            viewModelScope.launch {
                // insert new profile into database
                profileRepository.insertProfile(
                    Profile(
                        name = _muscleGroupsUiState.value.newProfileName,
                        active = true,
                        note = _muscleGroupsUiState.value.newProfileNote
                    )
                )

                // update state and close welcome dialog
                _muscleGroupsUiState.update { currentState ->
                    currentState.copy(
                        welcomeDialogVisible = false,
                        newProfileName = "",
                        newProfileNote = ""
                    )
                }
            }
        }
    }

    fun getActiveProfile(): Profile? {
        for (profile in _muscleGroupsUiState.value.profileList) {
            if (profile.active) {
                return profile
            }
        }
        return null
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