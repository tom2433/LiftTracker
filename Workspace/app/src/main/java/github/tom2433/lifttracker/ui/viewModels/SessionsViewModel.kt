package github.tom2433.lifttracker.ui.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.lift.LiftRepository
import github.tom2433.lifttracker.data.profile.Profile
import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.liftset.LiftSetRepository
import github.tom2433.lifttracker.data.setmetric.SetMetric
import github.tom2433.lifttracker.data.profile.ProfileRepository
import github.tom2433.lifttracker.data.session.Session
import github.tom2433.lifttracker.data.session.SessionRepository
import github.tom2433.lifttracker.data.setmetric.SetMetricRepository
import github.tom2433.lifttracker.data.structures.DisplaySessionLiftSetRow
import github.tom2433.lifttracker.data.structures.LiftSearchDetail
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import github.tom2433.lifttracker.data.structures.SessionDetail
import github.tom2433.lifttracker.data.structures.SetCardData
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

// TODO: delete these debug calls before pull request
private const val TAG = "MainActivity"

/**
 * ViewModel for SessionsScreen
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository,
    private val liftRepository: LiftRepository,
    private val liftSetRepository: LiftSetRepository,
    private val setMetricRepository: SetMetricRepository
) : ViewModel() {
    private val _sessionsUiState = MutableStateFlow(SessionsUiState())
    // toast events are called via _toastEvents.emit("message")
    private val _toastEvents = MutableSharedFlow<String>()
    private var refreshJob: Job? = null
    private var setCollectionJob: Job? = null
    private var filterCollectionJob: Job? = null
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

        val startDate: String? = _sessionsUiState.value.startDate
        val endDate: String? = _sessionsUiState.value.endDate

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

            // constant collection to keep numSessionsInFilteredTimeFrame up to date; this informs
            // the user how many sessions are available to display
            launch {
                profileRepository.getActiveProfileStream().flatMapLatest { activeProfile ->
                    if (activeProfile == null) {
                        flowOf(0)
                    } else {
                        sessionRepository.getNumSessionsStreamForFilteredTimeFrame(
                            startDate = startDate,
                            endDate = endDate,
                            sessionName =
                                _sessionsUiState.value.filterStatesMap[FilterType.SESSION_NAME]?.selectedElementName,
                            muscleGroupName =
                                _sessionsUiState.value.filterStatesMap[FilterType.MUSCLE_GROUP]?.selectedElementName,
                            liftName =
                                _sessionsUiState.value.filterStatesMap[FilterType.LIFT_NAME]?.selectedElementName
                        )
                    }
                }.collect { numSessions ->
                    _sessionsUiState.update { currentState ->
                        currentState.copy(
                            numSessionsInFilteredTimeFrame = numSessions
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
                            endDate = _sessionsUiState.value.endDate,
                            sessionName =
                                _sessionsUiState.value.filterStatesMap[FilterType.SESSION_NAME]?.selectedElementName,
                            muscleGroupName =
                                _sessionsUiState.value.filterStatesMap[FilterType.MUSCLE_GROUP]?.selectedElementName,
                            liftName =
                                _sessionsUiState.value.filterStatesMap[FilterType.LIFT_NAME]?.selectedElementName
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
                            fetchLimit = _sessionsUiState.value.fetchLimit,
                            sessionName =
                                _sessionsUiState.value.filterStatesMap[FilterType.SESSION_NAME]?.selectedElementName,
                            muscleGroupName =
                                _sessionsUiState.value.filterStatesMap[FilterType.MUSCLE_GROUP]?.selectedElementName,
                            liftName =
                                _sessionsUiState.value.filterStatesMap[FilterType.LIFT_NAME]?.selectedElementName
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

        if (filterCollectionJob != null) {
            filterCollectionJob?.cancel()
            beginFilterCollectionJob()
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
                                    displaySessionLiftSetRow.liftSet.id to SetCardData(
                                        liftSet = displaySessionLiftSetRow.liftSet,
                                        weightMetric = displaySessionLiftSetRow.weightMetric,
                                        secondMetric = displaySessionLiftSetRow.secondMetric,
                                        selected = currentState.currentSessionLiftSetMap[displaySessionLiftSetRow.liftSet.id]?.selected ?: false
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

    fun historicalSetSectionLongClicked(liftSetId: Int) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                currentSessionLiftSetMap = currentState.currentSessionLiftSetMap.mapValues { (thisLiftSetId, thisSetCardData) ->
                    if (thisLiftSetId == liftSetId) {
                        thisSetCardData.copy(
                            selected = !thisSetCardData.selected
                        )
                    } else {
                        thisSetCardData.copy(
                            selected = false
                        )
                    }
                }
            )
        }
    }

    fun historicalSetSectionClicked(liftSetId: Int) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                currentSessionLiftSetMap = currentState.currentSessionLiftSetMap.mapValues { (thisLiftSetId, thisSetCardData) ->
                    if (thisLiftSetId == liftSetId) {
                        thisSetCardData.copy(
                            selected = false
                        )
                    } else {
                        thisSetCardData
                    }
                }
            )
        }
    }

    fun dismissEditHistoricalSetDialog() {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                editSetDialogVisible = false,
                liftSetIdToEdit = null,
                newSetName = "",
                newSetNote = "",
                newWeightValue = "",
                newWeightNote = "",
                newRepsValue = "",
                newHoursValue = "",
                newMinutesValue = "",
                newSecondsValue = "",
                newSecondMetricNote = "",
            )
        }
    }

    fun editHistoricalSetSectionClicked(liftSetId: Int) {
        _sessionsUiState.update { currentState ->
            val setCardData: SetCardData = currentState.currentSessionLiftSetMap[liftSetId] ?: return
            val liftId: Int = setCardData.liftSet.lift_id
            val metricType: String = currentState.currentSessionLiftDetailMap[liftId]?.metricType ?: return
            val timeTriple: Triple<Int, Int, Double> = if (metricType == "time") {
                DateTimeCalculator.convertDoubleTimeToTripleTime(
                    minutes = setCardData.secondMetric.value
                )
            } else {
                Triple(0, 0, 0.0)
            }

            currentState.copy(
                editSetDialogVisible = true,
                liftSetIdToEdit = liftSetId,
                newSetName = setCardData.liftSet.set_label,
                newSetNote = setCardData.liftSet.set_note,
                newWeightValue = setCardData.weightMetric.value.toString(),
                newWeightNote = setCardData.weightMetric.note,
                newRepsValue = if (metricType == "reps") {
                    setCardData.secondMetric.value.toString()
                } else {
                    ""
                },
                newHoursValue = if (metricType == "time") {
                    timeTriple.first.toString()
                } else {
                    ""
                },
                newMinutesValue = if (metricType == "time") {
                    timeTriple.second.toString()
                } else {
                    ""
                },
                newSecondsValue = if (metricType == "time") {
                    timeTriple.third.toString()
                } else {
                    ""
                },
                newSecondMetricNote = setCardData.secondMetric.note
            )
        }
    }

    fun validateHistoricalSetEdit(): Boolean {
        // check that the lift set id is real
        val liftSetId: Int = _sessionsUiState.value.liftSetIdToEdit ?: return false
        val liftId: Int = _sessionsUiState.value.currentSessionLiftSetMap[liftSetId]?.liftSet?.lift_id ?: return false
        val metricType: String = _sessionsUiState.value.currentSessionLiftDetailMap[liftId]?.metricType ?: return false

        // check set name
        if (_sessionsUiState.value.newSetName.isBlank()) {
            return false
        }

        // check weight metric
        if ((_sessionsUiState.value.newWeightValue.toDoubleOrNull() ?: -1.0) < 0.0) {
            return false
        }

        // check reps metric if applicable
        if (metricType == "reps") {
            if ((_sessionsUiState.value.newRepsValue.toDoubleOrNull() ?: -1.0) < 0.0) {
                return false
            }
        } else {
            // check time metrics if applicable
            val hoursValue: Int? = _sessionsUiState.value.newHoursValue.toIntOrNull() ?:
                if (_sessionsUiState.value.newHoursValue.isBlank()) {
                    0
                } else {
                    null
                }
            val minutesValue: Int? = _sessionsUiState.value.newMinutesValue.toIntOrNull() ?:
                if (_sessionsUiState.value.newMinutesValue.isBlank()) {
                    0
                } else {
                    null
                }
            val secondsValue: Double? = _sessionsUiState.value.newSecondsValue.toDoubleOrNull() ?:
                if (_sessionsUiState.value.newSecondsValue.isBlank()) {
                    0.0
                } else {
                    null
                }
            if (hoursValue != null && minutesValue != null && secondsValue != null) {
                if (hoursValue < 0 || minutesValue < 0 || secondsValue < 0.0) {
                    return false
                }
                if (hoursValue.toDouble() + minutesValue.toDouble() + secondsValue <= 0.0) {
                    return false
                }
            } else {
                return false
            }
        }

        return true
    }

    fun updateHistoricalSet() {
        if (validateHistoricalSetEdit()) {
            // retrieve the lift set and its two metrics
            val liftSetIdToEdit: Int = _sessionsUiState.value.liftSetIdToEdit ?: return
            val setCardData: SetCardData = _sessionsUiState.value.currentSessionLiftSetMap[liftSetIdToEdit] ?: return
            val liftSetToUpdate: LiftSet = setCardData.liftSet
            val weightMetricToUpdate: SetMetric = setCardData.weightMetric
            val secondMetricToUpdate: SetMetric = setCardData.secondMetric
            val newWeightValue: Double = _sessionsUiState.value.newWeightValue.toDoubleOrNull() ?: return
            val liftId: Int = setCardData.liftSet.lift_id
            val metricType: String = _sessionsUiState.value.currentSessionLiftDetailMap[liftId]?.metricType ?: return
            val newSecondMetricValue: Double = if (metricType == "reps") {
                _sessionsUiState.value.newRepsValue.toDoubleOrNull() ?: return
            } else {
                DateTimeCalculator.convertTripleTimeToDoubleTime(
                    hours = _sessionsUiState.value.newHoursValue.toIntOrNull() ?:
                    if (_sessionsUiState.value.newHoursValue.isBlank()) {
                        0
                    } else {
                        return
                    },
                    minutes = _sessionsUiState.value.newMinutesValue.toIntOrNull() ?:
                    if (_sessionsUiState.value.newMinutesValue.isBlank()) {
                        0
                    } else {
                        return
                    },
                    seconds = _sessionsUiState.value.newSecondsValue.toDoubleOrNull() ?:
                    if (_sessionsUiState.value.newSecondsValue.isBlank()) {
                        0.0
                    } else {
                        return
                    }
                )
            }
            val newSetName: String = _sessionsUiState.value.newSetName
            val newSetNote: String = _sessionsUiState.value.newSetNote
            val newWeightNote: String = _sessionsUiState.value.newWeightNote
            val newSecondMetricNote: String = _sessionsUiState.value.newSecondMetricNote

            viewModelScope.launch {
                // first update the lift set
                liftSetRepository.updateLiftSet(
                    liftSet = liftSetToUpdate.copy(
                        set_label = newSetName,
                        set_note = newSetNote
                    )
                )

                // then update the weight metric
                setMetricRepository.updateSetMetric(
                    setMetric = weightMetricToUpdate.copy(
                        value = newWeightValue,
                        note = newWeightNote
                    )
                )

                // then update the second metric
                setMetricRepository.updateSetMetric(
                    setMetric = secondMetricToUpdate.copy(
                        value = newSecondMetricValue,
                        note = newSecondMetricNote
                    )
                )

                // dismiss dialog
                dismissEditHistoricalSetDialog()
            }
        }
    }

    fun updateNewSetName(setName: String) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                newSetName = setName
            )
        }
    }

    fun updateNewSetNote(setNote: String) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                newSetNote = setNote
            )
        }
    }

    fun updateNewWeightValue(weightValue: String) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                newWeightValue = weightValue
            )
        }
    }

    fun updateNewWeightNote(weightNote: String) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                newWeightNote = weightNote
            )
        }
    }

    fun updateNewSecondMetricNote(secondMetricNote: String) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                newSecondMetricNote = secondMetricNote
            )
        }
    }

    fun updateNewRepsValue(repsValue: String) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                newRepsValue = repsValue
            )
        }
    }

    fun updateNewHoursValue(hoursValue: String) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                newHoursValue = hoursValue
            )
        }
    }

    fun updateNewMinutesValue(minutesValue: String) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                newMinutesValue = minutesValue
            )
        }
    }

    fun updateNewSecondsValue(secondsValue: String) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                newSecondsValue = secondsValue
            )
        }
    }

    fun moveHistoricalSetUp(liftSetId: Int) {
        viewModelScope.launch {
            sessionRepository.moveLiftSet(
                liftSetId = liftSetId
            )
        }
    }

    fun moveHistoricalSetDown(liftSetId: Int) {
        viewModelScope.launch {
            sessionRepository.moveLiftSet(
                liftSetId = liftSetId,
                down = true
            )
        }
    }

    fun showDeleteHistoricalSetDialog(liftSetId: Int) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                deleteSetDialogVisible = true,
                liftSetIdToDelete = liftSetId
            )
        }
    }

    fun deleteHistoricalSet() {
        // retrieve the lift set to delete
        val liftIdToDelete: Int = _sessionsUiState.value.liftSetIdToDelete ?: return
        val liftSetToDelete: LiftSet = _sessionsUiState.value.currentSessionLiftSetMap[liftIdToDelete]?.liftSet ?: return

        // delete the lift set and dismiss the dialog
        viewModelScope.launch {
            liftSetRepository.deleteLiftSet(
                liftSet = liftSetToDelete
            )
            dismissDeleteHistoricalSetDialog()
        }
    }

    fun dismissDeleteHistoricalSetDialog() {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                deleteSetDialogVisible = false,
                liftSetIdToDelete = null
            )
        }
    }

    fun closeFilterSection() {
        viewModelScope.launch {
            _sessionsUiState.update { currentState ->
                currentState.copy(
                    filterSectionStage2Expanded = false
                )
            }

            delay(300)

            // finish animation and reset all filter states
            _sessionsUiState.update { currentState ->
                currentState.copy(
                    filterSectionStage1Expanded = false,
                    filterStatesMap = mapOf(
                        FilterType.SESSION_NAME to FilterState(),
                        FilterType.MUSCLE_GROUP to FilterState(),
                        FilterType.LIFT_NAME to FilterState()
                    )
                )
            }

            // cancel filter collection job
            filterCollectionJob?.cancel()
            filterCollectionJob = null

            // refresh screen only if the user has applied filters
            if (_sessionsUiState.value.filterText != "Filter") {
                refresh(refreshEverything = true)
            }

            // reset filter label
            updateFilterLabel()
        }
    }

    fun beginFilterCollectionJob() {
        filterCollectionJob = viewModelScope.launch {
            // collect all unique session names sorted in descending order of frequency
            launch {
                combine(
                    sessionRepository.getUniqueSessionNamesAndFrequenciesStream(
                        fetchLimit = _sessionsUiState.value.filterStatesMap[FilterType.SESSION_NAME]?.fetchLimit
                            ?: 10,
                        startDate = _sessionsUiState.value.startDate,
                        endDate = _sessionsUiState.value.endDate
                    ),
                    sessionRepository.getNumSessionsStreamForTimeFrame(
                        startDate = _sessionsUiState.value.startDate,
                        endDate = _sessionsUiState.value.endDate
                    ),
                    sessionRepository.getNumberOfUniqueSessionNamesAndFrequenciesStream(
                        startDate = _sessionsUiState.value.startDate,
                        endDate = _sessionsUiState.value.endDate
                    )
                ) { sessionNamesAndFrequencies, anyCount, totalCount ->
                    Triple(
                        first = sessionNamesAndFrequencies.map { sessionNameAndFrequency ->
                            Pair(
                                first = sessionNameAndFrequency.sessionName,
                                second = sessionNameAndFrequency.sessionFrequency
                            )
                        },
                        second = anyCount,
                        third = totalCount
                    )
                }.collect { dataTriple ->
                    _sessionsUiState.update { currentState ->
                        currentState.copy(
                            filterStatesMap = currentState.filterStatesMap.mapValues { (filterType, filterState) ->
                                if (filterType == FilterType.SESSION_NAME) {
                                    filterState.copy(
                                        elementList = dataTriple.first,
                                        anyCount = dataTriple.second,
                                        totalNumberOfElements = dataTriple.third
                                    )
                                } else {
                                    filterState
                                }
                            }
                        )
                    }
                }
            }

            // collect all unique muscle group names sorted in descending order of frequency
            launch {
                combine(
                    sessionRepository.getUniqueMuscleGroupNamesAndFrequenciesStream(
                        sessionName = _sessionsUiState.value.filterStatesMap[FilterType.SESSION_NAME]?.selectedElementName,
                        fetchLimit = _sessionsUiState.value.filterStatesMap[FilterType.MUSCLE_GROUP]?.fetchLimit ?: 10,
                        startDate = _sessionsUiState.value.startDate,
                        endDate = _sessionsUiState.value.endDate
                    ),
                    sessionRepository.getNumSessionsStreamAfterNameFilter(
                        sessionName = _sessionsUiState.value.filterStatesMap[FilterType.SESSION_NAME]?.selectedElementName,
                        startDate = _sessionsUiState.value.startDate,
                        endDate = _sessionsUiState.value.endDate
                    ),
                    sessionRepository.getNumberOfUniqueMuscleGroupNamesAndFrequenciesStream(
                        sessionName = _sessionsUiState.value.filterStatesMap[FilterType.SESSION_NAME]?.selectedElementName,
                        startDate = _sessionsUiState.value.startDate,
                        endDate = _sessionsUiState.value.endDate
                    )
                ) { muscleGroupNamesAndFrequencies, anyCount, totalCount ->
                    Triple(
                        first = muscleGroupNamesAndFrequencies.map { muscleGroupNameAndFrequency ->
                            Pair(
                                first = muscleGroupNameAndFrequency.muscleGroupName,
                                second = muscleGroupNameAndFrequency.muscleGroupFrequency
                            )
                        },
                        second = anyCount,
                        third = totalCount
                    )
                }.collect { dataTriple ->
                    _sessionsUiState.update { currentState ->
                        currentState.copy(
                            filterStatesMap = currentState.filterStatesMap.mapValues { (thisFilterType, thisFilterState) ->
                                if (thisFilterType == FilterType.MUSCLE_GROUP) {
                                    thisFilterState.copy(
                                        elementList = dataTriple.first,
                                        anyCount = dataTriple.second,
                                        totalNumberOfElements = dataTriple.third
                                    )
                                } else {
                                    thisFilterState
                                }
                            }
                        )
                    }
                }
            }

            // collect all unique lift names sorted in descending order of frequency
            launch {
                combine(
                    sessionRepository.getUniqueLiftNamesAndFrequenciesStream(
                        sessionName = _sessionsUiState.value.filterStatesMap[FilterType.SESSION_NAME]?.selectedElementName,
                        muscleGroupName = _sessionsUiState.value.filterStatesMap[FilterType.MUSCLE_GROUP]?.selectedElementName,
                        fetchLimit = _sessionsUiState.value.filterStatesMap[FilterType.LIFT_NAME]?.fetchLimit ?: 10,
                        startDate = _sessionsUiState.value.startDate,
                        endDate = _sessionsUiState.value.endDate
                    ),
                    sessionRepository.getNumSessionsStreamAfterMuscleGroupFilter(
                        sessionName = _sessionsUiState.value.filterStatesMap[FilterType.SESSION_NAME]?.selectedElementName,
                        muscleGroupName = _sessionsUiState.value.filterStatesMap[FilterType.MUSCLE_GROUP]?.selectedElementName,
                        startDate = _sessionsUiState.value.startDate,
                        endDate = _sessionsUiState.value.endDate
                    ),
                    sessionRepository.getNumberOfUniqueLiftNamesAndFrequenciesStream(
                        sessionName = _sessionsUiState.value.filterStatesMap[FilterType.SESSION_NAME]?.selectedElementName,
                        muscleGroupName = _sessionsUiState.value.filterStatesMap[FilterType.MUSCLE_GROUP]?.selectedElementName,
                        startDate = _sessionsUiState.value.startDate,
                        endDate = _sessionsUiState.value.endDate
                    )
                ) { liftNamesAndFrequencies, anyCount, totalCount ->
                    Triple(
                        first = liftNamesAndFrequencies.map { liftNameAndFrequency ->
                            Pair(
                                first = liftNameAndFrequency.liftName,
                                second = liftNameAndFrequency.liftFrequency
                            )
                        },
                        second = anyCount,
                        third = totalCount
                    )
                }.collect { dataTriple ->
                    _sessionsUiState.update { currentState ->
                        currentState.copy(
                            filterStatesMap = currentState.filterStatesMap.mapValues { (thisFilterType, thisFilterState) ->
                                if (thisFilterType == FilterType.LIFT_NAME) {
                                    thisFilterState.copy(
                                        elementList = dataTriple.first,
                                        anyCount = dataTriple.second,
                                        totalNumberOfElements = dataTriple.third
                                    )
                                } else {
                                    thisFilterState
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    fun openFilterSection() {
        // begin collecting all unique session names if not already done
        if (filterCollectionJob == null) {
            beginFilterCollectionJob()
        }

        viewModelScope.launch {
            _sessionsUiState.update { currentState ->
                currentState.copy(
                    filterSectionStage1Expanded = true
                )
            }

            delay(150)

            _sessionsUiState.update { currentState ->
                currentState.copy(
                    filterSectionStage2Expanded = true
                )
            }
        }
    }

    fun filterButtonClicked() {
        val filterCurrentlyOpen = _sessionsUiState.value.filterSectionStage1Expanded &&
                _sessionsUiState.value.filterSectionStage2Expanded

        if (filterCurrentlyOpen) {
            closeFilterSection()
        } else {
            openFilterSection()
        }
    }

    fun filterDropdownClicked(filterType: FilterType) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                filterStatesMap = currentState.filterStatesMap.mapValues { (thisFilterType, thisFilterState) ->
                    if (thisFilterType == filterType) {
                        thisFilterState.copy(
                            dropdownExpanded = true
                        )
                    } else {
                        thisFilterState
                    }
                }
            )
        }
    }

    fun dismissFilterDropdown(filterType: FilterType) {
        _sessionsUiState.update { currentState ->
            currentState.copy(
                filterStatesMap = currentState.filterStatesMap.mapValues { (thisFilterType, thisFilterState) ->
                    if (thisFilterType == filterType) {
                        thisFilterState.copy(
                            dropdownExpanded = false
                        )
                    } else {
                        thisFilterState
                    }
                }
            )
        }
    }

    fun filterRemoved(filterType: FilterType) {
        // update the selected element for the given filterType to be null
        _sessionsUiState.update { currentState ->
            currentState.copy(
                filterStatesMap = currentState.filterStatesMap.mapValues { (thisFilterType, thisFilterState) ->
                    if (thisFilterType == filterType) {
                        thisFilterState.copy(
                            selectedElementName = null
                        )
                    } else {
                        thisFilterState
                    }
                }
            )
        }

        // update the filter label
        updateFilterLabel()

        // dismiss dropdown
        dismissFilterDropdown(filterType)

        // refresh everything
        refresh(refreshEverything = true)
        filterCollectionJob?.cancel()
        beginFilterCollectionJob()
    }

    fun filterApplied(filterType: FilterType, element: Pair<String, Int>) {
        // update the selected element for the given filtertype
        _sessionsUiState.update { currentState ->
            currentState.copy(
                filterStatesMap = currentState.filterStatesMap.mapValues { (thisFilterType, thisFilterState) ->
                    if (thisFilterType == filterType) {
                        thisFilterState.copy(
                            selectedElementName = element.first
                        )
                    } else {
                        thisFilterState
                    }
                }
            )
        }

        // update the filter label
        updateFilterLabel()

        // dismiss dropdown
        dismissFilterDropdown(filterType)

        // refresh everything
        refresh(refreshEverything = true)
        filterCollectionJob?.cancel()
        beginFilterCollectionJob()
    }

    fun updateFilterLabel() {
        var numFiltersApplied = 0
        for (filterState in _sessionsUiState.value.filterStatesMap.values) {
            if (filterState.selectedElementName != null) {
                numFiltersApplied++
            }
        }

        if (numFiltersApplied != 0) {
            _sessionsUiState.update { currentState ->
                currentState.copy(
                    filterText = "Filter (${numFiltersApplied})"
                )
            }
        } else {
            _sessionsUiState.update { currentState ->
                currentState.copy(
                    filterText = "Filter"
                )
            }
        }
    }

    fun loadMoreFilterElements(filterType: FilterType) {
        // update the fetch limit +10 for this filter state
        _sessionsUiState.update { currentState ->
            currentState.copy(
                filterStatesMap = currentState.filterStatesMap.mapValues { (thisFilterType, thisFilterState) ->
                    if (thisFilterType == filterType) {
                        thisFilterState.copy(
                            fetchLimit = thisFilterState.fetchLimit + 10
                        )
                    } else {
                        thisFilterState
                    }
                }
            )
        }

        // refresh the filter state
        filterCollectionJob?.cancel()
        beginFilterCollectionJob()
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
    val numSessionsInFilteredTimeFrame: Int = 0,
    val timeFrameDropdownExpanded: Boolean = false,
    // list of LiftSetCountPerMuscleGroup objects to create the donut chart at the top.
    // this includes ALL data in the selected timeframe, not affected by fetchLimit
    val muscleGroupFrequencyList: List<LiftSetCountPerMuscleGroup> = emptyList(),
    // sessionDetailMap: session ids pointing to SessionDetail objects; affected by fetchLimit
    val sessionDetailMap: Map<Int, SessionDetail> = emptyMap(),
    // weekStringPairList: list of pairs with first element as a formatted week string,
    // second element as a list of session ids
    val weekStringPairList: List<Pair<String, List<Int>>> = emptyList(),
    // currentSessionLiftSetMap: Map of LiftSet ids pointing to SetCardData containing a lift set
    // object,  both of its SetMetric objects, and its selected state
    val currentSessionLiftSetMap: Map<Int, SetCardData> = emptyMap(),
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
    val donutChartsVisible: Boolean = false,
    // fields for editing a set
    val editSetDialogVisible: Boolean = false,
    val liftSetIdToEdit: Int? = null,
    val newSetName: String = "",
    val newSetNote: String = "",
    val newWeightValue: String = "",
    val newWeightNote: String = "",
    val newRepsValue: String = "",
    val newHoursValue: String = "",
    val newMinutesValue: String = "",
    val newSecondsValue: String = "",
    val newSecondMetricNote: String = "",
    // fields for deleting a set
    val deleteSetDialogVisible: Boolean = false,
    val liftSetIdToDelete: Int? = null,
    // properties for filtering
    val filterStatesMap: Map<FilterType, FilterState> = mapOf(
        FilterType.SESSION_NAME to FilterState(),
        FilterType.MUSCLE_GROUP to FilterState(),
        FilterType.LIFT_NAME to FilterState()
    ),
    val filterText: String = "Filter",
    val filterSectionStage1Expanded: Boolean = false,
    val filterSectionStage2Expanded: Boolean = false,
)

data class FilterState(
    val selectedElementName: String? = null,
    val elementList: List<Pair<String, Int>> = emptyList(),
    val dropdownExpanded: Boolean = false,
    val fetchLimit: Int = 10,
    val anyCount: Int = 0,
    val totalNumberOfElements: Int = 0
)

enum class FilterType(
    val label: String,
    val defaultElementLabel: String
) {
    SESSION_NAME(
        label = "Session name:",
        defaultElementLabel = "Any"
    ),
    MUSCLE_GROUP(
        label = "Muscle group:",
        defaultElementLabel = "Any"
    ),
    LIFT_NAME(
        label = "Lift name:",
        defaultElementLabel = "Any"
    )
}
