package com.example.lifttracker.data

import kotlinx.coroutines.flow.Flow

interface LiftRepository {
    suspend fun insertLift(lift: Lift)
    suspend fun updateLift(lift: Lift)
    suspend fun deleteLift(lift: Lift)
    fun getLiftStream(id: Int): Flow<Lift?>
}