package github.tom2433.lifttracker.data.liftset

import github.tom2433.lifttracker.data.lift.Lift
import github.tom2433.lifttracker.data.structures.LiftSetCountPerLift
import github.tom2433.lifttracker.data.structures.RecordSessionLiftSetRow
import kotlinx.coroutines.flow.Flow

class OfflineLiftSetRepository(private val liftSetDao: LiftSetDao) : LiftSetRepository {
    override suspend fun insertLiftSet(
        sessionId: Int,
        liftId: Int,
        setNote: String
    ): Int {
        return liftSetDao.insert(
            LiftSet(
                session_id = sessionId,
                lift_id = liftId,
                muscle_group_id = 0,
                lift_set_number = 0,
                session_set_number = 0,
                muscle_group_session_set_number = 0,
                set_label = "",
                set_note = setNote
            )
        )
    }

    override suspend fun updateLiftSet(liftSet: LiftSet) = liftSetDao.update(liftSet)

    override suspend fun deleteLiftSet(liftSet: LiftSet) = liftSetDao.delete(liftSet)

    override fun getLiftSetStream(id: Int): Flow<LiftSet?> = liftSetDao.getLiftSet(id)

    override fun getRecordSessionLiftSetRowsForSessionStream(sessionId: Int): Flow<List<RecordSessionLiftSetRow>> =
        liftSetDao.getRecordSessionLiftSetRowsForSession(sessionId)

    override fun getLiftSetCountPerLiftIdForSessionIdStream(sessionId: Int): Flow<List<LiftSetCountPerLift>> =
        liftSetDao.getLiftSetCountPerLiftIdForSessionId(sessionId)

}