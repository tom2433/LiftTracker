package github.tom2433.lifttracker.data.profile

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(profile: Profile)

    @Update
    suspend fun update(profile: Profile)

    @Delete
    suspend fun delete(profile: Profile)

    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getProfile(id: Int): Flow<Profile?>

    @Query("SELECT * FROM profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<Profile>>

    @Query("""
        SELECT * FROM profiles
        WHERE active = 1
        LIMIT 1
    """)
    fun getActiveProfile(): Flow<Profile?>
}