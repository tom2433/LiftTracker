package github.tom2433.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.Lift
import github.tom2433.lifttracker.data.LiftRepository
import github.tom2433.lifttracker.data.MuscleGroup
import github.tom2433.lifttracker.data.MuscleGroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LiftScreenViewModel(
    private val liftId: Int,
    private val liftRepository: LiftRepository,
    private val muscleGroupRepository: MuscleGroupRepository
) : ViewModel() {
    private val _liftScreenUiState = MutableStateFlow(LiftScreenUiState())
    val liftScreenUiState: StateFlow<LiftScreenUiState> = _liftScreenUiState.asStateFlow()

    init {
        viewModelScope.launch {
            // retrieve the lift object that this id belongs to
            // infinite collection, this lift object will always reflect updates
            liftRepository.getLiftStream(liftId).collect { thisLift ->
                _liftScreenUiState.update { currentState ->
                    currentState.copy(
                        lift = thisLift
                    )
                }
            }

            // retrieve all muscle groups from active profile
            muscleGroupRepository.getAllMuscleGroupsForActiveProfileStream().collect { theseMuscleGroups ->
                _liftScreenUiState.update { currentState ->
                    currentState.copy(
                        muscleGroups = theseMuscleGroups
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
    val lift: Lift? = null,
    val muscleGroups: List<MuscleGroup> = listOf(),
    val threeDotMenuOpen: Boolean = false
)