package github.tom2433.lifttracker.data

import kotlinx.coroutines.flow.Flow

class OfflineMuscleGroupRepository(private val muscleGroupDao: MuscleGroupDao) : MuscleGroupRepository {
    override suspend fun insertMuscleGroup(muscleGroup: MuscleGroup) = muscleGroupDao.insert(muscleGroup)

    override suspend fun updateMuscleGroup(muscleGroup: MuscleGroup) = muscleGroupDao.update(muscleGroup)

    override suspend fun deleteMuscleGroup(muscleGroup: MuscleGroup) = muscleGroupDao.delete(muscleGroup)

    override fun getMuscleGroupStream(id: Int): Flow<MuscleGroup?> = muscleGroupDao.getMuscleGroup(id)

    override fun getAllMuscleGroupsForActiveProfileStream(): Flow<List<MuscleGroup>> = muscleGroupDao.getAllMuscleGroupsForActiveProfile()

    // This delegates the active-profile detail query to Room so every referenced table remains observable. - Codex
    override fun getAllMuscleGroupDetailDataForActiveProfileStream(): Flow<List<MuscleGroupDetailData>> =
        muscleGroupDao.getAllMuscleGroupDetailDataForActiveProfile()
}
