package github.tom2433.lifttracker.data.liftday

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

    /**
     * Retrieve currently in progress lift day for the currently active profile
     */
    fun getActiveLiftDayForActiveProfileStream(): Flow<LiftDay?>

    /**
     * get the next day number for the profile id
     */
    suspend fun getNextDayNumber(profile_id: Int): Int

    /**
     * Retrieve number of lifts for a given lift day id
     */
    fun getNumOfLiftsForDay(id: Int): Flow<Int>
}