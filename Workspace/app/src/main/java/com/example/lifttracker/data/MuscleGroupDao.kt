package com.example.lifttracker.data

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
}