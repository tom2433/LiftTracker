package com.example.lifttracker.data

import kotlinx.coroutines.flow.Flow

interface UnitRepository {
    suspend fun insertUnit(unit: Unit)
    suspend fun updateUnit(unit: Unit)
    suspend fun deleteUnit(unit: Unit)
    fun getUnitStream(id: Int): Flow<Unit?>
    fun getAllUnitsStream(): Flow<List<Unit>>
    fun getUnitFromNameStream(unitName: String): Flow<Unit?>
}