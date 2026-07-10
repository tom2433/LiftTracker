package github.tom2433.lifttracker.data.liftset

import kotlinx.coroutines.flow.Flow

interface LiftSetRepository {
    suspend fun insertLiftSet(liftSet: LiftSet)
    suspend fun updateLiftSet(liftSet: LiftSet)
    suspend fun deleteLiftSet(liftSet: LiftSet)
    fun getLiftSetStream(id: Int): Flow<LiftSet?>
}