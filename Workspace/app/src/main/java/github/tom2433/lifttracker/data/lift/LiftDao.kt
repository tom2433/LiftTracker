package github.tom2433.lifttracker.data.lift

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import github.tom2433.lifttracker.data.structures.LiftSearchDetail
import github.tom2433.lifttracker.data.structures.LiftStatisticsData
import kotlinx.coroutines.flow.Flow

@Dao
interface LiftDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(lift: Lift)

    @Update
    suspend fun update(lift: Lift)

    @Query("""
        SELECT DISTINCT
            s.id
        FROM sessions AS s
        INNER JOIN lift_sets AS ls
            ON ls.session_id = s.id
        WHERE ls.lift_id = :liftId
    """)
    suspend fun getSessionIdsForLift(liftId: Int): List<Int>

    @Query("""
        SELECT DISTINCT
            ls.muscle_group_id
        FROM lift_sets AS ls
        WHERE ls.lift_id = :liftId
    """)
    suspend fun getAllMuscleGroupIdsForLift(liftId: Int): List<Int>

    @Query("""
        UPDATE lift_sets
        SET muscle_group_session_set_number = -id
        WHERE session_id IN (:affectedSessionIds)
            AND muscle_group_id IN (:muscleGroupIds)
    """)
    suspend fun stageMuscleGroupSessionSetNumbersForMove(
        affectedSessionIds: List<Int>,
        muscleGroupIds: List<Int>
    )

    @Query("""
        UPDATE lift_sets
        SET muscle_group_id = :newMuscleGroupId
        WHERE lift_id = :liftId
    """)
    suspend fun updateLiftSetsMuscleGroupCascade(
        liftId: Int,
        newMuscleGroupId: Int
    )

    @Query("""
        UPDATE lift_sets
        SET muscle_group_id = :newMuscleGroupId
        WHERE lift_id = :liftId
            AND muscle_group_id = :oldMuscleGroupId
    """)
    suspend fun updateLiftSetsMuscleGroupNoCascade(
        liftId: Int,
        newMuscleGroupId: Int,
        oldMuscleGroupId: Int
    )

    @Query("""
        SELECT id
        FROM lift_sets
        WHERE session_id = :sessionId
            AND muscle_group_id = :muscleGroupId
        ORDER BY session_set_number ASC, id ASC
    """)
    suspend fun getRowsForMuscleGroupSessionSetRenumbering(
        sessionId: Int,
        muscleGroupId: Int
    ): List<Int>

    @Query("""
        UPDATE lift_sets
        SET muscle_group_session_set_number = :muscleGroupSessionSetNumber
        WHERE id = :liftSetId
    """)
    suspend fun updateMuscleGroupSessionSetNumber(
        liftSetId: Int,
        muscleGroupSessionSetNumber: Int
    )

    @Transaction
    suspend fun renumberMuscleGroupSessionSetNumbers(
        sessionId: Int,
        muscleGroupId: Int
    ) {
        val rows: List<Int> = getRowsForMuscleGroupSessionSetRenumbering(
            sessionId = sessionId,
            muscleGroupId = muscleGroupId
        )

        rows.forEachIndexed { index, liftSetId ->
            updateMuscleGroupSessionSetNumber(
                liftSetId = liftSetId,
                muscleGroupSessionSetNumber = index + 1
            )
        }
    }

    @Transaction
    suspend fun moveLiftToMuscleGroup(
        lift: Lift,
        newMuscleGroupId: Int,
        migrateOldSetData: Boolean,
        cascadeMigration: Boolean
    ) {
        val oldMuscleGroupId = lift.muscle_group_id
        if (oldMuscleGroupId == newMuscleGroupId) return

        val affectedSessionIds: List<Int> = getSessionIdsForLift(lift.id)

        val affectedMuscleGroupIds = if (cascadeMigration) {
            (getAllMuscleGroupIdsForLift(lift.id) + newMuscleGroupId).distinct()
        } else {
            listOf(oldMuscleGroupId, newMuscleGroupId)
        }

        if (affectedSessionIds.isNotEmpty() && migrateOldSetData) {
            stageMuscleGroupSessionSetNumbersForMove(
                affectedSessionIds = affectedSessionIds,
                muscleGroupIds = affectedMuscleGroupIds
            )
        }

        update(lift.copy(muscle_group_id = newMuscleGroupId))

        if (affectedSessionIds.isNotEmpty() && migrateOldSetData) {
            if (cascadeMigration) {
                updateLiftSetsMuscleGroupCascade(
                    liftId = lift.id,
                    newMuscleGroupId = newMuscleGroupId
                )
            } else {
                updateLiftSetsMuscleGroupNoCascade(
                    liftId = lift.id,
                    newMuscleGroupId = newMuscleGroupId,
                    oldMuscleGroupId = oldMuscleGroupId
                )
            }

            for (sessionId in affectedSessionIds) {
                for (muscleGroupId in affectedMuscleGroupIds) {
                    renumberMuscleGroupSessionSetNumbers(
                        sessionId = sessionId,
                        muscleGroupId = muscleGroupId
                    )
                }
            }
        }
    }

    @Delete
    suspend fun delete(lift: Lift)

    @Query("SELECT * FROM lifts WHERE id = :id")
    fun getLift(id: Int): Flow<Lift?>

    @Query("""
        SELECT * FROM lifts
        WHERE muscle_group_id = :muscle_group_id
    """)
    fun getAllLiftsFromMuscleGroupId(muscle_group_id: Int): Flow<List<Lift>>

    // This query calculates one reusable statistics window without materializing every set in application memory. - Codex
    // Nullable boundaries make the same query support rolling windows and an unbounded lifetime map. - Codex
    @Query("""
        SELECT
            (
                SELECT COUNT(*)
                FROM lift_sets AS target_ls
                INNER JOIN sessions AS target_session
                    ON target_session.id = target_ls.session_id
                WHERE target_ls.lift_id = target_lift.id
                    AND (:startDate IS NULL OR target_session.date >= :startDate)
                    AND (:endDate IS NULL OR target_session.date <= :endDate)
            ) AS liftSetCount,
            (
                SELECT COUNT(DISTINCT target_ls.session_id)
                FROM lift_sets AS target_ls
                INNER JOIN sessions AS target_session
                    ON target_session.id = target_ls.session_id
                WHERE target_ls.lift_id = target_lift.id
                    AND (:startDate IS NULL OR target_session.date >= :startDate)
                    AND (:endDate IS NULL OR target_session.date <= :endDate)
            ) AS liftSessionCount,
            (
                SELECT COUNT(*)
                FROM lift_sets AS overall_ls
                INNER JOIN sessions AS overall_session
                    ON overall_session.id = overall_ls.session_id
                INNER JOIN lifts AS overall_lift
                    ON overall_lift.id = overall_ls.lift_id
                INNER JOIN muscle_groups AS overall_mg
                    ON overall_mg.id = overall_lift.muscle_group_id
                    AND overall_mg.profile_id = overall_session.profile_id
                INNER JOIN profiles AS active_profile
                    ON active_profile.id = overall_mg.profile_id
                    AND active_profile.active = 1
                WHERE (:startDate IS NULL OR overall_session.date >= :startDate)
                    AND (:endDate IS NULL OR overall_session.date <= :endDate)
            ) AS overallSetCount,
            (
                SELECT COUNT(*)
                FROM lift_sets AS group_ls
                INNER JOIN sessions AS group_session
                    ON group_session.id = group_ls.session_id
                    AND group_session.profile_id = target_mg.profile_id
                INNER JOIN lifts AS group_lift
                    ON group_lift.id = group_ls.lift_id
                WHERE group_lift.muscle_group_id = target_lift.muscle_group_id
                    AND (:startDate IS NULL OR group_session.date >= :startDate)
                    AND (:endDate IS NULL OR group_session.date <= :endDate)
            ) AS muscleGroupSetCount,
            (
                SELECT AVG(weight_metric.value)
                FROM set_metrics AS weight_metric
                INNER JOIN lift_sets AS weight_ls
                    ON weight_ls.id = weight_metric.set_id
                INNER JOIN sessions AS weight_session
                    ON weight_session.id = weight_ls.session_id
                WHERE weight_ls.lift_id = target_lift.id
                    AND weight_metric.metric_position = 1
                    AND (:startDate IS NULL OR weight_session.date >= :startDate)
                    AND (:endDate IS NULL OR weight_session.date <= :endDate)
            ) AS averageWeight,
            (
                SELECT AVG(second_metric.value)
                FROM set_metrics AS second_metric
                INNER JOIN lift_sets AS second_ls
                    ON second_ls.id = second_metric.set_id
                INNER JOIN sessions AS second_session
                    ON second_session.id = second_ls.session_id
                WHERE second_ls.lift_id = target_lift.id
                    AND second_metric.metric_position = 2
                    AND (:startDate IS NULL OR second_session.date >= :startDate)
                    AND (:endDate IS NULL OR second_session.date <= :endDate)
            ) AS averageSecondMetric,
            target_mg.name AS muscleGroupName,
            target_lift_unit.name AS unitName,
            target_lift.metric_type AS metricType,
            (
                SELECT MAX(last_session.date)
                FROM lift_sets AS last_ls
                INNER JOIN sessions AS last_session
                    ON last_session.id = last_ls.session_id
                WHERE last_ls.lift_id = target_lift.id
            ) AS lastDateTrained
        FROM lifts AS target_lift
        INNER JOIN muscle_groups AS target_mg
            ON target_mg.id = target_lift.muscle_group_id
        INNER JOIN lift_units AS target_lift_unit
            ON target_lift_unit.id = target_lift.unit_id
        WHERE target_lift.id = :liftId
        LIMIT 1
    """)
    fun getLiftStatistics(
        liftId: Int,
        startDate: String?,
        endDate: String?
    ): Flow<LiftStatisticsData?>

    @Query("""
        SELECT
            l.*,
            mg.name AS muscleGroupName,
            CASE
                WHEN l.metric_type = 1 THEN 'reps'
                ELSE 'time'
            END AS metricType,
            lu.name AS unitName,
            0 AS selected,
            1 AS visible
        FROM lifts AS l
        INNER JOIN muscle_groups AS mg
            ON mg.id = l.muscle_group_id
        INNER JOIN profiles AS p
            ON p.id = mg.profile_id
        INNER JOIN lift_units AS lu
            ON lu.id = l.unit_id
        WHERE p.active = 1
            AND instr(lower(l.name), lower(:searchText)) > 0
        ORDER BY l.name ASC
    """)
    fun getLiftSearchDetailsContaining(searchText: String): Flow<List<LiftSearchDetail>>

    @Query("""
        SELECT DISTINCT
            l.*,
            mg.name AS muscleGroupName,
            CASE
                WHEN l.metric_type = 1 THEN 'reps'
                ELSE 'time'
            END AS metricType,
            lu.name AS unitName,
            0 AS selected,
            0 AS visible
        FROM lifts AS l
        INNER JOIN muscle_groups AS mg
            ON mg.id = l.muscle_group_id
        INNER JOIN lift_units AS lu
            ON lu.id = l.unit_id
        INNER JOIN lift_sets AS ls
            ON ls.lift_id = l.id
        WHERE ls.session_id = :sessionId        
    """)
    fun getLiftSearchDetailsForSessionId(sessionId: Int): Flow<List<LiftSearchDetail>>

    @Query("""
        SELECT
            l.metric_type
        FROM lifts AS l
        INNER JOIN lift_sets AS ls
            ON ls.lift_id = l.id
        INNER JOIN set_metrics AS sm
            ON sm.set_id = ls.id
        WHERE sm.id = :setMetricId
        LIMIT 1
    """)
    suspend fun getMetricTypeFromSetMetricId(setMetricId: Int): Int?
}
