package github.tom2433.lifttracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MuscleGroupDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(muscleGroup: MuscleGroup)

    @Update
    suspend fun update(muscleGroup: MuscleGroup)

    @Delete
    suspend fun delete(muscleGroup: MuscleGroup)

    @Query("SELECT * FROM muscle_groups WHERE id = :id")
    fun getMuscleGroup(id: Int): Flow<MuscleGroup?>

    @Query("""
        SELECT mg.*
        FROM muscle_groups AS mg
        INNER JOIN profiles AS p
            ON mg.profile_id = p.id
        WHERE p.active = 1
    """)
    fun getAllMuscleGroupsForActiveProfile(): Flow<List<MuscleGroup>>

    // This query returns one aggregate row per muscle group owned by the active profile. - Codex
    // LEFT JOIN keeps muscle groups in the result even when they do not have lifts, sets, metrics, or sessions yet. - Codex
    // DISTINCT prevents the one-to-many joins from counting a lift, set, or session more than once. - Codex
    @Query("""
        SELECT
            mg.id AS id,
            mg.name AS name,
            mg.note AS note,
            COUNT(DISTINCT l.id) AS numLifts,
            COUNT(DISTINCT ld.id) AS numSessions,
            COUNT(DISTINCT CASE WHEN ld.id IS NOT NULL THEN ls.id END) AS numSets,
            COUNT(DISTINCT CASE WHEN l.metric_type = 1 THEN l.id END) AS numRepLifts,
            AVG(
                CASE
                    WHEN l.metric_type = 1
                        AND sm.metric_position = 2
                        AND ld.id IS NOT NULL
                    THEN sm.value
                    ELSE NULL
                END
            ) AS avgNumRepsPerSet,
            MIN(ld.date) AS firstDateTrained,
            MAX(ld.date) AS lastDateTrained
        FROM muscle_groups AS mg
        INNER JOIN profiles AS p
            ON p.id = mg.profile_id
        LEFT JOIN lifts AS l
            ON l.muscle_group_id = mg.id
        LEFT JOIN lift_sets AS ls
            ON ls.lift_id = l.id
        LEFT JOIN lift_days AS ld
            ON ld.id = ls.lift_day_id
            AND ld.profile_id = mg.profile_id
        LEFT JOIN set_metrics AS sm
            ON sm.set_id = ls.id
        WHERE p.active = 1
        GROUP BY mg.id, mg.name, mg.note
        ORDER BY mg.id ASC
    """)
    fun getAllMuscleGroupDetailDataForActiveProfile(): Flow<List<MuscleGroupDetailData>>

    @Query("""
        SELECT mg.*
        FROM muscle_groups AS mg
        INNER JOIN lifts AS l
            ON l.muscle_group_id = mg.id
        WHERE l.id = :lift_id
        LIMIT 1
    """)
    fun getMuscleGroupFromLiftId(lift_id: Int): Flow<MuscleGroup?>
}
