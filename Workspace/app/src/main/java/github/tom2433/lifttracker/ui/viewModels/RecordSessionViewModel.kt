package github.tom2433.lifttracker.ui.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.lift.LiftRepository
import github.tom2433.lifttracker.data.liftday.LiftDay
import github.tom2433.lifttracker.data.liftday.LiftDayRepository
import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.profile.Profile
import github.tom2433.lifttracker.data.profile.ProfileRepository
import github.tom2433.lifttracker.data.setmetric.SetMetric
import github.tom2433.lifttracker.data.structures.LiftSearchDetail
import github.tom2433.lifttracker.data.utils.DateCalculator
import kotlinx.coroutines.Job
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
    private val liftDayRepository: LiftDayRepository,
    private val liftRepository: LiftRepository
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

        // I need to ensure that when a lift set is inserted into the database,
        // the lift set number is calculated as the set number for the lift,
        // the day set number is calculated as the set number for the day,
        // and the set label is created with the lift set number. (done)

        // I also need to ensure that when a lift set is deleted,
        // the lift set numbers of all other sets in this lift are adjusted appropriately,
        // the day set numbers of all other sets in this day are adjusted appropriately,
        // and the set labels of all other sets in this lift are adjusted appropriately
        // IF they are of the pattern "Set n" (done)
    }
}

/**
 * Ui State for RecordSessionScreen
 */
data class RecordSessionUiState(
    val activeProfile: Profile? = null,
    val activeLiftDay: LiftDay? = null,
    val totalNumOfLifts: Int = 0,
    val dayEditDialogVisible: Boolean = false,
    val newDayName: String = "",
    val newDayNote: String = "",
    val userIsAddingLift: Boolean = false,
    val inputLiftName: String = "",
    val liftSuggestionsList: List<LiftSearchDetail> = emptyList(),
    val liftSetMap: Map<LiftSearchDetail, Map<LiftSet, Pair<SetMetric, SetMetric>>> = emptyMap() // TODO: populate this through a flow
)
