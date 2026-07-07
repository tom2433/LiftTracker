package github.tom2433.lifttracker.data

import kotlinx.coroutines.flow.Flow

interface MuscleGroupRepository {
    suspend fun insertMuscleGroup(muscleGroup: MuscleGroup)
    suspend fun updateMuscleGroup(muscleGroup: MuscleGroup)
    suspend fun deleteMuscleGroup(muscleGroup: MuscleGroup)
    fun getMuscleGroupStream(id: Int): Flow<MuscleGroup?>
    fun getAllMuscleGroupsForActiveProfileStream(): Flow<List<MuscleGroup>>

    // This stream exposes the reactive database aggregates used to calculate muscle-group details. - Codex
    fun getAllMuscleGroupDetailDataForActiveProfileStream(): Flow<List<MuscleGroupDetailData>>

    fun getMuscleGroupFromLiftIdStream(lift_id: Int): Flow<MuscleGroup?>
}
