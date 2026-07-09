package github.tom2433.lifttracker.data

import kotlinx.coroutines.flow.Flow

class OfflineLiftUnitRepository(private val liftUnitDao: LiftUnitDao) : LiftUnitRepository {
    override suspend fun insertLiftUnit(liftUnit: LiftUnit) = liftUnitDao.insert(liftUnit)

    override suspend fun updateLiftUnit(liftUnit: LiftUnit) = liftUnitDao.update(liftUnit)

    override suspend fun deleteLiftUnit(liftUnit: LiftUnit) = liftUnitDao.delete(liftUnit)

    override fun getLiftUnitStream(id: Int): Flow<LiftUnit?> = liftUnitDao.getLiftUnit(id)

    override fun getAllLiftUnitsStream(): Flow<List<LiftUnit>> = liftUnitDao.getAllLiftUnits()

    override fun getLiftUnitFromNameStream(liftUnitName: String): Flow<LiftUnit?> =
        liftUnitDao.getLiftUnitFromName(liftUnitName)
}
