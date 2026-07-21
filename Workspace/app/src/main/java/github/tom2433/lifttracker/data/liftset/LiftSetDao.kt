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
        val muscleGroupId = getMuscleGroupIdFromLiftId(liftSet.lift_id)

        val nextLiftSetNumber = getNextLiftSetNumber(
            sessionId = liftSet.session_id,
            liftId = liftSet.lift_id
        )

        val nextSessionSetNumber = getNextSessionSetNumber(
            sessionId = liftSet.session_id
        )

        val nextMuscleGroupSessionSetNumber = getNextMuscleGroupSessionSetNumber(
            sessionId = liftSet.session_id,
            muscleGroupId = muscleGroupId
        )

        val insertedLiftSetId = insertPreparedLiftSet(
            liftSet.copy(
                muscle_group_id = muscleGroupId,
                lift_set_number = nextLiftSetNumber,
                session_set_number = nextSessionSetNumber,
                muscle_group_session_set_number = nextMuscleGroupSessionSetNumber,
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
        SELECT COALESCE(MAX(muscle_group_session_set_number), 0) + 1
        FROM lift_sets
        WHERE session_id = :sessionId
            AND muscle_group_id = :muscleGroupId
    """)
    suspend fun getNextMuscleGroupSessionSetNumber(
        sessionId: Int,
        muscleGroupId: Int
    ): Int

    @Query("""
        SELECT COALESCE(MAX(lift_set_number), 0) + 1
        FROM lift_sets
        WHERE session_id = :sessionId
            AND lift_id = :liftId
    """)
    suspend fun getNextLiftSetNumber(
        sessionId: Int,
        liftId: Int
    ): Int

    @Query("""
        SELECT COALESCE(MAX(session_set_number), 0) + 1
        FROM lift_sets
        WHERE session_id = :sessionId
    """)
    suspend fun getNextSessionSetNumber(sessionId: Int): Int

    @Query("""
        SELECT l.muscle_group_id
        FROM lifts AS l
        WHERE l.id = :liftId
    """)
    suspend fun getMuscleGroupIdFromLiftId(liftId: Int): Int

    @Update
    suspend fun update(liftSet: LiftSet)

    @Query("""
        UPDATE lift_sets
        SET set_label = 'Set ' || (lift_set_number - 1)
        WHERE session_id = :sessionId
            AND lift_id = :liftId
            AND lift_set_number > :deletedLiftSetNumber
            AND set_label = 'Set ' || lift_set_number
    """)
    suspend fun updateSetLabelsAfterLiftSetDelete(
        sessionId: Int,
        liftId: Int,
        deletedLiftSetNumber: Int
    )

    @Query("""
        UPDATE lift_sets
        SET muscle_group_session_set_number = -muscle_group_session_set_number
        WHERE session_id = :sessionId
            AND muscle_group_id = :muscleGroupId
            AND muscle_group_session_set_number > :deletedMuscleGroupSessionSetNumber
    """)
    suspend fun stageMuscleGroupSessionSetNumbersAfterDelete(
        sessionId: Int,
        muscleGroupId: Int,
        deletedMuscleGroupSessionSetNumber: Int
    )

    @Query("""
        UPDATE lift_sets
        SET muscle_group_session_set_number = (-muscle_group_session_set_number) - 1
        WHERE session_id = :sessionId
            AND muscle_group_id = :muscleGroupId
            AND muscle_group_session_set_number < 0
    """)
    suspend fun decrementStagedMuscleGroupSessionSetNumbersAfterDelete(
        sessionId: Int,
        muscleGroupId: Int
    )

    @Query("""
        UPDATE lift_sets
        SET lift_set_number = -lift_set_number
        WHERE session_id = :sessionId
            AND lift_id = :liftId
            AND lift_set_number > :deletedLiftSetNumber
    """)
    suspend fun stageLiftSetNumbersAfterDelete(
        sessionId: Int,
        liftId: Int,
        deletedLiftSetNumber: Int
    )

    @Query("""
        UPDATE lift_sets
        SET lift_set_number = (-lift_set_number) - 1
        WHERE session_id = :sessionId
            AND lift_id = :liftId
            AND lift_set_number < 0
    """)
    suspend fun decrementStagedLiftSetNumbersAfterDelete(
        sessionId: Int,
        liftId: Int,
    )

    @Query("""
        UPDATE lift_sets
        SET session_set_number = -session_set_number
        WHERE session_id = :sessionId
            AND session_set_number > :deletedSessionSetNumber
    """)
    suspend fun stageSessionSetNumbersAfterDelete(
        sessionId: Int,
        deletedSessionSetNumber: Int
    )

    @Query("""
        UPDATE lift_sets
        SET session_set_number = (-session_set_number) - 1
        WHERE session_id = :sessionId
            AND session_set_number < 0
    """)
    suspend fun decrementStagedSessionSetNumbersAfterDelete(
        sessionId: Int
    )

    @Delete
    suspend fun deletePreparedLiftSet(liftSet: LiftSet)

    @Transaction
    suspend fun delete(liftSet: LiftSet) {
        deletePreparedLiftSet(liftSet)

        updateSetLabelsAfterLiftSetDelete(
            sessionId = liftSet.session_id,
            liftId = liftSet.lift_id,
            deletedLiftSetNumber = liftSet.lift_set_number
        )

        stageLiftSetNumbersAfterDelete(
            sessionId = liftSet.session_id,
            liftId = liftSet.lift_id,
            deletedLiftSetNumber = liftSet.lift_set_number
        )

        decrementStagedLiftSetNumbersAfterDelete(
            sessionId = liftSet.session_id,
            liftId = liftSet.lift_id
        )

        stageSessionSetNumbersAfterDelete(
            sessionId = liftSet.session_id,
            deletedSessionSetNumber = liftSet.session_set_number
        )

        decrementStagedSessionSetNumbersAfterDelete(
            sessionId = liftSet.session_id
        )

        stageMuscleGroupSessionSetNumbersAfterDelete(
            sessionId = liftSet.session_id,
            muscleGroupId = liftSet.muscle_group_id,
            deletedMuscleGroupSessionSetNumber = liftSet.muscle_group_session_set_number
        )

        decrementStagedMuscleGroupSessionSetNumbersAfterDelete(
            sessionId = liftSet.session_id,
            muscleGroupId = liftSet.muscle_group_id
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
            ls.session_id AS set_session_id,
            ls.lift_id AS set_lift_id,
            ls.muscle_group_id AS set_muscle_group_id,
            ls.lift_set_number AS set_lift_set_number,
            ls.session_set_number AS set_session_set_number,
            ls.muscle_group_session_set_number AS set_muscle_group_session_set_number,
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
        WHERE ls.session_id = :sessionId
        ORDER BY ls.session_set_number ASC
    """)
    fun getRecordSessionLiftSetRowsForSession(
        sessionId: Int
    ): Flow<List<RecordSessionLiftSetRow>>

    @Query("""
        SELECT
            ls.lift_id AS liftId,
            COUNT(ls.id) AS liftSetCount
        FROM lift_sets AS ls
        WHERE ls.session_id = :sessionId
        GROUP BY ls.lift_id
    """)
    fun getLiftSetCountPerLiftIdForSessionId(sessionId: Int): Flow<List<LiftSetCountPerLift>>
}