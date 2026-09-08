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
import github.tom2433.lifttracker.data.structures.LiftDataVisTimed
import github.tom2433.lifttracker.data.structures.LiftDataVisUntimed
import github.tom2433.lifttracker.data.structures.LiftNameAndFrequency
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import github.tom2433.lifttracker.data.structures.LiftSummary
import github.tom2433.lifttracker.data.structures.MuscleGroupNameAndFrequency
import github.tom2433.lifttracker.data.structures.SessionDataPoint
import github.tom2433.lifttracker.data.structures.SessionDetail
import github.tom2433.lifttracker.data.structures.SessionDetailData
import github.tom2433.lifttracker.data.structures.SessionMuscleGroupCountData
import github.tom2433.lifttracker.data.structures.SessionNameAndFrequency
import github.tom2433.lifttracker.data.structures.SessionSummary
import github.tom2433.lifttracker.data.structures.SetDistributionPoint
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.abs

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(session: Session)

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
            SELECT DISTINCT s.id
            FROM sessions AS s
            WHERE s.date <= :endDate
                AND s.date >= :startDate
                AND s.profile_id = :activeProfileId
                AND CASE
                    WHEN :sessionName = '' THEN 1
                    ELSE TRIM(s.session_label) = TRIM(:sessionName)
                END
                AND CASE
                    WHEN :muscleGroupName = '' THEN 1
                    ELSE EXISTS(
                        SELECT 1
                        FROM lift_sets AS ls
                        INNER JOIN muscle_groups AS mg
                            ON mg.id = ls.muscle_group_id
                        WHERE ls.session_id = s.id
                            AND TRIM(mg.name) = TRIM(:muscleGroupName)
                    )
                END
                AND CASE
                    WHEN :liftName = '' THEN 1
                    ELSE EXISTS(
                        SELECT 1
                        FROM lift_sets AS ls
                        INNER JOIN lifts AS l
                            ON l.id = ls.lift_id
                        WHERE ls.session_id = s.id
                            AND TRIM(l.name) = TRIM(:liftName)
                    )
                END
                AND CASE
                    WHEN TRIM(:sessionNote) = '' THEN 1
                    ELSE TRIM(LOWER(s.note)) LIKE '%' || TRIM(LOWER(:sessionNote)) || '%'
                END
        )
        GROUP BY mg.id, mg.name
        ORDER BY mg.name
    """)
    fun getMuscleGroupFrequencyListFromStartEndDates(
        activeProfileId: Int,
        startDate: String,
        endDate: String,
        sessionName: String,
        muscleGroupName: String,
        liftName: String,
        sessionNote: String
    ): Flow<List<LiftSetCountPerMuscleGroup>>

    fun getMuscleGroupFrequencyList(
        activeProfileId: Int,
        startDate: String?,
        endDate: String?,
        sessionName: String?,
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String
    ): Flow<List<LiftSetCountPerMuscleGroup>> {
        val datePair: Pair<String, String> = DateTimeCalculator.getStartAndEndDatesFromNullable(
            startDate = startDate,
            endDate = endDate
        )
        val realSessionName: String = sessionName ?: ""
        val realMuscleGroupName: String = muscleGroupName ?: ""
        val realLiftName: String = liftName ?: ""

        return getMuscleGroupFrequencyListFromStartEndDates(
            activeProfileId = activeProfileId,
            startDate = datePair.first,
            endDate = datePair.second,
            sessionName = realSessionName,
            muscleGroupName = realMuscleGroupName,
            liftName = realLiftName,
            sessionNote = sessionNote
        )
    }

    @Query("""
        SELECT COUNT(s.id)
        FROM sessions AS s
        INNER JOIN profiles AS p
            ON p.id = s.profile_id
        WHERE s.date <= :endDate
            AND s.date >= :startDate
            AND p.active = 1
            AND CASE
                WHEN :muscleGroupName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN muscle_groups AS mg
                        ON mg.id = ls.muscle_group_id
                    WHERE ls.session_id = s.id
                    AND TRIM(mg.name) = TRIM(:muscleGroupName)
                )
            END
            AND CASE
                WHEN :liftName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN lifts AS l
                        ON l.id = ls.lift_id
                    WHERE ls.session_id = s.id
                    AND TRIM(l.name) = TRIM(:liftName)
                )
            END
            AND CASE
                WHEN TRIM(:sessionNote) = '' THEN 1
                ELSE TRIM(LOWER(s.note)) LIKE '%' || TRIM(LOWER(:sessionNote)) || '%'
            END
    """)
    fun getNumSessionsFromStartEndDates(
        muscleGroupName: String,
        liftName: String,
        sessionNote: String,
        startDate: String,
        endDate: String
    ): Flow<Int>

    @Query("""
        SELECT COUNT(DISTINCT s.id)
        FROM sessions AS s
        INNER JOIN profiles AS p
            ON p.id = s.profile_id
        WHERE p.active = 1
            AND s.date <= :endDate
            AND s.date >= :startDate
            AND CASE
                WHEN :sessionName = '' THEN 1
                ELSE TRIM(s.session_label) = TRIM(:sessionName)
            END
            AND CASE
                WHEN :muscleGroupName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN muscle_groups AS mg
                        ON mg.id = ls.muscle_group_id
                    WHERE ls.session_id = s.id
                        AND TRIM(mg.name) = TRIM(:muscleGroupName)
                )
            END
            AND CASE
                WHEN :liftName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN lifts AS l
                        ON l.id = ls.lift_id
                    WHERE ls.session_id = s.id
                        AND TRIM(l.name) = TRIM(:liftName)
                )
            END
            AND CASE
                WHEN TRIM(:sessionNote) = '' THEN 1
                ELSE TRIM(LOWER(s.note)) LIKE '%' || TRIM(LOWER(:sessionNote)) || '%'
            END
    """)
    fun getNumSessionsForFilteredTimeFrameFromStartEndDates(
        startDate: String,
        endDate: String,
        sessionName: String,
        muscleGroupName: String,
        liftName: String,
        sessionNote: String
    ): Flow<Int>

    fun getNumSessionsFromTimeFrame(
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String,
        startDate: String?,
        endDate: String?
    ): Flow<Int> {
        val realMuscleGroupName: String = muscleGroupName ?: ""
        val realLiftName: String = liftName ?: ""
        val datePair: Pair<String, String> = DateTimeCalculator.getStartAndEndDatesFromNullable(
            startDate = startDate,
            endDate = endDate
        )

        return getNumSessionsFromStartEndDates(
            muscleGroupName = realMuscleGroupName,
            liftName = realLiftName,
            sessionNote = sessionNote,
            startDate = datePair.first,
            endDate = datePair.second
        )
    }

    fun getNumSessionsForFilteredTimeFrame(
        startDate: String?,
        endDate: String?,
        sessionName: String?,
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String
    ): Flow<Int> {
        val datePair: Pair<String, String> = DateTimeCalculator.getStartAndEndDatesFromNullable(
            startDate = startDate,
            endDate = endDate
        )
        val realSessionName: String = sessionName ?: ""
        val realMuscleGroupName: String = muscleGroupName ?: ""
        val realLiftName: String = liftName ?: ""

        return getNumSessionsForFilteredTimeFrameFromStartEndDates(
            startDate = datePair.first,
            endDate = datePair.second,
            sessionName = realSessionName,
            muscleGroupName = realMuscleGroupName,
            liftName = realLiftName,
            sessionNote = sessionNote
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
            AND CASE
                WHEN :sessionName = '' THEN 1
                ELSE TRIM(s.session_label) = TRIM(:sessionName)
            END
            AND CASE
                WHEN :muscleGroupName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN muscle_groups AS mg
                        ON mg.id = ls.muscle_group_id
                    WHERE ls.session_id = s.id
                        AND TRIM(mg.name) = TRIM(:muscleGroupName)
                )
            END
            AND CASE
                WHEN :liftName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN lifts AS l
                        ON l.id = ls.lift_id
                    WHERE ls.session_id = s.id
                        AND TRIM(l.name) = TRIM(:liftName)
                )
            END
            AND CASE
                WHEN TRIM(:sessionNote) = '' THEN 1
                ELSE TRIM(LOWER(s.note)) LIKE '%' || TRIM(LOWER(:sessionNote)) || '%'
            END
        ORDER BY s.date DESC, s.id DESC
        LIMIT :fetchLimit
    """)
    fun getSessionDetailDataListFromStartEndDates(
        activeProfileId: Int,
        startDate: String,
        endDate: String,
        fetchLimit: Int,
        sessionName: String,
        muscleGroupName: String,
        liftName: String,
        sessionNote: String
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
            SELECT DISTINCT s.id
            FROM sessions AS s
            WHERE s.profile_id = :activeProfileId
                AND s.date >= :startDate
                AND s.date <= :endDate
                AND CASE
                    WHEN :sessionName = '' THEN 1
                    ELSE TRIM(s.session_label) = TRIM(:sessionName)
                END
                AND CASE
                    WHEN :muscleGroupName = '' THEN 1
                    ELSE EXISTS(
                        SELECT 1
                        FROM lift_sets AS ls
                        INNER JOIN muscle_groups AS mg
                            ON mg.id = ls.muscle_group_id
                        WHERE ls.session_id = s.id
                            AND TRIM(mg.name) = TRIM(:muscleGroupName)
                    )
                END
                AND CASE
                    WHEN :liftName = '' THEN 1
                    ELSE EXISTS(
                        SELECT 1
                        FROM lift_sets AS ls
                        INNER JOIN lifts AS l
                            ON l.id = ls.lift_id
                        WHERE ls.session_id = s.id
                            AND TRIM(l.name) = TRIM(:liftName)
                    )
                END
                AND CASE
                    WHEN TRIM(:sessionNote) = '' THEN 1
                    ELSE TRIM(LOWER(s.note)) LIKE '%' || TRIM(LOWER(:sessionNote)) || '%'
                END
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
        fetchLimit: Int,
        sessionName: String,
        muscleGroupName: String,
        liftName: String,
        sessionNote: String
    ): Flow<List<SessionMuscleGroupCountData>>

    fun getSessionDetailsForSessionScreen(
        activeProfileId: Int,
        startDate: String?,
        endDate: String?,
        fetchLimit: Int,
        sessionName: String?,
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String
    ): Flow<List<SessionDetail>> {
        val datePair: Pair<String, String> = DateTimeCalculator.getStartAndEndDatesFromNullable(
            startDate = startDate,
            endDate = endDate
        )
        val realSessionName: String = sessionName ?: ""
        val realMuscleGroupName: String = muscleGroupName ?: ""
        val realLiftName: String = liftName ?: ""

        val sessionDetailDataListFlow: Flow<List<SessionDetailData>> = getSessionDetailDataListFromStartEndDates(
            activeProfileId = activeProfileId,
            startDate = datePair.first,
            endDate = datePair.second,
            fetchLimit = fetchLimit,
            sessionName = realSessionName,
            muscleGroupName = realMuscleGroupName,
            liftName = realLiftName,
            sessionNote = sessionNote
        )

        val muscleGroupCountDataListFlow: Flow<List<SessionMuscleGroupCountData>> = getSessionMuscleGroupCountDataListFromStartEndDates(
            activeProfileId = activeProfileId,
            startDate = datePair.first,
            endDate = datePair.second,
            fetchLimit = fetchLimit,
            sessionName = realSessionName,
            muscleGroupName = realMuscleGroupName,
            liftName = realLiftName,
            sessionNote = sessionNote
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

    @Query("""
        SELECT ls.*
        FROM lift_sets AS ls
        WHERE ls.session_id = :sessionId
            AND ls.session_set_number = :sessionSetNumber
        LIMIT 1
    """)
    suspend fun getLiftSetBySessionSetNumber(
        sessionId: Int,
        sessionSetNumber: Int
    ): LiftSet?

    @Query("""
        UPDATE lift_sets
        SET session_set_number = -id,
            lift_set_number = -id,
            muscle_group_session_set_number = -id
        WHERE id IN (:liftSetIds)
    """)
    suspend fun stageLiftSetsForMove(liftSetIds: List<Int>)

    @Query("""
        UPDATE lift_sets
        SET session_set_number = :newSessionSetNumber,
            lift_set_number = :newLiftSetNumber,
            muscle_group_session_set_number = :newMuscleGroupSessionSetNumber,
            set_label = 
                CASE set_label
                    WHEN :oldAutoSetLabel THEN :newAutoSetLabel
                    ELSE set_label
                END
        WHERE id = :liftSetId
    """)
    suspend fun updateLiftSetMoveNumbers(
        liftSetId: Int,
        newSessionSetNumber: Int,
        newLiftSetNumber: Int,
        newMuscleGroupSessionSetNumber: Int,
        oldAutoSetLabel: String,
        newAutoSetLabel: String
    )

    @Query("""
        SELECT COUNT(DISTINCT ls.id)
        FROM lift_sets AS ls
            WHERE ls.session_id = :sessionId
    """)
    suspend fun getTotalNumOfLiftSetsForSession(sessionId: Int): Int

    @Transaction
    suspend fun moveLiftSet(liftSetId: Int, down: Boolean) {
        // retrieve the lift set
        val movingLiftSet: LiftSet = getLiftSetById(liftSetId) ?: return

        // retrieve the number of lift sets for the LiftSet's session
        val totalNumLiftSets: Int = getTotalNumOfLiftSetsForSession(
            sessionId = movingLiftSet.session_id
        )

        // lift sets that are first of the session can't be moved down,
        // and lift sets that are last of the session can't be moved up
        if (down && movingLiftSet.session_set_number == 1) {
            return
        } else if (!down && movingLiftSet.session_set_number == totalNumLiftSets) {
            return
        }

        // determine the lift set being displaced
        // moving down: (current session # - 1)
        // moving up: (current session # + 1)
        val displacedLiftSet: LiftSet = getLiftSetBySessionSetNumber(
            sessionId = movingLiftSet.session_id,
            sessionSetNumber = if (down) {
                movingLiftSet.session_set_number - 1
            } else {
                movingLiftSet.session_set_number + 1
            }
        ) ?: return

        // stage the lift set session, lift, and muscle group # to avoid collision
        stageLiftSetsForMove(listOf(movingLiftSet.id, displacedLiftSet.id))

        // determine the new session #s, lift #s, and muscle group #s
        val movingLiftSetNewSessionNumber: Int = displacedLiftSet.session_set_number
        val displacedLiftSetNewSessionNumber: Int = movingLiftSet.session_set_number
        // lift set # will only be different if the two sets are of the same lift
        val movingLiftSetNewLiftSetNumber: Int = if (movingLiftSet.lift_id == displacedLiftSet.lift_id) {
            displacedLiftSet.lift_set_number
        } else {
            movingLiftSet.lift_set_number
        }
        val displacedLiftSetNewLiftSetNumber: Int = if (movingLiftSet.lift_id == displacedLiftSet.lift_id) {
            movingLiftSet.lift_set_number
        } else {
            displacedLiftSet.lift_set_number
        }
        val movingLiftSetNewMuscleGroupSessionSetNumber: Int = if (movingLiftSet.muscle_group_id == displacedLiftSet.muscle_group_id) {
            displacedLiftSet.muscle_group_session_set_number
        } else {
            movingLiftSet.muscle_group_session_set_number
        }
        val displacedLiftSetNewMuscleGroupSessionSetNumber: Int = if (movingLiftSet.muscle_group_id == displacedLiftSet.muscle_group_id) {
            movingLiftSet.muscle_group_session_set_number
        } else {
            displacedLiftSet.muscle_group_session_set_number
        }

        // update each lift set's new session #s, lift #s, and muscle group #s
        updateLiftSetMoveNumbers(
            liftSetId = movingLiftSet.id,
            newSessionSetNumber = movingLiftSetNewSessionNumber,
            newLiftSetNumber = movingLiftSetNewLiftSetNumber,
            newMuscleGroupSessionSetNumber = movingLiftSetNewMuscleGroupSessionSetNumber,
            oldAutoSetLabel = "Set ${movingLiftSet.lift_set_number}",
            newAutoSetLabel = "Set $movingLiftSetNewLiftSetNumber"
        )
        updateLiftSetMoveNumbers(
            liftSetId = displacedLiftSet.id,
            newSessionSetNumber = displacedLiftSetNewSessionNumber,
            newLiftSetNumber = displacedLiftSetNewLiftSetNumber,
            newMuscleGroupSessionSetNumber = displacedLiftSetNewMuscleGroupSessionSetNumber,
            oldAutoSetLabel = "Set ${displacedLiftSet.lift_set_number}",
            newAutoSetLabel = "Set $displacedLiftSetNewLiftSetNumber"
        )
    }

    @Query("""
        SELECT
            TRIM(s.session_label) AS sessionName,
            COUNT(s.id) AS sessionFrequency
        FROM sessions AS s
        INNER JOIN profiles AS p
            ON p.id = s.profile_id
        WHERE p.active = 1
            AND s.date >= :startDate
            AND s.date <= :endDate
            AND CASE
                WHEN :muscleGroupName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN muscle_groups AS mg
                        ON mg.id = ls.muscle_group_id
                    WHERE ls.session_id = s.id
                        AND TRIM(mg.name) = TRIM(:muscleGroupName)
                ) 
            END
            AND CASE
                WHEN :liftName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN lifts AS l
                        ON l.id = ls.lift_id
                    WHERE ls.session_id = s.id
                        AND TRIM(l.name) = TRIM(:liftName)
                )
            END
            AND CASE
                WHEN TRIM(:sessionNote) = '' THEN 1
                ELSE TRIM(LOWER(s.note)) LIKE '%' || TRIM(LOWER(:sessionNote)) || '%'
            END
        GROUP BY sessionName
        ORDER BY sessionFrequency DESC
        LIMIT :fetchLimit
    """)
    fun getUniqueSessionNamesAndFrequenciesFromStartAndEndDate(
        muscleGroupName: String,
        liftName: String,
        sessionNote: String,
        fetchLimit: Int,
        startDate: String,
        endDate: String
    ): Flow<List<SessionNameAndFrequency>>

    @Query("""
        SELECT
            COUNT(DISTINCT TRIM(s.session_label))
        FROM sessions AS s
        INNER JOIN profiles AS p
            ON p.id = s.profile_id
        WHERE p.active = 1
            AND s.date >= :startDate
            AND s.date <= :endDate
            AND CASE
                WHEN :muscleGroupName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN muscle_groups AS mg
                        ON mg.id = ls.muscle_group_id
                    WHERE ls.session_id = s.id
                        AND TRIM(mg.name) = TRIM(:muscleGroupName)
                )
            END
            AND CASE
                WHEN :liftName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN lifts AS l
                        ON l.id = ls.lift_id
                    WHERE ls.session_id = s.id
                        AND TRIM(l.name) = TRIM(:liftName)
                )
            END
            AND CASE
                WHEN TRIM(:sessionNote) = '' THEN 1
                ELSE TRIM(LOWER(s.note)) LIKE '%' || TRIM(LOWER(:sessionNote)) || '%'
            END
    """)
    fun getNumberOfUniqueSessionNamesAndFrequenciesFromStartAndEndDate(
        muscleGroupName: String,
        liftName: String,
        sessionNote: String,
        startDate: String,
        endDate: String
    ): Flow<Int>

    fun getUniqueSessionNamesAndFrequencies(
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String,
        fetchLimit: Int,
        startDate: String?,
        endDate: String?
    ): Flow<List<SessionNameAndFrequency>> {
        val realMuscleGroupName: String = muscleGroupName ?: ""
        val realLiftName: String = liftName ?: ""
        val datePair: Pair<String, String> = DateTimeCalculator.getStartAndEndDatesFromNullable(
            startDate = startDate,
            endDate = endDate
        )

        return getUniqueSessionNamesAndFrequenciesFromStartAndEndDate(
            muscleGroupName = realMuscleGroupName,
            liftName = realLiftName,
            sessionNote = sessionNote,
            fetchLimit = fetchLimit,
            startDate = datePair.first,
            endDate = datePair.second
        )
    }

    fun getNumberOfUniqueSessionNamesAndFrequencies(
        muscleGroupName: String?,
        liftName: String?,
        sessionNote: String,
        startDate: String?,
        endDate: String?
    ): Flow<Int> {
        val realMuscleGroupName: String = muscleGroupName ?: ""
        val realLiftName: String = liftName ?: ""
        val datePair: Pair<String, String> = DateTimeCalculator.getStartAndEndDatesFromNullable(
            startDate = startDate,
            endDate = endDate
        )

        return getNumberOfUniqueSessionNamesAndFrequenciesFromStartAndEndDate(
            muscleGroupName = realMuscleGroupName,
            liftName = realLiftName,
            sessionNote = sessionNote,
            startDate = datePair.first,
            endDate = datePair.second
        )
    }

    @Query("""
        SELECT 
            TRIM(mg.name) AS muscleGroupName,
            COUNT(DISTINCT s.id) AS muscleGroupFrequency
        FROM sessions AS s
        INNER JOIN lift_sets AS ls
            ON ls.session_id = s.id
        INNER JOIN muscle_groups AS mg
            ON mg.id = ls.muscle_group_id
        INNER JOIN profiles AS p
            ON p.id = s.profile_id
        WHERE p.active = 1
            AND s.date >= :startDate
            AND s.date <= :endDate
            AND CASE
                WHEN :sessionName = '' THEN 1
                ELSE TRIM(s.session_label) = TRIM(:sessionName)
            END
            AND CASE
                WHEN :liftName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN lifts AS l
                        ON l.id = ls.lift_id
                    WHERE ls.session_id = s.id
                        AND TRIM(l.name) = TRIM(:liftName)
                )
            END
            AND CASE
                WHEN TRIM(:sessionNote) = '' THEN 1
                ELSE TRIM(LOWER(s.note)) LIKE '%' || TRIM(LOWER(:sessionNote)) || '%'
            END
        GROUP BY TRIM(mg.name)
        ORDER BY muscleGroupFrequency DESC
        LIMIT :fetchLimit
    """)
    fun getUniqueMuscleGroupNamesAndFrequenciesFromStartEndDate(
        sessionName: String,
        liftName: String,
        sessionNote: String,
        fetchLimit: Int,
        startDate: String,
        endDate: String
    ): Flow<List<MuscleGroupNameAndFrequency>>

    @Query("""
        SELECT
            TRIM(l.name) AS liftName,
            COUNT(DISTINCT s.id) AS liftFrequency
        FROM sessions AS s
        INNER JOIN lift_sets AS ls
            ON ls.session_id = s.id
        INNER JOIN lifts AS l
            ON l.id = ls.lift_id
        INNER JOIN profiles AS p
            ON p.id = s.profile_id
        WHERE p.active = 1
            AND s.date >= :startDate
            AND s.date <= :endDate
            AND CASE 
                WHEN :sessionName = '' THEN 1
                ELSE TRIM(s.session_label) = TRIM(:sessionName)
            END
            AND CASE
                WHEN :muscleGroupName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN muscle_groups AS mg
                        ON mg.id = ls.muscle_group_id
                    WHERE ls.session_id = s.id
                        AND TRIM(mg.name) = TRIM(:muscleGroupName)
                )
            END
            AND CASE
                WHEN TRIM(:sessionNote) = '' THEN 1
                ELSE TRIM(LOWER(s.note)) LIKE '%' || TRIM(LOWER(:sessionNote)) || '%'
            END
        GROUP BY TRIM(l.name)
        ORDER BY liftFrequency DESC
        LIMIT :fetchLimit
    """)
    fun getUniqueLiftNamesAndFrequenciesFromStartEndDates(
        sessionName: String,
        muscleGroupName: String,
        sessionNote: String,
        fetchLimit: Int,
        startDate: String,
        endDate: String
    ): Flow<List<LiftNameAndFrequency>>

    fun getUniqueMuscleGroupNamesAndFrequencies(
        sessionName: String?,
        liftName: String?,
        sessionNote: String,
        fetchLimit: Int,
        startDate: String?,
        endDate: String?
    ): Flow<List<MuscleGroupNameAndFrequency>> {
        val realSessionName: String = sessionName ?: ""
        val realLiftName: String = liftName ?: ""
        val datePair = DateTimeCalculator.getStartAndEndDatesFromNullable(
            startDate = startDate,
            endDate = endDate
        )

        return getUniqueMuscleGroupNamesAndFrequenciesFromStartEndDate(
            sessionName = realSessionName,
            liftName = realLiftName,
            sessionNote = sessionNote,
            fetchLimit = fetchLimit,
            startDate = datePair.first,
            endDate = datePair.second
        )
    }

    fun getUniqueLiftNamesAndFrequencies(
        sessionName: String?,
        muscleGroupName: String?,
        sessionNote: String,
        fetchLimit: Int,
        startDate: String?,
        endDate: String?
    ): Flow<List<LiftNameAndFrequency>> {
        val realSessionName: String = sessionName ?: ""
        val realMuscleGroupName: String = muscleGroupName ?: ""
        val datePair = DateTimeCalculator.getStartAndEndDatesFromNullable(
            startDate = startDate,
            endDate = endDate
        )

        return getUniqueLiftNamesAndFrequenciesFromStartEndDates(
            sessionName = realSessionName,
            muscleGroupName = realMuscleGroupName,
            sessionNote = sessionNote,
            fetchLimit = fetchLimit,
            startDate = datePair.first,
            endDate = datePair.second
        )
    }

    @Query("""
        SELECT
            COUNT(DISTINCT TRIM(mg.name))
        FROM sessions AS s
        INNER JOIN lift_sets AS ls
            ON ls.session_id = s.id
        INNER JOIN muscle_groups AS mg
            ON mg.id = ls.muscle_group_id
        INNER JOIN profiles AS p
            ON p.id = s.profile_id
        WHERE p.active = 1
            AND s.date >= :startDate
            AND s.date <= :endDate
            AND CASE
                WHEN :sessionName = '' THEN 1
                ELSE TRIM(s.session_label) = TRIM(:sessionName)
            END
            AND CASE
                WHEN :liftName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN lifts AS l
                        ON l.id = ls.lift_id
                    WHERE ls.session_id = s.id
                        AND TRIM(l.name) = TRIM(:liftName)
                )
            END
            AND CASE
                WHEN TRIM(:sessionNote) = '' THEN 1
                ELSE TRIM(LOWER(s.note)) LIKE '%' || TRIM(LOWER(:sessionNote)) || '%'
            END
    """)
    fun getNumberOfUniqueMuscleGroupNamesAndFrequenciesFromStartEndDate(
        sessionName: String,
        liftName: String,
        sessionNote: String,
        startDate: String,
        endDate: String
    ): Flow<Int>

    @Query("""
        SELECT
            COUNT(DISTINCT TRIM(l.name))
        FROM sessions AS s
        INNER JOIN lift_sets AS ls
            ON ls.session_id = s.id
        INNER JOIN lifts AS l
            ON l.id = ls.lift_id
        INNER JOIN profiles AS p
            ON p.id = s.profile_id
        WHERE p.active = 1
            AND s.date >= :startDate
            AND s.date <= :endDate
            AND CASE
                WHEN :sessionName = '' THEN 1
                ELSE TRIM(s.session_label) = TRIM(:sessionName)
            END
            AND CASE
                WHEN :muscleGroupName = '' THEN 1
                ELSE EXISTS(
                    SELECT 1
                    FROM lift_sets AS ls
                    INNER JOIN muscle_groups AS mg
                        ON mg.id = ls.muscle_group_id
                    WHERE ls.session_id = s.id
                        AND TRIM(mg.name) = :muscleGroupName
                )
            END
            AND CASE
                WHEN TRIM(:sessionNote) = '' THEN 1
                ELSE TRIM(LOWER(s.note)) LIKE '%' || TRIM(LOWER(:sessionNote)) || '%'
            END
    """)
    fun getNumberOfUniqueLiftNamesAndFrequenciesFromStartEndDate(
        sessionName: String,
        muscleGroupName: String,
        sessionNote: String,
        startDate: String,
        endDate: String
    ): Flow<Int>

    fun getNumberOfUniqueMuscleGroupNamesAndFrequencies(
        sessionName: String?,
        liftName: String?,
        sessionNote: String,
        startDate: String?,
        endDate: String?
    ): Flow<Int> {
        val realSessionName: String = sessionName ?: ""
        val realLiftName: String = liftName ?: ""
        val datePair = DateTimeCalculator.getStartAndEndDatesFromNullable(
            startDate = startDate,
            endDate = endDate
        )

        return getNumberOfUniqueMuscleGroupNamesAndFrequenciesFromStartEndDate(
            sessionName = realSessionName,
            liftName = realLiftName,
            sessionNote = sessionNote,
            startDate = datePair.first,
            endDate = datePair.second
        )
    }

    fun getNumberOfUniqueLiftNamesAndFrequencies(
        sessionName: String?,
        muscleGroupName: String?,
        sessionNote: String,
        startDate: String?,
        endDate: String?
    ): Flow<Int> {
        val realSessionName: String = sessionName ?: ""
        val realMuscleGroupName: String = muscleGroupName ?: ""
        val datePair = DateTimeCalculator.getStartAndEndDatesFromNullable(
            startDate = startDate,
            endDate = endDate
        )

        return getNumberOfUniqueLiftNamesAndFrequenciesFromStartEndDate(
            sessionName = realSessionName,
            muscleGroupName = realMuscleGroupName,
            sessionNote = sessionNote,
            startDate = datePair.first,
            endDate = datePair.second
        )
    }

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM lift_sets AS ls
            INNER JOIN set_metrics AS weight
                ON weight.set_id = ls.id
                AND weight.metric_position = 1
            INNER JOIN set_metrics AS mins
                ON mins.set_id = ls.id
                AND mins.metric_position = 2
            INNER JOIN lifts AS l
                ON l.id = ls.lift_id
            WHERE ls.session_id = :id
                AND l.metric_type = 1
                AND weight.value != -1.0
                AND mins.value > 0.0
        )
    """)
    suspend fun sessionHasUntimedLifts(id: Int): Boolean

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM lift_sets AS ls
            INNER JOIN set_metrics AS weight
                ON weight.set_id = ls.id
                AND weight.metric_position = 1
            INNER JOIN set_metrics AS reps
                ON reps.set_id = ls.id
                AND reps.metric_position = 1
            INNER JOIN lifts AS l
                ON l.id = ls.lift_id
            WHERE ls.session_id = :id
                AND l.metric_type = 2
                AND weight.value != -1.0
                AND reps.value > 0.0
        )
    """)
    suspend fun sessionHasTimedLifts(id: Int): Boolean

    @Query("""
        SELECT TRIM(s.session_label)
        FROM sessions AS s
            WHERE s.id = :id
        LIMIT 1
    """)
    suspend fun getTrimmedSessionNameFromId(id: Int): String?

    @Query("""
        SELECT COALESCE(CASE 
            WHEN (
                SELECT COUNT(DISTINCT l.unit_id)
                FROM lifts AS l
                INNER JOIN lift_sets AS ls
                    ON ls.lift_id = l.id
                WHERE ls.session_id = :id
                    AND l.metric_type = CASE WHEN :untimed = 1 THEN 1 ELSE 2 END
            ) > 1 THEN 'units' 
            ELSE (
                SELECT lu.name
                FROM lift_units AS lu
                INNER JOIN lifts AS l
                    ON l.unit_id = lu.id
                INNER JOIN lift_sets AS ls
                    ON ls.lift_id = l.id
                WHERE ls.session_id = :id
                    AND l.metric_type = CASE WHEN :untimed = 1 THEN 1 ELSE 2 END
                LIMIT 1
            )
            END, 'units')
    """)
    suspend fun getUnitsFromSessionId(id: Int, untimed: Boolean): String

    @Query("""
        SELECT
            l.id AS liftId,
            AVG(weight.value) AS weight,
            AVG(mins.value) AS mins,
            AVG(weight.value / mins.value) AS weightPerMin
        FROM lift_sets AS ls
        INNER JOIN lifts AS l
            ON l.id = ls.lift_id
        INNER JOIN set_metrics AS weight
            ON weight.set_id = ls.id
            AND weight.metric_position = 1
        INNER JOIN set_metrics AS mins
            ON mins.set_id = ls.id
            AND mins.metric_position = 2
        WHERE ls.session_id = :id
            AND l.metric_type = 2
            AND weight.value != -1.0
            AND mins.value > 0.0
        GROUP BY l.id
    """)
    suspend fun getLiftDataVisObjectsTimedFromSessionId(id: Int): List<LiftDataVisTimed>

    @Query("""
        SELECT
            l.id AS liftId,
            AVG(weight.value) AS weight,
            AVG(reps.value) AS reps,
            AVG(weight.value * reps.value) AS volumePerSet
        FROM lift_sets AS ls
        INNER JOIN lifts AS l
            ON l.id = ls.lift_id
        INNER JOIN set_metrics AS weight
            ON weight.set_id = ls.id
            AND weight.metric_position = 1
        INNER JOIN set_metrics AS reps
            ON reps.set_id = ls.id
            AND reps.metric_position = 2
        WHERE ls.session_id = :id
            AND l.metric_type = 1
            AND weight.value != -1.0
            AND reps.value > 0.0
        GROUP BY l.id
    """)
    suspend fun getLiftDataVisObjectsUntimedFromSessionId(id: Int): List<LiftDataVisUntimed>

    @Query("""
        SELECT
            previous_session_lift_avgs.liftId AS liftId,
            AVG(previous_session_lift_avgs.weight) AS weight,
            AVG(previous_session_lift_avgs.reps) AS reps,
            AVG(previous_session_lift_avgs.volumePerSet) AS volumePerSet
        FROM (
            SELECT
                l.id AS liftId,
                s.id AS sessionId,
                AVG(weight.value) AS weight,
                AVG(reps.value) AS reps,
                AVG(weight.value * reps.value) AS volumePerSet
            FROM sessions AS target
            INNER JOIN (
                SELECT s.*
                FROM sessions AS s
                INNER JOIN sessions AS target
                    ON target.id = :id
                WHERE s.profile_id = target.profile_id
                AND s.session_number < target.session_number
                AND TRIM(s.session_label) = TRIM(target.session_label)
                AND CASE
                    WHEN :startDate = '' THEN 1 ELSE s.date >= :startDate
                END
                ORDER BY s.session_number DESC
                LIMIT :numSessions
            ) AS s
            INNER JOIN lift_sets AS ls
                ON ls.session_id = s.id
            INNER JOIN lifts AS l
                ON l.id = ls.lift_id
            INNER JOIN set_metrics AS weight
                ON weight.set_id = ls.id
                AND weight.metric_position = 1
            INNER JOIN set_metrics AS reps
                ON reps.set_id = ls.id
                AND reps.metric_position = 2
            WHERE target.id = :id
                AND l.id IN(:liftIds)
                AND l.metric_type = 1
                AND weight.value != -1.0
                AND reps.value > 0.0
            GROUP BY s.id, l.id
        ) AS previous_session_lift_avgs
        GROUP BY previous_session_lift_avgs.liftId
    """)
    suspend fun getLiftDataVisObjectsUntimedBeforeSessionNumber(
        id: Int,
        liftIds: List<Int>,
        startDate: String,
        numSessions: Int
    ): List<LiftDataVisUntimed>

    @Query("""
        SELECT
            previous_session_lift_avgs.liftId AS liftId,
            AVG(previous_session_lift_avgs.weight) AS weight,
            AVG(previous_session_lift_avgs.mins) AS mins,
            AVG(previous_session_lift_avgs.weightPerMin) AS weightPerMin
        FROM (
            SELECT
                l.id AS liftId,
                s.id AS sessionId,
                AVG(weight.value) AS weight,
                AVG(mins.value) AS mins,
                AVG(weight.value / mins.value) AS weightPerMin
            FROM sessions AS target
            INNER JOIN (
                SELECT s.*
                FROM sessions AS s
                INNER JOIN sessions AS target
                    ON target.id = :id
                WHERE s.profile_id = target.profile_id
                    AND s.session_number < target.session_number
                    AND TRIM(s.session_label) = TRIM(target.session_label)
                    AND CASE
                        WHEN :startDate = '' THEN 1 ELSE s.date >= :startDate
                    END
                    ORDER BY s.session_number DESC
                    LIMIT :numSessions
            ) AS s
            INNER JOIN lift_sets AS ls
                ON ls.session_id = s.id
            INNER JOIN lifts AS l
                ON l.id = ls.lift_id
            INNER JOIN set_metrics AS weight
                ON weight.set_id = ls.id
                AND weight.metric_position = 1
            INNER JOIN set_metrics AS mins
                ON mins.set_id = ls.id
                AND mins.metric_position = 2
            WHERE target.id = :id
                AND l.id IN(:liftIds)
                AND l.metric_type = 2
                AND weight.value != -1.0
                AND mins.value > 0.0
            GROUP BY s.id, l.id
        ) AS previous_session_lift_avgs
        GROUP BY previous_session_lift_avgs.liftId
    """)
    suspend fun getLiftDataVisObjectsTimedBeforeSessionNumber(
        id: Int,
        liftIds: List<Int>,
        startDate: String,
        numSessions: Int
    ): List<LiftDataVisTimed>

    @Query("""
        SELECT l.name
        FROM lifts AS l
        WHERE l.id IN (:liftIds)
    """)
    suspend fun getLiftNamesFromLiftIds(
        liftIds: List<Int>
    ): List<String?>

    @Query("""
        SELECT
            s.id
        FROM sessions AS s
        INNER JOIN sessions AS target
            ON target.id = :targetSessionId
        WHERE s.session_number < target.session_number
            AND TRIM(s.session_label) = TRIM(target.session_label)
            AND CASE
                WHEN :startDate = '' THEN 1 ELSE s.date >= :startDate
            END
            AND s.profile_id = target.profile_id
            AND EXISTS (
                SELECT 1
                FROM lift_sets AS ls
                INNER JOIN set_metrics AS weight
                    ON weight.set_id = ls.id
                    AND weight.metric_position = 1
                INNER JOIN set_metrics AS second
                    ON second.set_id = ls.id
                    AND second.metric_position = 2
                WHERE ls.session_id = s.id
                    AND ls.lift_id IN (:liftIds)
                    AND weight.value != -1.0
                    AND second.value > 0.0
            )
        ORDER BY s.session_number DESC
        LIMIT :numSessions
    """)
    suspend fun getApplicableSessionIdsForLineGraph(
        liftIds: List<Int>,
        targetSessionId: Int,
        startDate: String,
        numSessions: Int
    ): List<Int>

    @Query("""
        SELECT s.note
        FROM sessions AS s
        WHERE s.id = :sessionId
        LIMIT 1
    """)
    suspend fun getNoteFromSessionId(
        sessionId: Int
    ): String?

    @Query("""
        SELECT s.date
        FROM sessions AS s
        WHERE s.id = :sessionId
        LIMIT 1
    """)
    suspend fun getDateFromSessionId(
        sessionId: Int
    ): String?

    suspend fun getSessionSummaryForSessionId(
        id: Int,
        startDate: String?,
        numSessionsToFetch: Int?
    ): SessionSummary {
        val sessionHasUntimedLifts: Boolean = sessionHasUntimedLifts(id)
        val sessionHasTimedLifts: Boolean = sessionHasTimedLifts(id)
        // retrieve its units (if there are lifts with different units, this is just "units")
        val untimedUnits = getUnitsFromSessionId(id, untimed = true)
        val timedUnits = getUnitsFromSessionId(id, untimed = false)
        val trimmedSessionName: String = getTrimmedSessionNameFromId(id) ?: return SessionSummary(
            paragraph1 = "Something went wrong.",
            paragraph2 = "",
            explanation = "Something went wrong.",
            untimedDataPoints = listOf(),
            timedDataPoints = listOf(),
            timedUnits = timedUnits,
            untimedUnits = untimedUnits
        )
        val liftIdsList: MutableList<Int> = mutableListOf()
        val untimedSessionDataPoints: MutableList<SessionDataPoint> = mutableListOf()
        val timedSessionDataPoints: MutableList<SessionDataPoint> = mutableListOf()

        var untimedLiftSummary = ""
        var timedLiftSummary = ""
        var explanation = ""

        if (!sessionHasUntimedLifts && !sessionHasTimedLifts) {
            return SessionSummary(
                paragraph1 = "This session does not have any set data yet.",
                paragraph2 = "",
                explanation = "Try recording some sets for this session to view its summary.",
                untimedDataPoints = listOf(),
                timedDataPoints = listOf(),
                timedUnits = timedUnits,
                untimedUnits = untimedUnits
            )
        }

        // if session has at least one lift with reps, fill the untimedLiftSummary
        if (sessionHasUntimedLifts) {
            // retrieve list of LiftDataVisUntimed objects (avg weight, reps, and volume per set)
            // one for each untimed lift for this session
            val liftAvgsForSession: List<LiftDataVisUntimed> = getLiftDataVisObjectsUntimedFromSessionId(id)
            val onlyOneLift: Boolean = liftAvgsForSession.size == 1

            // retrieve list of historical LiftDataVisUntimed objects
            // one for each untimed lift for this session; averages only the lifts which occurred in
            // sessions of the same name with lower session number, after and including startDate
            val previousLiftAvgs: List<LiftDataVisUntimed> = getLiftDataVisObjectsUntimedBeforeSessionNumber(
                id = id,
                liftIds = liftAvgsForSession.map { it.liftId },
                startDate = startDate ?: "",
                numSessions = numSessionsToFetch ?: -1
            )

            val previousLiftAvgsMap: Map<Int, LiftDataVisUntimed> =
                previousLiftAvgs.associateBy { it.liftId }

            // calculate the deviations from the mean for each lift. If the user has not recorded
            // a particular lift before for this session name, it will not be included in this list.
            val deviationsList: List<LiftDataVisUntimed> = liftAvgsForSession.mapNotNull { current ->
                val previous: LiftDataVisUntimed = previousLiftAvgsMap[current.liftId] ?:
                    return@mapNotNull null

                liftIdsList.add(current.liftId)

                LiftDataVisUntimed(
                    liftId = current.liftId,
                    weight = current.weight - previous.weight,
                    reps = current.reps - previous.reps,
                    volumePerSet = current.volumePerSet - previous.volumePerSet
                )
            }

            // don't fill the untimedLiftSummary if the user has NOT recorded any untimed lifts for
            // previous sessions of the same name
            if (deviationsList.isEmpty()) {
                untimedLiftSummary = ""
            } else {
                // otherwise retrieve all data for the untimed line graph
                // first retrieve all session ids which contain at least one lift from previousLiftAvgs
                // target session's data point will be added at the end
                val historicalSessionIdsList: List<Int> = getApplicableSessionIdsForLineGraph(
                    liftIds = previousLiftAvgs.map { it.liftId },
                    targetSessionId = id,
                    startDate = startDate ?: "",
                    numSessions = numSessionsToFetch ?: -1
                )

                // determine the average deviation for each session id
                for (historicalSessionId in historicalSessionIdsList) {
                    val liftAvgsForHistoricalSession: List<LiftDataVisUntimed> = getLiftDataVisObjectsUntimedFromSessionId(historicalSessionId)

                    val historicalDeviations: List<LiftDataVisUntimed> = liftAvgsForHistoricalSession
                        .mapNotNull { current ->
                            val previous: LiftDataVisUntimed = previousLiftAvgsMap[current.liftId] ?:
                                return@mapNotNull null

                            LiftDataVisUntimed(
                                liftId = current.liftId,
                                weight = current.weight - previous.weight,
                                reps = current.reps - previous.reps,
                                volumePerSet = current.volumePerSet - previous.volumePerSet
                            )
                        }

                    untimedSessionDataPoints.add(
                        SessionDataPoint(
                            sessionId = historicalSessionId,
                            sessionNote = getNoteFromSessionId(historicalSessionId) ?: "",
                            sessionDateIso = getDateFromSessionId(historicalSessionId) ?: "",
                            unitName = getUnitsFromSessionId(historicalSessionId, true),
                            weightDeviation = historicalDeviations.map { it.weight }.average(),
                            repsOrTimeDeviation = historicalDeviations.map { it.reps }.average(),
                            intensityDeviation = historicalDeviations.map { it.volumePerSet }.average()
                        )
                    )
                }

                // add this session's data point
                untimedSessionDataPoints.add(
                    SessionDataPoint(
                        sessionId = id,
                        sessionNote = getNoteFromSessionId(id) ?: "",
                        sessionDateIso = getDateFromSessionId(id) ?: "",
                        unitName = untimedUnits,
                        weightDeviation = deviationsList.map { it.weight }.average(),
                        repsOrTimeDeviation = deviationsList.map { it.reps }.average(),
                        intensityDeviation = deviationsList.map { it.volumePerSet }.average()
                    )
                )

                // and fill the untimedLiftSummary
                val avgWeightDeviation = deviationsList.map { it.weight }.average()
                val avgRepsDeviation = deviationsList.map { it.reps }.average()
                val avgVolumePerSetDeviation =
                    deviationsList.map { it.volumePerSet }.average()

                untimedLiftSummary += "For this session, your lift"
                untimedLiftSummary += if (onlyOneLift) {
                    " "
                } else {
                    "s "
                }
                if (sessionHasTimedLifts) {
                    untimedLiftSummary += "with a metric type of 'reps' "
                }
                untimedLiftSummary += if (onlyOneLift) {
                    "was "
                } else {
                    "were "
                }
                untimedLiftSummary += if (avgWeightDeviation < 0.0) {
                    "${"%.2f".format(abs(avgWeightDeviation))} $untimedUnits lighter than "
                } else if (avgWeightDeviation > 0.0) {
                    "${"%.2f".format(abs(avgWeightDeviation))} $untimedUnits heavier than "
                } else {
                    "about the same as "
                }
                untimedLiftSummary += "your last "
                untimedLiftSummary += if (untimedSessionDataPoints.size > 2 ) {
                    "${untimedSessionDataPoints.size - 1} $trimmedSessionName sessions, "
                } else {
                    "$trimmedSessionName session, "
                }
                untimedLiftSummary += "your volume was "
                untimedLiftSummary += if (avgVolumePerSetDeviation < 0.0) {
                    "${"%.2f".format(abs(avgVolumePerSetDeviation))} $untimedUnits lighter "
                } else if (avgVolumePerSetDeviation > 0.0) {
                    "${"%.2f".format(abs(avgVolumePerSetDeviation))} $untimedUnits heavier "
                } else {
                    "about the same "
                }
                untimedLiftSummary += "per set, and you performed "
                untimedLiftSummary += if (avgRepsDeviation < 0.0) {
                    "${"%.2f".format(abs(avgRepsDeviation))} reps less per set than usual. "
                } else if (avgRepsDeviation > 0.0) {
                    "${"%.2f".format(abs(avgRepsDeviation))} reps more per set than usual. "
                } else {
                    "about the same number of reps per set. "
                }
                untimedLiftSummary += "Accounted for ${deviationsList.size}/${liftAvgsForSession.size} lifts."
            }
        }

        // if session has at least one lift with time, fill the timedLiftSummary
        if (sessionHasTimedLifts) {
            // same logic as above but with timed lifts
            val liftAvgsForSession: List<LiftDataVisTimed> = getLiftDataVisObjectsTimedFromSessionId(id)
            val onlyOneLift: Boolean = liftAvgsForSession.size == 1

            val previousLiftAvgs: List<LiftDataVisTimed> = getLiftDataVisObjectsTimedBeforeSessionNumber(
                id = id,
                liftIds = liftAvgsForSession.map { it.liftId },
                startDate = startDate ?: "",
                numSessions = numSessionsToFetch ?: -1
            )

            val previousLiftAvgsMap: Map<Int, LiftDataVisTimed> =
                previousLiftAvgs.associateBy { it.liftId }

            val deviationsList: List<LiftDataVisTimed> = liftAvgsForSession.mapNotNull { current ->
                val previous: LiftDataVisTimed = previousLiftAvgsMap[current.liftId] ?:
                    return@mapNotNull null

                liftIdsList.add(current.liftId)

                LiftDataVisTimed(
                    liftId = current.liftId,
                    weight = current.weight - previous.weight,
                    mins = current.mins - previous.mins,
                    weightPerMin = current.weightPerMin - previous.weightPerMin
                )
            }

            if (deviationsList.isEmpty()) {
                timedLiftSummary = ""
            } else {
                // otherwise retrieve all data for the timed line graph
                // first retrieve all session ids which contain at least one lift from previousLiftAvgs
                // target session's data point will be added at the end
                val historicalSessionIdsList: List<Int> = getApplicableSessionIdsForLineGraph(
                    liftIds = previousLiftAvgs.map { it.liftId },
                    targetSessionId = id,
                    startDate = startDate ?: "",
                    numSessions = numSessionsToFetch ?: -1
                )

                // determine the average deviation for each session id
                for (historicalSessionId in historicalSessionIdsList) {
                    val liftAvgsForHistoricalSession: List<LiftDataVisTimed> =
                        getLiftDataVisObjectsTimedFromSessionId(historicalSessionId)

                    val historicalDeviations: List<LiftDataVisTimed> = liftAvgsForHistoricalSession
                        .mapNotNull { current ->
                            val previous: LiftDataVisTimed = previousLiftAvgsMap[current.liftId] ?:
                                return@mapNotNull null

                            LiftDataVisTimed(
                                liftId = current.liftId,
                                weight = current.weight - previous.weight,
                                mins = current.mins - previous.mins,
                                weightPerMin = current.weightPerMin - previous.weightPerMin
                            )
                        }

                    timedSessionDataPoints.add(
                        SessionDataPoint(
                            sessionId = historicalSessionId,
                            sessionNote = getNoteFromSessionId(historicalSessionId) ?: "",
                            sessionDateIso = getDateFromSessionId(historicalSessionId) ?: "",
                            unitName = getUnitsFromSessionId(historicalSessionId, false),
                            weightDeviation = historicalDeviations.map { it.weight }.average(),
                            repsOrTimeDeviation = historicalDeviations.map { it.mins }.average(),
                            intensityDeviation = historicalDeviations.map { it.weightPerMin }.average()
                        )
                    )
                }

                // add this session's data point
                timedSessionDataPoints.add(
                    SessionDataPoint(
                        sessionId = id,
                        sessionNote = getNoteFromSessionId(id) ?: "",
                        sessionDateIso = getDateFromSessionId(id) ?: "",
                        unitName = timedUnits,
                        weightDeviation = deviationsList.map { it.weight }.average(),
                        repsOrTimeDeviation = deviationsList.map { it.mins }.average(),
                        intensityDeviation = deviationsList.map { it.weightPerMin }.average()
                    )
                )

                val avgWeightDeviation = deviationsList.map { it.weight }.average()
                val avgMinsDeviation = deviationsList.map { it.mins }.average()
                val avgWeightPerMinDeviation =
                    deviationsList.map { it.weightPerMin }.average()
                val avgTimeDeviationString: String = DateTimeCalculator
                    .convertMinutesDoubleToSummaryDetail(abs(avgMinsDeviation))

                timedLiftSummary += "For this session, your lift"
                timedLiftSummary += if (onlyOneLift) {
                    " "
                } else {
                    "s "
                }
                if (sessionHasUntimedLifts) {
                    timedLiftSummary += "with a metric type of 'time' "
                }
                timedLiftSummary += if (onlyOneLift) {
                    "was "
                } else {
                    "were "
                }
                timedLiftSummary += if (avgWeightDeviation < 0.0) {
                    "${"%.2f".format(abs(avgWeightDeviation))} $timedUnits lower than "
                } else if (avgWeightDeviation > 0.0) {
                    "${"%.2f".format(abs(avgWeightDeviation))} $timedUnits higher than "
                } else {
                    "about the same as "
                }
                timedLiftSummary += if (timedSessionDataPoints.size > 2) {
                    "your last $numSessionsToFetch $trimmedSessionName sessions"
                } else {
                    "your last $trimmedSessionName session"
                }
                timedLiftSummary += ", your intensity was "
                timedLiftSummary += if (avgWeightPerMinDeviation < 0.0) {
                    "${"%.2f".format(abs(avgWeightPerMinDeviation))} $timedUnits per minute lower, "
                } else if (avgWeightPerMinDeviation > 0.0) {
                    "${"%.2f".format(abs(avgWeightPerMinDeviation))} $timedUnits per minute higher, "
                } else {
                    "about the same, "
                }
                timedLiftSummary += "and "
                timedLiftSummary += if (onlyOneLift) {
                    "it took "
                } else {
                    "they took "
                }
                timedLiftSummary += if (avgMinsDeviation < 0.0) {
                    "$avgTimeDeviationString shorter than "
                } else if (avgMinsDeviation > 0.0) {
                    "$avgTimeDeviationString longer than "
                } else {
                    "about the same amount of time as "
                }
                timedLiftSummary += if (startDate == null && numSessionsToFetch != null) {
                    if (numSessionsToFetch == 1) {
                        "your last $trimmedSessionName session."
                    } else {
                        "your last $numSessionsToFetch $trimmedSessionName sessions."
                    }
                } else {
                    " usual. "
                }
                timedLiftSummary += "Accounted for ${deviationsList.size}/${liftAvgsForSession.size} lifts."
            }
        }

        // if session has a summary, add an explanation which includes all lifts that were factored
        // into the generation of the summary.
        if (liftIdsList.isNotEmpty()) {
            val liftNameList: List<String> = getLiftNamesFromLiftIds(liftIdsList).mapNotNull { it }
            if (liftNameList.isNotEmpty()) {
                explanation += "This summary was generated only using the lift"
                explanation += if (liftNameList.size == 1) {
                    " "
                } else {
                    "s "
                }
                for ((index, liftName) in liftNameList.withIndex()) {
                    explanation += "${liftName}, "
                    if (index == liftNameList.size - 2) {
                        explanation += "and "
                    }
                }
                explanation += if (liftNameList.size == 1) {
                    "since it is the only lift that has been trained before for " +
                            "this session name."
                } else {
                    "since they are the only lifts that have been trained before for " +
                            "this session name."
                }
            }
        }

        if (untimedLiftSummary.isBlank() && timedLiftSummary.isBlank()) {
            untimedLiftSummary = if (startDate == DateTimeCalculator.START_DATE) {
                "This was your first ever $trimmedSessionName workout with this " +
                        "routine!"
            } else {
                "This is your first $trimmedSessionName workout with this routine in the " +
                        "selected timeframe."
            }
            timedLiftSummary = "For your next $trimmedSessionName workout, You'll see a more " +
                    "detailed summary if you train some of the same lifts."
            explanation =  "Since you have not recorded any of this session's lifts before in a " +
                    "session with the same name, no summary can be generated. Try using this " +
                    "routine again in in a session with the same name for best results."
        }

        return SessionSummary(
            paragraph1 = untimedLiftSummary,
            paragraph2 = timedLiftSummary,
            explanation = explanation,
            untimedDataPoints = untimedSessionDataPoints,
            timedDataPoints = timedSessionDataPoints,
            timedUnits = timedUnits,
            untimedUnits = untimedUnits
        )
    }

    @Query("""
        SELECT COUNT(ls.id)
        FROM lift_sets AS ls
        INNER JOIN set_metrics AS weight
            ON weight.set_id = ls.id
            AND weight.metric_position = 1
        INNER JOIN set_metrics AS second
            ON second.set_id = ls.id
            AND second.metric_position = 2
        INNER JOIN sessions AS s
            ON s.id = ls.session_id
        WHERE s.id = :sessionId
            AND ls.lift_id = :liftId
            AND weight.value != -1.0
            AND second.value > 0.0
    """)
    suspend fun getNumberOfSetsForLiftInSession(
        liftId: Int,
        sessionId: Int
    ): Int

    @Query("""
        SELECT AVG(session_avg_weight)
        FROM (
            SELECT
                AVG(weight.value) AS session_avg_weight
                FROM lift_sets AS ls
                INNER JOIN lifts AS l
                    ON l.id = ls.lift_id
                INNER JOIN (
                    SELECT s.*
                    FROM sessions AS s
                    INNER JOIN sessions AS target
                        ON target.id = :sessionId
                    WHERE s.profile_id = target.profile_id
                        AND TRIM(s.session_label) = TRIM(target.session_label)
                        AND s.session_number < target.session_number
                        AND CASE
                            WHEN :startDate = '' THEN 1 ELSE s.date >= :startDate
                        END
                    ORDER BY s.session_number DESC
                    LIMIT :numSessions
                ) AS s ON s.id = ls.session_id
                INNER JOIN set_metrics AS weight
                    ON weight.set_id = ls.id
                    AND weight.metric_position = 1
                INNER JOIN set_metrics AS second
                    ON second.set_id = ls.id
                    AND second.metric_position = 2
                WHERE l.id = :liftId
                    AND weight.value != -1.0
                    AND second.value > 0.0
                GROUP BY s.id
        )
    """)
    suspend fun getAvgHistoricalWeightForLiftAndSessionName(
        liftId: Int,
        sessionId: Int,
        startDate: String,
        numSessions: Int
    ): Double?

    @Query("""
        SELECT AVG(session_avg_reps)
        FROM (
            SELECT
                AVG(second.value) AS session_avg_reps
                FROM lift_sets AS ls
                INNER JOIN lifts AS l
                    ON l.id = ls.lift_id
                INNER JOIN (
                    SELECT s.*
                    FROM sessions AS s
                    INNER JOIN sessions AS target
                        ON target.id = :sessionId
                    WHERE s.profile_id = target.profile_id
                        AND TRIM(s.session_label) = TRIM(target.session_label)
                        AND s.session_number < target.session_number
                        AND CASE
                            WHEN :startDate = '' THEN 1 ELSE s.date >= :startDate
                        END
                    ORDER BY s.session_number DESC
                    LIMIT :numSessions
                ) AS s
                    ON s.id = ls.session_id
                INNER JOIN set_metrics AS weight
                    ON weight.set_id = ls.id
                    AND weight.metric_position = 1
                INNER JOIN set_metrics AS second
                    ON second.set_id = ls.id
                    AND second.metric_position = 2
                WHERE l.id = :liftId
                    AND weight.value != -1.0
                    AND second.value > 0.0
                GROUP BY s.id
        )
    """)
    suspend fun getAvgHistoricalRepsOrTimeForLiftAndSessionName(
        liftId: Int,
        sessionId: Int,
        startDate: String,
        numSessions: Int
    ): Double?

    @Query("""
        SELECT AVG(session_avg_intensity)
        FROM (
            SELECT AVG(weight.value / second.value) AS session_avg_intensity
            FROM lift_sets AS ls
            INNER JOIN lifts AS l
                ON l.id = ls.lift_id
            INNER JOIN (
                SELECT s.*
                FROM sessions AS s
                INNER JOIN sessions AS target
                    ON target.id = :sessionId
                WHERE s.profile_id = target.profile_id
		            AND TRIM(s.session_label) = TRIM(target.session_label)
		            AND s.session_number < target.session_number
		            AND CASE
			            WHEN :startDate = '' THEN 1 ELSE s.date >= :startDate
		            END
	            ORDER BY s.session_number DESC
	            LIMIT :numSessions
            ) AS s
                ON s.id = ls.session_id
            INNER JOIN set_metrics AS weight
                ON weight.set_id = ls.id
                AND weight.metric_position = 1
            INNER JOIN set_metrics AS second
                ON second.set_id = ls.id
                AND second.metric_position = 2
            WHERE l.id = :liftId
                AND weight.value != -1.0
                AND second.value > 0.0
            GROUP BY s.id
        )
    """)
    suspend fun getAvgHistoricalIntensityForLiftAndSessionName(
        liftId: Int,
        sessionId: Int,
        startDate: String,
        numSessions: Int
    ): Double?

    @Query("""
        SELECT AVG(session_avg_volume_per_set)
        FROM (
            SELECT AVG(weight.value * reps.value) AS session_avg_volume_per_set
            FROM lift_sets AS ls
            INNER JOIN lifts AS l
                ON l.id = ls.lift_id
            INNER JOIN (
                SELECT s.*
                FROM sessions AS s
                INNER JOIN sessions AS target
                    ON target.id = :sessionId
                WHERE s.profile_id = target.profile_id
                    AND TRIM(s.session_label) = TRIM(target.session_label)
                    AND s.session_number < target.session_number
                    AND CASE
                        WHEN :startDate = '' THEN 1 ELSE s.date >= :startDate
                    END
                ORDER BY s.session_number DESC
                LIMIT :numSessions
            ) AS s
                ON s.id = ls.session_id
            INNER JOIN set_metrics AS weight
                ON weight.set_id = ls.id
                AND weight.metric_position = 1
            INNER JOIN set_metrics AS reps
                ON reps.set_id = ls.id
                AND reps.metric_position = 2
            WHERE l.id = :liftId
                AND l.metric_type = 1
                AND weight.value != -1.0
                AND reps.value > 0.0
            GROUP BY s.id
        )
    """)
    suspend fun getAvgHistoricalVolumePerSetForLiftAndSessionName(
        liftId: Int,
        sessionId: Int,
        startDate: String,
        numSessions: Int
    ): Double?

    @Query("""
        SELECT COUNT(DISTINCT ls.session_id)
        FROM lift_sets AS ls
        INNER JOIN lifts AS l
            ON l.id = ls.lift_id
        INNER JOIN (
            SELECT s.*
            FROM sessions AS s
            INNER JOIN sessions AS target
                ON target.id = :sessionId
            WHERE s.profile_id = target.profile_id
                AND TRIM(s.session_label) = TRIM(target.session_label)
                AND s.session_number < target.session_number
                AND CASE
                    WHEN :startDate = '' THEN 1 ELSE s.date >= :startDate
                END
            ORDER BY s.session_number DESC
            LIMIT :numSessions
        ) AS s
            ON s.id = ls.session_id
        INNER JOIN set_metrics AS weight
            ON weight.set_id = ls.id
            AND weight.metric_position = 1
        INNER JOIN set_metrics AS second
            ON second.set_id = ls.id
            AND second.metric_position = 2
        WHERE l.id = :liftId
            AND weight.value != -1.0
            AND second.value > 0.0
    """)
    suspend fun getNumHistoricalSessionsForLift(
        liftId: Int,
        sessionId: Int,
        startDate: String,
        numSessions: Int
    ): Int

    @Query("""
        SELECT AVG(session_set_count)
        FROM (
            SELECT COUNT(ls.id) AS session_set_count
            FROM lift_sets AS ls
            INNER JOIN lifts AS l
                ON l.id = ls.lift_id
            INNER JOIN (
                SELECT s.*
                FROM sessions AS s
                INNER JOIN sessions AS target
                    ON target.id = :sessionId
                WHERE s.profile_id = target.profile_id
                    AND TRIM(s.session_label) = TRIM(target.session_label)
                    AND s.session_number < target.session_number
                    AND CASE
                        WHEN :startDate = '' THEN 1 ELSE s.date >= :startDate
                    END
                ORDER BY s.session_number DESC
                LIMIT :numSessions
            ) AS s
                ON s.id = ls.session_id
            INNER JOIN set_metrics AS weight
                ON weight.set_id = ls.id
                AND weight.metric_position = 1
            INNER JOIN set_metrics AS second
                ON second.set_id = ls.id
                AND second.metric_position = 2
            WHERE l.id = :liftId
                AND weight.value != -1.0
                AND second.value > 0.0
            GROUP BY s.id
        )
    """)
    suspend fun getTypicalNumberOfSetsForLiftWithSessionName(
        liftId: Int,
        sessionId: Int,
        startDate: String,
        numSessions: Int
    ): Double?

    @Query("""
        SELECT AVG(weight.value)
        FROM lift_sets AS ls
        INNER JOIN lifts AS l
            ON l.id = ls.lift_id
        INNER JOIN sessions AS s
            ON ls.session_id = s.id
        INNER JOIN set_metrics AS weight
            ON weight.set_id = ls.id
            AND weight.metric_position = 1
        INNER JOIN set_metrics AS second
            ON second.set_id = ls.id
            AND second.metric_position = 2
        WHERE l.id = :liftId
            AND s.id = :sessionId
            AND weight.value != -1.0
            AND second.value > 0.0
    """)
    suspend fun getAvgWeightForLiftForSession(
        liftId: Int,
        sessionId: Int
    ): Double?

    @Query("""
        SELECT AVG(second.value)
        FROM lift_sets AS ls
        INNER JOIN lifts AS l
            ON l.id = ls.lift_id
        INNER JOIN sessions AS s
            ON ls.session_id = s.id
        INNER JOIN set_metrics AS weight
            ON weight.set_id = ls.id
            AND weight.metric_position = 1
        INNER JOIN set_metrics AS second
            ON second.set_id = ls.id
            AND second.metric_position = 2
        WHERE l.id = :liftId
            AND s.id = :sessionId
            AND weight.value != -1.0
            AND second.value > 0.0
    """)
    suspend fun getAvgRepsOrTimeForLiftForSession(
        liftId: Int,
        sessionId: Int
    ): Double?

    @Query("""
        SELECT AVG(intensity)
        FROM (
            SELECT weight.value / second.value AS intensity
            FROM lift_sets AS ls
            INNER JOIN lifts AS l
                ON l.id = ls.lift_id
            INNER JOIN sessions AS s
                ON ls.session_id = s.id
            INNER JOIN set_metrics AS weight
                ON weight.set_id = ls.id
                AND weight.metric_position = 1
            INNER JOIN set_metrics AS second
                ON second.set_id = ls.id
                AND second.metric_position = 2
            WHERE l.id = :liftId
                AND s.id = :sessionId
                AND weight.value != -1.0
                AND second.value > 0.0
        )
    """)
    suspend fun getAvgIntensityForLiftForSession(
        liftId: Int,
        sessionId: Int
    ): Double?

    @Query("""
        SELECT AVG(volume_per_set)
        FROM (
            SELECT weight.value * reps.value AS volume_per_set
            FROM lift_sets AS ls
            INNER JOIN lifts AS l
                ON l.id = ls.lift_id
            INNER JOIN sessions AS s
                ON ls.session_id = s.id
            INNER JOIN set_metrics AS weight
                ON weight.set_id = ls.id
                AND weight.metric_position = 1
            INNER JOIN set_metrics AS reps
                ON reps.set_id = ls.id
                AND reps.metric_position = 2
            WHERE l.id = :liftId
                AND s.id = :sessionId
                AND l.metric_type = 1
                AND weight.value != -1.0
                AND reps.value > 0.0
        )
    """)
    suspend fun getAvgVolumePerSetForLiftForSession(
        liftId: Int,
        sessionId: Int
    ): Double?

    @Query("""
        SELECT TRIM(lu.name)
        FROM lift_units AS lu
        INNER JOIN lifts AS l
            ON l.unit_id = lu.id
        WHERE l.id = :liftId
        LIMIT 1
    """)
    suspend fun getTrimmedUnitNameFromLiftId(liftId: Int): String?

    @Query("""
        SELECT CASE WHEN l.metric_type = 2 THEN 1 ELSE 0 END
        FROM lifts AS l
        WHERE l.id = :liftId
        LIMIT 1
    """)
    suspend fun liftIsTimed(liftId: Int): Boolean?

    @Query("""
        SELECT TRIM(l.name)
        FROM lifts AS l
        WHERE l.id = :liftId
    """)
    suspend fun getTrimmedLiftNameFromId(liftId: Int): String?

    @Query("""
        SELECT
            -- setNumber
            :liftSetNumber AS setNumber,
            -- weightValue for this lift for this set number for this session
            (
                SELECT weight.value
                FROM set_metrics AS weight
                INNER JOIN lift_sets AS ls
                    ON ls.id = weight.set_id
                    AND ls.lift_id = :liftId
                    AND ls.lift_set_number = :liftSetNumber
                    AND ls.session_id = :sessionId
                WHERE weight.metric_position = 1
                    AND weight.value != -1.0
                LIMIT 1
            ) AS weightValue,
            -- repsOrTime for this lift for this set number for this session
            (
                SELECT second.value
                FROM set_metrics AS second
                INNER JOIN lift_sets AS ls
                    ON ls.id = second.set_id
                    AND ls.lift_id = :liftId
                    AND ls.lift_set_number = :liftSetNumber
                    AND ls.session_id = :sessionId
                WHERE second.metric_position = 2
                    AND second.value > 0.0
                LIMIT 1
            ) AS repsOrTime,
            -- intensity for this lift for this set number for this session
            -- = weight * reps if untimed, reps/weight if timed
            (
                SELECT CASE WHEN :timed = 1 THEN weight.value / second.value ELSE weight.value * second.value END
                FROM lift_sets AS ls
                INNER JOIN set_metrics AS weight
                    ON weight.set_id = ls.id
                    AND weight.metric_position = 1
                    AND weight.value != -1.0
                INNER JOIN set_metrics AS second
                    ON second.set_id = ls.id
                    AND second.metric_position = 2
                    AND second.value != -1.0
                WHERE ls.lift_id = :liftId
                    AND ls.lift_set_number = :liftSetNumber
                    AND ls.session_id = :sessionId
                LIMIT 1
            ) AS intensity,
            -- avgWeightValue of all previous lift sets belonging to sessions of the same name
            -- within the selected timeframe
            (
                SELECT AVG(weight.value)
                FROM set_metrics AS weight
                INNER JOIN lift_sets AS ls
                    ON ls.id = weight.set_id
                    AND ls.lift_id = :liftId
                    AND ls.lift_set_number = :liftSetNumber
                    AND ls.session_id IN (
                        SELECT s.id
                        FROM sessions AS s
                        INNER JOIN sessions AS target
                            ON target.id = :sessionId
                        INNER JOIN profiles AS p
                            ON s.profile_id = p.id
                            AND p.active = 1
                        WHERE TRIM(s.session_label) = TRIM(target.session_label)
                            AND s.session_number < target.session_number
                            AND CASE WHEN :startDate = '' THEN 1 ELSE s.date >= :startDate END
                        ORDER BY s.session_number DESC
                        LIMIT :numSessions
                    )
                WHERE weight.metric_position = 1
                    AND weight.value != -1.0
            ) AS avgWeightValue,
            -- avgRepsOrTime of all previous lift sets belonging to sessions of the same name
            -- within the selected timeframe
            (
                SELECT AVG(second.value)
                FROM set_metrics AS second
                INNER JOIN lift_sets AS ls
                    ON ls.id = second.set_id
                    AND ls.lift_id = :liftId
                    AND ls.lift_set_number = :liftSetNumber
                    AND ls.session_id IN (
                        SELECT s.id
                        FROM sessions AS s
                        INNER JOIN sessions AS target
                            ON target.id = :sessionId
                        WHERE TRIM(s.session_label) = TRIM(target.session_label)
                            AND s.session_number < target.session_number
                            AND s.profile_id = target.profile_id
                            AND CASE WHEN :startDate = '' THEN 1 ELSE s.date >= :startDate END
                        ORDER BY s.session_number DESC
                        LIMIT :numSessions
                    )
                WHERE second.metric_position = 2
                    AND second.value > 0.0
            ) AS avgRepsOrTime,
            -- avgIntensity of all previous lift sets belonging to sessions of the same name
            -- within the selected timeframe = weight * reps if untimed, reps/weight if timed
            (
                SELECT CASE WHEN :timed = 1 THEN AVG(weight.value / second.value) ELSE AVG(weight.value * second.value) END
                FROM lift_sets AS ls
                INNER JOIN set_metrics AS weight
                    ON weight.set_id = ls.id
                    AND weight.metric_position = 1
                    AND weight.value != -1.0
                INNER JOIN set_metrics AS second
                    ON second.set_id = ls.id
                    AND second.metric_position = 2
                    AND second.value > 0.0
                WHERE ls.lift_id = :liftId
                    AND ls.lift_set_number = :liftSetNumber
                    AND ls.session_id IN (
                        SELECT s.id
                        FROM sessions AS s
                        INNER JOIN sessions AS target
                            ON target.id = :sessionId
                        WHERE TRIM(s.session_label) = TRIM(target.session_label)
                            AND s.profile_id = target.profile_id
                            AND s.session_number < target.session_number
                            AND CASE WHEN :startDate = '' THEN 1 ELSE s.date >= :startDate END
                        ORDER BY s.session_number DESC
                        LIMIT :numSessions
                    )
            ) AS avgIntensity
    """)
    suspend fun getSetDistributionPointForLift(
        sessionId: Int,
        liftId: Int,
        startDate: String,
        numSessions: Int,
        liftSetNumber: Int,
        timed: Boolean
    ): SetDistributionPoint

    suspend fun getLiftSummaryForSessionIdAndLiftId(
        sessionId: Int,
        liftId: Int,
        startDate: String?,
        numSessionsToFetch: Int?
    ): LiftSummary {
        val trimmedSessionLabel: String = getTrimmedSessionNameFromId(sessionId)
            ?: return LiftSummary(
                liftName = "",
                timed = false,
                unitName = "units",
                paragraph = "Something went wrong.",
                avgWeight = null,
                historicalAvgWeight = null,
                avgRepsOrTime = null,
                historicalAvgRepsOrTime = null,
                avgIntensity = null,
                historicalAvgIntensity = null,
                setDistributionPoints = listOf()
            )
        val trimmedLiftName: String = getTrimmedLiftNameFromId(liftId)
            ?: return LiftSummary(
                liftName = "",
                timed = false,
                unitName = "units",
                paragraph = "Something went wrong.",
                avgWeight = null,
                historicalAvgWeight = null,
                avgRepsOrTime = null,
                historicalAvgRepsOrTime = null,
                avgIntensity = null,
                historicalAvgIntensity = null,
                setDistributionPoints = listOf()
            )
        val trimmedUnitName: String = getTrimmedUnitNameFromLiftId(liftId)
            ?: return LiftSummary(
                liftName = trimmedLiftName,
                timed = false,
                unitName = "units",
                paragraph = "Something went wrong.",
                avgWeight = null,
                historicalAvgWeight = null,
                avgRepsOrTime = null,
                historicalAvgRepsOrTime = null,
                avgIntensity = null,
                historicalAvgIntensity = null,
                setDistributionPoints = listOf()
            )
        val liftIsTimed: Boolean = liftIsTimed(liftId)
            ?: return LiftSummary(
                liftName = trimmedLiftName,
                timed = false,
                unitName = "units",
                paragraph = "Something went wrong.",
                avgWeight = null,
                historicalAvgWeight = null,
                avgRepsOrTime = null,
                historicalAvgRepsOrTime = null,
                avgIntensity = null,
                historicalAvgIntensity = null,
                setDistributionPoints = listOf()
            )
        var paragraph = ""

        // get number of sets for this lift for this session
        val numberOfSetsForThisSession: Int = getNumberOfSetsForLiftInSession(
            liftId = liftId,
            sessionId = sessionId
        )
        // get the typical number of sets for this lift for this session name within the timeframe
        val typicalNumberOfSets: Double? = getTypicalNumberOfSetsForLiftWithSessionName(
            liftId = liftId,
            sessionId = sessionId,
            startDate = startDate ?: "",
            numSessions = numSessionsToFetch ?: -1
        )
        // get the avg weight value for this lift for this session
        val averageWeightForSession: Double? = getAvgWeightForLiftForSession(
            liftId = liftId,
            sessionId = sessionId,
        )
        // get the avg weight value for this lift for this session name within the timeframe
        val averageHistoricalWeight: Double? = getAvgHistoricalWeightForLiftAndSessionName(
            liftId = liftId,
            sessionId = sessionId,
            startDate = startDate ?: "",
            numSessions = numSessionsToFetch ?: -1
        )
        // get the avg reps/time value for this lift for this session
        val averageRepsOrTimeForSession: Double? = getAvgRepsOrTimeForLiftForSession(
            liftId = liftId,
            sessionId = sessionId
        )
        // get the avg reps/time value for this lift for this session name within the timeframe
        val averageHistoricalRepsOrTime: Double? = getAvgHistoricalRepsOrTimeForLiftAndSessionName(
            liftId = liftId,
            sessionId = sessionId,
            startDate = startDate ?: "",
            numSessions = numSessionsToFetch ?: -1
        )
        // get the avg intensity for this lift for this session
        val averageIntensityForSession: Double? = if (liftIsTimed) {
            getAvgIntensityForLiftForSession(
                liftId = liftId,
                sessionId = sessionId
            )
        } else {
            getAvgVolumePerSetForLiftForSession(
                liftId = liftId,
                sessionId = sessionId
            )
        }
        // get the avg intensity for this lift for this session name within the timeframe
        val averageHistoricalIntensity: Double? = if (liftIsTimed) {
            getAvgHistoricalIntensityForLiftAndSessionName(
                liftId = liftId,
                sessionId = sessionId,
                startDate = startDate ?: "",
                numSessions = numSessionsToFetch ?: -1
            )
        } else {
            getAvgHistoricalVolumePerSetForLiftAndSessionName(
                liftId = liftId,
                sessionId = sessionId,
                startDate = startDate ?: "",
                numSessions = numSessionsToFetch ?: -1
            )
        }
        // get the total number of sessions that are actually being fetched to determine this data
        val numberOfHistoricalSessions: Int = getNumHistoricalSessionsForLift(
            liftId = liftId,
            sessionId = sessionId,
            startDate = startDate ?: "",
            numSessions = numSessionsToFetch ?: -1
        )

        // fill the setDistributionPoints list.
        val setDistributionPoints: MutableList<SetDistributionPoint> = mutableListOf()
        if (averageWeightForSession != null && averageRepsOrTimeForSession != null &&
            averageIntensityForSession != null) {
            for (i in (1..numberOfSetsForThisSession)) {
                setDistributionPoints.add(
                    getSetDistributionPointForLift(
                        sessionId = sessionId,
                        liftId = liftId,
                        startDate = startDate ?: "",
                        numSessions = numSessionsToFetch ?: -1,
                        liftSetNumber = i,
                        timed = liftIsTimed
                    )
                )
            }
        }

        // add this session's number of sets vs typical number of sets to summary
        if (typicalNumberOfSets == null) {
            paragraph += "You have not trained this lift before during a $trimmedSessionLabel workout, but " +
                    "for this session, you logged $numberOfSetsForThisSession set"
            paragraph += if (numberOfSetsForThisSession == 1) {
                ". "
            } else {
                "s. "
            }
        } else {
            paragraph += "For this session, you trained $trimmedLiftName for $numberOfSetsForThisSession set"
            if (numberOfSetsForThisSession != 1) {
                paragraph += "s"
            }
            paragraph += if (numberOfSetsForThisSession.toDouble() == typicalNumberOfSets) {
                " as usual. "
            } else {
                ", but you usually train about ${"%.2f".format(typicalNumberOfSets)}. "
            }
        }

        // add tip to lift summary if there is no historical weight value
        if (averageHistoricalWeight == null) {
            paragraph +=
                "Try training this lift again in a session with the same name to see how your " +
                "routines are progressing. "
        }

        // add tip to lift summary if the user has not logged any weight for this lift for this
        // session
        if (averageWeightForSession == null) {
            paragraph +=
                "You haven't logged any valid sets for this lift in this session yet. "
        }

        // null check the remaining metrics before creating the rest of the summary
        if (averageWeightForSession != null && averageHistoricalWeight != null &&
            averageRepsOrTimeForSession != null && averageHistoricalRepsOrTime != null &&
            averageIntensityForSession != null && averageHistoricalIntensity != null &&
            averageWeightForSession != 0.0 && averageHistoricalWeight != 0.0 &&
            averageRepsOrTimeForSession != 0.0 && averageHistoricalRepsOrTime != 0.0 &&
            averageIntensityForSession != 0.0 && averageHistoricalIntensity != 0.0) {
            val difference = averageWeightForSession - averageHistoricalWeight

            if (difference > 0) {
                val percentage = ((averageWeightForSession / averageHistoricalWeight) - 1.0) * 100.0
                paragraph += "You logged about ${"%.1f".format(percentage)}% more ${trimmedUnitName}, "
            } else if (difference < 0) {
                val percentage = ((averageHistoricalWeight / averageWeightForSession) - 1.0) * 100.0
                paragraph += "You logged about ${"%.1f".format(percentage)}% less ${trimmedUnitName}, "
            } else {
                paragraph += "You logged about the same ${trimmedUnitName}, "
            }

            if (averageRepsOrTimeForSession > averageHistoricalRepsOrTime) {
                val percentage = ((averageRepsOrTimeForSession / averageHistoricalRepsOrTime) - 1.0) * 100.0
                paragraph += "${"%.1f".format(percentage)}% more "
            } else if (averageHistoricalRepsOrTime > averageRepsOrTimeForSession) {
                val percentage = ((averageHistoricalRepsOrTime / averageRepsOrTimeForSession) - 1.0) * 100.0
                paragraph += "${"%.1f".format(percentage)}% less "
            } else {
                paragraph += "about the same "
            }
            paragraph += if (liftIsTimed) {
                "minutes, "
            } else {
                "reps, "
            }

            if (averageIntensityForSession > averageHistoricalIntensity) {
                val percentage = ((averageIntensityForSession / averageHistoricalIntensity) - 1.0) * 100.0
                paragraph += "and ${"%.1f".format(percentage)}% more $trimmedUnitName per "
            } else if (averageHistoricalIntensity > averageIntensityForSession ) {
                val percentage = ((averageHistoricalIntensity / averageIntensityForSession) - 1.0) * 100.0
                paragraph += "and ${"%.1f".format(percentage)}% less $trimmedUnitName per "
            } else {
                paragraph += "and about the same $trimmedUnitName per "
            }
            paragraph += if (liftIsTimed) {
                "minute "
            } else {
                "set "
            }
            paragraph += if (numberOfHistoricalSessions > 1) {
                "when compared to your last $numberOfHistoricalSessions $trimmedSessionLabel sessions where you trained this lift."
            } else {
                "when compared to your last $trimmedSessionLabel session where you trained this lift."
            }
        }

        return LiftSummary(
            liftName = trimmedLiftName,
            timed = liftIsTimed,
            unitName = trimmedUnitName,
            paragraph = paragraph,
            avgWeight = averageWeightForSession,
            historicalAvgWeight = averageHistoricalWeight,
            avgRepsOrTime = averageRepsOrTimeForSession,
            historicalAvgRepsOrTime = averageHistoricalRepsOrTime,
            avgIntensity = averageIntensityForSession,
            historicalAvgIntensity = averageHistoricalIntensity,
            setDistributionPoints = setDistributionPoints
        )
    }
}
