package com.example.lifttracker.data

import kotlinx.coroutines.flow.Flow

interface MuscleGroupRepository {
    suspend fun insertMuscleGroup(muscleGroup: MuscleGroup)
    suspend fun updateMuscleGroup(muscleGroup: MuscleGroup)
    suspend fun deleteMuscleGroup(muscleGroup: MuscleGroup)
    fun getMuscleGroupStream(id: Int): Flow<MuscleGroup?>
    fun getAllMuscleGroupsForActiveProfileStream(): Flow<List<MuscleGroup>>
}