package github.tom2433.lifttracker.ui.viewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.profile.Profile
import github.tom2433.lifttracker.data.profile.ProfileRepository
import github.tom2433.lifttracker.data.session.SessionRepository
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import github.tom2433.lifttracker.data.structures.SessionDetail
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
import github.tom2433.lifttracker.ui.screens.TimeFrameOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _sessionsUiState = MutableStateFlow(SessionsUiState())
    private var refreshJob: Job? = null
    val sessionsUiState: StateFlow<SessionsUiState> = _sessionsUiState.asStateFlow()

    init {
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

        refresh()
    }

    /**
     * This refresh exists because the muscleGroupFrequencyMap does not automatically update when
     * startDate/endDate is changed, so it must be called manually
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun refresh() {
        refreshJob?.cancel()

        refreshJob = viewModelScope.launch {
            makeAllSessionCardsInvisible()
            delay(300)

            // constant collection to fill the muscleGroupFrequencyMap for the given time period,
            // only updates automatically when the profile is changed or when the data in the database
            // that sessionRepository.getMuscleGroupFrequencyListStream() relies on updates.
            launch {
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

            // constant collection to retrieve a list of all SessionDetail objects for all sessions
            // completed in the user-specified timeframe. Each SessionDetail object's visible attribute
            // is initially set to false, so this sets them to true once they're loaded in
            launch {
                profileRepository.getActiveProfileStream().flatMapLatest { activeProfile ->
                    if (activeProfile == null) {
                        flowOf(emptyList())
                    } else {
                        sessionRepository.getSessionDetailsListStreamForSessionScreen(
                            activeProfileId = activeProfile.id,
                            startDate = _sessionsUiState.value.startDate,
                            endDate = _sessionsUiState.value.endDate,
                            fetchLimit = _sessionsUiState.value.fetchLimit
                        )
                    }
                }.collect { sessionDetails ->
                    _sessionsUiState.update { currentState ->
                        val updatedSessionDetails = sessionDetails.map { sessionDetail ->
                            sessionDetail.copy(
                                selected = currentState.sessionDetailMap[sessionDetail.sessionId]?.selected ?: false
                            )
                        }

                        currentState.copy(
                            // map of session ids pointing to SessionDetail objects
                            sessionDetailMap = updatedSessionDetails.associateBy { it.sessionId },
                            // pair list with first element as week string and second element as a
                            // list of session ids
                            weekStringPairList = updatedSessionDetails
                                .groupBy { DateTimeCalculator.getWeekStringFromIsoDate(it.sessionDateIso) }
                                .map { (weekString, sessionsInWeek) ->
                                    weekString to sessionsInWeek.map { it.sessionId }
                                }
                        )
                    }

                    delay(300)
                    makeAllSessionCardsVisible()
                }
            }
        }
    }

    fun makeAllSessionCardsInvisible() {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                sessionDetailMap = currentState.sessionDetailMap.mapValues { (_, sessionDetail) ->
                    sessionDetail.copy(
                        visible = false
                    )
                }
            )
        }
    }

    fun makeAllSessionCardsVisible() {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                sessionDetailMap = currentState.sessionDetailMap.mapValues { (_, sessionDetail) ->
                    sessionDetail.copy(
                        visible = true
                    )
                }
            )
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

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
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
    // sessionDetailMap: session ids pointing to SessionDetail objects
    val sessionDetailMap: Map<Int, SessionDetail> = emptyMap(),
    // weekStringPairList: list of pairs with first element as a formatted week string,
    // second element as a list of session ids
    val weekStringPairList: List<Pair<String, List<Int>>> = emptyList(),
    val dateRangePickerVisible: Boolean = false
)
