package github.tom2433.lifttracker.data.liftunit

import kotlinx.coroutines.flow.Flow

interface LiftUnitRepository {
    suspend fun insertLiftUnit(liftUnit: LiftUnit)
    suspend fun updateLiftUnit(liftUnit: LiftUnit)
    suspend fun deleteLiftUnit(liftUnit: LiftUnit)
    fun getLiftUnitStream(id: Int): Flow<LiftUnit?>
    fun getAllLiftUnitsStream(): Flow<List<LiftUnit>>
    fun getLiftUnitFromNameStream(liftUnitName: String): Flow<LiftUnit?>
}