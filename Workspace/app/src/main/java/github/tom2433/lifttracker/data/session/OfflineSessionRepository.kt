package github.tom2433.lifttracker.data.session

import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import kotlinx.coroutines.flow.Flow

class OfflineSessionRepository(private val sessionDao: SessionDao) : SessionRepository {
    override fun getAllSessionsStream(): Flow<List<Session>> = sessionDao.getAllSessions()

    override fun getSessionStream(id: Int): Flow<Session?> = sessionDao.getSession(id)

    override suspend fun insertSession(session: Session) = sessionDao.insert(session)

    override suspend fun deleteSession(session: Session) = sessionDao.deleteAndRenumber(session)

    override suspend fun updateSession(session: Session) = sessionDao.update(session)

    override fun getActiveSessionForActiveProfileStream(): Flow<Session?> = sessionDao.getActiveSessionForActiveProfile()

    override suspend fun getNextSessionNumber(profile_id: Int): Int = sessionDao.getNextSessionNumber(profile_id)

    override fun getNumOfSetsForSession(id: Int): Flow<Int> = sessionDao.getNumOfSetsForSession(id)

    override fun getSetCountPerMuscleGroupForSessionIdStream(sessionId: Int): Flow<List<LiftSetCountPerMuscleGroup>> = sessionDao.getSetCountPerMuscleGroup(sessionId)
}
