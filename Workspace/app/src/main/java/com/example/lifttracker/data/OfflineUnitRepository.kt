package com.example.lifttracker.data

import kotlinx.coroutines.flow.Flow

class OfflineUnitRepository(private val unitDao: UnitDao) : UnitRepository {
    override suspend fun insertUnit(unit: Unit) = unitDao.insert(unit)

    override suspend fun updateUnit(unit: Unit) = unitDao.update(unit)

    override suspend fun deleteUnit(unit: Unit) = unitDao.delete(unit)

    override fun getUnitStream(id: Int): Flow<Unit?> = unitDao.getUnit(id)

    override fun getAllUnitsStream(): Flow<List<Unit>> = unitDao.getAllUnits()

    override fun getUnitFromNameStream(unitName: String): Flow<Unit?> = unitDao.getUnitFromName(unitName)
}