package github.tom2433.lifttracker.data.liftset

import kotlinx.coroutines.flow.Flow

class OfflineLiftSetRepository(private val liftSetDao: LiftSetDao) : LiftSetRepository {
    override suspend fun insertLiftSet(
        liftDayId: Int,
        liftId: Int,
        setNote: String
    ) {
        liftSetDao.insert(
            LiftSet(
                lift_day_id = liftDayId,
                lift_id = liftId,
                lift_set_number = 0,
                day_set_number = 0,
                set_label = "",
                set_note = setNote
            )
        )
    }

    override suspend fun updateLiftSet(liftSet: LiftSet) = liftSetDao.update(liftSet)

    override suspend fun deleteLiftSet(liftSet: LiftSet) = liftSetDao.delete(liftSet)

    override fun getLiftSetStream(id: Int): Flow<LiftSet?> = liftSetDao.getLiftSet(id)

}