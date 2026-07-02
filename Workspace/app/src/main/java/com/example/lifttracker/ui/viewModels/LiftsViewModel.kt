package com.example.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifttracker.data.Lift
import com.example.lifttracker.data.LiftRepository
import com.example.lifttracker.data.MuscleGroup
import com.example.lifttracker.data.MuscleGroupRepository
import com.example.lifttracker.data.Profile
import com.example.lifttracker.data.ProfileRepository
import com.example.lifttracker.data.Unit
import com.example.lifttracker.data.UnitRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LiftsViewModel(
    private val liftRepository: LiftRepository,
    private val muscleGroupRepository: MuscleGroupRepository,
    private val unitRepository: UnitRepository
) : ViewModel() {
    private val _liftsUiState = MutableStateFlow(LiftsUiState())
    val liftsUiState: StateFlow<LiftsUiState> = _liftsUiState.asStateFlow()

    // this function is used in place of the init {} block.
    // terrible coding practice, I know
    fun initializeLiftList(muscleGroupId: Int) {
        // retrieve all lifts belonging to the given muscle group
        viewModelScope.launch {
            // infinite collection
            liftRepository.getAllLiftsFromMuscleGroupIdStream(muscleGroupId).collect { lifts ->
                _liftsUiState.update { currentState ->
                    currentState.copy(
                        liftList = lifts
                    )
                }
            }
        }

        // retrieve the muscle group object that this lift belongs to
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

        // retrieve all units for this specific profile
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
                newLiftMetricType = 1
            )
        }
    }

    fun dismissAddLiftDialog() {
        _liftsUiState.update { currentState ->
            currentState.copy(
                userIsAddingLift = false,
                newLiftName = "",
                newLiftNote = "",
                newLiftUnitName = "",
                newLiftMetricType = -1
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
                    muscle_group_id = _liftsUiState.value.muscleGroup!!.id,
                    unit_id = liftUnit!!.id,
                    name = _liftsUiState.value.newLiftName,
                    metric_type = _liftsUiState.value.newLiftMetricType,
                    note = _liftsUiState.value.newLiftNote
                )
            )

            dismissAddLiftDialog()
        }
    }
}

data class LiftsUiState(
    val liftList: List<Lift> = listOf(),
    val unitList: List<Unit> = listOf(),
    val muscleGroup: MuscleGroup? = null,
    val newLiftName: String = "",
    val newLiftNote: String = "",
    val newLiftMetricType: Int = 0,         // 1 = reps, 2 = time
    val newLiftUnitName: String = "",
    val userIsAddingLift: Boolean = false,
)