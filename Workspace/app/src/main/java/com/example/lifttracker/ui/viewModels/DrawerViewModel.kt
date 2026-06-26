package com.example.lifttracker.ui.viewModels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifttracker.R
import com.example.lifttracker.data.Profile
import com.example.lifttracker.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for LiftTrackerDrawer
 */
class DrawerViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _drawerUiState = MutableStateFlow(DrawerUiState())
    val drawerUiState: StateFlow<DrawerUiState> = _drawerUiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.getAllProfilesStream().collect { profiles ->
                _drawerUiState.update { currentState ->
                    currentState.copy(
                        profileList = profiles
                    )
                }
            }
        }
    }

    fun initializeDrawerOffset() {
        _drawerUiState.update { currentState ->
            currentState.copy(
                hasInitializedDrawerOffset = true
            )
        }
    }

    fun toggleDrawer() {
        _drawerUiState.update { currentState ->
            currentState.copy(
                isDrawerOpen = !currentState.isDrawerOpen,
                switchProfileSelected = false
            )
        }
    }

    fun closeDrawer() {
        _drawerUiState.update { currentState ->
            currentState.copy(
                isDrawerOpen = false,
                switchProfileSelected = false
            )
        }
    }

    fun updateDrawerWidthPx(newVal: Float) {
        _drawerUiState.update { currentState ->
            currentState.copy(
                drawerWidthPx = newVal
            )
        }
    }

    fun updateIsDrawerOpen(newVal: Boolean) {
        _drawerUiState.update { currentState ->
            currentState.copy(
                isDrawerOpen = newVal,
                switchProfileSelected = false
            )
        }
    }

    fun toggleSwitchProfileSelected() {
        _drawerUiState.update { currentState ->
            currentState.copy(
                switchProfileSelected = !currentState.switchProfileSelected
            )
        }
    }

    fun isProfileValid(): Boolean {
        return _drawerUiState.value.newProfileName.isNotBlank()
    }

    fun updateNewProfileName(newName: String) {
        _drawerUiState.update { currentState ->
            currentState.copy(
                newProfileName = newName
            )
        }
    }

    fun updateNewProfileNote(newNote: String) {
        _drawerUiState.update { currentState ->
            currentState.copy(
                newProfileNote = newNote
            )
        }
    }

    fun dismissProfileEntryDialog() {
        _drawerUiState.update { currentState ->
            currentState.copy(
                profileEntryDialogVisible = false,
                newProfileName = "",
                newProfileNote = "",
                userIsAddingProfile = true
            )
        }
    }

    fun getActiveProfile(): Profile {
        for (profile in _drawerUiState.value.profileList) {
            if (profile.active) {
                return profile
            }
        }
        return _drawerUiState.value.profileList[0]
    }

    fun submitProfileEntryDialog() {
        // validate user input
        if (isProfileValid()) {
            // decide whether to add or update
            if (_drawerUiState.value.userIsAddingProfile) {
                // add profile
                viewModelScope.launch {
                    // retrieve currently active profile and deactivate it
                    val currentlyActiveProfile = getActiveProfile()
                    profileRepository.updateProfile(
                        currentlyActiveProfile.copy(
                            active = false
                        )
                    )

                    // insert new profile and make it active
                    profileRepository.insertProfile(
                        Profile(
                            name = _drawerUiState.value.newProfileName,
                            active = true,
                            note = _drawerUiState.value.newProfileNote
                        )
                    )

                    // update uiState
                    _drawerUiState.update { currentState ->
                        currentState.copy(
                            profileEntryDialogVisible = false,
                            newProfileName = "",
                            newProfileNote = ""
                        )
                    }
                }
            } else {
                // update profile (user can only update active profile)
                viewModelScope.launch {
                    profileRepository.updateProfile(
                        getActiveProfile().copy(
                            name = _drawerUiState.value.newProfileName,
                            note = _drawerUiState.value.newProfileNote
                        )
                    )

                    // update uiState
                    _drawerUiState.update { currentState ->
                        currentState.copy(
                            profileEntryDialogVisible = false,
                            newProfileName = "",
                            newProfileNote = "",
                            userIsAddingProfile = true
                        )
                    }
                }
            }
        }
    }

    fun addProfileBtnClicked() {
        _drawerUiState.update { currentState ->
            currentState.copy(
                profileEntryDialogVisible = true,
                userIsAddingProfile = true,
                newProfileName = "",
                newProfileNote = ""
            )
        }
    }

    fun editProfileBtnClicked(profileToEdit: Profile) {
        _drawerUiState.update { currentState ->
            currentState.copy(
                profileEntryDialogVisible = true,
                userIsAddingProfile = false,
                newProfileName = profileToEdit.name,
                newProfileNote = profileToEdit.note
            )
        }
    }

    fun changeActiveProfile(newActiveProfile: Profile) {
        val oldActiveProfile = getActiveProfile()

        viewModelScope.launch {
            // deactivate old profile
            profileRepository.updateProfile(
                oldActiveProfile.copy(
                    active = false
                )
            )

            // activate new profile
            profileRepository.updateProfile(
                newActiveProfile.copy(
                    active = true
                )
            )
        }
    }

    fun dismissDeleteProfileDialog() {
        _drawerUiState.update { currentState ->
            currentState.copy(
                profileToDelete = null,
                deleteProfileDialogVisible = false
            )
        }
    }

    fun setProfileToDelete(deletedProfile: Profile) {
        _drawerUiState.update { currentState ->
            currentState.copy(
                profileToDelete = deletedProfile,
                deleteProfileDialogVisible = true
            )
        }
    }

    fun deleteProfile() {
        viewModelScope.launch {
            if (_drawerUiState.value.profileToDelete != null) {
                profileRepository.deleteProfile(
                    profile = _drawerUiState.value.profileToDelete ?: Profile(
                        id = -1,
                        name = "",
                        active = false,
                        note = ""
                    )
                )
            }

            _drawerUiState.update { currentState ->
                currentState.copy(
                    deleteProfileDialogVisible = false,
                    profileToDelete = null
                )
            }
        }
    }

    fun checkScreenForFab(@StringRes titleRes: Int) {
        if (titleRes == R.string.muscle_groups_title) {
            _drawerUiState.update { currentState ->
                currentState.copy(
                    showFab = true
                )
            }
        } else {
            _drawerUiState.update { currentState ->
                currentState.copy(
                    showFab = false
                )
            }
        }
    }
}

/**
 * Ui State for SettingsScreen
 */
data class DrawerUiState(
    val profileList: List<Profile> = listOf(),
    val isDrawerOpen: Boolean = false,
    val drawerWidthPx: Float = 0f,
    val hasInitializedDrawerOffset: Boolean = false,
    val switchProfileSelected: Boolean = false,
    val profileEntryDialogVisible: Boolean = false,
    val userIsAddingProfile: Boolean = true,
    val newProfileName: String = "",
    val newProfileNote: String = "",
    val deleteProfileDialogVisible: Boolean = false,
    val profileToDelete: Profile? = null,
    val showFab: Boolean = false
)