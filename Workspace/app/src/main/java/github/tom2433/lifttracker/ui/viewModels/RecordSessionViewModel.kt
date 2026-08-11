package github.tom2433.lifttracker.ui.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.lift.Lift
import github.tom2433.lifttracker.data.lift.LiftRepository
import github.tom2433.lifttracker.data.session.Session
import github.tom2433.lifttracker.data.session.SessionRepository
import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.liftset.LiftSetRepository
import github.tom2433.lifttracker.data.profile.Profile
import github.tom2433.lifttracker.data.profile.ProfileRepository
import github.tom2433.lifttracker.data.setmetric.SetMetric
import github.tom2433.lifttracker.data.setmetric.SetMetricRepository
import github.tom2433.lifttracker.data.structures.LiftSearchDetail
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import github.tom2433.lifttracker.data.structures.RecordSessionLiftSetRow
import github.tom2433.lifttracker.data.structures.SetMetricDisplayDetail
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
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
import kotlin.collections.emptyList

/**
 * ViewModel for RecordSessionScreen
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecordSessionViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository,
    private val liftRepository: LiftRepository,
    private val liftSetRepository: LiftSetRepository,
    private val setMetricRepository: SetMetricRepository
) : ViewModel() {
    private val _recordSessionUiState = MutableStateFlow(RecordSessionUiState())
    private val _toastEvents = MutableSharedFlow<String>()
    private var liftSuggestionsJob: Job? = null
    private var liftIdPendingReveal: Int? = null
    private var liftSetIdPendingReveal: Int? = null
    val recordSessionUiState: StateFlow<RecordSessionUiState> = _recordSessionUiState.asStateFlow()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    init {
        // constant collection: retrieve active session for currently active profile
        viewModelScope.launch {
            sessionRepository.getActiveSessionForActiveProfileStream().collect { thisSession ->
                _recordSessionUiState.update { currentState ->
                    currentState.copy(
                        activeSession = thisSession
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

        // constant collection: fill the liftSetMap. liftSetMap will always be updated for the UI state
        // also fill the liftSetVisible map to indicate which LiftSet cards are visible
        viewModelScope.launch {
            sessionRepository.getActiveSessionForActiveProfileStream()
                .flatMapLatest { activeSession ->
                    if (activeSession == null) {
                        flowOf(emptyList())
                    } else {
                        liftSetRepository.getRecordSessionLiftSetRowsForSessionStream(activeSession.id)
                    }
                }
                .collect { rows -> // contains LiftSet objects
                    _recordSessionUiState.update { currentState ->
                        currentState.copy(
                            liftSetMap = rows.toLiftSetMap(),
                            liftSetVisibleMap = rows.toLiftSetVisibleMap(currentState.liftSetVisibleMap)
                        )
                    }

                    revealPendingLiftIfReady()
                    revealPendingLiftSetIfReady()
                }
        }

        // constant collection: fill the liftDetailMap. liftDetailMap will always be updated for the UI state
        viewModelScope.launch {
            sessionRepository.getActiveSessionForActiveProfileStream()
                .flatMapLatest { activeSession ->
                    if (activeSession == null) {
                        flowOf(emptyList())
                    } else {
                        liftRepository.getLiftSearchDetailsForSessionIdStream(activeSession.id)
                    }
                }
                .collect { rows ->
                    _recordSessionUiState.update { currentState ->
                        currentState.copy(
                            liftDetailMap = rows.toLiftDetailMap(
                                previousLiftDetailMap = currentState.liftDetailMap
                            )
                        )
                    }

                    revealPendingLiftIfReady()
                    revealPendingLiftSetIfReady()
                }
        }

        // constant collection: fill the setMetricDisplayDetailMap
        viewModelScope.launch {
            sessionRepository.getActiveSessionForActiveProfileStream()
                .flatMapLatest { activeSession ->
                    if (activeSession == null) {
                        flowOf(emptyList())
                    } else {
                        setMetricRepository.getSetMetricIdsFromSessionIdStream(activeSession.id)
                    }
                }
                .collect { setMetricIds ->
                    _recordSessionUiState.update { currentState ->
                        currentState.copy(
                            setMetricDisplayDetailMap = setMetricIds.toSetMetricDisplayDetailMap(
                                previousSetMetricDisplayDetailMap = currentState.setMetricDisplayDetailMap
                            )
                        )
                    }
                }
        }

        // constant collection: fill setCountPerLiftMap to count the number of sets per lift that
        // the user has logged so far.
        viewModelScope.launch {
            sessionRepository.getActiveSessionForActiveProfileStream()
                .flatMapLatest { activeSession ->
                    if (activeSession == null) {
                        flowOf(emptyList())
                    } else {
                        liftSetRepository.getLiftSetCountPerLiftIdForSessionIdStream(activeSession.id)
                    }
                }
                .collect { liftSetCountPerLifts ->
                    _recordSessionUiState.update { currentState ->
                        currentState.copy(
                            setCountPerLiftMap = liftSetCountPerLifts.associate { liftSetCountPerLift ->
                                liftSetCountPerLift.liftId to liftSetCountPerLift.liftSetCount
                            }
                        )
                    }
                }
        }

        // constant collection: fill the setCountPerMuscleGroupPairList
        viewModelScope.launch {
            sessionRepository.getActiveSessionForActiveProfileStream()
                .flatMapLatest { activeSession ->
                    if (activeSession == null) {
                        flowOf(emptyList())
                    } else {
                        sessionRepository.getSetCountPerMuscleGroupForSessionIdStream(activeSession.id)
                    }
                }
                .collect { liftSetCountPerMuscleGroups ->
                    _recordSessionUiState.update { currentState ->
                        currentState.copy(
                            setCountPerMuscleGroupList = liftSetCountPerMuscleGroups
                        )
                    }
                }
        }

        // collect once at the beginning: fill the setMetricDisplayDetailMap with values from the
        // database when this viewModel is destroyed and re-created
        // this helps to avoid losing the displayed data when closing the app or switching to
        // a different screen
        viewModelScope.launch {
            val setMetricList: List<SetMetric> = setMetricRepository.getSetMetricsFromActiveSession()

            // fill the setMetricDisplayDetailMap if the list of setMetrics returned is not empty
            // (meaning that a session is already in progress that has not been accounted for)
            if (setMetricList.isNotEmpty()) {
                _recordSessionUiState.update { currentState ->
                    currentState.copy(
                        setMetricDisplayDetailMap = setMetricList.associate { setMetricObj ->
                            // retrieve metric type (1 = reps, 2 = time)
                            val metricType: Int? = liftRepository.getMetricTypeFromSetMetricId(setMetricObj.id)

                            setMetricObj.id to createSetMetricDisplayDetail(setMetricObj, metricType)
                        }
                    )
                }
            }
        }
    }

    private fun createSetMetricDisplayDetail(setMetric: SetMetric, metricType: Int?): SetMetricDisplayDetail {
        val value: Double =
            if (setMetric.metric_position == 1) {
                // weight
                setMetric.value
            } else {
                // rep or time
                when (metricType) {
                    1 -> setMetric.value    // reps
                    else -> -1.0            // time (calculated later)
                }
            }
        var timeTriple: Triple<Int, Int, Double> = Triple(0, 0, 0.0)
        val inputIsLogged = setMetric.value != -1.0

        if (setMetric.metric_position == 2 && metricType == 2 && setMetric.value != -1.0) {
            timeTriple = DateTimeCalculator.convertDoubleTimeToTripleTime(setMetric.value)
        }

        return SetMetricDisplayDetail(
            value = if (value != -1.0) {
                value.toString()
            } else {
                ""
            },
            hours = if (timeTriple.first != 0) {
                timeTriple.first.toString()
            } else {
                ""
            },
            minutes = if (timeTriple.second != 0) {
                timeTriple.second.toString()
            } else {
                ""
            },
            seconds = if (timeTriple.third != 0.0) {
                timeTriple.third.toString()
            } else {
                ""
            },
            inputIsLogged = inputIsLogged
        )
    }

    private fun revealPendingLiftSetIfReady() {
        val pendingLiftSetId = liftSetIdPendingReveal ?: return

        var pendingLiftSetIsReady = false

        for (nestedMap in _recordSessionUiState.value.liftSetMap.values) {
            for (liftSet in nestedMap.keys) {
                if (liftSet.id == pendingLiftSetId) {
                    pendingLiftSetIsReady = true
                    break
                }
            }

            if (pendingLiftSetIsReady) break
        }

        pendingLiftSetIsReady = pendingLiftSetIsReady &&
                (pendingLiftSetId in _recordSessionUiState.value.liftSetVisibleMap)

        if (!pendingLiftSetIsReady) {
            return
        }

        // pending lift set is officially ready, so now we can set it visible
        viewModelScope.launch {
            // wait a little for compose ot render the hidden card
            delay(50)

            _recordSessionUiState.update { currentState ->
                currentState.copy(
                    liftSetVisibleMap = currentState.liftSetVisibleMap.mapValues { (liftSetId, visible) ->
                        if (liftSetId == pendingLiftSetId) {
                            true
                        } else {
                            visible
                        }
                    }
                )
            }
        }
    }

    private fun revealPendingLiftIfReady() {
        val pendingLiftId = liftIdPendingReveal ?: return

        val pendingLiftIsReady =
            pendingLiftId in _recordSessionUiState.value.liftDetailMap.keys &&
            pendingLiftId in _recordSessionUiState.value.liftSetMap.keys

        if (!pendingLiftIsReady) {
            return
        }

        viewModelScope.launch {
            // wait a little for compose to render the hidden card
            delay(50)

            _recordSessionUiState.update { currentState ->
                currentState.copy(
                    liftDetailMap = currentState.liftDetailMap.mapValues { (liftId, liftDetail) ->
                        if (liftId == pendingLiftId) {
                            liftDetail.copy(visible = true)
                        } else {
                            liftDetail
                        }
                    }
                )
            }

            // wait for enter animation to finish
            delay(150)
            _recordSessionUiState.update { currentState ->
                currentState.copy(
                    liftDetailMap = currentState.liftDetailMap.mapValues { (liftId, liftDetail) ->
                        if (liftId == pendingLiftId) {
                            liftDetail.copy(selected = true)
                        } else {
                            liftDetail
                        }
                    }
                )
            }

            liftIdPendingReveal = null
        }
    }

    fun toggleSelectedInProgressLiftCard(thisLiftId: Int) {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                liftDetailMap = currentState.liftDetailMap.mapValues { (liftId, liftDetail) ->
                    if (liftId == thisLiftId) {
                        liftDetail.copy(
                            selected = !liftDetail.selected
                        )
                    } else {
                        liftDetail
                    }
                }
            )
        }
    }

    private fun List<Int>.toSetMetricDisplayDetailMap(
        previousSetMetricDisplayDetailMap: Map<Int, SetMetricDisplayDetail>
    ): Map<Int, SetMetricDisplayDetail> {
        return associate { thisSetMetricId ->
            thisSetMetricId to SetMetricDisplayDetail(
                value = previousSetMetricDisplayDetailMap[thisSetMetricId]?.value ?: "",
                hours = previousSetMetricDisplayDetailMap[thisSetMetricId]?.hours ?: "",
                minutes = previousSetMetricDisplayDetailMap[thisSetMetricId]?.minutes ?: "",
                seconds = previousSetMetricDisplayDetailMap[thisSetMetricId]?.seconds ?: "",
                inputIsLogged = previousSetMetricDisplayDetailMap[thisSetMetricId]?.inputIsLogged ?: false
            )
        }
    }

    private fun List<LiftSearchDetail>.toLiftDetailMap(
        previousLiftDetailMap: Map<Int, LiftSearchDetail>
    ): Map<Int, LiftSearchDetail> {
        return associate { detail ->
            val liftId = detail.liftObj.id
            val previousSelected = previousLiftDetailMap[liftId]?.selected
            val previousVisible = previousLiftDetailMap[liftId]?.visible

            liftId to detail.copy(
                selected = previousSelected ?: false,
                visible = when {
                    previousVisible != null -> previousVisible
                    liftId == liftIdPendingReveal -> false
                    else -> true
                }
            )
        }
    }

    private fun List<RecordSessionLiftSetRow>.toLiftSetVisibleMap(
        previousLiftSetVisibleMap: Map<Int, Boolean>
    ): Map<Int, Boolean> {
        return associate { row ->
            val previousVisible: Boolean? = previousLiftSetVisibleMap[row.liftSet.id]
            row.liftSet.id to when {
                previousVisible != null -> previousVisible
                row.liftSet.id == liftSetIdPendingReveal -> false
                else -> true
            }
        }
    }

    private fun List<RecordSessionLiftSetRow>.toLiftSetMap(): Map<Int, Map<LiftSet, Pair<SetMetric, SetMetric>>> {
        return groupBy { row ->
            row.liftId
        }.mapValues { (_, rowsForLift) ->
            rowsForLift.associate { row ->
                row.liftSet to Pair(row.weightMetric, row.secondMetric)
            }
        }
    }

    fun beginSession() {
        // need to create a session object that is active. the flows defined in init {} should
        // automatically update the activeSession for RecordSessionUiState
        viewModelScope.launch {
            val activeProfileId: Int = _recordSessionUiState.value.activeProfile?.id ?: return@launch
            val sessionNum: Int = sessionRepository.getNextSessionNumber(activeProfileId)

            sessionRepository.insertSession(
                session = Session(
                    in_progress = true,
                    profile_id = activeProfileId,
                    session_number = sessionNum,
                    session_label = "Session $sessionNum",
                    date = DateTimeCalculator.getCurrentIsoDate(),
                    note = ""
                )
            )
        }
    }

    fun saveSession() {
        viewModelScope.launch {
            val sessionSaved = sessionRepository.finishSession(
                id = _recordSessionUiState.value.activeSession?.id ?: return@launch
            )

            if (sessionSaved) {
                _toastEvents.emit("Session saved! Check it out in the Sessions screen.")
            } else {
                _toastEvents.emit("Nothing Saved")
            }
        }
    }

    fun showSessionEditDialog() {
        val currentActiveSession: Session = _recordSessionUiState.value.activeSession ?: return

        _recordSessionUiState.update { currentState ->
            currentState.copy(
                sessionEditDialogVisible = true,
                newSessionName = currentActiveSession.session_label,
                newSessionNote = currentActiveSession.note
            )
        }
    }

    fun dismissSessionEditDialog() {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                sessionEditDialogVisible = false,
                newSessionName = "",
                newSessionNote = ""
            )
        }
    }

    fun validateSessionInput(): Boolean {
        return (_recordSessionUiState.value.newSessionName.isNotBlank())
    }

    fun updateNewSessionName(newSessionName: String) {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                newSessionName = newSessionName
            )
        }
    }

    fun updateNewSessionNote(newSessionNote: String) {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                newSessionNote = newSessionNote
            )
        }
    }

    fun updateSessionNameAndNote() {
        if (!validateSessionInput()) {
            return
        }

        viewModelScope.launch {
            val currentActiveSession: Session = _recordSessionUiState.value.activeSession ?: return@launch

            // update session name and note in database
            sessionRepository.updateSession(
                session = currentActiveSession.copy(
                    session_label = _recordSessionUiState.value.newSessionName,
                    note = _recordSessionUiState.value.newSessionNote
                )
            )

            // dismiss session edit dialog
            dismissSessionEditDialog()
        }
    }

    fun addLiftClicked() {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                userIsAddingLift = true,
                inputLiftName = ""
            )
        }
    }

    fun cancelAddLift() {
        liftSuggestionsJob?.cancel()

        _recordSessionUiState.update { currentState ->
            currentState.copy(
                userIsAddingLift = false,
                inputLiftName = "",
                liftSuggestionsList = emptyList()
            )
        }
    }

    fun liftEntryDropdownButtonClicked() {
        liftSuggestionsJob?.cancel()

        // search for no value, should give a list of all lifts
        liftSuggestionsJob = viewModelScope.launch {
            liftRepository
                .getLiftSearchDetailsContainingStream("")
                .collect { liftDetails ->
                    _recordSessionUiState.update { currentState ->
                        val filteredLiftDetails = liftDetails.filter { liftDetail ->
                            liftDetail.liftObj.id !in currentState.liftDetailMap.keys
                        }

                        val lastIndex = filteredLiftDetails.lastIndex

                        currentState.copy(
                            liftSuggestionsList = filteredLiftDetails.mapIndexed { index, liftDetail ->
                                liftDetail.copy(
                                    selected = index == lastIndex
                                )
                            }
                        )
                    }
                }
        }
    }

    fun onInputLiftValueChanged(newValue: String) {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                inputLiftName = newValue
            )
        }

        liftSuggestionsJob?.cancel()

        // if inputted value is blank, don't search anything, and return
        if (newValue.isBlank()) {
            _recordSessionUiState.update { currentState ->
                currentState.copy(
                    liftSuggestionsList = emptyList()
                )
            }
            return
        }

        // search for user's inputted value and fill LiftDetailList
        liftSuggestionsJob = viewModelScope.launch {
            liftRepository
                .getLiftSearchDetailsContainingStream(newValue.trim())
                .collect { liftDetails ->
                    _recordSessionUiState.update { currentState ->
                        val filteredLiftDetails = liftDetails.filter { liftDetail ->
                            liftDetail.liftObj.id !in currentState.liftDetailMap.keys
                        }

                        val lastIndex = filteredLiftDetails.lastIndex

                        currentState.copy(
                            liftSuggestionsList = filteredLiftDetails.mapIndexed { index, liftDetail ->
                                liftDetail.copy(
                                    selected = index == lastIndex
                                )
                            }
                        )
                    }
                }
        }
    }

    fun dismissDropdownSuggestionList() {
        liftSuggestionsJob?.cancel()

        _recordSessionUiState.update { currentState ->
            currentState.copy(
                liftSuggestionsList = emptyList()
            )
        }
    }

    fun liftSuggestionClicked(id: Int) {
        var liftSearchDetail: LiftSearchDetail? = null

        for (detail in _recordSessionUiState.value.liftSuggestionsList) {
            if (detail.liftObj.id == id) {
                liftSearchDetail = detail
                break
            }
        }

        if (liftSearchDetail == null) {
            return
        }

        _recordSessionUiState.update { currentState ->
            currentState.copy(
                liftSuggestionsList = listOf(
                    liftSearchDetail.copy(
                        selected = true
                    )
                )
            )
        }

        onGoLiftEntry()
    }

    fun onGoLiftEntry() {
        // retrieve the selected lift
        val selectedLift: LiftSearchDetail =
            _recordSessionUiState.value.liftSuggestionsList.getSelected()
                ?: return

        val activeSession: Session =
            _recordSessionUiState.value.activeSession ?: return

        // swipe away existingLiftEntryCard since selected lift has been found
        cancelAddLift()

        liftIdPendingReveal = selectedLift.liftObj.id

        // insert new lift set for active session and selected lift
        // this will automatically create two lift set metrics
        // UI will also update since liftSetMap will recognize the addition
        viewModelScope.launch {
            liftSetRepository.insertLiftSet(
                sessionId = activeSession.id,
                liftId = selectedLift.liftObj.id
            )
        }
    }

    fun List<LiftSearchDetail>.getSelected(): LiftSearchDetail? {
        val selectedLifts = filter { liftSearchDetail ->
            liftSearchDetail.selected
        }

        return if (selectedLifts.isNotEmpty()) {
            selectedLifts[0]
        } else {
            null
        }
    }

    fun showDeleteLiftInProgressDialog(liftId: Int) {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                deleteLiftInProgressDialogVisible = true,
                liftIdToDelete = liftId
            )
        }
    }

    fun dismissDeleteLiftInProgressDialog() {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                deleteLiftInProgressDialogVisible = false,
                liftIdToDelete = -1
            )
        }
    }

    fun deleteLiftInProgress() {
        // dismiss dialog
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                deleteLiftInProgressDialogVisible = false
            )
        }

        // determine id of lift to delete sets for
        val idToDelete: Int = if (_recordSessionUiState.value.liftIdToDelete != -1) {
            _recordSessionUiState.value.liftIdToDelete
        } else {
            return
        }

        // retrieve all lift sets to delete in descending order
        val liftSetsToDelete: List<LiftSet> =
            _recordSessionUiState.value.liftSetMap[idToDelete]?.keys?.sortedByDescending { liftSet ->
                liftSet.session_set_number
            } ?: return

        viewModelScope.launch {
            // make not visible the lift that is being deleted
            _recordSessionUiState.update { currentState ->
                currentState.copy(
                    liftDetailMap = currentState.liftDetailMap.mapValues { (liftId, liftDetail) ->
                        if (liftId == idToDelete) {
                            liftDetail.copy(
                                visible = false
                            )
                        } else {
                            liftDetail
                        }
                    },
                    deleteButtonsEnabled = false
                )
            }

            // wait for card to swipe away
            delay(1500)

            // delete lift sets
            for (liftSet in liftSetsToDelete) {
                liftSetRepository.deleteLiftSet(liftSet)
            }

            // reset id to delete
            _recordSessionUiState.update { currentState ->
                currentState.copy(
                    liftIdToDelete = -1,
                    deleteButtonsEnabled = true
                )
            }
        }
    }

    fun showEditLiftSetDialog(liftSet: LiftSet, lift: Lift) {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                editLiftSetDialogVisible = true,
                newLiftSetLabel = liftSet.set_label,
                newLiftSetNote = liftSet.set_note,
                liftSetToEdit = liftSet,
                liftWithSetToEdit = lift
            )
        }
    }

    fun validateLiftSetEntry(): Boolean {
        return _recordSessionUiState.value.newLiftSetLabel.isNotBlank()
    }

    fun updateLiftSetName(newName: String) {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                newLiftSetLabel = newName
            )
        }
    }

    fun updateLiftSetNote(newNote: String) {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                newLiftSetNote = newNote
            )
        }
    }

    fun dismissEditLiftSetDialog() {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                editLiftSetDialogVisible = false,
                newLiftSetLabel = "",
                newLiftSetNote = "",
                liftSetToEdit = null,
                liftWithSetToEdit = null
            )
        }
    }

    fun updateLiftSet() {
        if (!validateLiftSetEntry()) {
            return
        }

        val oldLiftSet: LiftSet = _recordSessionUiState.value.liftSetToEdit ?: return

        val newLiftSet = oldLiftSet.copy(
            set_label = _recordSessionUiState.value.newLiftSetLabel,
            set_note = _recordSessionUiState.value.newLiftSetNote
        )

        // update with new lift set
        viewModelScope.launch {
            liftSetRepository.updateLiftSet(
                liftSet = newLiftSet
            )

            // dismiss edit lift set dialog
            dismissEditLiftSetDialog()
        }
    }

    fun showEditSetMetricNoteDialog(
        setMetricToEdit: SetMetric,
        liftSetWithSetMetricToEdit: LiftSet,
        liftWithSetMetricToEdit: Lift
    ) {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                setMetricToEdit = setMetricToEdit,
                liftSetWithSetMetricToEdit = liftSetWithSetMetricToEdit,
                liftWithSetMetricToEdit = liftWithSetMetricToEdit,
                editSetMetricNoteDialogVisible = true,
                newSetMetricNote = setMetricToEdit.note
            )
        }
    }

    fun dismissEditSetMetricNoteDialog() {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                setMetricToEdit = null,
                liftSetWithSetMetricToEdit = null,
                liftWithSetMetricToEdit = null,
                editSetMetricNoteDialogVisible = false,
                newSetMetricNote = ""
            )
        }
    }

    fun updateSetMetricNote(newSetMetricNote: String) {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                newSetMetricNote = newSetMetricNote
            )
        }
    }

    fun updateSetMetric() {
        val oldSetMetric: SetMetric = _recordSessionUiState.value.setMetricToEdit ?: return
        val newSetMetric: SetMetric = oldSetMetric.copy(
            note = _recordSessionUiState.value.newSetMetricNote
        )

        // update SetMetric in database
        viewModelScope.launch {
            setMetricRepository.updateSetMetric(
                setMetric = newSetMetric
            )

            dismissEditSetMetricNoteDialog()
        }
    }

    fun addLiftSetForLiftId(liftId: Int) {
        viewModelScope.launch {
            val activeSessionId = _recordSessionUiState.value.activeSession?.id ?: return@launch

            val newLiftSetId = liftSetRepository.insertLiftSet(
                sessionId = activeSessionId,
                liftId = liftId
            )

            liftSetIdPendingReveal = newLiftSetId
        }
    }

    fun showDeleteLiftSetDialog(liftSetToDelete: LiftSet) {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                liftSetToDelete = liftSetToDelete,
                deleteLiftSetDialogVisible = true
            )
        }
    }

    fun dismissDeleteLiftSetDialog() {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                liftSetToDelete = null,
                deleteLiftSetDialogVisible = false
            )
        }
    }

    fun deleteLiftSet() {
        val liftSetToDelete: LiftSet = _recordSessionUiState.value.liftSetToDelete ?: return

        viewModelScope.launch {
            // make not visible the lift set in progress card corresponding to the lift set to delete
            _recordSessionUiState.update { currentState ->
                currentState.copy(
                    liftSetVisibleMap = currentState.liftSetVisibleMap.mapValues { (liftSetId, visible) ->
                        if (liftSetId == liftSetToDelete.id) {
                            false
                        } else {
                            visible
                        }
                    },
                    deleteButtonsEnabled = false
                )
            }

            dismissDeleteLiftSetDialog()

            // wait for the lift set card to swipe away
            delay(1000)

            // delete the liftSet
            liftSetRepository.deleteLiftSet(
                liftSet = liftSetToDelete
            )

            // re-enable delete buttons
            _recordSessionUiState.update { currentState ->
                currentState.copy(
                    deleteButtonsEnabled = true
                )
            }
        }
    }

    fun setMetricTimeValueChanged(newValue: String, setMetric: SetMetric, inputType: String) {
        // update the value in the setMetricDisplayDetailMap
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                setMetricDisplayDetailMap = currentState.setMetricDisplayDetailMap.mapValues { (thisSetMetricId, setMetricDisplayDetail) ->
                    if (thisSetMetricId == setMetric.id) {
                        setMetricDisplayDetail.copy(
                            hours = if (inputType == "hours") {
                                newValue
                            } else {
                                setMetricDisplayDetail.hours
                            },
                            minutes = if (inputType == "minutes") {
                                newValue
                            } else {
                                setMetricDisplayDetail.minutes
                            },
                            seconds = if (inputType == "seconds") {
                                newValue
                            } else {
                                setMetricDisplayDetail.seconds
                            },
                            inputIsLogged = false
                        )
                    } else {
                        setMetricDisplayDetail
                    }
                }
            )
        }

        // check to see if we can log this input
        val hoursInput: String = _recordSessionUiState.value.setMetricDisplayDetailMap[setMetric.id]?.hours ?: return
        val minutesInput: String = _recordSessionUiState.value.setMetricDisplayDetailMap[setMetric.id]?.minutes ?: return
        val secondsInput: String = _recordSessionUiState.value.setMetricDisplayDetailMap[setMetric.id]?.seconds ?: return
        val hoursToLog: Int? = if (hoursInput.isNotBlank()) {
            hoursInput.toIntOrNull()
        } else {
            0
        }
        val minutesToLog: Int? = if (minutesInput.isNotBlank()) {
            minutesInput.toIntOrNull()
        } else {
            0
        }
        val secondsToLog: Double? = if (secondsInput.isNotBlank()) {
            secondsInput.toDoubleOrNull()
        } else {
            0.0
        }

        // if all time inputs are valid, log this time input
        if (hoursToLog != null && hoursToLog >= 0.0 &&
            minutesToLog != null && minutesToLog >= 0.0 &&
            secondsToLog != null && secondsToLog >= 0.0 &&
            !(hoursInput.isBlank() && minutesInput.isBlank() && secondsInput.isBlank()) &&
            (hoursToLog.toDouble() + minutesToLog.toDouble() + secondsToLog > 0.0)) {
            viewModelScope.launch {
                // log the valid time input in the database
                setMetricRepository.updateSetMetric(
                    setMetric = setMetric.copy(
                        value = DateTimeCalculator.convertTripleTimeToDoubleTime(
                            hours = hoursToLog,
                            minutes = minutesToLog,
                            seconds = secondsToLog
                        )
                    )
                )

                // mark this input as logged
                _recordSessionUiState.update { currentState ->
                    currentState.copy(
                        setMetricDisplayDetailMap = currentState.setMetricDisplayDetailMap.mapValues { (thisSetMetricId, setMetricDisplayDetail) ->
                            if (thisSetMetricId == setMetric.id) {
                                setMetricDisplayDetail.copy(
                                    inputIsLogged = true
                                )
                            } else {
                                setMetricDisplayDetail
                            }
                        }
                    )
                }
            }
        } else {
            // otherwise, reset the setMetric's value back to -1.0
            viewModelScope.launch {
                setMetricRepository.updateSetMetric(
                    setMetric = setMetric.copy(
                        value = -1.0
                    )
                )
            }
        }
    }

    // only called for set metrics belonging to a lift with a metric type of reps
    // (could be either weight or reps)
    fun setMetricValueChanged(newValue: String, setMetric: SetMetric) {
        // update the value in the setMetricDisplayDetailMap
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                setMetricDisplayDetailMap = currentState.setMetricDisplayDetailMap.mapValues { (thisSetMetricId, setMetricDisplayDetail) ->
                    if (thisSetMetricId == setMetric.id) {
                        setMetricDisplayDetail.copy(
                            value = newValue.trim(),
                            inputIsLogged = false
                        )
                    } else {
                        setMetricDisplayDetail
                    }
                }
            )
        }

        // now check to see if the input is valid
        val valueToLog: Double? = newValue.trim().toDoubleOrNull()

        // if inputted value is valid, update the database
        if (valueToLog != null && (setMetric.metric_position != 2 || valueToLog > 0.0) && valueToLog >= 0.0) {
            viewModelScope.launch {
                setMetricRepository.updateSetMetric(
                    setMetric = setMetric.copy(
                        value = valueToLog
                    )
                )

                // update the UI state to mark this metric as logged
                _recordSessionUiState.update { currentState ->
                    currentState.copy(
                        setMetricDisplayDetailMap = currentState.setMetricDisplayDetailMap.mapValues { (thisSetMetricId, setMetricDisplayDetail) ->
                            if (thisSetMetricId == setMetric.id) {
                                setMetricDisplayDetail.copy(
                                    inputIsLogged = true
                                )
                            } else {
                                setMetricDisplayDetail
                            }
                        }
                    )
                }
            }
        } else {
            // otherwise, reset the set metric's value back to -1.0
            viewModelScope.launch {
                setMetricRepository.updateSetMetric(
                    setMetric = setMetric.copy(
                        value = -1.0
                    )
                )
            }
        }
    }
}

/**
 * Ui State for RecordSessionScreen
 */
data class RecordSessionUiState(
    val activeProfile: Profile? = null,
    val activeSession: Session? = null,
    val sessionEditDialogVisible: Boolean = false,
    val newSessionName: String = "",
    val newSessionNote: String = "",
    val userIsAddingLift: Boolean = false,
    val inputLiftName: String = "",
    val liftSuggestionsList: List<LiftSearchDetail> = emptyList(),
    // Map(LiftId -> Map(LiftSet -> Pair(WeightMetric, Time/RepMetric)))
    val liftSetMap: Map<Int, Map<LiftSet, Pair<SetMetric, SetMetric>>> = emptyMap(),
    // Map(LiftId -> LiftSearchDetail)
    val liftDetailMap: Map<Int, LiftSearchDetail> = emptyMap(),
    // Map(SetMetricId -> SetMetricDisplayDetail)
    val setMetricDisplayDetailMap: Map<Int, SetMetricDisplayDetail> = emptyMap(),
    // Map(LiftSetId -> LiftSetCardVisible?)
    val liftSetVisibleMap: Map<Int, Boolean> = emptyMap(),
    // Map(LiftSetId -> number of completed sets for that lift)
    val setCountPerLiftMap: Map<Int, Int> = emptyMap(),
    // List(Pair(Muscle group name, number of sets))
    val setCountPerMuscleGroupList: List<LiftSetCountPerMuscleGroup> = emptyList(),
    val deleteLiftInProgressDialogVisible: Boolean = false,
    val liftIdToDelete: Int = -1,
    val editLiftSetDialogVisible: Boolean = false,
    val newLiftSetLabel: String = "",
    val newLiftSetNote: String = "",
    val liftSetToEdit: LiftSet? = null,
    val liftWithSetToEdit: Lift? = null,
    val editSetMetricNoteDialogVisible: Boolean = false,
    val newSetMetricNote: String = "",
    val setMetricToEdit: SetMetric? = null,
    val liftSetWithSetMetricToEdit: LiftSet? = null,
    val liftWithSetMetricToEdit: Lift? = null,
    val deleteLiftSetDialogVisible: Boolean = false,
    val liftSetToDelete: LiftSet? = null,
    val deleteButtonsEnabled: Boolean = true
)
