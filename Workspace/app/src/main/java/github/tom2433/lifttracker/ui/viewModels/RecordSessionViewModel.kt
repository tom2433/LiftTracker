package github.tom2433.lifttracker.ui.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.lift.Lift
import github.tom2433.lifttracker.data.lift.LiftRepository
import github.tom2433.lifttracker.data.liftday.LiftDay
import github.tom2433.lifttracker.data.liftday.LiftDayRepository
import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.liftset.LiftSetRepository
import github.tom2433.lifttracker.data.profile.Profile
import github.tom2433.lifttracker.data.profile.ProfileRepository
import github.tom2433.lifttracker.data.setmetric.SetMetric
import github.tom2433.lifttracker.data.setmetric.SetMetricRepository
import github.tom2433.lifttracker.data.structures.LiftSearchDetail
import github.tom2433.lifttracker.data.structures.RecordLiftDetail
import github.tom2433.lifttracker.data.structures.RecordSessionLiftSetRow
import github.tom2433.lifttracker.data.structures.SetMetricDisplayDetail
import github.tom2433.lifttracker.data.utils.DateCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMap
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
    private val liftDayRepository: LiftDayRepository,
    private val liftRepository: LiftRepository,
    private val liftSetRepository: LiftSetRepository,
    private val setMetricRepository: SetMetricRepository
) : ViewModel() {
    private val _recordSessionUiState = MutableStateFlow(RecordSessionUiState())
    private var liftSuggestionsJob: Job? = null
    private var liftIdPendingReveal: Int? = null
    private var liftSetIdPendingReveal: Int? = null
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

        // constant collection for a running total of the number of lift sets completed for the day
        viewModelScope.launch {
            liftDayRepository.getActiveLiftDayForActiveProfileStream()
                .flatMapLatest { activeLiftDay ->
                    if (activeLiftDay == null) {
                        flowOf(0)
                    } else {
                        liftDayRepository.getNumOfLiftsForDay(activeLiftDay.id)
                    }
                }
                .collect { totalNumOfLiftSets ->
                    _recordSessionUiState.update { currentState ->
                        currentState.copy(
                            totalNumOfLiftSets = totalNumOfLiftSets
                        )
                    }
                }
        }

        // constant collection: fill the liftSetMap. liftSetMap will always be updated for the UI state
        // also fill the liftSetVisible map to indicate which LiftSet cards are visible
        viewModelScope.launch {
            liftDayRepository.getActiveLiftDayForActiveProfileStream()
                .flatMapLatest { activeLiftDay ->
                    if (activeLiftDay == null) {
                        flowOf(emptyList())
                    } else {
                        liftSetRepository.getRecordSessionLiftSetRowsForDayStream(activeLiftDay.id)
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
            liftDayRepository.getActiveLiftDayForActiveProfileStream()
                .flatMapLatest { activeLiftDay ->
                    if (activeLiftDay == null) {
                        flowOf(emptyList())
                    } else {
                        liftRepository.getLiftSearchDetailsForDayIdStream(activeLiftDay.id)
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
            liftDayRepository.getActiveLiftDayForActiveProfileStream()
                .flatMapLatest { activeLiftDay ->
                    if (activeLiftDay == null) {
                        flowOf(emptyList())
                    } else {
                        setMetricRepository.getSetMetricIdsFromDayIdStream(activeLiftDay.id)
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
    }

    private fun revealPendingLiftSetIfReady() {
        val pendingLiftSetId = liftSetIdPendingReveal ?: return

        var pendingLiftSetIsReady: Boolean = false

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
                inputIsValid = previousSetMetricDisplayDetailMap[thisSetMetricId]?.inputIsValid ?: false,
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
    }

    fun endSession() {
        val currentDay: LiftDay = _recordSessionUiState.value.activeLiftDay ?: return

        // delete the lift day. activeLiftDay should update automatically
        viewModelScope.launch {
            liftDayRepository.deleteLiftDay(currentDay)
        }
    }

    fun saveSession() {
        // retrieve all lift set objects where its set metrics are both the default -1.0
        val defaultLiftSets: List<LiftSet> =
            _recordSessionUiState.value.liftSetMap
                .values
                .flatMap { setMap -> setMap.entries }
                .filter { (_, setMetrics) ->
                    val (weightMetric, secondMetric) = setMetrics

                    weightMetric.value == -1.0 || secondMetric.value == -1.0
                }
                .map { (liftSet, _) -> liftSet }

        // retrieve all valid lift set objects where their set metrics are both not the default -1.0
        val validLiftSets: List<LiftSet> =
            _recordSessionUiState.value.liftSetMap
                .values
                .flatMap { setMap -> setMap.entries }
                .filter { (_, setMetrics) ->
                    val (weightMetric, secondMetric) = setMetrics

                    weightMetric.value != -1.0 && secondMetric.value != -1.0
                }
                .map { (liftSet, _) -> liftSet }

        viewModelScope.launch {
            // if default set metrics are still remaining, delete their LiftSets
            if (defaultLiftSets.isNotEmpty()) {
                for (liftSet in defaultLiftSets.sortedByDescending { it.day_set_number }) {
                    liftSetRepository.deleteLiftSet(liftSet)
                }
            }

            val activeLiftDay: LiftDay = _recordSessionUiState.value.activeLiftDay ?: return@launch

            // if there are no remaining valid lift sets, delete this day
            if (validLiftSets.isEmpty()) {
                liftDayRepository.deleteLiftDay(activeLiftDay)
            } else {
                // otherwise, save this lift day by setting in progress = false
                liftDayRepository.updateLiftDay(
                    liftDay = activeLiftDay.copy(
                        in_progress = false
                    )
                )
            }
        }
    }

    fun finishSession() {
        if (_recordSessionUiState.value.totalNumOfLiftSets == 0) {
            endSession()
        } else {
            saveSession()
        }
    }

    fun showDayEditDialog() {
        val currentActiveDay: LiftDay = _recordSessionUiState.value.activeLiftDay ?: return

        _recordSessionUiState.update { currentState ->
            currentState.copy(
                dayEditDialogVisible = true,
                newDayName = currentActiveDay.day_label,
                newDayNote = currentActiveDay.note
            )
        }
    }

    fun dismissDayEditDialog() {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                dayEditDialogVisible = false,
                newDayName = "",
                newDayNote = ""
            )
        }
    }

    fun validateDayInput(): Boolean {
        return (_recordSessionUiState.value.newDayName.isNotBlank())
    }

    fun updateNewDayName(newDayName: String) {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                newDayName = newDayName
            )
        }
    }

    fun updateNewDayNote(newDayNote: String) {
        _recordSessionUiState.update { currentState ->
            currentState.copy(
                newDayNote = newDayNote
            )
        }
    }

    fun updateDayNameAndNote() {
        if (!validateDayInput()) {
            return
        }

        viewModelScope.launch {
            val currentActiveDay: LiftDay = _recordSessionUiState.value.activeLiftDay ?: return@launch

            // update day name and note in database
            liftDayRepository.updateLiftDay(
                liftDay = currentActiveDay.copy(
                    day_label = _recordSessionUiState.value.newDayName,
                    note = _recordSessionUiState.value.newDayNote
                )
            )

            // dismiss day edit dialog
            dismissDayEditDialog()
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

        val activeDay: LiftDay =
            _recordSessionUiState.value.activeLiftDay ?: return

        // swipe away existingLiftEntryCard since selected lift has been found
        cancelAddLift()

        liftIdPendingReveal = selectedLift.liftObj.id

        // insert new lift set for active day and selected lift
        // this will automatically create two lift set metrics
        // UI will also update since liftSetMap will recognize the addition
        viewModelScope.launch {
            liftSetRepository.insertLiftSet(
                liftDayId = activeDay.id,
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
                liftSet.day_set_number
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
                    }
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
                    liftIdToDelete = -1
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

    fun validateSetMetricNote(): Boolean {
        return _recordSessionUiState.value.newSetMetricNote.isNotBlank()
    }

    fun updateSetMetric() {
        if (!validateSetMetricNote()) {
            return
        }

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
            val activeLiftDayId = _recordSessionUiState.value.activeLiftDay?.id ?: return@launch

            val newLiftSetId = liftSetRepository.insertLiftSet(
                liftDayId = activeLiftDayId,
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
                    }
                )
            }

            dismissDeleteLiftSetDialog()

            // wait for the lift set card to swipe away
            delay(1000)

            // delete the liftSet
            liftSetRepository.deleteLiftSet(
                liftSet = liftSetToDelete
            )
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
        if (valueToLog != null) {
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
    val activeLiftDay: LiftDay? = null,
    val totalNumOfLiftSets: Int = 0,
    val dayEditDialogVisible: Boolean = false,
    val newDayName: String = "",
    val newDayNote: String = "",
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
    val liftSetToDelete: LiftSet? = null
)
