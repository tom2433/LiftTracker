package github.tom2433.lifttracker.ui.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import github.tom2433.lifttracker.data.LiftDay
import github.tom2433.lifttracker.data.LiftDayRepository
import github.tom2433.lifttracker.data.OfflineLiftDayRepository
import github.tom2433.lifttracker.data.Profile
import github.tom2433.lifttracker.data.ProfileRepository
import github.tom2433.lifttracker.data.utils.DateCalculator
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
        // constant collection: retrieve active lift day for currently active profile
        viewModelScope.launch {
            liftDayRepository.getActiveLiftDayForActiveProfileStream().collect { thisLiftDay ->
                _recordSessionUiState.update { currentState ->
                    currentState.copy(
                        activeLiftDay = thisLiftDay
                    )
                }
            }
        }

        // constant collection: retrieve currently active profile
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

    fun beginSession() {
        // need to create a lift day object that is active. the flows defined in init {} should
        // automatically update the activeLiftDay for RecordSessionUiState
        viewModelScope.launch {
            val activeProfileId: Int = _recordSessionUiState.value.activeProfile?.id ?: return@launch
            val dayNum: Int = liftDayRepository.getNextDayNumber(activeProfileId)

            liftDayRepository.insertLiftDay(
                liftDay = LiftDay(
                    in_progress = true,
                    profile_id = activeProfileId,
                    day_number = dayNum,
                    day_label = "Day $dayNum",
                    date = DateCalculator.getCurrentIsoDate(),
                    note = ""
                )
            )
        }

        // start a collection with a running total of all lifts completed for the day
        viewModelScope.launch {
            liftDayRepository.getNumOfLiftsForDay(
                id = _recordSessionUiState.value.activeLiftDay?.id ?: return@launch
            ).collect { thisNumOfLifts ->
                _recordSessionUiState.update { currentState ->
                    currentState.copy(
                        totalNumOfLifts = thisNumOfLifts
                    )
                }
            }
        }
    }

    fun endSession() {
        val currentDay: LiftDay = _recordSessionUiState.value.activeLiftDay ?: return

        // if this lift day does not have any set data, delete it
        if (_recordSessionUiState.value.totalNumOfLifts == 0) {
            // delete the lift day. activeLiftDay should update automatically
            viewModelScope.launch {
                liftDayRepository.deleteLiftDay(currentDay)
            }
        } else {
            // otherwise, save the lift day by setting in progress = false.
            // activeLiftDay should update automatically
            viewModelScope.launch {
                liftDayRepository.updateLiftDay(
                    liftDay = currentDay.copy(
                        in_progress = false
                    )
                )
            }
        }
    }

    fun finishSession() {
        if (_recordSessionUiState.value.totalNumOfLifts == 0) {
            endSession()
        }
    }
}

/**
 * Ui State for RecordSessionScreen
 */
data class RecordSessionUiState(
    val activeProfile: Profile? = null,
    val activeLiftDay: LiftDay? = null,
    val totalNumOfLifts: Int = 0
)
