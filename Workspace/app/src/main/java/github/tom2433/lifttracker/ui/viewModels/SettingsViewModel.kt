package github.tom2433.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.profile.Profile
import github.tom2433.lifttracker.data.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for SettingsScreen
 */
class SettingsViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _settingsUiState = MutableStateFlow(SettingsUiState())
    val settingsUiState: StateFlow<SettingsUiState> = _settingsUiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.getAllProfilesStream().collect { profiles ->
                _settingsUiState.update { currentState ->
                    currentState.copy(
                        profileList = profiles
                    )
                }
            }
        }
    }
}

/**
 * Ui State for SettingsScreen
 */
data class SettingsUiState(
    val profileList: List<Profile> = listOf()
)
