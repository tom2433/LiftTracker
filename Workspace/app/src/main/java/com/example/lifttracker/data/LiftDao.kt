package com.example.lifttracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LiftDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
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
}