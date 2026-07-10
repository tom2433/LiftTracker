package github.tom2433.lifttracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LiftDayDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(liftDay: LiftDay)

    @Transaction
    suspend fun insertWithNextDayNumber(liftDay: LiftDay) {
        val nextDayNumber = getNextDayNumber(liftDay.profile_id)
        insert(liftDay.copy(day_number = nextDayNumber))
    }

    // the entity that's updated has the same primary key as the entity that's passed in.
    // you can update some or all of the entity's other properties.
    @Update
    suspend fun update(liftDay: LiftDay)

    // @Delete annotation deletes an item or a list of items.
    // You need to pass the entities you want to delete
    // If you don't have the entity, you might have to fetch it before calling the delete() function
    @Delete
    suspend fun delete(liftDay: LiftDay)

    @Query("SELECT * FROM lift_days WHERE id = :id")
    fun getLiftDay(id: Int): Flow<LiftDay?>

    @Query("SELECT * FROM lift_days ORDER BY day_number ASC")
    fun getAllLiftDays(): Flow<List<LiftDay>>

    @Query("""
        SELECT ld.*
        FROM lift_days AS ld
        INNER JOIN profiles AS p
            ON ld.profile_id = p.id
        WHERE p.active = 1 AND ld.in_progress = 1
        LIMIT 1
    """)
    fun getActiveLiftDayForActiveProfile(): Flow<LiftDay?>

    @Query("""
        SELECT COUNT(*)
        FROM lift_sets AS ls
        INNER JOIN lift_days AS ld
            ON ls.lift_day_id = ld.id
        WHERE ld.id = :liftDayId
    """)
    fun getNumOfLiftsForDay(liftDayId: Int): Flow<Int>

    @Transaction
    suspend fun deleteAndRenumber(liftDay: LiftDay) {
        delete(liftDay)
        decrementDayNumbersAfter(liftDay.profile_id, liftDay.day_number)
    }

    @Query("""
        UPDATE lift_days
        SET day_number = day_number - 1
        WHERE profile_id = :profileId
            AND day_number > :deletedDayNumber
    """)
    suspend fun decrementDayNumbersAfter(profileId: Int, deletedDayNumber: Int)

    @Query("""
        SELECT COALESCE(MAX(day_number), 0) + 1
        FROM lift_days
        WHERE profile_id = :profileId
    """)
    suspend fun getNextDayNumber(profileId: Int): Int


}
