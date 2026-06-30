package com.example.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifttracker.data.Lift
import com.example.lifttracker.data.LiftRepository
import com.example.lifttracker.data.MuscleGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LiftsViewModel(
    private val liftRepository: LiftRepository,
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
    }
}

data class LiftsUiState(
    val liftList: List<Lift> = listOf()
)