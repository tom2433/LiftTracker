package github.tom2433.lifttracker.ui.viewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.profile.Profile
import github.tom2433.lifttracker.data.profile.ProfileRepository
import github.tom2433.lifttracker.data.session.Session
import github.tom2433.lifttracker.data.session.SessionRepository
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import github.tom2433.lifttracker.data.structures.SessionDetail
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
import github.tom2433.lifttracker.ui.screens.TimeFrameOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    // toast events are called via _toastEvents.emit("message")
    private val _toastEvents = MutableSharedFlow<String>()
    private var refreshJob: Job? = null
    val sessionsUiState: StateFlow<SessionsUiState> = _sessionsUiState.asStateFlow()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

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
    private fun refresh(refreshEverything: Boolean = false) {
        refreshJob?.cancel()

        refreshJob = viewModelScope.launch {
            if (refreshEverything) {
                // redo exit/entry animation on all cards
                makeAllSessionCardsInvisible()
                delay(300)
                // reset the fetch limit
                _sessionsUiState.update { currentState ->
                    currentState.copy(
                        fetchLimit = 10
                    )
                }
            }

            // constant collection to keep numSessionsInTimeFrame up to date; this informs the user
            // how many sessions are in the time frame (not how many are actually displayed)
            launch {
                profileRepository.getActiveProfileStream().flatMapLatest { activeProfile ->
                    if (activeProfile != null) {
                        sessionRepository.getNumSessionsStreamForTimeFrame(
                            activeProfileId = activeProfile.id,
                            startDate = _sessionsUiState.value.startDate,
                            endDate = _sessionsUiState.value.endDate
                        )
                    } else {
                        flowOf(0)
                    }
                }.collect { numSessions ->
                    _sessionsUiState.update { currentState ->
                        currentState.copy(
                            numSessionsInTimeFrame = numSessions
                        )
                    }
                }
            }

            // constant collection to fill the muscleGroupFrequencyMap for the given time period,
            // only updates automatically when the profile is changed or when the data in the database
            // changes. Must manually refresh when start/end dates are changed.
            launch {
                profileRepository.getActiveProfileStream().flatMapLatest { activeProfile ->
                    if (activeProfile != null) {
                        sessionRepository.getMuscleGroupFrequencyListStream(
                            activeProfileId = activeProfile.id,
                            startDate = _sessionsUiState.value.startDate,
                            endDate = _sessionsUiState.value.endDate
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
                                visible = currentState.sessionDetailMap[sessionDetail.sessionId]?.visible ?: false,
                                selected = currentState.sessionDetailMap[sessionDetail.sessionId]?.selected ?: false,
                                menuExpanded = currentState.sessionDetailMap[sessionDetail.sessionId]?.menuExpanded ?: false
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
                        visible = false,
                        menuExpanded = false
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
                        visible = true,
                        menuExpanded = false
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

        refresh(true)
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

    fun threeDotMenuClicked(sessionCardId: Int) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                sessionDetailMap = currentState.sessionDetailMap.mapValues { (id, sessionDetail) ->
                    if (id == sessionCardId) {
                        sessionDetail.copy(
                            menuExpanded = true
                        )
                    } else {
                        sessionDetail.copy(
                            menuExpanded = false
                        )
                    }
                }
            )
        }
    }

    fun dismissThreeDotMenus() {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                sessionDetailMap = currentState.sessionDetailMap.mapValues { (_, sessionDetail) ->
                    sessionDetail.copy(
                        menuExpanded = false
                    )
                }
            )
        }
    }

    fun switchSessionToInProgress(sessionCardId: Int) {
        viewModelScope.launch {
            // return if the user already has a session in progress
            if (sessionRepository.sessionIsInProgress()) {
                _toastEvents.emit("Please finish your existing session.")
                return@launch
            }

            // make the card invisible and delay
            changeSessionCardVisibility(
                sessionCardId = sessionCardId,
                newVisibility = false
            )
            delay(300)

            // otherwise, switch this session to in progress
            sessionRepository.switchSessionIdToInProgress(sessionCardId)

            // delay and make the session card visible
            delay(300)
            changeSessionCardVisibility(
                sessionCardId = sessionCardId,
                newVisibility = true
            )

            // then instruct the user to navigate to the record session screen
            _toastEvents.emit("Go to the Resume Session screen to continue your session")
        }
    }

    fun finishSession(sessionCardId: Int) {
        viewModelScope.launch {
            // make the session card invisible and delay
            changeSessionCardVisibility(
                sessionCardId = sessionCardId,
                newVisibility = false
            )
            delay(300)

            val sessionSaved: Boolean = sessionRepository.finishSession(sessionCardId)

            // delay and make the session card visible
            delay(300)
            changeSessionCardVisibility(
                sessionCardId = sessionCardId,
                newVisibility = true
            )

            // display toast to inform user whether the session was saved or not
            if (sessionSaved) {
                _toastEvents.emit("Session saved!")
            } else {
                _toastEvents.emit("No valid sets; Nothing saved.")
            }
        }
    }

    fun changeSessionCardVisibility(
        sessionCardId: Int,
        newVisibility: Boolean
    ) {
        dismissThreeDotMenus()

        _sessionsUiState.update { currentState ->
            currentState.copy(
                sessionDetailMap = currentState.sessionDetailMap.mapValues { (thisId, sessionDetail) ->
                    if (thisId == sessionCardId) {
                        sessionDetail.copy(
                            visible = newVisibility
                        )
                    } else {
                        sessionDetail
                    }
                }
            )
        }
    }

    fun showDeleteSessionDialog(sessionCardId: Int) {
        dismissThreeDotMenus()

        viewModelScope.launch {
            val sessionToDelete: Session =
                sessionRepository.getSessionById(sessionCardId) ?: return@launch

            _sessionsUiState.update { currentState ->
                currentState.copy(
                    deleteSessionDialogVisible = true,
                    sessionToDelete = sessionToDelete
                )
            }
        }
    }

    fun showEditSessionDialog(sessionCardId: Int) {
        dismissThreeDotMenus()

        viewModelScope.launch {
            val sessionToEdit: Session =
                sessionRepository.getSessionById(sessionCardId) ?: return@launch

            _sessionsUiState.update { currentState ->
                currentState.copy(
                    editSessionDialogVisible = true,
                    sessionToEdit = sessionToEdit,
                    newSessionName = sessionToEdit.session_label,
                    newSessionNote = sessionToEdit.note
                )
            }
        }
    }

    fun dismissEditSessionDialog() {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                editSessionDialogVisible = false,
                sessionToEdit = null,
                newSessionName = "",
                newSessionNote = ""
            )
        }
    }

    fun updateNewSessionName(newSessionName: String) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                newSessionName = newSessionName
            )
        }
    }

    fun updateNewSessionNote(newSessionNote: String) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                newSessionNote = newSessionNote
            )
        }
    }

    fun validateSessionEntry(): Boolean {
        return _sessionsUiState.value.newSessionName.isNotBlank()
    }

    fun updateSession() {
        if (validateSessionEntry()) {
            viewModelScope.launch {
                val sessionToUpdate: Session =
                    _sessionsUiState.value.sessionToEdit ?: return@launch

                sessionRepository.updateSession(sessionToUpdate.copy(
                    session_label = _sessionsUiState.value.newSessionName,
                    note = _sessionsUiState.value.newSessionNote
                ))

                dismissEditSessionDialog()
            }
        }
    }

    fun dismissDeleteSessionDialog() {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                deleteSessionDialogVisible = false,
                sessionToDelete = null
            )
        }
    }

    fun deleteSession() {
        val sessionToDelete: Session = _sessionsUiState.value.sessionToDelete ?: return

        viewModelScope.launch {
            // dismiss delete session dialog and make corresponding session card invisible and delay
            dismissDeleteSessionDialog()
            changeSessionCardVisibility(
                sessionCardId = sessionToDelete.id,
                newVisibility = false
            )
            delay(300)

            // delete session from database
            sessionRepository.deleteSession(sessionToDelete)
        }
    }

    fun loadMoreSessions(moreSessionsToLoad: Int) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                fetchLimit = currentState.fetchLimit + moreSessionsToLoad
            )
        }

        refresh()
    }

    fun toggleListLayout() {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                donutChartsVisible = !currentState.donutChartsVisible
            )
        }
    }

    fun toggleCardSelected(sessionCardId: Int) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                sessionDetailMap = currentState.sessionDetailMap.mapValues { (thisSessionId, thisSessionDetail) ->
                    if (thisSessionId == sessionCardId) {
                        thisSessionDetail.copy(
                            selected = !thisSessionDetail.selected
                        )
                    } else {
                        thisSessionDetail.copy(
                            selected = false
                        )
                    }
                }
            )
        }
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
    val numSessionsInTimeFrame: Int = 0,
    val timeFrameDropdownExpanded: Boolean = false,
    // list of LiftSetCountPerMuscleGroup objects to create the donut chart at the top.
    // this includes ALL data in the selected timeframe, not affected by fetchLimit
    val muscleGroupFrequencyList: List<LiftSetCountPerMuscleGroup> = emptyList(),
    // sessionDetailMap: session ids pointing to SessionDetail objects; affected by fetchLimit
    val sessionDetailMap: Map<Int, SessionDetail> = emptyMap(),
    // weekStringPairList: list of pairs with first element as a formatted week string,
    // second element as a list of session ids
    val weekStringPairList: List<Pair<String, List<Int>>> = emptyList(),
    val dateRangePickerVisible: Boolean = false,
    val deleteSessionDialogVisible: Boolean = false,
    val editSessionDialogVisible: Boolean = false,
    val sessionToDelete: Session? = null,
    val sessionToEdit: Session? = null,
    val newSessionName: String = "",
    val newSessionNote: String = "",
    val donutChartsVisible: Boolean = true
)
