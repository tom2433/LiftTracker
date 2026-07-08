package github.tom2433.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.Lift
import github.tom2433.lifttracker.data.LiftRepository
import github.tom2433.lifttracker.data.MuscleGroup
import github.tom2433.lifttracker.data.MuscleGroupRepository
import github.tom2433.lifttracker.data.Unit
import github.tom2433.lifttracker.data.UnitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class LiftScreenViewModel(
    private val lift: Lift,
    private val liftRepository: LiftRepository,
    private val muscleGroupRepository: MuscleGroupRepository,
    private val unitRepository: UnitRepository
) : ViewModel() {
    private val _liftScreenUiState = MutableStateFlow(LiftScreenUiState(lift = lift))
    val liftScreenUiState: StateFlow<LiftScreenUiState> = _liftScreenUiState.asStateFlow()

    init {
        viewModelScope.launch {
            // retrieve the lift object that this id belongs to
            // infinite collection so that this lift object will always reflect updates
            liftRepository.getLiftStream(lift.id).collect { thisLift ->
                _liftScreenUiState.update { currentState ->
                    val newLift: Lift = thisLift ?: currentState.lift

                    currentState.copy(
                        lift = newLift,
                        liftScreenDetail = currentState.liftScreenDetail.copy(
                            metricType = if (newLift.metric_type == 1) {
                                "reps"
                            } else {
                                "time"
                            }
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            // get unit name for lift.
            // retrieving the unit name will depend on the current lift using flatMapLatest {}
            liftRepository.getLiftStream(lift.id)
                .filterNotNull()
                .flatMapLatest { updatedLift ->
                    unitRepository.getUnitStream(updatedLift.unit_id)
                }
                .collect { thisUnit ->
                    _liftScreenUiState.update { currentState ->
                        currentState.copy(
                            liftScreenDetail = currentState.liftScreenDetail.copy(
                                unitName = thisUnit?.name ?: "null"
                            )
                        )
                    }
                }
        }

        viewModelScope.launch {
            // retrieve all units
            unitRepository.getAllUnitsStream().collect { theseUnits ->
                _liftScreenUiState.update { currentState ->
                    currentState.copy(
                        unitList = theseUnits
                    )
                }
            }
        }

        viewModelScope.launch {
            // retrieve all muscle groups from active profile
            muscleGroupRepository.getAllMuscleGroupsForActiveProfileStream()
                .collect { theseMuscleGroups ->
                    _liftScreenUiState.update { currentState ->
                        currentState.copy(
                            muscleGroups = theseMuscleGroups
                        )
                    }
                }
        }

        viewModelScope.launch {
            // retrieve the one muscle group that this lift belongs to
            // infinite collection, this muscle group object will always reflect updates
            // in the event that the user moves this lift to a different muscle group
            muscleGroupRepository.getMuscleGroupFromLiftIdStream(lift.id)
                .collect { thisMuscleGroup ->
                    _liftScreenUiState.update { currentState ->
                        currentState.copy(
                            muscleGroup = thisMuscleGroup
                        )
                    }
                }
        }
    }

    fun openThreeDotMenu() {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                threeDotMenuOpen = true
            )
        }
    }

    fun closeThreeDotMenu() {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                threeDotMenuOpen = false
            )
        }
    }

    fun openLiftEditDialog() {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                userIsEditingLift = true,
                newLiftName = currentState.lift.name,
                newLiftNote = currentState.lift.note,
                newLiftMetricType = currentState.lift.metric_type,
                newLiftUnitName = currentState.liftScreenDetail.unitName
            )
        }
    }

    fun closeLiftEditDialog() {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                userIsEditingLift = false,
                newLiftName = "",
                newLiftNote = "",
                newLiftMetricType = -1,
                newLiftUnitName = ""
            )
        }
    }

    fun validateLift(): Boolean {
        return (_liftScreenUiState.value.newLiftName.isNotBlank()
                && _liftScreenUiState.value.newLiftUnitName.isNotBlank())
    }

    fun updateLift() {
        // check that the lift entry is valid
        if (!validateLift()) {
            return
        }

        viewModelScope.launch {
            // determine if the inputted unit exists
            var liftUnit: Unit? = null
            for (currentLiftUnit in _liftScreenUiState.value.unitList) {
                if (currentLiftUnit.name == _liftScreenUiState.value.newLiftUnitName) {
                    liftUnit = currentLiftUnit
                }
            }

            // if it doesn't exist, create it
            if (liftUnit == null) {
                unitRepository.insertUnit(
                    unit = Unit(
                        name = _liftScreenUiState.value.newLiftUnitName
                    )
                )

                liftUnit = unitRepository.getUnitFromNameStream(_liftScreenUiState.value.newLiftUnitName).firstOrNull()
            }

            // now update the lift in the lift table
            liftRepository.updateLift(
                lift = _liftScreenUiState.value.lift.copy(
                    unit_id = liftUnit?.id ?: return@launch,
                    name = _liftScreenUiState.value.newLiftName,
                    metric_type = _liftScreenUiState.value.newLiftMetricType,
                    note = _liftScreenUiState.value.newLiftNote
                )
            )

            // dismiss the dialog
            closeLiftEditDialog()
        }
    }

    fun updateLiftName(newName: String) {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                newLiftName = newName
            )
        }
    }

    fun updateLiftNote(newNote: String) {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                newLiftNote = newNote
            )
        }
    }

    fun selectReps() {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                newLiftMetricType = 1
            )
        }
    }

    fun selectTime() {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                newLiftMetricType = 2
            )
        }
    }

    fun updateUnit(newUnitName: String) {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                newLiftUnitName = newUnitName
            )
        }
    }
}

data class LiftScreenUiState(
    val lift: Lift,
    val liftScreenDetail: LiftScreenDetail = LiftScreenDetail(
        metricType = "",
        unitName = ""
    ),
    val muscleGroup: MuscleGroup? = null,
    val muscleGroups: List<MuscleGroup> = listOf(),
    val threeDotMenuOpen: Boolean = false,
    val userIsEditingLift: Boolean = false,
    val newLiftName: String = "",
    val newLiftNote: String = "",
    val newLiftMetricType: Int = -1,
    val newLiftUnitName: String = "",
    val unitList: List<Unit> = listOf()
)

data class LiftScreenDetail(
    val metricType: String,
    val unitName: String
)