package github.tom2433.lifttracker.ui.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.lift.LiftRepository
import github.tom2433.lifttracker.data.profile.Profile
import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.setmetric.SetMetric
import github.tom2433.lifttracker.data.profile.ProfileRepository
import github.tom2433.lifttracker.data.session.Session
import github.tom2433.lifttracker.data.session.SessionRepository
import github.tom2433.lifttracker.data.structures.DisplaySessionLiftSetRow
import github.tom2433.lifttracker.data.structures.LiftSearchDetail
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

// TODO: delete these debug calls before release
private const val TAG = "MainActivity"

/**
 * ViewModel for SessionsScreen
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository,
    private val liftRepository: LiftRepository
) : ViewModel() {
    private val _sessionsUiState = MutableStateFlow(SessionsUiState())
    // toast events are called via _toastEvents.emit("message")
    private val _toastEvents = MutableSharedFlow<String>()
    private var refreshJob: Job? = null
    private var setCollectionJob: Job? = null
//    private var sessionCardIdPendingExpand: Int? = null
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
        // if user is going from donutCharts visible to donut charts not visible, we need to reset
        // the session data
        viewModelScope.launch {
            if (_sessionsUiState.value.donutChartsVisible) {
                resetSessionData()
            }

            _sessionsUiState.update { currentState ->
                currentState.copy(
                    donutChartsVisible = !currentState.donutChartsVisible
                )
            }
        }
    }

    private fun convertLiftSetRowsToSetList(
        displaySessionLiftSetRows: List<DisplaySessionLiftSetRow>
    ): List<Pair<Int, List<Int>>> {
        // displaySetList: ordered list of pairs (ordered by session_set_number):
        //      first element: Lift id
        //      second element: list of LiftSet ids maintaining order
        val mutableDisplaySetList = mutableListOf<Pair<Int, List<Int>>>()
        var index = 0
        while (index < displaySessionLiftSetRows.size) {
            val newLiftId = displaySessionLiftSetRows[index].liftSet.lift_id

            // determine the last index where this lift id shows up
            var newLiftIdEndIndex = index
            while (displaySessionLiftSetRows[newLiftIdEndIndex].liftSet.lift_id == newLiftId) {
                newLiftIdEndIndex++
                if (newLiftIdEndIndex == displaySessionLiftSetRows.size) {
                    break
                }
            }
            newLiftIdEndIndex--

            // now create list of liftset ids that belong to this new lift id
            val liftSetIdsForNewLift: List<Int> = displaySessionLiftSetRows
                .slice(index..newLiftIdEndIndex)
                .map { it.liftSet.id }

            mutableDisplaySetList.add(Pair(newLiftId, liftSetIdsForNewLift))

            // index now goes to the next index after the end index of this lift id
            index = newLiftIdEndIndex + 1
        }

        return mutableDisplaySetList.toList()
    }

    private suspend fun resetSessionData() {
        var selectedCardsRemain = false

        // close all cards before anything
        _sessionsUiState.update { currentState ->
            currentState.copy(
                sessionDetailMap = currentState.sessionDetailMap.mapValues { (_, thisSessionDetail) ->
                    if (thisSessionDetail.selected) {
                        selectedCardsRemain = true
                        thisSessionDetail.copy(
                            selected = false
                        )
                    } else {
                        thisSessionDetail
                    }
                }
            )
        }

        // wait for close animation to execute before cancelling the collection job
        if (selectedCardsRemain) {
            delay(300)
        }
        setCollectionJob?.cancel()

        // reset all data
        _sessionsUiState.update { currentState ->
            currentState.copy(
                currentSessionLiftSetMap = emptyMap(),
                currentSessionLiftDetailMap = emptyMap(),
                currentSessionDisplaySetList = emptyList()
            )
        }
    }

    fun toggleCardSelected(sessionCardId: Int) {
        // ensure that the session still exists in the UI and backend
        val sessionDetail: SessionDetail =
            _sessionsUiState.value.sessionDetailMap[sessionCardId] ?: return

        // determine if the card is being opened.
        // if it is, begin the collection for its lift sets
        if (!sessionDetail.selected) {
            // load all data before opening card
            viewModelScope.launch {
                // close all cards, cancel the setCollectionJob and reset session display data
                resetSessionData()

                setCollectionJob = launch {
                    combine(
                        sessionRepository.getDisplaySessionLiftSetRowsStream(sessionDetail.sessionId),
                        liftRepository.getLiftSearchDetailsForSessionIdStream(sessionDetail.sessionId)
                    ) { displaySessionLiftSetRows, liftSearchDetails ->
                        displaySessionLiftSetRows to liftSearchDetails
                    }.collect { (displaySessionLiftSetRows, liftSearchDetails) ->
                        _sessionsUiState.update { currentState ->
                            currentState.copy(
                                currentSessionLiftSetMap = displaySessionLiftSetRows.associate { displaySessionLiftSetRow ->
                                    displaySessionLiftSetRow.liftSet.id to Triple(
                                        first = displaySessionLiftSetRow.liftSet,
                                        second = displaySessionLiftSetRow.weightMetric,
                                        third = displaySessionLiftSetRow.secondMetric
                                    )
                                },
                                currentSessionDisplaySetList = convertLiftSetRowsToSetList(
                                    displaySessionLiftSetRows = displaySessionLiftSetRows
                                ),
                                currentSessionLiftDetailMap = liftSearchDetails.associate { liftSearchDetail ->
                                    liftSearchDetail.liftObj.id to liftSearchDetail.copy(
                                        selected = currentState.currentSessionLiftDetailMap[liftSearchDetail.liftObj.id]?.selected
                                            ?: false
                                    )
                                },
                                sessionDetailMap = currentState.sessionDetailMap.mapValues { (thisSessionId, thisSessionDetail) ->
                                    if (thisSessionId == sessionCardId) {
                                        thisSessionDetail.copy(
                                            selected = true
                                        )
                                    } else {
                                        thisSessionDetail.copy(
                                            selected = false
                                        )
                                    }
                                }
                            )
                        }

                        Log.d(TAG, "Data loaded for session id $sessionCardId")
                    }
                }
            }
        } else {
            viewModelScope.launch {
                // if it's not, close card and delete all data, cancel collection job
                resetSessionData()
            }
        }
    }

    fun liftCardClicked(liftCardId: Int) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                currentSessionLiftDetailMap = currentState.currentSessionLiftDetailMap.mapValues { (thisLiftId, thisLiftDetail) ->
                    if (thisLiftId == liftCardId) {
                        thisLiftDetail.copy(
                            selected = !thisLiftDetail.selected
                        )
                    } else {
                        thisLiftDetail.copy(
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
    // currentSessionLiftSetMap: Map of LiftSet ids pointing to triples containing a lift set object
    // and both of its SetMetric objects. The first SetMetric is weight, second is reps or time
    val currentSessionLiftSetMap: Map<Int, Triple<LiftSet, SetMetric, SetMetric>> = emptyMap(),
    // currentSessionLiftDetailMap: Map of lift ids pointing to their corresponding LiftSearchDetail
    // objects
    val currentSessionLiftDetailMap: Map<Int, LiftSearchDetail> = emptyMap(),
    // displaySetList: ordered list of pairs (ordered by session_set_number):
    //      first element: Lift id
    //      second element: list of LiftSet ids maintaining order
    val currentSessionDisplaySetList: List<Pair<Int, List<Int>>> = emptyList(),
    val dateRangePickerVisible: Boolean = false,
    val deleteSessionDialogVisible: Boolean = false,
    val editSessionDialogVisible: Boolean = false,
    val sessionToDelete: Session? = null,
    val sessionToEdit: Session? = null,
    val newSessionName: String = "",
    val newSessionNote: String = "",
    val donutChartsVisible: Boolean = true,

    )
