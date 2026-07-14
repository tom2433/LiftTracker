package github.tom2433.lifttracker.data.lift

import github.tom2433.lifttracker.data.structures.LiftSearchDetail
import github.tom2433.lifttracker.data.structures.LiftStatisticsData
import kotlinx.coroutines.flow.Flow

interface LiftRepository {
    suspend fun insertLift(lift: Lift)
    suspend fun updateLift(lift: Lift)
    suspend fun deleteLift(lift: Lift)
    fun getLiftStream(id: Int): Flow<Lift?>
    fun getAllLiftsFromMuscleGroupIdStream(muscle_group_id: Int): Flow<List<Lift>>
    // This exposes the DAO's reusable bounded-or-lifetime aggregate as a reactive stream. - Codex
    fun getLiftStatisticsStream(
        liftId: Int,
        startDate: String?,
        endDate: String?
    ): Flow<LiftStatisticsData?>

    fun getLiftSearchDetailsContainingStream(searchText: String): Flow<List<LiftSearchDetail>>

    fun getLiftSearchDetailsForDayIdStream(liftDayId: Int): Flow<List<LiftSearchDetail>>
}