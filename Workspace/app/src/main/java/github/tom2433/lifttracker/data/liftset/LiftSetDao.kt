package github.tom2433.lifttracker.data.liftset

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LiftSetDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(liftSet: LiftSet)

    @Update
    suspend fun update(liftSet: LiftSet)

    @Delete
    suspend fun delete(liftSet: LiftSet)

    @Query("SELECT * FROM lift_sets WHERE id = :id")
    fun getLiftSet(id: Int): Flow<LiftSet?>
}