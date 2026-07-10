package github.tom2433.lifttracker.data.liftday

import kotlinx.coroutines.flow.Flow

class OfflineLiftDayRepository(private val liftDayDao: LiftDayDao) : LiftDayRepository {
    override fun getAllLiftDaysStream(): Flow<List<LiftDay>> = liftDayDao.getAllLiftDays()

    override fun getLiftDayStream(id: Int): Flow<LiftDay?> = liftDayDao.getLiftDay(id)

    override suspend fun insertLiftDay(liftDay: LiftDay) = liftDayDao.insert(liftDay)

    override suspend fun deleteLiftDay(liftDay: LiftDay) = liftDayDao.deleteAndRenumber(liftDay)

    override suspend fun updateLiftDay(liftDay: LiftDay) = liftDayDao.update(liftDay)

    override fun getActiveLiftDayForActiveProfileStream(): Flow<LiftDay?> = liftDayDao.getActiveLiftDayForActiveProfile()

    override suspend fun getNextDayNumber(profile_id: Int): Int = liftDayDao.getNextDayNumber(profile_id)

    override fun getNumOfLiftsForDay(id: Int): Flow<Int> = liftDayDao.getNumOfLiftsForDay(id)
}