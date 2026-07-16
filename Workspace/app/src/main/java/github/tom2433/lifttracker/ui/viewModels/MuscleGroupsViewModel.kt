package github.tom2433.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.lift.Lift
import github.tom2433.lifttracker.data.musclegroup.MuscleGroup
import github.tom2433.lifttracker.data.structures.MuscleGroupDetailData
import github.tom2433.lifttracker.data.musclegroup.MuscleGroupRepository
import github.tom2433.lifttracker.data.profile.Profile
import github.tom2433.lifttracker.data.profile.ProfileRepository
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve all profiles in the Room database
 */
class MuscleGroupsViewModel(
    private val profileRepository: ProfileRepository,
    private val muscleGroupRepository: MuscleGroupRepository
) : ViewModel() {
    private val _muscleGroupsUiState = MutableStateFlow(MuscleGroupsUiState())
    val muscleGroupsUiState: StateFlow<MuscleGroupsUiState> = _muscleGroupsUiState.asStateFlow()

    init {
        viewModelScope.launch {
            // retrieve all profiles. this is an infinite collection
            profileRepository.getAllProfilesStream().collect { profiles ->
                _muscleGroupsUiState.update { currentState ->
                    currentState.copy(
                        profileList = profiles,
                        welcomeDialogVisible = profiles.isEmpty()
                    )
                }
            }
        }

        viewModelScope.launch {
            // Collect the aggregate rows as a Flow so Room recalculates them whenever any referenced table changes. - Codex
            muscleGroupRepository.getAllMuscleGroupDetailDataForActiveProfileStream().collect { detailData ->
                // Capture today once per database emission so every card uses the same rolling-week boundary. - Codex
                val today = DateTimeCalculator.getCurrentIsoDate()

                // Publish the newly calculated immutable list so Compose can react to the database change. - Codex
                _muscleGroupsUiState.update { currentState ->
                    // Key each calculated detail by its muscle-groups table ID while preserving the query order. - Codex
                    val muscleGroupDetails = detailData.associate { data ->
                        // preserve card is open status across UiState updates
                        val oldDetail = currentState.muscleGroupList[data.id]

                        data.id to createMuscleGroupDetail(data, today).copy(
                            cardIsOpen = oldDetail?.cardIsOpen ?: false,
                            menuIsOpen = oldDetail?.menuIsOpen ?: false
                        )
                    }

                    currentState.copy(
                        muscleGroupList = muscleGroupDetails
                    )
                }
            }
        }
    }

    fun isValidProfileName(): Boolean {
        return _muscleGroupsUiState.value.newProfileName.isNotBlank()
    }

    fun updateNewProfileName(newName: String) {
        _muscleGroupsUiState.update { currentState ->
            currentState.copy(
                newProfileName = newName
            )
        }
    }

    fun updateNewProfileNote(newNote: String) {
        _muscleGroupsUiState.update { currentState ->
            currentState.copy(
                newProfileNote = newNote
            )
        }
    }

    fun createProfile() {
        if (isValidProfileName()) {
            viewModelScope.launch {
                // insert new profile into database
                profileRepository.insertProfile(
                    Profile(
                        name = _muscleGroupsUiState.value.newProfileName,
                        active = true,
                        note = _muscleGroupsUiState.value.newProfileNote
                    )
                )

                // update state and close welcome dialog
                _muscleGroupsUiState.update { currentState ->
                    currentState.copy(
                        welcomeDialogVisible = false,
                        newProfileName = "",
                        newProfileNote = ""
                    )
                }
            }
        }
    }

    fun getActiveProfile(): Profile? {
        for (profile in _muscleGroupsUiState.value.profileList) {
            if (profile.active) {
                return profile
            }
        }
        return null
    }

    // This converts one Room aggregate row into the complete detail model required by the screen. - Codex
    private fun createMuscleGroupDetail(
        data: MuscleGroupDetailData,
        today: String
    ): MuscleGroupDetail {
        // The rolling averages include the current seven-day bucket and every bucket back through the first session. - Codex
        val numWeeks = DateTimeCalculator.calculateNumWeeks(data.firstDateTrained, today)

        // A muscle group without sessions has no per-session divisor, so its average is defined as zero. - Codex
        val avgNumSetsPerSession = if (data.numSessions == 0) {
            0.0
        } else {
            data.numSets.toDouble() / data.numSessions.toDouble()
        }

        // A value of -1.0 identifies a non-empty muscle group whose lifts are all measured in time. - Codex
        val avgNumRepsPerSet = if (data.numLifts > 0 && data.numRepLifts == 0) {
            -1.0
        } else {
            // Rep-capable muscle groups without recorded rep metrics receive a neutral zero average. - Codex
            data.avgNumRepsPerSet ?: 0.0
        }

        // Build the display model while preserving the stored name and note exactly as Room returned them. - Codex
        return MuscleGroupDetail(
            name = data.name,
            note = data.note,
            numLifts = data.numLifts,
            numSessions = data.numSessions,
            avgNumSessionsPerWeek = data.numSessions.toDouble() / numWeeks,
            avgNumSetsPerSession = avgNumSetsPerSession,
            avgNumSetsPerWeek = data.numSets.toDouble() / numWeeks,
            avgNumRepsPerSet = avgNumRepsPerSet,
            lastDateTrained = DateTimeCalculator.formatLastDateTrained(data.lastDateTrained, today)
        )
    }

    fun muscleGroupCardClicked(id: Int) {
        _muscleGroupsUiState.update { currentState ->
            if (id !in currentState.muscleGroupList) {
                return@update currentState
            }

            currentState.copy(
                muscleGroupList = currentState.muscleGroupList.mapValues { (muscleGroupId, muscleGroupDetail) ->
                    muscleGroupDetail.copy(
                        cardIsOpen = if (muscleGroupId == id) {
                            !muscleGroupDetail.cardIsOpen
                        } else {
                            false
                        }
                    )
                }
            )
        }
    }

    fun openThreeDotMenu(id: Int) {
        _muscleGroupsUiState.update { currentState ->
            if (id !in currentState.muscleGroupList) {
                return@update currentState
            }

            currentState.copy(
                muscleGroupList = currentState.muscleGroupList.mapValues { (muscleGroupId, muscleGroupDetail) ->
                    muscleGroupDetail.copy(
                        menuIsOpen = muscleGroupId == id
                    )
                }
            )
        }
    }

    fun closeThreeDotMenu(id: Int) {
        _muscleGroupsUiState.update { currentState ->
            if (id !in currentState.muscleGroupList) {
                return@update currentState
            }

            currentState.copy(
                muscleGroupList = currentState.muscleGroupList.mapValues { (muscleGroupId, muscleGroupDetail) ->
                    muscleGroupDetail.copy(
                        menuIsOpen = false
                    )
                }
            )
        }
    }

    fun showEditMuscleGroupDialog(id: Int) {
        viewModelScope.launch {
            val muscleGroupToEdit = muscleGroupRepository.getMuscleGroupStream(id).firstOrNull()

            _muscleGroupsUiState.update { currentState ->
                if (id !in currentState.muscleGroupList || muscleGroupToEdit == null) {
                    return@update currentState
                }

                currentState.copy(
                    muscleGroupToEdit = muscleGroupToEdit,
                    muscleGroupEditDialogVisible = true
                )
            }
        }
    }

    fun showDeleteMuscleGroupDialog(id: Int) {
        viewModelScope.launch {
            val muscleGroupToDelete = muscleGroupRepository.getMuscleGroupStream(id).firstOrNull()

            _muscleGroupsUiState.update { currentState ->
                if (id !in currentState.muscleGroupList || muscleGroupToDelete == null) {
                    return@update currentState
                }

                currentState.copy(
                    muscleGroupToDelete = muscleGroupToDelete,
                    muscleGroupDeleteDialogVisible = true
                )
            }
        }
    }

    fun validateMuscleGroup(): Boolean {
        return _muscleGroupsUiState.value.muscleGroupToEdit?.name?.isNotBlank() ?: false
    }

    fun updateMuscleGroupName(newName: String) {
        _muscleGroupsUiState.update { currentState ->
            currentState.copy(
                muscleGroupToEdit = currentState.muscleGroupToEdit?.copy(
                    name = newName
                )
            )
        }
    }

    fun updateMuscleGroupNote(newNote: String) {
        _muscleGroupsUiState.update { currentState ->
            currentState.copy(
                muscleGroupToEdit = currentState.muscleGroupToEdit?.copy(
                    note = newNote
                )
            )
        }
    }

    fun dismissEditMuscleGroupDialog() {
        _muscleGroupsUiState.update { currentState ->
            currentState.copy(
                muscleGroupToEdit = null,
                muscleGroupEditDialogVisible = false
            )
        }
    }

    fun dismissDeleteMuscleGroupDialog() {
        _muscleGroupsUiState.update { currentState ->
            currentState.copy(
                muscleGroupToDelete = null,
                muscleGroupDeleteDialogVisible = false
            )
        }
    }

    fun updateMuscleGroup() {
        if (validateMuscleGroup() && _muscleGroupsUiState.value.muscleGroupToEdit != null) {
            viewModelScope.launch {
                muscleGroupRepository.updateMuscleGroup(_muscleGroupsUiState.value.muscleGroupToEdit!!)
                dismissEditMuscleGroupDialog()
            }
        }
    }

    fun deleteMuscleGroup() {
        if (_muscleGroupsUiState.value.muscleGroupToDelete != null) {
            viewModelScope.launch {
                // update state to tell screen to do swipe animation
                _muscleGroupsUiState.update { currentState ->
                    currentState.copy(
                        muscleGroupIdToDelete = currentState.muscleGroupToDelete!!.id
                    )
                }

                // delay for swipe animation
                delay(300)

                muscleGroupRepository.deleteMuscleGroup(_muscleGroupsUiState.value.muscleGroupToDelete!!)
                dismissDeleteMuscleGroupDialog()
            }
        }
    }

    fun openLiftScreen(lift: Lift) {
        _muscleGroupsUiState.update { currentState ->
            currentState.copy(
                liftScreenLiftObj = lift
            )
        }
    }

    fun dismissLiftScreen() {
        _muscleGroupsUiState.update { currentState ->
            currentState.copy(
                liftScreenLiftObj = null
            )
        }
    }
}

/**
 * Ui State for MuscleGroupsScreen
 */
data class MuscleGroupsUiState(
    val profileList: List<Profile> = listOf(),
    val muscleGroupList: Map<Int, MuscleGroupDetail> = emptyMap(),
    val welcomeDialogVisible: Boolean = false,
    val newProfileName: String = "",
    val newProfileNote: String = "",
    val muscleGroupEditDialogVisible: Boolean = false,
    val muscleGroupDeleteDialogVisible: Boolean = false,
    val muscleGroupToEdit: MuscleGroup? = null,
    val muscleGroupToDelete: MuscleGroup? = null,
    val muscleGroupIdToDelete: Int = -1,
    val liftScreenLiftObj: Lift? = null
)

data class MuscleGroupDetail(
    val name: String,
    val note: String,
    val numLifts: Int,
    val numSessions: Int,
    val avgNumSessionsPerWeek: Double,
    val avgNumSetsPerSession: Double,
    val avgNumSetsPerWeek: Double,
    val avgNumRepsPerSet: Double,
    val lastDateTrained: String,
    val cardIsOpen: Boolean = false,
    val menuIsOpen: Boolean = false
)
