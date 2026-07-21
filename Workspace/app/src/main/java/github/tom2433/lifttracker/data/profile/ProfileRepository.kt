package github.tom2433.lifttracker.data.profile

import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun insertProfile(profile: Profile)
    suspend fun updateProfile(profile: Profile)
    suspend fun deleteProfile(profile: Profile)
    fun getProfileStream(id: Int): Flow<Profile?>
    fun getActiveProfileStream(): Flow<Profile?>

    fun getAllProfilesStream(): Flow<List<Profile>>
}