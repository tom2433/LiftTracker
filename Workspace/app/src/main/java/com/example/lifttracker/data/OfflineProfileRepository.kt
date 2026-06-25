package com.example.lifttracker.data

import kotlinx.coroutines.flow.Flow

class OfflineProfileRepository(private val profileDao: ProfileDao) : ProfileRepository {
    override suspend fun insertProfile(profile: Profile) = profileDao.insert(profile)

    override suspend fun updateProfile(profile: Profile) = profileDao.update(profile)

    override suspend fun deleteProfile(profile: Profile) = profileDao.delete(profile)

    override fun getProfileStream(id: Int): Flow<Profile?> = profileDao.getProfile(id)

    override fun getAllProfilesStream(): Flow<List<Profile>> = profileDao.getAllProfiles()

}