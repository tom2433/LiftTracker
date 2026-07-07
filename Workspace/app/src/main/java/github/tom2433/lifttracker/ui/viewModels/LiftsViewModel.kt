package github.tom2433.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.Lift
import github.tom2433.lifttracker.data.LiftRepository
import github.tom2433.lifttracker.data.MuscleGroup
import github.tom2433.lifttracker.data.MuscleGroupRepository
import github.tom2433.lifttracker.data.Unit
import github.tom2433.lifttracker.data.UnitRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LiftsViewModel(
    private val muscleGroupId: Int,
    private val liftRepository: LiftRepository,
    private val muscleGroupRepository: MuscleGroupRepository,
    private val unitRepository: UnitRepository
) : ViewModel() {
    private val _liftsUiState = MutableStateFlow(LiftsUiState())
    val liftsUiState: StateFlow<LiftsUiState> = _liftsUiState.asStateFlow()

    // this function is used in place of the init {} block.
    // terrible coding practice, I know
    init {
        // retrieve all lifts belonging to the given muscle group
        viewModelScope.launch {
            // infinite collection
            liftRepository.getAllLiftsFromMuscleGroupIdStream(muscleGroupId).collect { lifts ->
                _liftsUiState.update { currentState ->
                    val updatedLiftMap = lifts.associate { lift ->
                        val previousDetail = currentState.liftMap[lift.id]

                        lift.id to LiftDetail(
                            liftObj = lift,
                            threeDotMenuOpen = previousDetail?.threeDotMenuOpen ?: false
                        )
                    }

                    currentState.copy(
                        liftMap = updatedLiftMap
                    )
                }
            }
        }

        // retrieve the muscle group object that these lifts belongs to
        viewModelScope.launch {
            // retrieve muscle group object if it exists
            val muscleGroup: MuscleGroup? = muscleGroupRepository.getMuscleGroupStream(muscleGroupId).firstOrNull()

            // add to Ui state
            _liftsUiState.update { currentState ->
                currentState.copy(
                    muscleGroup = muscleGroup
                )
            }
        }

        // retrieve all units
        viewModelScope.launch {
            // infinite collection for units
            unitRepository.getAllUnitsStream().collect { units ->
                _liftsUiState.update { currentState ->
                    currentState.copy(
                        unitList = units
                    )
                }
            }
        }
    }

    // validate new lift: check if the name is blank
    fun validateLift(): Boolean {
        return (_liftsUiState.value.newLiftName.isNotBlank()
                && _liftsUiState.value.newLiftUnitName.isNotBlank())
    }

    fun updateLiftName(newName: String) {
        _liftsUiState.update { currentState ->
            currentState.copy(
                newLiftName = newName
            )
        }
    }

    fun updateLiftNote(newNote: String) {
        _liftsUiState.update { currentState ->
            currentState.copy(
                newLiftNote = newNote
            )
        }
    }

    fun showAddLiftDialog() {
        _liftsUiState.update { currentState ->
            currentState.copy(
                userIsAddingLift = true,
                newLiftMetricType = 1,
                newLiftUnitName = "",
                newLiftNote = "",
                newLiftName = ""
            )
        }
    }

    fun selectReps() {
        _liftsUiState.update { currentState ->
            currentState.copy(
                newLiftMetricType = 1
            )
        }
    }

    fun updateUnit(newUnitName: String) {
        _liftsUiState.update { currentState ->
            currentState.copy(
                newLiftUnitName = newUnitName
            )
        }
    }

    fun selectTime() {
        _liftsUiState.update { currentState ->
            currentState.copy(
                newLiftMetricType = 2
            )
        }
    }

    fun threeDotMenuClicked(id: Int) {
        _liftsUiState.update { currentState ->
            if (id !in currentState.liftMap) {
                return@update currentState
            }

            currentState.copy(
                liftMap = currentState.liftMap.mapValues { (liftId, liftDetail) ->
                    liftDetail.copy(
                        threeDotMenuOpen = if (liftId == id) {
                            !liftDetail.threeDotMenuOpen
                        } else {
                            false
                        }
                    )
                }
            )
        }
    }

    fun dismissLiftEntryDialog() {
        _liftsUiState.update { currentState ->
            currentState.copy(
                userIsAddingLift = false,
                userIsEditingLift = false,
                newLiftName = "",
                newLiftNote = "",
                newLiftUnitName = "",
                newLiftMetricType = -1
            )
        }
    }

    fun showEditLiftDialog(id: Int) {
        viewModelScope.launch {
            // retrieve lift to update
            val liftToUpdate: Lift = _liftsUiState.value.liftMap[id]?.liftObj ?: return@launch

            // retrieve the unit object associated with this lift
            val unitObj: Unit =
                unitRepository.getUnitStream(liftToUpdate.unit_id).firstOrNull() ?: return@launch

            // update UI state to show lift entry dialog for this specific lift
            _liftsUiState.update { currentState ->
                currentState.copy(
                    userIsEditingLift = true,
                    liftToEdit = liftToUpdate,
                    newLiftName = liftToUpdate.name,
                    newLiftNote = liftToUpdate.note,
                    newLiftMetricType = liftToUpdate.metric_type,
                    newLiftUnitName = unitObj.name
                )
            }

            // dismiss the 3 dot menu
            threeDotMenuClicked(id)
        }
    }

    fun addLift() {
        if (!validateLift()) {
            return
        }

        viewModelScope.launch {
            // determine if the inputted unit exists
            var liftUnit: Unit? = null
            for (currentLiftUnit in _liftsUiState.value.unitList) {
                if (currentLiftUnit.name == _liftsUiState.value.newLiftUnitName) {
                    liftUnit = currentLiftUnit
                }
            }

            // if it doesn't exist, create it.
            if (liftUnit == null) {
                unitRepository.insertUnit(
                    unit = Unit(
                        name = _liftsUiState.value.newLiftUnitName
                    )
                )

                // wait for unit to be added
                delay(100)

                liftUnit = unitRepository.getUnitFromNameStream(_liftsUiState.value.newLiftUnitName).firstOrNull()
            }

            // now insert the new lift from the user's inputs
            liftRepository.insertLift(
                Lift(
                    muscle_group_id = muscleGroupId,
                    unit_id = liftUnit?.id ?: return@launch,
                    name = _liftsUiState.value.newLiftName,
                    metric_type = _liftsUiState.value.newLiftMetricType,
                    note = _liftsUiState.value.newLiftNote
                )
            )

            dismissLiftEntryDialog()
        }
    }

    fun updateLift() {
        // check that lift entry is valid
        if (!validateLift()) {
            return
        }

        viewModelScope.launch {
            // determine if the inputted unit exists
            var liftUnit: Unit? = null
            for (currentLiftUnit in _liftsUiState.value.unitList) {
                if (currentLiftUnit.name == _liftsUiState.value.newLiftUnitName) {
                    liftUnit = currentLiftUnit
                }
            }

            // if it doesn't exist, create it.
            if (liftUnit == null) {
                unitRepository.insertUnit(
                    unit = Unit(
                        name = _liftsUiState.value.newLiftUnitName
                    )
                )

                // wait for unit to be added
                delay(100)

                liftUnit = unitRepository.getUnitFromNameStream(_liftsUiState.value.newLiftUnitName).firstOrNull()
            }

            // now update the lift in the lift table
            liftRepository.updateLift(
                lift = Lift(
                    id = _liftsUiState.value.liftToEdit?.id ?: return@launch,
                    muscle_group_id = muscleGroupId,
                    unit_id = liftUnit?.id ?: return@launch,
                    name = _liftsUiState.value.newLiftName,
                    metric_type = _liftsUiState.value.newLiftMetricType,
                    note = _liftsUiState.value.newLiftNote
                )
            )

            // dismiss the dialog
            dismissLiftEntryDialog()
        }
    }

    fun showDeleteLiftDialog(id: Int) {
        _liftsUiState.update { currentState ->
            if (id !in currentState.liftMap) {
                return@update currentState
            }

            currentState.copy(
                userIsDeletingLift = true,
                liftToDelete = currentState.liftMap[id]!!.liftObj
            )
        }
    }

    fun dismissDeleteLiftDialog() {
        _liftsUiState.update { currentState ->
            currentState.copy(
                userIsDeletingLift = false,
                liftToDelete = null
            )
        }
    }

    fun deleteLift() {
        viewModelScope.launch {
            // delete lift from table
            liftRepository.deleteLift(
                lift = _liftsUiState.value.liftToDelete ?: return@launch
            )

            // dismiss the delete lift dialog
            dismissDeleteLiftDialog()
        }
    }
}

data class LiftsUiState(
    val liftMap: Map<Int, LiftDetail> = emptyMap(),
    val unitList: List<Unit> = listOf(),
    val muscleGroup: MuscleGroup? = null,
    val newLiftName: String = "",
    val newLiftNote: String = "",
    val newLiftMetricType: Int = 0,         // 1 = reps, 2 = time
    val newLiftUnitName: String = "",
    val userIsAddingLift: Boolean = false,
    val userIsEditingLift: Boolean = false,
    val userIsDeletingLift: Boolean = false,
    val liftToEdit: Lift? = null,
    val liftToDelete: Lift? = null
)

data class LiftDetail(
    val liftObj: Lift,
    val threeDotMenuOpen: Boolean = false,
)
