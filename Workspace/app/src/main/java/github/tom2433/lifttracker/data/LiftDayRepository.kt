package github.tom2433.lifttracker.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieval of [LiftDay] from a given data source
 * These functions all map to the DAO implementation.
 */
interface LiftDayRepository {
    /**
     * Retrieve all days from the given data source
     */
    fun getAllLiftDaysStream(): Flow<List<LiftDay>>

    /**
     * Retrieve a day from the given data source that matches with the id.
     */
    fun getLiftDayStream(id: Int): Flow<LiftDay?>

    /**
     * Insert day in the data source
     */
    suspend fun insertLiftDay(liftDay: LiftDay)

    /**
     * Delete day from the data source
     */
    suspend fun deleteLiftDay(liftDay: LiftDay)

    /**
     * Update day in the data source
     */
    suspend fun updateLiftDay(liftDay: LiftDay)
}