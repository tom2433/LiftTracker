package github.tom2433.lifttracker.data.session

import github.tom2433.lifttracker.data.structures.DisplaySessionLiftSetRow
import github.tom2433.lifttracker.data.structures.LiftNameAndFrequency
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import github.tom2433.lifttracker.data.structures.MuscleGroupNameAndFrequency
import github.tom2433.lifttracker.data.structures.SessionDetail
import github.tom2433.lifttracker.data.structures.SessionNameAndFrequency
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

    suspend fun deleteSessionById(id: Int)

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
    suspend fun getNumOfSetsForSession(id: Int): Int

    suspend fun getNumOfLiftsForSession(id: Int): Int

    fun getSetCountPerMuscleGroupForSessionIdStream(sessionId: Int): Flow<List<LiftSetCountPerMuscleGroup>>

    fun getMuscleGroupFrequencyListStream(
        activeProfileId: Int,
        startDate: String?,
        endDate: String?,
        sessionName: String?,
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String
    ): Flow<List<LiftSetCountPerMuscleGroup>>

    fun getNumSessionsStreamForTimeFrame(
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String,
        startDate: String?,
        endDate: String?
    ): Flow<Int>

    fun getSessionDetailsListStreamForSessionScreen(
        activeProfileId: Int,
        startDate: String?,
        endDate: String?,
        fetchLimit: Int,
        sessionName: String?,
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String
    ): Flow<List<SessionDetail>>

    suspend fun sessionIsInProgress(): Boolean

    suspend fun switchSessionIdToInProgress(id: Int)

    suspend fun sessionHasUnfinishedLifts(id: Int): Boolean

    suspend fun sessionHasLifts(id: Int): Boolean

    suspend fun finishSession(id: Int): Boolean

    suspend fun getSessionById(id: Int): Session?

    fun getDisplaySessionLiftSetRowsStream(id: Int): Flow<List<DisplaySessionLiftSetRow>>

    suspend fun moveLiftSet(liftSetId: Int, down: Boolean = false)

    fun getUniqueSessionNamesAndFrequenciesStream(
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String,
        fetchLimit: Int,
        startDate: String?,
        endDate: String?
    ): Flow<List<SessionNameAndFrequency>>

    fun getNumberOfUniqueSessionNamesAndFrequenciesStream(
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String,
        startDate: String?,
        endDate: String?
    ): Flow<Int>

    fun getNumSessionsStreamForFilteredTimeFrame(
        startDate: String?,
        endDate: String?,
        sessionName: String?,
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String
    ): Flow<Int>

    fun getUniqueMuscleGroupNamesAndFrequenciesStream(
        sessionName: String?,
        liftName: String?,
        sessionNote: String,
        fetchLimit: Int,
        startDate: String?,
        endDate: String?
    ): Flow<List<MuscleGroupNameAndFrequency>>

    fun getUniqueLiftNamesAndFrequenciesStream(
        sessionName: String?,
        muscleGroupName: String?,
        sessionNote: String,
        fetchLimit: Int,
        startDate: String?,
        endDate: String?
    ): Flow<List<LiftNameAndFrequency>>

    fun getNumberOfUniqueMuscleGroupNamesAndFrequenciesStream(
        sessionName: String?,
        liftName: String?,
        sessionNote: String,
        startDate: String?,
        endDate: String?
    ): Flow<Int>

    fun getNumberOfUniqueLiftNamesAndFrequenciesStream(
        sessionName: String?,
        muscleGroupName: String?,
        sessionNote: String,
        startDate: String?,
        endDate: String?
    ): Flow<Int>

    suspend fun getSessionSummaryFromId(
        id: Int,
        startDate: String
    ): Triple<String, String, String>

    suspend fun getLiftSummaryForLiftAndSession(
        sessionId: Int,
        liftId: Int,
        startDate: String
    ): String
}
