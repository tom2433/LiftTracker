package github.tom2433.lifttracker.data.lift

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
                INNER JOIN lift_days AS target_ld
                    ON target_ld.id = target_ls.lift_day_id
                WHERE target_ls.lift_id = target_lift.id
                    AND (:startDate IS NULL OR target_ld.date >= :startDate)
                    AND (:endDate IS NULL OR target_ld.date <= :endDate)
            ) AS liftSetCount,
            (
                SELECT COUNT(DISTINCT target_ls.lift_day_id)
                FROM lift_sets AS target_ls
                INNER JOIN lift_days AS target_ld
                    ON target_ld.id = target_ls.lift_day_id
                WHERE target_ls.lift_id = target_lift.id
                    AND (:startDate IS NULL OR target_ld.date >= :startDate)
                    AND (:endDate IS NULL OR target_ld.date <= :endDate)
            ) AS liftSessionCount,
            (
                SELECT COUNT(*)
                FROM lift_sets AS overall_ls
                INNER JOIN lift_days AS overall_ld
                    ON overall_ld.id = overall_ls.lift_day_id
                INNER JOIN lifts AS overall_lift
                    ON overall_lift.id = overall_ls.lift_id
                INNER JOIN muscle_groups AS overall_mg
                    ON overall_mg.id = overall_lift.muscle_group_id
                    AND overall_mg.profile_id = overall_ld.profile_id
                INNER JOIN profiles AS active_profile
                    ON active_profile.id = overall_mg.profile_id
                    AND active_profile.active = 1
                WHERE (:startDate IS NULL OR overall_ld.date >= :startDate)
                    AND (:endDate IS NULL OR overall_ld.date <= :endDate)
            ) AS overallSetCount,
            (
                SELECT COUNT(*)
                FROM lift_sets AS group_ls
                INNER JOIN lift_days AS group_ld
                    ON group_ld.id = group_ls.lift_day_id
                    AND group_ld.profile_id = target_mg.profile_id
                INNER JOIN lifts AS group_lift
                    ON group_lift.id = group_ls.lift_id
                WHERE group_lift.muscle_group_id = target_lift.muscle_group_id
                    AND (:startDate IS NULL OR group_ld.date >= :startDate)
                    AND (:endDate IS NULL OR group_ld.date <= :endDate)
            ) AS muscleGroupSetCount,
            (
                SELECT AVG(weight_metric.value)
                FROM set_metrics AS weight_metric
                INNER JOIN lift_sets AS weight_ls
                    ON weight_ls.id = weight_metric.set_id
                INNER JOIN lift_days AS weight_ld
                    ON weight_ld.id = weight_ls.lift_day_id
                WHERE weight_ls.lift_id = target_lift.id
                    AND weight_metric.metric_position = 1
                    AND (:startDate IS NULL OR weight_ld.date >= :startDate)
                    AND (:endDate IS NULL OR weight_ld.date <= :endDate)
            ) AS averageWeight,
            (
                SELECT AVG(second_metric.value)
                FROM set_metrics AS second_metric
                INNER JOIN lift_sets AS second_ls
                    ON second_ls.id = second_metric.set_id
                INNER JOIN lift_days AS second_ld
                    ON second_ld.id = second_ls.lift_day_id
                WHERE second_ls.lift_id = target_lift.id
                    AND second_metric.metric_position = 2
                    AND (:startDate IS NULL OR second_ld.date >= :startDate)
                    AND (:endDate IS NULL OR second_ld.date <= :endDate)
            ) AS averageSecondMetric,
            target_mg.name AS muscleGroupName,
            target_lift_unit.name AS unitName,
            target_lift.metric_type AS metricType,
            (
                SELECT MAX(last_ld.date)
                FROM lift_sets AS last_ls
                INNER JOIN lift_days AS last_ld
                    ON last_ld.id = last_ls.lift_day_id
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
        WHERE ls.lift_day_id = :liftDayId        
    """)
    fun getLiftSearchDetailsForDayId(liftDayId: Int): Flow<List<LiftSearchDetail>>

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