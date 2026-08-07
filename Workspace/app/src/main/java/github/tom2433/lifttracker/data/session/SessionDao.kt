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
import github.tom2433.lifttracker.data.structures.LiftNameAndFrequency
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import github.tom2433.lifttracker.data.structures.MuscleGroupNameAndFrequency
import github.tom2433.lifttracker.data.structures.SessionDetail
import github.tom2433.lifttracker.data.structures.SessionDetailData
import github.tom2433.lifttracker.data.structures.SessionMuscleGroupCountData
import github.tom2433.lifttracker.data.structures.SessionNameAndFrequency
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
import github.tom2433.lifttracker.ui.screens.TimeFrameOption
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
        liftName: String
    ): Flow<List<LiftSetCountPerMuscleGroup>>

    fun getMuscleGroupFrequencyList(
        activeProfileId: Int,
        startDate: String?,
        endDate: String?,
        sessionName: String?,
        muscleGroupName: String?,
        liftName: String?
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
            liftName = realLiftName
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
    """)
    fun getNumSessionsFromStartEndDates(
        muscleGroupName: String,
        liftName: String,
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
    """)
    fun getNumSessionsForFilteredTimeFrameFromStartEndDates(
        startDate: String,
        endDate: String,
        sessionName: String,
        muscleGroupName: String,
        liftName: String
    ): Flow<Int>

    fun getNumSessionsFromTimeFrame(
        muscleGroupName: String?,
        liftName: String?,
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
            startDate = datePair.first,
            endDate = datePair.second
        )
    }

    fun getNumSessionsForFilteredTimeFrame(
        startDate: String?,
        endDate: String?,
        sessionName: String?,
        muscleGroupName: String?,
        liftName: String?
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
            liftName = realLiftName
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
        liftName: String
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
        liftName: String
    ): Flow<List<SessionMuscleGroupCountData>>

    fun getSessionDetailsForSessionScreen(
        activeProfileId: Int,
        startDate: String?,
        endDate: String?,
        fetchLimit: Int,
        sessionName: String?,
        muscleGroupName: String?,
        liftName: String?
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
            liftName = realLiftName
        )

        val muscleGroupCountDataListFlow: Flow<List<SessionMuscleGroupCountData>> = getSessionMuscleGroupCountDataListFromStartEndDates(
            activeProfileId = activeProfileId,
            startDate = datePair.first,
            endDate = datePair.second,
            fetchLimit = fetchLimit,
            sessionName = realSessionName,
            muscleGroupName = realMuscleGroupName,
            liftName = realLiftName
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
        GROUP BY sessionName
        ORDER BY sessionFrequency DESC
        LIMIT :fetchLimit
    """)
    fun getUniqueSessionNamesAndFrequenciesFromStartAndEndDate(
        muscleGroupName: String,
        liftName: String,
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
    """)
    fun getNumberOfUniqueSessionNamesAndFrequenciesFromStartAndEndDate(
        muscleGroupName: String,
        liftName: String,
        startDate: String,
        endDate: String
    ): Flow<Int>

    fun getUniqueSessionNamesAndFrequencies(
        muscleGroupName: String?,
        liftName: String?,
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
            fetchLimit = fetchLimit,
            startDate = datePair.first,
            endDate = datePair.second
        )
    }

    fun getNumberOfUniqueSessionNamesAndFrequencies(
        muscleGroupName: String?,
        liftName: String?,
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
        GROUP BY TRIM(mg.name)
        ORDER BY muscleGroupFrequency DESC
        LIMIT :fetchLimit
    """)
    fun getUniqueMuscleGroupNamesAndFrequenciesFromStartEndDate(
        sessionName: String,
        liftName: String,
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
        GROUP BY TRIM(l.name)
        ORDER BY liftFrequency DESC
        LIMIT :fetchLimit
    """)
    fun getUniqueLiftNamesAndFrequenciesFromStartEndDates(
        sessionName: String,
        muscleGroupName: String,
        fetchLimit: Int,
        startDate: String,
        endDate: String
    ): Flow<List<LiftNameAndFrequency>>

    fun getUniqueMuscleGroupNamesAndFrequencies(
        sessionName: String?,
        liftName: String?,
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
            fetchLimit = fetchLimit,
            startDate = datePair.first,
            endDate = datePair.second
        )
    }

    fun getUniqueLiftNamesAndFrequencies(
        sessionName: String?,
        muscleGroupName: String?,
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
    """)
    fun getNumberOfUniqueMuscleGroupNamesAndFrequenciesFromStartEndDate(
        sessionName: String,
        liftName: String,
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
    """)
    fun getNumberOfUniqueLiftNamesAndFrequenciesFromStartEndDate(
        sessionName: String,
        muscleGroupName: String,
        startDate: String,
        endDate: String
    ): Flow<Int>

    fun getNumberOfUniqueMuscleGroupNamesAndFrequencies(
        sessionName: String?,
        liftName: String?,
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
            startDate = datePair.first,
            endDate = datePair.second
        )
    }

    fun getNumberOfUniqueLiftNamesAndFrequencies(
        sessionName: String?,
        muscleGroupName: String?,
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
            startDate = datePair.first,
            endDate = datePair.second
        )
    }

    @Query("""
        SELECT
            COUNT(s.id)
        FROM sessions AS s
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
    """)
    fun getNumSessionsAfterNameFilterFromStartEndDate(
        sessionName: String,
        liftName: String,
        startDate: String,
        endDate: String
    ): Flow<Int>

    @Query("""
        SELECT
            COUNT(DISTINCT s.id)
        FROM sessions AS s
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
    """)
    fun getNumSessionsAfterMuscleGroupFilterFromStartEndDate(
        sessionName: String,
        muscleGroupName: String,
        startDate: String,
        endDate: String
    ): Flow<Int>

    fun getNumSessionsAfterNameFilter(
        sessionName: String?,
        liftName: String?,
        startDate: String?,
        endDate: String?
    ): Flow<Int> {
        val realSessionName: String = sessionName ?: ""
        val realLiftName: String = liftName ?: ""
        val datePair = DateTimeCalculator.getStartAndEndDatesFromNullable(
            startDate = startDate,
            endDate = endDate
        )

        return getNumSessionsAfterNameFilterFromStartEndDate(
            sessionName = realSessionName,
            liftName = realLiftName,
            startDate = datePair.first,
            endDate = datePair.second
        )
    }

    fun getNumSessionsAfterMuscleGroupFilter(
        sessionName: String?,
        muscleGroupName: String?,
        startDate: String?,
        endDate: String?
    ): Flow<Int> {
        val realSessionName: String = sessionName ?: ""
        val realMuscleGroupName: String = muscleGroupName ?: ""
        val datePair = DateTimeCalculator.getStartAndEndDatesFromNullable(
            startDate = startDate,
            endDate = endDate
        )

        return getNumSessionsAfterMuscleGroupFilterFromStartEndDate(
            sessionName = realSessionName,
            muscleGroupName = realMuscleGroupName,
            startDate = datePair.first,
            endDate = datePair.second
        )
    }
}
