package github.tom2433.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.Lift
import github.tom2433.lifttracker.data.LiftRepository
import github.tom2433.lifttracker.data.MuscleGroup
import github.tom2433.lifttracker.data.MuscleGroupRepository
import github.tom2433.lifttracker.data.UnitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            unitRepository.getUnitStream(_liftScreenUiState.value.lift.unit_id).collect { thisUnit ->
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
}

data class LiftScreenUiState(
    val lift: Lift,
    val liftScreenDetail: LiftScreenDetail = LiftScreenDetail(
        metricType = "",
        unitName = ""
    ),
    val muscleGroup: MuscleGroup? = null,
    val muscleGroups: List<MuscleGroup> = listOf(),
    val threeDotMenuOpen: Boolean = false
)

data class LiftScreenDetail(
    val metricType: String,
    val unitName: String
)