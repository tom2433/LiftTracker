package github.tom2433.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.tom2433.lifttracker.data.lift.Lift
import github.tom2433.lifttracker.data.lift.LiftRepository
import github.tom2433.lifttracker.data.structures.LiftStatisticsData
import github.tom2433.lifttracker.data.musclegroup.MuscleGroup
import github.tom2433.lifttracker.data.musclegroup.MuscleGroupRepository
import github.tom2433.lifttracker.data.liftunit.LiftUnit
import github.tom2433.lifttracker.data.liftunit.LiftUnitRepository
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import kotlin.collections.mapValues
import kotlin.math.roundToLong

@OptIn(ExperimentalCoroutinesApi::class)
class LiftScreenViewModel(
    private val lift: Lift,
    private val liftRepository: LiftRepository,
    private val muscleGroupRepository: MuscleGroupRepository,
    private val liftUnitRepository: LiftUnitRepository
) : ViewModel() {
    private val _liftScreenUiState = MutableStateFlow(LiftScreenUiState(lift = lift))
    val liftScreenUiState: StateFlow<LiftScreenUiState> = _liftScreenUiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Capture one local date so all three maps use exactly the same inclusive end boundary. - Codex
            val today = DateTimeCalculator.getCurrentIsoDate()

            // Subtracting 27 days makes today the twenty-eighth and final day of the four-week window. - Codex
            val pastMonthStartDate = calculateStartDate(today, 27L)

            // Subtracting 364 days makes today the three-hundred-sixty-fifth and final day of the annual window. - Codex
            val pastYearStartDate = calculateStartDate(today, 364L)

            // Observe all three aggregates together so changes to sets, metrics, lifts, profiles, lift units, or groups refresh the UI. - Codex
            combine(
                liftRepository.getLiftStatisticsStream(lift.id, pastMonthStartDate, today),
                liftRepository.getLiftStatisticsStream(lift.id, pastYearStartDate, today),
                liftRepository.getLiftStatisticsStream(lift.id, null, null)
            ) { pastMonthStatistics, pastYearStatistics, lifetimeStatistics ->
                // A deleted or otherwise missing lift produces no projection, so retain the current state until navigation closes. - Codex
                if (pastMonthStatistics == null || pastYearStatistics == null || lifetimeStatistics == null) {
                    return@combine null
                }

                // Package the synchronized projections for one atomic StateFlow update below. - Codex
                Triple(pastMonthStatistics, pastYearStatistics, lifetimeStatistics)
            }.collect { statisticsByTimeframe ->
                // Ignore a missing projection rather than publishing partially populated maps. - Codex
                if (statisticsByTimeframe == null) {
                    return@collect
                }

                // Destructure the three well-named timeframe values to keep the state assignment easy to audit. - Codex
                val (pastMonthStatistics, pastYearStatistics, lifetimeStatistics) = statisticsByTimeframe

                // Publish the relative last date and all formatted maps together so the screen never displays mixed emissions. - Codex
                _liftScreenUiState.update { currentState ->
                    currentState.copy(
                        lastDateTrained = DateTimeCalculator.formatLastDateTrained(
                            lifetimeStatistics.lastDateTrained,
                            today
                        ),
                        pastMonthStatMap = createStatMap(pastMonthStatistics),
                        pastYearStatMap = createStatMap(pastYearStatistics),
                        lifetimeStatMap = createStatMap(lifetimeStatistics)
                    )
                }
            }
        }

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
            // get lift unit name for lift.
            // retrieving the lift unit name will depend on the current lift using flatMapLatest {}
            liftRepository.getLiftStream(lift.id)
                .filterNotNull()
                .flatMapLatest { updatedLift ->
                    liftUnitRepository.getLiftUnitStream(updatedLift.unit_id)
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
            // retrieve all lift units
            liftUnitRepository.getAllLiftUnitsStream().collect { theseLiftUnits ->
                _liftScreenUiState.update { currentState ->
                    currentState.copy(
                        liftUnitList = theseLiftUnits
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
            liftRepository.getLiftStream(lift.id)
                .filterNotNull()
                .flatMapLatest { updatedLift ->
                    muscleGroupRepository.getMuscleGroupStream(updatedLift.muscle_group_id)
                }
                .collect { thisMuscleGroup ->
                    _liftScreenUiState.update { currentState ->
                        currentState.copy(
                            muscleGroup = thisMuscleGroup
                        )
                    }
                }
        }
    }

    // This calculates an inclusive rolling-window start date using DateCalculator's strict UTC epoch-day parsing. - Codex
    private fun calculateStartDate(today: String, daysBeforeToday: Long): String {
        // Today's value is generated internally and is therefore valid; this fallback keeps initialization safe if that contract changes. - Codex
        val todayEpochDay = DateTimeCalculator.parseIsoDateToEpochDay(today) ?: return today

        // Convert the shifted UTC epoch day back to the ISO format stored by lift_days.date. - Codex
        return DateTimeCalculator.createIsoDateFormatter().format(
            Date((todayEpochDay - daysBeforeToday) * DateTimeCalculator.MILLIS_PER_DAY)
        )
    }

    // This converts one timeframe projection into the exact ordered labels and values required by LiftScreenUiState. - Codex
    private fun createStatMap(statistics: LiftStatisticsData): Map<String, String> {
        // Only sessions containing this lift contribute to its average sets-per-session denominator. - Codex
        val averageSetsPerSession = if (statistics.liftSessionCount == 0) {
            0.0
        } else {
            statistics.liftSetCount.toDouble() / statistics.liftSessionCount.toDouble()
        }

        // A zero denominator can only yield a displayable zero percentage rather than NaN or infinity. - Codex
        val overallSetPercentage = calculatePercentage(
            numerator = statistics.liftSetCount,
            denominator = statistics.overallSetCount
        )

        // The same guarded calculation is reused for the selected muscle group's set-volume percentage. - Codex
        val muscleGroupSetPercentage = calculatePercentage(
            numerator = statistics.liftSetCount,
            denominator = statistics.muscleGroupSetCount
        )

        // Missing metric rows are represented as zero while recorded averages retain their Double precision until formatting. - Codex
        val averageWeight = statistics.averageWeight ?: 0.0
        val averageSecondMetric = statistics.averageSecondMetric ?: 0.0

        // Linked insertion order keeps the six requested statistics stable for any UI that iterates over this map. - Codex
        return linkedMapOf(
            "Total # of sets performed" to "${statistics.liftSetCount} sets",
            "Avg. # of sets per session" to "${formatDecimal(averageSetsPerSession)} sets/session",
            "% of overall set volume" to "${formatDecimal(overallSetPercentage)} %",
            "% of set volume for ${statistics.muscleGroupName}" to "${formatDecimal(muscleGroupSetPercentage)} %",
            "Avg. weight" to "${formatDecimal(averageWeight)} ${statistics.unitName}",
            createSecondMetricEntry(statistics.metricType, averageSecondMetric)
        )
    }

    // This prevents division by zero and guarantees that all percentage calculations use floating-point division. - Codex
    private fun calculatePercentage(numerator: Int, denominator: Int): Double {
        // The requested empty-lift behavior is exactly zero regardless of the broader timeframe's volume. - Codex
        if (numerator == 0 || denominator == 0) {
            return 0.0
        }

        // Multiply after division to express the lift's share on the conventional zero-to-one-hundred scale. - Codex
        return numerator.toDouble() / denominator.toDouble() * 100.0
    }

    // This selects the final map label and formatting rule from the lift's current metric type. - Codex
    private fun createSecondMetricEntry(metricType: Int, averageSecondMetric: Double): Pair<String, String> {
        // Metric type one is a numeric reps average rounded for display to exactly two decimal places. - Codex
        return if (metricType == 1) {
            "Avg. # of reps per set" to "${formatDecimal(averageSecondMetric)} reps"
        } else {
            // Time metrics are decimal minutes in Room and must be translated into a whole-second clock string. - Codex
            "Avg. time per set" to formatTimeMetric(averageSecondMetric)
        }
    }

    // This applies a locale-stable decimal point and exactly two digits after it to every numeric statistic. - Codex
    private fun formatDecimal(value: Double): String = String.format(Locale.US, "%.2f", value)

    // This converts decimal minutes to an unbounded HH:MM:SS duration, rounding fractional seconds where necessary. - Codex
    private fun formatTimeMetric(decimalMinutes: Double): String {
        // Clamp unexpected negative legacy data to zero before converting minutes into the nearest whole second. - Codex
        val totalSeconds = (decimalMinutes.coerceAtLeast(0.0) * 60.0).roundToLong()

        // Hours intentionally remain unbounded because elapsed workout durations are not times of day. - Codex
        val hours = totalSeconds / 3_600L

        // Removing complete hours leaves the minute component in the required zero-to-fifty-nine range. - Codex
        val minutes = totalSeconds % 3_600L / 60L

        // Modulo sixty yields the final zero-to-fifty-nine seconds component. - Codex
        val seconds = totalSeconds % 60L

        // Locale.US guarantees ASCII digits and colon-separated, two-character clock components. - Codex
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
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
            // determine if the inputted lift unit exists
            var liftUnit: LiftUnit? = null
            for (currentLiftUnit in _liftScreenUiState.value.liftUnitList) {
                if (currentLiftUnit.name == _liftScreenUiState.value.newLiftUnitName) {
                    liftUnit = currentLiftUnit
                }
            }

            // if it doesn't exist, create it
            if (liftUnit == null) {
                liftUnitRepository.insertLiftUnit(
                    liftUnit = LiftUnit(
                        name = _liftScreenUiState.value.newLiftUnitName
                    )
                )

                liftUnit = liftUnitRepository.getLiftUnitFromNameStream(_liftScreenUiState.value.newLiftUnitName).firstOrNull()
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

    fun updateLiftUnit(newLiftUnitName: String) {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                newLiftUnitName = newLiftUnitName
            )
        }
    }

    fun openSwitchMuscleGroupDialog() {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                userIsSwitchingMuscleGroup = true,
                selectedMuscleGroup = currentState.muscleGroup
            )
        }
    }

    fun closeSwitchMuscleGroupDialog() {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                userIsSwitchingMuscleGroup = false,
                selectedMuscleGroup = null
            )
        }
    }

    fun selectMuscleGroup(muscleGroupToSelect: MuscleGroup) {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                selectedMuscleGroup = muscleGroupToSelect
            )
        }
    }

    fun validateSwitchMuscleGroupDialog(): Boolean {
        return (_liftScreenUiState.value.selectedMuscleGroup != null
                && _liftScreenUiState.value.selectedMuscleGroup != _liftScreenUiState.value.muscleGroup)
    }

    fun submitSwitchMuscleGroupDialog() {
        if (!validateSwitchMuscleGroupDialog()) {
            return
        }

        viewModelScope.launch {
            // update the lift in the database with the new muscle group FK
            liftRepository.updateLift(
                lift = _liftScreenUiState.value.lift.copy(
                    muscle_group_id = _liftScreenUiState.value.selectedMuscleGroup!!.id
                )
            )

            // close the dialog
            closeSwitchMuscleGroupDialog()
        }
    }

    fun filterChipClicked(keyClicked: String) {
        _liftScreenUiState.update { currentState ->
            currentState.copy(
                statDisplayFilterMap = currentState.statDisplayFilterMap.mapValues { (chipLabel, selected) ->
                    keyClicked == chipLabel
                }
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
    val liftUnitList: List<LiftUnit> = listOf(),
    val userIsSwitchingMuscleGroup: Boolean = false,
    val selectedMuscleGroup: MuscleGroup? = null,
    val lastDateTrained: String = "",
    val pastMonthStatMap: Map<String, String> = mapOf(),
    val pastYearStatMap: Map<String, String> = mapOf(),
    val lifetimeStatMap: Map<String, String> = mapOf(),
    val statDisplayFilterMap: Map<String, Boolean> = mapOf(
        "Past Month" to true,
        "Past Year" to false,
        "Lifetime" to false
    )
)

data class LiftScreenDetail(
    val metricType: String,
    val unitName: String
)
