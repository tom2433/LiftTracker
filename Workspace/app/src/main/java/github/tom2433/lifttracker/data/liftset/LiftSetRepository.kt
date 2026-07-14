package github.tom2433.lifttracker.data.liftset

import github.tom2433.lifttracker.data.structures.RecordSessionLiftSetRow
import kotlinx.coroutines.flow.Flow

interface LiftSetRepository {
    suspend fun insertLiftSet(
        liftDayId: Int,
        liftId: Int,
        setNote: String = ""
    )
    suspend fun updateLiftSet(liftSet: LiftSet)
    suspend fun deleteLiftSet(liftSet: LiftSet)
    fun getLiftSetStream(id: Int): Flow<LiftSet?>

    fun getRecordSessionLiftSetRowsForDayStream(liftDayId: Int): Flow<List<RecordSessionLiftSetRow>>
}