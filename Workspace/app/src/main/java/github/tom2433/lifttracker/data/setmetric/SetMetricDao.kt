package github.tom2433.lifttracker.data.setmetric

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SetMetricDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(setMetric: SetMetric)

    @Update
    suspend fun update(setMetric: SetMetric)

    @Delete
    suspend fun delete(setMetric: SetMetric)

    @Query("SELECT * FROM set_metrics WHERE id = :id")
    fun getSetMetric(id: Int): Flow<SetMetric?>

    // precondition when using inner join is that the lift day must exist.
    @Query("""
        SELECT
            sm.id
        FROM set_metrics AS sm
        INNER JOIN lift_sets AS ls
            ON ls.id = sm.set_id
        WHERE ls.lift_day_id = :dayId
    """)
    fun getSetMetricIdsFromDayId(dayId: Int): Flow<List<Int>>
}