package com.example.lifttracker.data

import kotlinx.coroutines.flow.Flow

class OfflineLiftSetRepository(private val liftSetDao: LiftSetDao) : LiftSetRepository {
    override suspend fun insertLiftSet(liftSet: LiftSet) = liftSetDao.insert(liftSet)

    override suspend fun updateLiftSet(liftSet: LiftSet) = liftSetDao.update(liftSet)

    override suspend fun deleteLiftSet(liftSet: LiftSet) = liftSetDao.delete(liftSet)

    override fun getLiftSetStream(id: Int): Flow<LiftSet?> = liftSetDao.getLiftSet(id)

}