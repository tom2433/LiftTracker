package github.tom2433.lifttracker.data.liftset

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LiftSetDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPreparedLiftSet(liftSet: LiftSet)

    @Transaction
    suspend fun insert(liftSet: LiftSet) {
        val nextLiftSetNumber = getNextLiftSetNumber(
            liftDayId = liftSet.lift_day_id,
            liftId = liftSet.lift_id
        )

        val nextDaySetNumber = getNextDaySetNumber(
            liftDayId = liftSet.lift_day_id
        )

        insertPreparedLiftSet(
            liftSet.copy(
                lift_set_number = nextLiftSetNumber,
                day_set_number = nextDaySetNumber,
                set_label = "Set $nextLiftSetNumber"
            )
        )
    }

    @Query("""
        SELECT COALESCE(MAX(lift_set_number), 0) + 1
        FROM lift_sets
        WHERE lift_day_id = :liftDayId
            AND lift_id = :liftId
    """)
    suspend fun getNextLiftSetNumber(
        liftDayId: Int,
        liftId: Int
    ): Int

    @Query("""
        SELECT COALESCE(MAX(day_set_number), 0) + 1
        FROM lift_sets
        WHERE lift_day_id = :liftDayId
    """)
    suspend fun getNextDaySetNumber(liftDayId: Int): Int

    @Update
    suspend fun update(liftSet: LiftSet)

    @Query("""
        UPDATE lift_sets
        SET set_label = 'Set ' || (lift_set_number - 1)
        WHERE lift_day_id = :liftDayId
            AND lift_id = :liftId
            AND lift_set_number > :deletedLiftSetNumber
            AND set_label = 'Set ' || lift_set_number
    """)
    suspend fun updateSetLabelsAfterLiftSetDelete(
        liftDayId: Int,
        liftId: Int,
        deletedLiftSetNumber: Int
    )

    @Query("""
        UPDATE lift_sets
        SET lift_set_number = lift_set_number - 1
        WHERE lift_day_id = :liftDayId
            AND lift_id = :liftId
            AND lift_set_number > :deletedLiftSetNumber
    """)
    suspend fun decrementLiftSetNumbersAfterDelete(
        liftDayId: Int,
        liftId: Int,
        deletedLiftSetNumber: Int
    )

    @Query("""
        UPDATE lift_sets
        SET day_set_number = day_set_number - 1
        WHERE lift_day_id = :liftDayId
            AND day_set_number > :deletedDaySetNumber
    """)
    suspend fun decrementDaySetNumbersAfterDelete(
        liftDayId: Int,
        deletedDaySetNumber: Int
    )

    @Delete
    suspend fun deletePreparedLiftSet(liftSet: LiftSet)

    @Transaction
    suspend fun delete(liftSet: LiftSet) {
        deletePreparedLiftSet(liftSet)

        updateSetLabelsAfterLiftSetDelete(
            liftDayId = liftSet.lift_day_id,
            liftId = liftSet.lift_id,
            deletedLiftSetNumber = liftSet.lift_set_number
        )

        decrementLiftSetNumbersAfterDelete(
            liftDayId = liftSet.lift_day_id,
            liftId = liftSet.lift_id,
            deletedLiftSetNumber = liftSet.lift_set_number
        )

        decrementDaySetNumbersAfterDelete(
            liftDayId = liftSet.lift_day_id,
            deletedDaySetNumber = liftSet.day_set_number
        )
    }

    @Query("SELECT * FROM lift_sets WHERE id = :id")
    fun getLiftSet(id: Int): Flow<LiftSet?>
}