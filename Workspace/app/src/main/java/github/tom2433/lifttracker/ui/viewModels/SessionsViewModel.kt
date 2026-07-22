package github.tom2433.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.profile.Profile
import github.tom2433.lifttracker.data.profile.ProfileRepository
import github.tom2433.lifttracker.data.session.SessionRepository
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
import github.tom2433.lifttracker.ui.screens.TimeFrameOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for SessionsScreen
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _sessionsUiState = MutableStateFlow(SessionsUiState())
    val sessionsUiState: StateFlow<SessionsUiState> = _sessionsUiState.asStateFlow()

    init {
        refresh()
    }

    private fun refresh() {
        // constant collection to keep active profile up to date
        viewModelScope.launch {
            profileRepository.getActiveProfileStream().collect { activeProfile ->
                _sessionsUiState.update { currentState ->
                    currentState.copy(
                        activeProfile = activeProfile
                    )
                }
            }
        }

        // constant collection to fill the muscleGroupFrequencyMap for the given time period
        viewModelScope.launch {
            profileRepository.getActiveProfileStream().flatMapLatest { activeProfile ->
                if (activeProfile != null) {
                    sessionRepository.getMuscleGroupFrequencyListStream(
                        activeProfileId = activeProfile.id,
                        startDate = _sessionsUiState.value.startDate,
                        endDate = _sessionsUiState.value.endDate,
                        fetchLimit = _sessionsUiState.value.fetchLimit
                    )
                } else {
                    flowOf(emptyList())
                }
            }.collect { muscleGroupFrequencies ->
                _sessionsUiState.update { currentState ->
                    currentState.copy(
                        muscleGroupFrequencyList = muscleGroupFrequencies
                    )
                }
            }
        }
    }

    fun toggleTimeFrameDropdown() {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                timeFrameDropdownExpanded = !currentState.timeFrameDropdownExpanded
            )
        }
    }

    fun dismissTimeFrameDropdown() {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                timeFrameDropdownExpanded = false
            )
        }
    }

    private fun updateStartAndEndDate(
        startDate: String?,
        endDate: String?
    ) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                startDate = startDate,
                endDate = endDate
            )
        }

        refresh()
    }

    fun timeFrameMenuOptionClicked(timeFrameLabel: String) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                timeFrameLabel = timeFrameLabel
            )
        }
        dismissTimeFrameDropdown()

        when (timeFrameLabel) {
            TimeFrameOption.ALL_TIME -> {
                // update start and end date to null
                updateStartAndEndDate(null, null)
            }
            TimeFrameOption.PAST_WEEK -> {
                // update start date to one week ago
                updateStartAndEndDate(
                    startDate = DateTimeCalculator.calculateStartDate(
                        today = DateTimeCalculator.getCurrentIsoDate(),
                        daysBeforeToday = 6L
                    ),
                    endDate = null
                )
            }
            TimeFrameOption.PAST_TWO_WEEKS -> {
                // update start date to two weeks ago
                updateStartAndEndDate(
                    startDate = DateTimeCalculator.calculateStartDate(
                        today = DateTimeCalculator.getCurrentIsoDate(),
                        daysBeforeToday = 13L
                    ),
                    endDate = null
                )
            }
            TimeFrameOption.PAST_MONTH -> {
                updateStartAndEndDate(
                    startDate = DateTimeCalculator.calculateStartDate(
                        today = DateTimeCalculator.getCurrentIsoDate(),
                        daysBeforeToday = 27L
                    ),
                    endDate = null
                )
            }
            TimeFrameOption.CHOOSE_DATE -> {
                showDateRangePicker()
            }
        }
    }

    fun dismissDateRangePicker() {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                dateRangePickerVisible = false
            )
        }
    }

    fun showDateRangePicker() {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                dateRangePickerVisible = true
            )
        }
    }

    fun customDatePicked(
        startDateMillis: Long?,
        endDateMillis: Long?
    ) {
        val startDate: String? = if (startDateMillis == null) {
            null
        } else {
            DateTimeCalculator.createIsoDateFormatter().format(Date(startDateMillis))
        }
        val endDate: String? = if (endDateMillis == null) {
            null
        } else {
            DateTimeCalculator.createIsoDateFormatter().format(Date(endDateMillis))
        }

        updateStartAndEndDate(startDate, endDate)
    }
}

/**
 * Ui State for SessionsScreen
 */
data class SessionsUiState(
    val activeProfile: Profile? = null,
    val startDate: String? = null,
    val timeFrameLabel: String = TimeFrameOption.ALL_TIME,
    val endDate: String? = null,
    val fetchLimit: Int = 10,
    val timeFrameDropdownExpanded: Boolean = false,
    val muscleGroupFrequencyList: List<LiftSetCountPerMuscleGroup> = emptyList(),
    val dateRangePickerVisible: Boolean = false
)
