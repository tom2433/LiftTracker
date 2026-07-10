package github.tom2433.lifttracker.ui.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.LiftDay
import github.tom2433.lifttracker.data.LiftDayRepository
import github.tom2433.lifttracker.data.OfflineLiftDayRepository
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
    private val profileRepository: ProfileRepository,
    private val liftDayRepository: LiftDayRepository
) : ViewModel() {
    private val _recordSessionUiState = MutableStateFlow(RecordSessionUiState())
    val recordSessionUiState: StateFlow<RecordSessionUiState> = _recordSessionUiState.asStateFlow()

    init {
        viewModelScope.launch {
            liftDayRepository.getActiveLiftDayForActiveProfileStream().collect { thisLiftDay ->
                _recordSessionUiState.update { currentState ->
                    currentState.copy(
                        activeLiftDay = thisLiftDay
                    )
                }
            }
        }

        viewModelScope.launch {
            profileRepository.getActiveProfileStream().collect { thisProfile ->
                _recordSessionUiState.update { currentState ->
                    currentState.copy(
                        activeProfile = thisProfile
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
    val activeProfile: Profile? = null,
    val activeLiftDay: LiftDay? = null,
)
