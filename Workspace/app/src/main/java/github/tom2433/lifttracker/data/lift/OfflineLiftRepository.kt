package github.tom2433.lifttracker.data.lift

import github.tom2433.lifttracker.data.structures.LiftSearchDetail
import github.tom2433.lifttracker.data.structures.LiftStatisticsData
import kotlinx.coroutines.flow.Flow

class OfflineLiftRepository(private val liftDao: LiftDao) : LiftRepository {
    override suspend fun insertLift(lift: Lift) = liftDao.insert(lift)

    override suspend fun updateLift(lift: Lift) = liftDao.update(lift)

    override suspend fun deleteLift(lift: Lift) = liftDao.delete(lift)

    override fun getLiftStream(id: Int): Flow<Lift?> = liftDao.getLift(id)

    override fun getAllLiftsFromMuscleGroupIdStream(muscle_group_id: Int): Flow<List<Lift>> = liftDao.getAllLiftsFromMuscleGroupId(muscle_group_id)

    // Delegating the aggregate preserves Room's automatic updates when any referenced table changes. - Codex
    override fun getLiftStatisticsStream(
        liftId: Int,
        startDate: String?,
        endDate: String?
    ): Flow<LiftStatisticsData?> = liftDao.getLiftStatistics(liftId, startDate, endDate)

    override fun getLiftSearchDetailsContainingStream(searchText: String): Flow<List<LiftSearchDetail>> = liftDao.getLiftSearchDetailsContaining(searchText)

    override fun getLiftSearchDetailsForDayIdStream(liftDayId: Int): Flow<List<LiftSearchDetail>> = liftDao.getLiftSearchDetailsForDayId(liftDayId)

    override suspend fun getMetricTypeFromSetMetricId(setMetricId: Int): Int? = liftDao.getMetricTypeFromSetMetricId(setMetricId)

    override suspend fun moveLiftToMuscleGroup(lift: Lift, newMuscleGroupId: Int) = liftDao.moveLiftToMuscleGroup(lift, newMuscleGroupId)
}