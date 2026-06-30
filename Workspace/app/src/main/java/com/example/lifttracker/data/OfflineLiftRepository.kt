package com.example.lifttracker.data

import kotlinx.coroutines.flow.Flow

class OfflineLiftRepository(private val liftDao: LiftDao) : LiftRepository {
    override suspend fun insertLift(lift: Lift) = liftDao.insert(lift)

    override suspend fun updateLift(lift: Lift) = liftDao.update(lift)

    override suspend fun deleteLift(lift: Lift) = liftDao.delete(lift)

    override fun getLiftStream(id: Int): Flow<Lift?> = liftDao.getLift(id)

    override fun getAllLiftsFromMuscleGroupIdStream(muscle_group_id: Int): Flow<List<Lift>> = liftDao.getAllLiftsFromMuscleGroupId(muscle_group_id)
}