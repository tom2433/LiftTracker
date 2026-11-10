package github.tom2433.lifttracker.data.session

import github.tom2433.lifttracker.data.structures.DisplaySessionLiftSetRow
import github.tom2433.lifttracker.data.structures.LiftNameAndFrequency
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import github.tom2433.lifttracker.data.structures.LiftSummary
import github.tom2433.lifttracker.data.structures.MuscleGroupNameAndFrequency
import github.tom2433.lifttracker.data.structures.SessionDetail
import github.tom2433.lifttracker.data.structures.SessionNameAndFrequency
import github.tom2433.lifttracker.data.structures.SessionSummary
import kotlinx.coroutines.flow.Flow

class OfflineSessionRepository(private val sessionDao: SessionDao) : SessionRepository {
    override fun getAllSessionsStream(): Flow<List<Session>> = sessionDao.getAllSessions()

    override fun getSessionStream(id: Int): Flow<Session?> = sessionDao.getSession(id)

    override suspend fun insertSession(session: Session) = sessionDao.insert(session)

    override suspend fun deleteSession(session: Session) = sessionDao.deleteAndRenumber(session)

    override suspend fun deleteSessionById(id: Int) = sessionDao.deleteSessionById(id)

    override suspend fun updateSession(session: Session) = sessionDao.update(session)

    override fun getActiveSessionForActiveProfileStream(): Flow<Session?> = sessionDao.getActiveSessionForActiveProfile()

    override suspend fun getNextSessionNumber(profile_id: Int): Int = sessionDao.getNextSessionNumber(profile_id)

    override suspend fun getNumOfSetsForSession(id: Int): Int = sessionDao.getNumOfSetsForSession(id)

    override suspend fun getNumOfLiftsForSession(id: Int): Int = sessionDao.getNumOfLiftsForSession(id)

    override fun getSetCountPerMuscleGroupForSessionIdStream(sessionId: Int): Flow<List<LiftSetCountPerMuscleGroup>> = sessionDao.getSetCountPerMuscleGroup(sessionId)

    override fun getMuscleGroupFrequencyListStream(
        activeProfileId: Int,
        startDate: String?,
        endDate: String?,
        sessionName: String?,
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String,
        lastNSessions: Int?
    ): Flow<List<LiftSetCountPerMuscleGroup>> = sessionDao.getMuscleGroupFrequencyList(
        activeProfileId = activeProfileId,
        startDate = startDate,
        endDate = endDate,
        sessionName = sessionName,
        muscleGroupName = muscleGroupName,
        liftName = liftName,
        sessionNote = sessionNote,
        lastNSessions = lastNSessions
    )

    override fun getNumSessionsStreamForTimeFrame(
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String,
        startDate: String?,
        endDate: String?
    ): Flow<Int> = sessionDao.getNumSessionsFromTimeFrame(
        muscleGroupName = muscleGroupName,
        liftName = liftName,
        sessionNote = sessionNote,
        startDate = startDate,
        endDate = endDate
    )

    override fun getSessionDetailsListStreamForSessionScreen(
        activeProfileId: Int,
        startDate: String?,
        endDate: String?,
        fetchLimit: Int,
        sessionName: String?,
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String,
        lastNSessions: Int?
    ): Flow<List<SessionDetail>> = sessionDao.getSessionDetailsForSessionScreen(
        activeProfileId = activeProfileId,
        startDate = startDate,
        endDate = endDate,
        fetchLimit = fetchLimit,
        sessionName = sessionName,
        muscleGroupName = muscleGroupName,
        liftName = liftName,
        sessionNote = sessionNote,
        lastNSessions = lastNSessions
    )

    override suspend fun sessionIsInProgress(): Boolean = sessionDao.sessionIsInProgress()

    override suspend fun switchSessionIdToInProgress(id: Int) = sessionDao.switchSessionIdToInProgress(id)

    override suspend fun sessionHasUnfinishedLifts(id: Int): Boolean = sessionDao.sessionHasUnfinishedLifts(id)

    override suspend fun sessionHasLifts(id: Int): Boolean = sessionDao.sessionHasLifts(id)

    override suspend fun finishSession(id: Int): Boolean = sessionDao.finishSession(id)

    override suspend fun getSessionById(id: Int): Session? = sessionDao.getSessionById(id)

    override fun getDisplaySessionLiftSetRowsStream(id: Int): Flow<List<DisplaySessionLiftSetRow>> = sessionDao.getDisplaySessionLiftSetRows(id)

    override suspend fun moveLiftSet(liftSetId: Int, down: Boolean) = sessionDao.moveLiftSet(liftSetId, down)

    override fun getUniqueSessionNamesAndFrequenciesStream(
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String,
        fetchLimit: Int,
        startDate: String?,
        endDate: String?,
        lastNSessions: Int?
    ): Flow<List<SessionNameAndFrequency>> = sessionDao.getUniqueSessionNamesAndFrequencies(
        muscleGroupName = muscleGroupName,
        liftName = liftName,
        sessionNote = sessionNote,
        fetchLimit = fetchLimit,
        startDate = startDate,
        endDate = endDate,
        lastNSessions = lastNSessions
    )

    override fun getNumberOfUniqueSessionNamesAndFrequenciesStream(
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String,
        startDate: String?,
        endDate: String?,
        lastNSessions: Int?
    ): Flow<Int> = sessionDao.getNumberOfUniqueSessionNamesAndFrequencies(
        muscleGroupName = muscleGroupName,
        liftName = liftName,
        sessionNote = sessionNote,
        startDate = startDate,
        endDate = endDate,
        lastNSessions = lastNSessions
    )

    override fun getNumSessionsStreamForFilteredTimeFrame(
        startDate: String?,
        endDate: String?,
        sessionName: String?,
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String,
        lastNSessions: Int?
    ): Flow<Int> = sessionDao.getNumSessionsForFilteredTimeFrame(
        startDate = startDate,
        endDate = endDate,
        sessionName = sessionName,
        muscleGroupName = muscleGroupName,
        liftName = liftName,
        sessionNote = sessionNote,
        lastNSessions = lastNSessions
    )

    override fun getUniqueMuscleGroupNamesAndFrequenciesStream(
        sessionName: String?,
        liftName: String?,
        sessionNote: String,
        fetchLimit: Int,
        startDate: String?,
        endDate: String?,
        lastNSessions: Int?
    ): Flow<List<MuscleGroupNameAndFrequency>> = sessionDao.getUniqueMuscleGroupNamesAndFrequencies(
        sessionName = sessionName,
        liftName = liftName,
        sessionNote = sessionNote,
        fetchLimit = fetchLimit,
        startDate = startDate,
        endDate = endDate,
        lastNSessions = lastNSessions
    )

    override fun getUniqueLiftNamesAndFrequenciesStream(
        sessionName: String?,
        muscleGroupName: String?,
        sessionNote: String,
        fetchLimit: Int,
        startDate: String?,
        endDate: String?,
        lastNSessions: Int?
    ): Flow<List<LiftNameAndFrequency>> = sessionDao.getUniqueLiftNamesAndFrequencies(
        sessionName = sessionName,
        muscleGroupName = muscleGroupName,
        sessionNote = sessionNote,
        fetchLimit = fetchLimit,
        startDate = startDate,
        endDate = endDate,
        lastNSessions = lastNSessions
    )

    override fun getNumberOfUniqueMuscleGroupNamesAndFrequenciesStream(
        sessionName: String?,
        liftName: String?,
        sessionNote: String,
        startDate: String?,
        endDate: String?,
        lastNSessions: Int?
    ): Flow<Int> = sessionDao.getNumberOfUniqueMuscleGroupNamesAndFrequencies(
        sessionName = sessionName,
        liftName = liftName,
        sessionNote = sessionNote,
        startDate = startDate,
        endDate = endDate,
        lastNSessions = lastNSessions
    )

    override fun getNumberOfUniqueLiftNamesAndFrequenciesStream(
        sessionName: String?,
        muscleGroupName: String?,
        sessionNote: String,
        startDate: String?,
        endDate: String?,
        lastNSessions: Int?
    ): Flow<Int> = sessionDao.getNumberOfUniqueLiftNamesAndFrequencies(
        sessionName = sessionName,
        muscleGroupName = muscleGroupName,
        sessionNote = sessionNote,
        startDate = startDate,
        endDate = endDate,
        lastNSessions = lastNSessions
    )

    override suspend fun getSessionSummaryFromId(
        id: Int,
        startDate: String?,
        numSessionsToFetch: Int?
    ): SessionSummary = sessionDao.getSessionSummaryForSessionId(
        id = id,
        startDate = startDate,
        numSessionsToFetch = numSessionsToFetch
    )

    override suspend fun getLiftSummaryForLiftAndSession(
        sessionId: Int,
        liftId: Int,
        startDate: String?,
        numSessionsToFetch: Int?
    ): LiftSummary = sessionDao.getLiftSummaryForSessionIdAndLiftId(
        sessionId = sessionId,
        liftId = liftId,
        startDate = startDate,
        numSessionsToFetch = numSessionsToFetch
    )
}
