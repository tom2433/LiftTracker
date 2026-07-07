package github.tom2433.lifttracker.ui.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.Profile
import github.tom2433.lifttracker.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for RecordSessionScreen
 */
class RecordSessionViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _recordSessionUiState = MutableStateFlow(RecordSessionUiState())
    val recordSessionUiState: StateFlow<RecordSessionUiState> = _recordSessionUiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.getAllProfilesStream().collect { profiles ->
                _recordSessionUiState.update { currentState ->
                    currentState.copy(
                        profileList = profiles
                    )
                }
            }
        }
    }
}

/**
 * Ui State for RecordSessionScreen
 */
data class RecordSessionUiState(
    val profileList: List<Profile> = listOf()
)
