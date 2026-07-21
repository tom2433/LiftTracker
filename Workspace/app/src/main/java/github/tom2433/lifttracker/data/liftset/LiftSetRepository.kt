package github.tom2433.lifttracker.data.liftset

import github.tom2433.lifttracker.data.structures.LiftSetCountPerLift
import github.tom2433.lifttracker.data.structures.RecordSessionLiftSetRow
import kotlinx.coroutines.flow.Flow

interface LiftSetRepository {
    suspend fun insertLiftSet(
        sessionId: Int,
        liftId: Int,
        setNote: String = ""
    ): Int
    suspend fun updateLiftSet(liftSet: LiftSet)
    suspend fun deleteLiftSet(liftSet: LiftSet)
    fun getLiftSetStream(id: Int): Flow<LiftSet?>

    fun getRecordSessionLiftSetRowsForSessionStream(sessionId: Int): Flow<List<RecordSessionLiftSetRow>>

    fun getLiftSetCountPerLiftIdForSessionIdStream(sessionId: Int): Flow<List<LiftSetCountPerLift>>
}