package com.example.lifttracker.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifttracker.data.MuscleGroupDetailData
import com.example.lifttracker.data.MuscleGroupRepository
import com.example.lifttracker.data.Profile
import com.example.lifttracker.data.ProfileRepository
import com.example.lifttracker.data.MuscleGroup
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
                val today = getCurrentIsoDate()

                // Key each calculated detail by its muscle-groups table ID while preserving the query order. - Codex
                val muscleGroupDetails = detailData.associate { data ->
                    data.id to createMuscleGroupDetail(data, today)
                }

                // Publish the newly calculated immutable list so Compose can react to the database change. - Codex
                _muscleGroupsUiState.update { currentState ->
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
        val numWeeks = calculateNumWeeks(data.firstDateTrained, today)

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
            lastDateTrained = formatLastDateTrained(data.lastDateTrained, today)
        )
    }

    // This counts rolling seven-day buckets whose final day is today rather than using calendar weeks. - Codex
    private fun calculateNumWeeks(firstDateTrained: String?, today: String): Double {
        // An untrained muscle group still includes the current week, which keeps both weekly averages at zero. - Codex
        if (firstDateTrained == null) {
            return 1.0
        }

        // Invalid legacy dates fall back to the current week instead of crashing collection of the Room Flow. - Codex
        val daysSinceFirstSession = calculateDaysBetween(firstDateTrained, today) ?: return 1.0

        // Dividing by seven assigns today through today minus six to the current week, then adds that current week. - Codex
        return (daysSinceFirstSession.coerceAtLeast(0L) / DAYS_PER_WEEK + 1L).toDouble()
    }

    // This translates the latest ISO date into the requested relative training-date category. - Codex
    private fun formatLastDateTrained(lastDateTrained: String?, today: String): String {
        // A null date means no set belonging to this muscle group has ever been recorded. - Codex
        if (lastDateTrained == null) {
            return "N/A"
        }

        // A malformed legacy date cannot be categorized reliably, so it is treated as unavailable. - Codex
        val calculatedDaysAgo = calculateDaysBetween(lastDateTrained, today) ?: return "N/A"

        // Future dates are clamped to today because the requested categories only describe elapsed time. - Codex
        val daysAgo = calculatedDaysAgo.coerceAtLeast(0L)

        // Categorize recent dates by day, then exact/partial weeks, then exact/partial four-week months. - Codex
        return when {
            daysAgo == 0L -> "Today"
            daysAgo == 1L -> "Yesterday"
            daysAgo < DAYS_PER_WEEK -> "$daysAgo days ago"
            daysAgo < DAYS_PER_MONTH -> formatElapsedUnit(daysAgo, DAYS_PER_WEEK, "week")
            daysAgo < DAYS_PER_YEAR -> formatElapsedUnit(daysAgo, DAYS_PER_MONTH, "month")
            daysAgo == DAYS_PER_YEAR -> "1 year ago"
            else -> "Over 1 year ago"
        }
    }

    // This formats exact boundaries as "2 weeks ago" and in-between values as "Over 2 weeks ago". - Codex
    private fun formatElapsedUnit(daysAgo: Long, daysPerUnit: Long, unitName: String): String {
        // Integer division gives the number of fully completed units in the elapsed period. - Codex
        val completedUnits = daysAgo / daysPerUnit

        // Add a plural suffix for every count other than one. - Codex
        val displayUnit = if (completedUnits == 1L) unitName else "${unitName}s"

        // A remainder means the date is beyond the exact unit boundary but has not reached the next one. - Codex
        return if (daysAgo % daysPerUnit == 0L) {
            "$completedUnits $displayUnit ago"
        } else {
            "Over $completedUnits $displayUnit ago"
        }
    }

    // This returns the number of whole date boundaries between two strict ISO-8601 calendar dates. - Codex
    private fun calculateDaysBetween(olderDate: String, newerDate: String): Long? {
        // Parse date-only values in UTC so daylight-saving transitions cannot create fractional days. - Codex
        val olderEpochDay = parseIsoDateToEpochDay(olderDate) ?: return null
        val newerEpochDay = parseIsoDateToEpochDay(newerDate) ?: return null

        // Subtracting epoch-day values produces an exact calendar-day difference. - Codex
        return newerEpochDay - olderEpochDay
    }

    // This parses one yyyy-MM-dd value into an epoch-day number without requiring API 26 java.time classes. - Codex
    private fun parseIsoDateToEpochDay(date: String): Long? {
        // A fresh formatter is used because SimpleDateFormat is mutable and not thread-safe. - Codex
        val formatter = createIsoDateFormatter()

        // The parse position check rejects values that contain extra characters after a valid date prefix. - Codex
        val parsePosition = ParsePosition(0)
        val parsedDate = formatter.parse(date, parsePosition)

        // Strict parsing and full input consumption guarantee the database value is a valid ISO date. - Codex
        if (parsedDate == null || parsePosition.index != date.length) {
            return null
        }

        // UTC midnight milliseconds divide evenly into whole epoch days. - Codex
        return parsedDate.time / MILLIS_PER_DAY
    }

    // This produces today's local calendar date in the same ISO-8601 format used by lift_days.date. - Codex
    private fun getCurrentIsoDate(): String {
        // Formatting in the device time zone ensures "Today" follows the user's local calendar day. - Codex
        val localFormatter = SimpleDateFormat(ISO_DATE_PATTERN, Locale.US)
        return localFormatter.format(Date())
    }

    // This creates the strict UTC formatter shared by the date-difference calculations. - Codex
    private fun createIsoDateFormatter(): SimpleDateFormat {
        // UTC makes date-only arithmetic independent of the device's daylight-saving rules. - Codex
        return SimpleDateFormat(ISO_DATE_PATTERN, Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private companion object {
        // These constants define the rolling date buckets requested for the detail calculations. - Codex
        const val ISO_DATE_PATTERN = "yyyy-MM-dd"
        const val MILLIS_PER_DAY = 86_400_000L
        const val DAYS_PER_WEEK = 7L
        const val DAYS_PER_MONTH = 28L
        const val DAYS_PER_YEAR = 12L * DAYS_PER_MONTH
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
    val muscleGroupIdToDelete: Int = -1
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
    val cardIsOpen: Boolean = false
)
