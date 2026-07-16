package github.tom2433.lifttracker.data.liftset

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import github.tom2433.lifttracker.data.setmetric.SetMetric
import github.tom2433.lifttracker.data.structures.LiftSetCountPerLift
import github.tom2433.lifttracker.data.structures.RecordSessionLiftSetRow
import kotlinx.coroutines.flow.Flow

@Dao
interface LiftSetDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPreparedLiftSet(liftSet: LiftSet): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPreparedSetMetric(setMetric: SetMetric)

    @Transaction
    suspend fun insert(liftSet: LiftSet): Int {
        val nextLiftSetNumber = getNextLiftSetNumber(
            liftDayId = liftSet.lift_day_id,
            liftId = liftSet.lift_id
        )

        val nextDaySetNumber = getNextDaySetNumber(
            liftDayId = liftSet.lift_day_id
        )

        val insertedLiftSetId = insertPreparedLiftSet(
            liftSet.copy(
                lift_set_number = nextLiftSetNumber,
                day_set_number = nextDaySetNumber,
                set_label = "Set $nextLiftSetNumber"
            )
        ).toInt()

        insertPreparedSetMetric(
            setMetric = SetMetric(
                set_id = insertedLiftSetId,
                metric_position = 1,
                value = -1.0,
                note = ""
            )
        )

        insertPreparedSetMetric(
            setMetric = SetMetric(
                set_id = insertedLiftSetId,
                metric_position = 2,
                value = -1.0,
                note = ""
            )
        )

        return insertedLiftSetId
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

    @Query("""
        SELECT
            -- lift id
            l.id AS liftId,
            
            -- LiftSet object (LiftSet rows preceded by set_)
            ls.id AS set_id,
            ls.lift_day_id AS set_lift_day_id,
            ls.lift_id AS set_lift_id,
            ls.lift_set_number AS set_lift_set_number,
            ls.day_set_number AS set_day_set_number,
            ls.set_label AS set_set_label,
            ls.set_note AS set_set_note,
            
            -- SetMetric object for weight (rows preceded by weight_ where weight represents the set_metrics rows where metric_position = 1)
            weight.id AS weight_id,
            weight.set_id AS weight_set_id,
            weight.metric_position AS weight_metric_position,
            weight.value AS weight_value,
            weight.note AS weight_note,
            
            -- SetMetric object for second metric (rows preceded by second_ where second represents the set_metrics rows where metric_position = 2)
            second.id AS second_id,
            second.set_id AS second_set_id,
            second.metric_position AS second_metric_position,
            second.value AS second_value,
            second.note AS second_note
            
        FROM lift_sets AS ls
        INNER JOIN lifts AS l
            ON l.id = ls.lift_id
        INNER JOIN set_metrics AS weight
            ON weight.set_id = ls.id
            AND weight.metric_position = 1
        INNER JOIN set_metrics AS second
            ON second.set_id = ls.id
            AND second.metric_position = 2
        WHERE ls.lift_day_id = :liftDayId
        ORDER BY ls.day_set_number ASC
    """)
    fun getRecordSessionLiftSetRowsForDay(
        liftDayId: Int
    ): Flow<List<RecordSessionLiftSetRow>>

    @Query("""
        SELECT
            ls.lift_id AS liftId,
            COUNT(ls.id) AS liftSetCount
        FROM lift_sets AS ls
        WHERE ls.lift_day_id = :dayId
        GROUP BY ls.lift_id
    """)
    fun getLiftSetCountPerLiftIdForDayId(dayId: Int): Flow<List<LiftSetCountPerLift>>
}