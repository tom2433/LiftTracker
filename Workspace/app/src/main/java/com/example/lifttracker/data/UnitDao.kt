package com.example.lifttracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(unit: Unit)

    @Update
    suspend fun update(unit: Unit)

    @Delete
    suspend fun delete(unit: Unit)

    @Query("SELECT * FROM units WHERE id = :id")
    fun getUnit(id: Int): Flow<Unit?>
}