package github.tom2433.lifttracker.data

import kotlinx.coroutines.flow.Flow

interface LiftRepository {
    suspend fun insertLift(lift: Lift)
    suspend fun updateLift(lift: Lift)
    suspend fun deleteLift(lift: Lift)
    fun getLiftStream(id: Int): Flow<Lift?>
    fun getAllLiftsFromMuscleGroupIdStream(muscle_group_id: Int): Flow<List<Lift>>
}