package com.example.lifttracker.data

import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun insertProfile(profile: Profile)
    suspend fun updateProfile(profile: Profile)
    suspend fun deleteProfile(profile: Profile)
    fun getProfileStream(id: Int): Flow<Profile?>
}