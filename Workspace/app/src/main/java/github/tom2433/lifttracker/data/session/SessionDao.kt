package github.tom2433.lifttracker.data.session

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.structures.DisplaySessionLiftSetRow
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import github.tom2433.lifttracker.data.structures.SessionDetail
import github.tom2433.lifttracker.data.structures.SessionDetailData
import github.tom2433.lifttracker.data.structures.SessionMuscleGroupCountData
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
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
        SELECT COUNT(ls.id)
        FROM lift_sets AS ls
        INNER JOIN sessions AS s
            ON ls.session_id = s.id
        WHERE s.id = :sessionId
    """)
    suspend fun getNumOfSetsForSession(sessionId: Int): Int

    @Query("""
        SELECT COUNT(DISTINCT l.id)
        FROM lifts AS l
        INNER JOIN lift_sets AS ls
            ON ls.lift_id = l.id
        INNER JOIN sessions AS s
            ON s.id = ls.session_id
        WHERE s.id = :sessionId
    """)
    suspend fun getNumOfLiftsForSession(sessionId: Int): Int

    @Transaction
    suspend fun deleteAndRenumber(session: Session) {
        delete(session)
        stageSessionNumbersForDeleteAfter(session.profile_id, session.session_number)
        decrementStagedSessionNumbersAfter(session.profile_id)
    }

    @Query("""
        SELECT *
        FROM sessions AS s
        WHERE s.id = :id
        LIMIT 1
    """)
    suspend fun getSessionById(id: Int): Session?

    @Transaction
    suspend fun deleteSessionById(id: Int) {
        val sessionToDelete: Session = getSessionById(id) ?: return
        deleteAndRenumber(sessionToDelete)
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

    @Query("""
        SELECT
            mg.name AS muscleGroupName,
            COUNT(ls.id) AS setCount
        FROM lift_sets AS ls
        INNER JOIN muscle_groups AS mg
            ON mg.id = ls.muscle_group_id
        WHERE ls.session_id IN (
            SELECT s.id
            FROM sessions AS s
            WHERE s.date <= :endDate
                AND s.date >= :startDate
                AND s.profile_id = :activeProfileId
        )
        GROUP BY mg.id, mg.name
        ORDER BY mg.name
    """)
    fun getMuscleGroupFrequencyListFromStartEndDates(
        activeProfileId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<LiftSetCountPerMuscleGroup>>

    fun getMuscleGroupFrequencyList(
        activeProfileId: Int,
        startDate: String?,
        endDate: String?
    ): Flow<List<LiftSetCountPerMuscleGroup>> {
        val realStartDate: String = startDate ?: "2025-07-03"
        val realEndDate: String = endDate ?: DateTimeCalculator.getCurrentIsoDate()

        return getMuscleGroupFrequencyListFromStartEndDates(
            activeProfileId = activeProfileId,
            startDate = realStartDate,
            endDate = realEndDate
        )
    }
    @Query("""
        SELECT COUNT(s.id)
        FROM sessions AS s
        WHERE s.date <= :endDate
            AND s.date >= :startDate
            AND s.profile_id = :activeProfileId
    """)
    fun getNumSessionsFromStartEndDates(
        activeProfileId: Int,
        startDate: String,
        endDate: String
    ): Flow<Int>

    fun getNumSessionsFromTimeFrame(
        activeProfileId: Int,
        startDate: String?,
        endDate: String?
    ): Flow<Int> {
        val realStartDate: String = startDate ?: "2025-07-03"
        val realEndDate: String = endDate ?: DateTimeCalculator.getCurrentIsoDate()

        return getNumSessionsFromStartEndDates(
            activeProfileId = activeProfileId,
            startDate = realStartDate,
            endDate = realEndDate
        )
    }

    @Query("""
        SELECT
            s.id AS sessionId,
            s.session_label AS sessionName,
            s.note AS sessionNote,
            s.date AS sessionDate,
            s.in_progress AS sessionInProgress
        FROM sessions AS s
        WHERE s.profile_id = :activeProfileId
            AND s.date >= :startDate
            AND s.date <= :endDate
        ORDER BY s.date DESC, s.id DESC
        LIMIT :fetchLimit
    """)
    fun getSessionDetailDataListFromStartEndDates(
        activeProfileId: Int,
        startDate: String,
        endDate: String,
        fetchLimit: Int
    ): Flow<List<SessionDetailData>>

    @Query("""
        SELECT
            ls.session_id AS sessionId,
            mg.name AS muscleGroupName,
            COUNT(ls.id) AS setCount
        FROM lift_sets AS ls
        INNER JOIN muscle_groups AS mg
            ON mg.id = ls.muscle_group_id
        WHERE ls.session_id IN (
            SELECT s.id
            FROM sessions AS s
            WHERE s.profile_id = :activeProfileId
                AND s.date >= :startDate
                AND s.date <= :endDate
            ORDER BY s.date DESC, s.id DESC
            LIMIT :fetchLimit
        )
        GROUP BY ls.session_id, mg.id, mg.name
        ORDER BY ls.session_id, mg.name
    """)
    fun getSessionMuscleGroupCountDataListFromStartEndDates(
        activeProfileId: Int,
        startDate: String,
        endDate: String,
        fetchLimit: Int
    ): Flow<List<SessionMuscleGroupCountData>>

    fun getSessionDetailsForSessionScreen(
        activeProfileId: Int,
        startDate: String?,
        endDate: String?,
        fetchLimit: Int
    ): Flow<List<SessionDetail>> {
        val realStartDate: String = startDate ?: "2025-07-03"
        val realEndDate: String = endDate ?: DateTimeCalculator.getCurrentIsoDate()

        val sessionDetailDataListFlow: Flow<List<SessionDetailData>> = getSessionDetailDataListFromStartEndDates(
            activeProfileId = activeProfileId,
            startDate = realStartDate,
            endDate = realEndDate,
            fetchLimit = fetchLimit
        )

        val muscleGroupCountDataListFlow: Flow<List<SessionMuscleGroupCountData>> = getSessionMuscleGroupCountDataListFromStartEndDates(
            activeProfileId = activeProfileId,
            startDate = realStartDate,
            endDate = realEndDate,
            fetchLimit = fetchLimit
        )

        return combine(
            sessionDetailDataListFlow,
            muscleGroupCountDataListFlow
        ) { sessionDetailDataList, muscleGroupCountDataList ->
            // turn into a map where sessionId points to list of SessionMuscleGroupCountData objects
            val countsBySessionId = muscleGroupCountDataList.groupBy { it.sessionId }

            sessionDetailDataList.map { sessionDetailData ->
                val muscleGroupCounts = countsBySessionId[sessionDetailData.sessionId].orEmpty()

                SessionDetail(
                    sessionId = sessionDetailData.sessionId,
                    sessionName = sessionDetailData.sessionName,
                    sessionNote = sessionDetailData.sessionNote,
                    sessionDateIso = sessionDetailData.sessionDate,
                    sessionInProgress = sessionDetailData.sessionInProgress,
                    visible = false,
                    selected = false,
                    menuExpanded = false,
                    // create a list for each SessionMuscleGroupCountData in the list pointed to by sessionId
                    liftSetCountPerMuscleGroupList = muscleGroupCounts.map { countData ->
                        LiftSetCountPerMuscleGroup(
                            muscleGroupName = countData.muscleGroupName,
                            setCount = countData.setCount
                        )
                    }
                )
            }
        }
    }

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM sessions AS s
            INNER JOIN profiles AS p
                ON p.id = s.profile_id
            WHERE p.active = 1 AND s.in_progress = 1
        )
    """)
    suspend fun sessionIsInProgress(): Boolean

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM set_metrics AS sm
            INNER JOIN lift_sets AS ls
                ON ls.id = sm.set_id
            INNER JOIN sessions AS s
                ON s.id = ls.session_id
            WHERE s.id = :id
                AND sm.value = -1.0
        )
    """)
    suspend fun sessionHasUnfinishedLifts(id: Int): Boolean

    @Query("""
        UPDATE sessions
        SET in_progress = 1
        WHERE id = :id 
    """)
    suspend fun switchSessionIdToInProgress(id: Int)

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM set_metrics AS sm
            INNER JOIN lift_sets AS ls
                ON ls.id = sm.set_id
            INNER JOIN sessions AS s
                ON s.id = ls.session_id
            WHERE s.id = :id
        )
    """)
    suspend fun sessionHasLifts(id: Int): Boolean

    @Query("""
        SELECT DISTINCT(ls.id)
        FROM lift_sets AS ls
        INNER JOIN set_metrics AS sm
            ON sm.set_id = ls.id
        WHERE sm.value = -1.0
            AND ls.session_id = :id
        ORDER BY ls.session_set_number DESC            
    """)
    suspend fun getSortedInvalidLiftSetIdsFromSessionId(id: Int): List<Int>

    @Query("""
        SELECT *
        FROM lift_sets AS ls
        WHERE ls.id = :liftSetId
        LIMIT 1
    """)
    suspend fun getLiftSetById(liftSetId: Int): LiftSet?

    @Query("""
        DELETE FROM lift_sets WHERE id = :liftSetId
    """)
    suspend fun deletePreparedLiftSetById(liftSetId: Int)

    suspend fun deleteLiftSetById(liftSetId: Int) {
        // retrieve the lift set object to delete (needed for properties)
        val liftSet: LiftSet = getLiftSetById(liftSetId) ?: return

        // delete the lift set
        deletePreparedLiftSetById(liftSetId)

        // renumber remaining set labels after deletion
        updateLiftSetSetLabelsAfterLiftSetDelete(
            sessionId = liftSet.session_id,
            liftId = liftSet.lift_id,
            deletedLiftSetNumber = liftSet.lift_set_number
        )

        // stage remaining lift set numbers after deletion
        stageLiftSetNumbersAfterDelete(
            sessionId = liftSet.session_id,
            liftId = liftSet.lift_id,
            deletedLiftSetNumber = liftSet.lift_set_number
        )

        // renumber lift set numbers
        decrementStagedLiftSetNumbersAfterDelete(
            sessionId = liftSet.session_id,
            liftId = liftSet.lift_id
        )

        // stage remaining session set numbers after deletion
        stageSessionSetNumbersAfterDelete(
            sessionId = liftSet.session_id,
            deletedSessionSetNumber = liftSet.session_set_number
        )

        // renumber session set numbers
        decrementStagedSessionSetNumbersAfterDelete(
            sessionId = liftSet.session_id
        )

        // stage remaining muscle group session set numbers after deletion
        stageMuscleGroupSessionSetNumbersAfterDelete(
            sessionId = liftSet.session_id,
            muscleGroupId = liftSet.muscle_group_id,
            deletedMuscleGroupSessionSetNumber = liftSet.muscle_group_session_set_number
        )

        // renumber muscle group session set numbers
        decrementStagedMuscleGroupSessionSetNumbersAfterDelete(
            sessionId = liftSet.session_id,
            muscleGroupId = liftSet.muscle_group_id
        )
    }

    @Query("""
        UPDATE lift_sets
        SET set_label = 'Set ' || (lift_set_number - 1)
        WHERE session_id = :sessionId
            AND lift_id = :liftId
            AND lift_set_number > :deletedLiftSetNumber
            AND set_label = 'Set ' || lift_set_number
    """)
    suspend fun updateLiftSetSetLabelsAfterLiftSetDelete(
        sessionId: Int,
        liftId: Int,
        deletedLiftSetNumber: Int
    )

    @Query("""
        UPDATE lift_sets
        SET lift_set_number = -lift_set_number
        WHERE session_id = :sessionId
            AND lift_id = :liftId
            AND lift_set_number > :deletedLiftSetNumber
    """)
    suspend fun stageLiftSetNumbersAfterDelete(
        sessionId: Int,
        liftId: Int,
        deletedLiftSetNumber: Int
    )

    @Query("""
        UPDATE lift_sets
        SET lift_set_number = (-lift_set_number) - 1
        WHERE session_id = :sessionId
            AND lift_id = :liftId
            AND lift_set_number < 0
    """)
    suspend fun decrementStagedLiftSetNumbersAfterDelete(
        sessionId: Int,
        liftId: Int,
    )

    @Query("""
        UPDATE lift_sets
        SET session_set_number = -session_set_number
        WHERE session_id = :sessionId
            AND session_set_number > :deletedSessionSetNumber
    """)
    suspend fun stageSessionSetNumbersAfterDelete(
        sessionId: Int,
        deletedSessionSetNumber: Int
    )

    @Query("""
        UPDATE lift_sets
        SET session_set_number = (-session_set_number) - 1
        WHERE session_id = :sessionId
            AND session_set_number < 0
    """)
    suspend fun decrementStagedSessionSetNumbersAfterDelete(
        sessionId: Int
    )

    @Query("""
        UPDATE lift_sets
        SET muscle_group_session_set_number = -muscle_group_session_set_number
        WHERE session_id = :sessionId
            AND muscle_group_id = :muscleGroupId
            AND muscle_group_session_set_number > :deletedMuscleGroupSessionSetNumber
    """)
    suspend fun stageMuscleGroupSessionSetNumbersAfterDelete(
        sessionId: Int,
        muscleGroupId: Int,
        deletedMuscleGroupSessionSetNumber: Int
    )

    @Query("""
        UPDATE lift_sets
        SET muscle_group_session_set_number = (-muscle_group_session_set_number) - 1
        WHERE session_id = :sessionId
            AND muscle_group_id = :muscleGroupId
            AND muscle_group_session_set_number < 0
    """)
    suspend fun decrementStagedMuscleGroupSessionSetNumbersAfterDelete(
        sessionId: Int,
        muscleGroupId: Int
    )

    @Query("""
        UPDATE sessions
        SET in_progress = 0
        WHERE id = :id
    """)
    suspend fun saveSession(id: Int)

    @Transaction
    suspend fun finishSession(id: Int): Boolean {
        // if session has unfinished lifts, delete these unfinished lifts
        if (sessionHasUnfinishedLifts(id)) {
            // retrieve all lift set objects where either of its set metrics are -1.0
            val sortedInvalidLiftSetIds: List<Int> = getSortedInvalidLiftSetIdsFromSessionId(id)

            // delete each invalid lift set
            for (liftSetId in sortedInvalidLiftSetIds) {
                deleteLiftSetById(liftSetId)
            }
        }

        // if there are remaining lift sets, save session by setting in progress = false
        // and return true
        if (sessionHasLifts(id)) {
            saveSession(id)
            return true
        } else {
            // if there aren't, delete the session and return false
            // retrieve session to delete
            deleteSessionById(id)
            return false
        }
    }

    @Query("""
        SELECT
            -- LiftSet object (LiftSet rows preceded by set_)
            ls.id AS set_id,
            ls.session_id AS set_session_id,
            ls.lift_id AS set_lift_id,
            ls.muscle_group_id AS set_muscle_group_id,
            ls.lift_set_number AS set_lift_set_number,
            ls.session_set_number AS set_session_set_number,
            ls.muscle_group_session_set_number AS set_muscle_group_session_set_number,
            ls.set_label AS set_set_label,
            ls.set_note AS set_set_note,
            
            -- SetMetric object for weight (rows where metric_position = 1, preceded by weight_)
            weight.id AS weight_id,
            weight.set_id AS weight_set_id,
            weight.metric_position AS weight_metric_position,
            weight.value AS weight_value,
            weight.note AS weight_note,
            
            -- SetMetric object for reps/time (rows where metric_position = 2, preceded by second_)
            second.id AS second_id,
            second.set_id AS second_set_id,
            second.metric_position AS second_metric_position,
            second.value AS second_value,
            second.note AS second_note
        FROM lift_sets AS ls
        INNER JOIN set_metrics AS weight
            ON weight.set_id = ls.id
            AND weight.metric_position = 1
        INNER JOIN set_metrics AS second
            ON second.set_id = ls.id
            AND second.metric_position = 2
        WHERE ls.session_id = :id
        ORDER BY ls.session_set_number ASC
    """)
    fun getDisplaySessionLiftSetRows(id: Int): Flow<List<DisplaySessionLiftSetRow>>
}
