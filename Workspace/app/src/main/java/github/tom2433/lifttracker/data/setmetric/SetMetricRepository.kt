package github.tom2433.lifttracker.data.setmetric

import kotlinx.coroutines.flow.Flow

interface SetMetricRepository {
    suspend fun insertSetMetric(setMetric: SetMetric)
    suspend fun updateSetMetric(setMetric: SetMetric)
    suspend fun deleteSetMetric(setMetric: SetMetric)
    fun getSetMetricStream(id: Int): Flow<SetMetric?>
}