package com.example.lifttracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LiftDayDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(liftDay: LiftDay)

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
}