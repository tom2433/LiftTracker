package github.tom2433.lifttracker.data.session

import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieval of [Session] from a given data source
 * These functions all map to the DAO implementation.
 */
interface SessionRepository {
    /**
     * Retrieve all sessions from the given data source
     */
    fun getAllSessionsStream(): Flow<List<Session>>

    /**
     * Retrieve a session from the given data source that matches with the id.
     */
    fun getSessionStream(id: Int): Flow<Session?>

    /**
     * Insert session in the data source
     */
    suspend fun insertSession(session: Session)

    /**
     * Delete session from the data source
     */
    suspend fun deleteSession(session: Session)

    /**
     * Update session in the data source
     */
    suspend fun updateSession(session: Session)

    /**
     * Retrieve currently in progress session for the currently active profile
     */
    fun getActiveSessionForActiveProfileStream(): Flow<Session?>

    /**
     * Get the next session number for the profile id.
     */
    suspend fun getNextSessionNumber(profile_id: Int): Int

    /**
     * Retrieve number of sets for a given session id.
     */
    fun getNumOfSetsForSession(id: Int): Flow<Int>

    fun getSetCountPerMuscleGroupForSessionIdStream(sessionId: Int): Flow<List<LiftSetCountPerMuscleGroup>>
}
