package github.tom2433.lifttracker.data.setmetric

import kotlinx.coroutines.flow.Flow

class OfflineSetMetricRepository(private val setMetricDao: SetMetricDao) : SetMetricRepository {
    override suspend fun insertSetMetric(setMetric: SetMetric) = setMetricDao.insert(setMetric)

    override suspend fun updateSetMetric(setMetric: SetMetric) = setMetricDao.update(setMetric)

    override suspend fun deleteSetMetric(setMetric: SetMetric) = setMetricDao.delete(setMetric)

    override fun getSetMetricStream(id: Int): Flow<SetMetric?> = setMetricDao.getSetMetric(id)

    override fun getSetMetricIdsFromSessionIdStream(sessionId: Int): Flow<List<Int>> = setMetricDao.getSetMetricIdsFromSessionId(sessionId)

    override suspend fun getSetMetricsFromActiveSession(): List<SetMetric> = setMetricDao.getSetMetricsFromActiveSession()
}