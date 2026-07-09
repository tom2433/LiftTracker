package github.tom2433.lifttracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LiftUnitDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(liftUnit: LiftUnit)

    @Update
    suspend fun update(liftUnit: LiftUnit)

    @Delete
    suspend fun delete(liftUnit: LiftUnit)

    @Query("SELECT * FROM lift_units WHERE id = :id")
    fun getLiftUnit(id: Int): Flow<LiftUnit?>

    @Query("SELECT * FROM lift_units")
    fun getAllLiftUnits(): Flow<List<LiftUnit>>

    @Query("""
        SELECT * FROM lift_units
        WHERE name = :liftUnitName
        LIMIT 1
    """)
    fun getLiftUnitFromName(liftUnitName: String): Flow<LiftUnit?>
}
