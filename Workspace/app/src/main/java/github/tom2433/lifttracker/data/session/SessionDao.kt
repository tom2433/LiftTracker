package github.tom2433.lifttracker.data.session

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(session: Session)

    @Transaction
    suspend fun insertWithNextSessionNumber(session: Session) {
        val nextSessionNumber = getNextSessionNumber(session.profile_id)
        insert(session.copy(session_number = nextSessionNumber))
    }

    // the entity that's updated has the same primary key as the entity that's passed in.
    // you can update some or all of the entity's other properties.
    @Update
    suspend fun update(session: Session)

    // @Delete annotation deletes an item or a list of items.
    // You need to pass the entities you want to delete
    // If you don't have the entity, you might have to fetch it before calling the delete() function
    @Delete
    suspend fun delete(session: Session)

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun getSession(id: Int): Flow<Session?>

    @Query("SELECT * FROM sessions ORDER BY session_number ASC")
    fun getAllSessions(): Flow<List<Session>>

    @Query("""
        SELECT s.*
        FROM sessions AS s
        INNER JOIN profiles AS p
            ON s.profile_id = p.id
        WHERE p.active = 1 AND s.in_progress = 1
        LIMIT 1
    """)
    fun getActiveSessionForActiveProfile(): Flow<Session?>

    @Query("""
        SELECT COUNT(*)
        FROM lift_sets AS ls
        INNER JOIN sessions AS s
            ON ls.session_id = s.id
        WHERE s.id = :sessionId
    """)
    fun getNumOfSetsForSession(sessionId: Int): Flow<Int>

    @Transaction
    suspend fun deleteAndRenumber(session: Session) {
        delete(session)
        stageSessionNumbersForDeleteAfter(session.profile_id, session.session_number)
        decrementStagedSessionNumbersAfter(session.profile_id)
    }

    @Query("""
        UPDATE sessions
        SET session_number = -(session_number)
        WHERE profile_id = :profileId
            AND session_number > :deletedSessionNumber
    """)
    suspend fun stageSessionNumbersForDeleteAfter(profileId: Int, deletedSessionNumber: Int)

    @Query("""
        UPDATE sessions
        SET session_number = (-session_number) - 1
        WHERE profile_id = :profileId
            AND session_number < 0
    """)
    suspend fun decrementStagedSessionNumbersAfter(profileId: Int)

    @Query("""
        SELECT COALESCE(MAX(session_number), 0) + 1
        FROM sessions
        WHERE profile_id = :profileId
    """)
    suspend fun getNextSessionNumber(profileId: Int): Int

    @Query("""
        SELECT
            mg.name AS muscleGroupName,
            COUNT(ls.id) AS setCount
        FROM lift_sets AS ls
        INNER JOIN muscle_groups AS mg
            ON mg.id = ls.muscle_group_id
        WHERE ls.session_id = :sessionId
        GROUP BY mg.id, mg.name
        ORDER BY ls.id
    """)
    fun getSetCountPerMuscleGroup(sessionId: Int): Flow<List<LiftSetCountPerMuscleGroup>>
}
