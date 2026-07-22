package github.tom2433.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.musclegroup.MuscleGroup
import github.tom2433.lifttracker.data.profile.Profile
import github.tom2433.lifttracker.data.profile.ProfileRepository
import github.tom2433.lifttracker.data.session.SessionRepository
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for SessionsScreen
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _sessionsUiState = MutableStateFlow(SessionsUiState())
    val sessionsUiState: StateFlow<SessionsUiState> = _sessionsUiState.asStateFlow()

    init {
        // constant collection to keep active profile up to date
        viewModelScope.launch {
            profileRepository.getActiveProfileStream().collect { activeProfile ->
                _sessionsUiState.update { currentState ->
                    currentState.copy(
                        activeProfile = activeProfile
                    )
                }
            }
        }

        // constant collection to fill the muscleGroupFrequencyMap for the given time period
        viewModelScope.launch {
            profileRepository.getActiveProfileStream().flatMapLatest { activeProfile ->
                if (activeProfile != null) {
                    sessionRepository.getMuscleGroupFrequencyListStream(
                        activeProfileId = activeProfile.id,
                        startDate = _sessionsUiState.value.startDate,
                        endDate = _sessionsUiState.value.endDate,
                        fetchLimit = _sessionsUiState.value.fetchLimit
                    )
                } else {
                    flowOf(emptyList())
                }
            }.collect { muscleGroupFrequencies ->
                _sessionsUiState.update { currentState ->
                    currentState.copy(
                        muscleGroupFrequencyList = muscleGroupFrequencies
                    )
                }
            }
        }
    }
}

/**
 * Ui State for SessionsScreen
 */
data class SessionsUiState(
    val activeProfile: Profile? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val fetchLimit: Int = 10,
    val muscleGroupFrequencyList: List<LiftSetCountPerMuscleGroup> = emptyList()
)
