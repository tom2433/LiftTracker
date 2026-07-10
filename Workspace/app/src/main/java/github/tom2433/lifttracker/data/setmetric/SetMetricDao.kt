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
}