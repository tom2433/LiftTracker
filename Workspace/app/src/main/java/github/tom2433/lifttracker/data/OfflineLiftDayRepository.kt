package github.tom2433.lifttracker.data

import kotlinx.coroutines.flow.Flow

class OfflineLiftDayRepository(private val liftDayDao: LiftDayDao) : LiftDayRepository {
    override fun getAllLiftDaysStream(): Flow<List<LiftDay>> = liftDayDao.getAllLiftDays()

    override fun getLiftDayStream(id: Int): Flow<LiftDay?> = liftDayDao.getLiftDay(id)

    override suspend fun insertLiftDay(liftDay: LiftDay) = liftDayDao.insert(liftDay)

    override suspend fun deleteLiftDay(liftDay: LiftDay) = liftDayDao.delete(liftDay)

    override suspend fun updateLiftDay(liftDay: LiftDay) = liftDayDao.update(liftDay)
}