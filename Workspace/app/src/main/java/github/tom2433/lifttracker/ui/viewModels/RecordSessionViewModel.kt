package github.tom2433.lifttracker.ui.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.lift.LiftRepository
import github.tom2433.lifttracker.data.liftday.LiftDay
import github.tom2433.lifttracker.data.liftday.LiftDayRepository
import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.liftset.LiftSetRepository
import github.tom2433.lifttracker.data.profile.Profile
import github.tom2433.lifttracker.data.profile.ProfileRepository
import github.tom2433.lifttracker.data.setmetric.SetMetric
import github.tom2433.lifttracker.data.structures.LiftSearchDetail
import github.tom2433.lifttracker.data.structures.RecordLiftDetail
import github.tom2433.lifttracker.data.structures.RecordSessionLiftSetRow
import github.tom2433.lifttracker.data.utils.DateCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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
    private val liftSetRepository: LiftSetRepository
) : ViewModel() {
    private val _recordSessionUiState = MutableStateFlow(RecordSessionUiState())
    private var liftSuggestionsJob: Job? = null
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
        viewModelScope.launch {
            liftDayRepository.getActiveLiftDayForActiveProfileStream()
                .flatMapLatest { activeLiftDay ->
                    if (activeLiftDay == null) {
                        flowOf(emptyList())
                    } else {
                        liftSetRepository.getRecordSessionLiftSetRowsForDayStream(activeLiftDay.id)
                    }
                }
                .collect { rows ->
                    _recordSessionUiState.update { currentState ->
                        currentState.copy(
                            liftSetMap = rows.toLiftSetMap()
                        )
                    }
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
                }
        }
    }

    private fun List<LiftSearchDetail>.toLiftDetailMap(
        previousLiftDetailMap: Map<Int, LiftSearchDetail>
    ): Map<Int, LiftSearchDetail> {
        return associate { detail ->
            val liftId = detail.liftObj.id
            val previousSelected = previousLiftDetailMap[liftId]?.selected

            liftId to detail.copy(
                selected = previousSelected ?: true
            )
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

        // if this lift day does not have any set data, delete it
        if (_recordSessionUiState.value.totalNumOfLiftSets == 0) {
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
        if (_recordSessionUiState.value.totalNumOfLiftSets == 0) {
            endSession()
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
                    val lastIndex = liftDetails.lastIndex

                    _recordSessionUiState.update { currentState ->
                        currentState.copy(
                            liftSuggestionsList = liftDetails.mapIndexed { index, liftDetail ->
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
        // TODO
        // this should swipe away the ExistingLiftEntryCard and replace it with a
        // lift in progress card (expandable to show lift sets)

        // the way to do this is to just immediately create a new lift set in the
        // database. that is all that will happen in this function with the
        // exception of cancelling the lift entry card.

        // the liftSetMap flow will handle all of the UI updates.

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
    val liftSetMap: Map<Int, Map<LiftSet, Pair<SetMetric, SetMetric>>> = emptyMap(),
    val liftDetailMap: Map<Int, LiftSearchDetail> = emptyMap()
)
