package github.tom2433.lifttracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import github.tom2433.lifttracker.data.lift.LiftDao
import github.tom2433.lifttracker.data.lift.Lift
import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.liftset.LiftSetDao
import github.tom2433.lifttracker.data.liftunit.LiftUnit
import github.tom2433.lifttracker.data.liftunit.LiftUnitDao
import github.tom2433.lifttracker.data.musclegroup.MuscleGroup
import github.tom2433.lifttracker.data.musclegroup.MuscleGroupDao
import github.tom2433.lifttracker.data.profile.Profile
import github.tom2433.lifttracker.data.profile.ProfileDao
import github.tom2433.lifttracker.data.session.Session
import github.tom2433.lifttracker.data.session.SessionDao
import github.tom2433.lifttracker.data.setmetric.SetMetric
import github.tom2433.lifttracker.data.setmetric.SetMetricDao

/**
# Tables:

## ```sessions```

The purpose of the ```sessions``` table is to keep track of all the sessions that the user has logged.

The ```sessions``` table has 7 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lift_sets``` table uses to associate set data with a specific session.
- ```in_progress``` (INTEGER): indicates 1 if the session is still in progress, or 0 if the user has already completed this session.
- ```profile_id``` (INTEGER): foreign key. This is what links the session to the appropriate profile.
- ```session_number``` (INTEGER): the number of the session; e.g. ```1```, ```2```, ```3```, etc.
- ```session_label``` (TEXT): the name of the session; e.g. ```"Session 1"```, ```"Session 2"```, ```"Session 3"```, etc. as default. The user may be able to change this name in future versions.
- ```date``` (TEXT): The date that the session was recorded in ISO-8601 format: YYYY-MM-DD
- ```note``` (TEXT): a user-written note for the session, may be blank

## ```lift_sets```

The purpose of the ```lift_sets``` table is to keep track of all sets that the user has completed. It also links each set with the specific lift that was trained, and the session that the user completed the set in.

The ```lift_sets``` table has 9 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```set_metrics``` table uses to associate specific set data (weight, reps) with a specific set that was completed for a specific lift in a specific session.
- ```session_id``` (INTEGER): foreign key referring to ```sessions```. This is what links this set to a particular session.
- ```lift_id``` (INTEGER): foreign key referring to ```lifts```. This is what links this set to a particular lift (e.g., bicep curls).
- ```muscle_group_id``` (INTEGER): foreign key referring to ```muscle_groups```. This is what links this set to a particular muscle group (e.g., biceps).
- ```lift_set_number``` (INTEGER): this set number identifies when this set took place, only in relation to the other sets completed for this specific lift in this specific session.
- ```session_set_number``` (INTEGER): this set number identifies when this set took place, in relation to all other sets completed in this specific session.
- ```muscle_group_session_set_number``` (INTEGER): this set number identifies when this set took place in relation to all other sets completed in this specific session for this specific muscle group.
- ```set_label``` (TEXT): just a string containing the name of the set (e.g., ```"Set 1"```, ```"Set 2"```, ```"Set 3"```, etc. as default). The user may be able to change this label in future versions.
- ```set_note``` (TEXT): a user-written note for the set, may be blank

## ```lifts```

The purpose of the ```lifts``` table is to store the names of all the different user-created lifts and link them to their corresponding muscle groups.

The ```lifts``` table has 4 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lift_sets``` table uses to associate lift names with set data.
- ```muscle_group_id``` (INTEGER): foreign key referring to the ```muscle_groups``` table. This is what links each lift to its corresponding muscle group.
- ```unit_id``` (INTEGER): foreign key referring to the ```lift_units``` table. This is what links each lift to its corresponding user-written lift unit.
- ```name``` (TEXT): the user-specified name for the lift.
- ```metric_type``` (INTEGER): Int indicating if the lift will be measured in reps (1) or time (2). If the metric type is time, then the unit_id will be overridden.
- ```note``` (TEXT): a user-written note for the lift, may be blank

## ```muscle_groups```

The purpose of the ```muscle_groups``` table is to store the user-generated names for all the muscle groups and link them to their corresponding profile.

The ```muscle_groups``` table has 4 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lifts``` table uses to associate lift names with muscle groups.
- ```profile_id``` (INTEGER): foreign key referring to the ```profiles``` table. This is what links each muscle group to its corresponding user (profile).
- ```name``` (TEXT): the user-specified name for the muscle group.
- ```note``` (TEXT): a user-written note for the muscle group, may be blank.

## ```set_metrics```

The purpose of the ```set_metrics``` table is to store all the user's set metrics (weight and rep data) and link them to their corresponding set (which is linked to the corresponding lift and the corresponding session via foreign keys).

The ```set_metrics``` table has 6 columns:

- ```id``` (INTEGER): primary key. This is the main identifier for each set metric.
- ```set_id``` (INTEGER): foreign key referring to the ```lift_sets``` table. This is what links each lift metric to its corresponding set.
- ```metric_position``` (INTEGER): this indicates whether the metric is a weight value or a rep value. ```1``` indicates weight, and ```2``` indicates reps.
- ```value``` (REAL): this indicates the number of reps performed, the weight value, or the time value for the specific set. Time values will be stored as doubles representing minutes, e.g. 1 minute and 30 seconds = 1.5 minutes
- ```note``` (TEXT): a user-written note for the metric, may be blank.

## ```profiles```

The purpose of the ```profiles``` table is to store the names of all the profiles that the user has created. The profile is linked to its data via the ```muscle_groups``` table.

The ```profiles``` table has 3 columns:

- ```id``` (INTEGER): primary key. This is the main identifier used to distinguish between each profile.
- ```name``` (TEXT): the user-written name for the profile.
- ```active``` (INTEGER): indicates whether the current profile is active (1) or not active (0)
- ```note``` (TEXT): a user-written note for the profile, may be blank.

## ```lift_units```

The purpose of the ```lift_units``` table is to store the names of all the user-written lift units, which are added to different lifts. The lift_units table is designed to be independent of profiles, so multiple profiles can use the same lift unit.

The ```lift_units``` table has two columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lifts``` table uses to associate lifts with their appropriate lift units.
- ```name``` (TEXT): the user-written name of the lift unit

> [!NOTE]
> May need some protection to ensure that a lift unit that is being used cannot be deleted.
*/

@Database(
    entities = [
        Session::class,
        Lift::class,
        LiftSet::class,
        MuscleGroup::class,
        Profile::class,
        SetMetric::class,
        LiftUnit::class
    ],
    version = 8,
    exportSchema = false
)
abstract class LiftTrackerDatabase : RoomDatabase() {
    abstract fun liftDao(): LiftDao
    abstract fun sessionDao(): SessionDao
    abstract fun liftSetDao(): LiftSetDao
    abstract fun muscleGroupDao(): MuscleGroupDao
    abstract fun profileDao(): ProfileDao
    abstract fun setMetricDao(): SetMetricDao
    abstract fun liftUnitDao(): LiftUnitDao


    companion object {
        private val LEGACY_SESSION_TABLE = listOf("lift", "days").joinToString("_")
        private val LEGACY_SESSION_ID_COLUMN = listOf("lift", "day", "id").joinToString("_")
        private val LEGACY_SESSION_NUMBER_COLUMN = listOf("day", "number").joinToString("_")
        private val LEGACY_SESSION_SET_NUMBER_COLUMN = listOf("day", "set", "number").joinToString("_")
        private val LEGACY_MUSCLE_GROUP_SESSION_SET_NUMBER_COLUMN =
            listOf("muscle", "group", "day", "set", "number").joinToString("_")

        private data class SessionMigrationSource(
            val tableName: String,
            val sessionNumberColumnName: String
        )

        private data class TableColumn(
            val name: String,
            val type: String
        )

        @Volatile
        private var Instance: LiftTrackerDatabase? = null
        fun getDatabase(context: Context): LiftTrackerDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    LiftTrackerDatabase::class.java,
                    "lift_tracker_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8
                    )
                    .build()
                    .also { Instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val oldUnitsTableExists = tableExists(db, "units")
                val liftUnitsTableExists = tableExists(db, "lift_units")

                if (oldUnitsTableExists && !liftUnitsTableExists) {
                    db.execSQL("ALTER TABLE units RENAME TO lift_units")
                }

                if (tableExists(db, "lifts")) {
                    rebuildLiftsTable(db)
                }
            }

            private fun tableExists(db: SupportSQLiteDatabase, tableName: String): Boolean {
                val cursor = db.query(
                    "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
                    arrayOf(tableName)
                )

                return cursor.use {
                    it.moveToFirst()
                }
            }

            private fun rebuildLiftsTable(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS lifts_migration")
                db.execSQL(
                    """
                    CREATE TABLE lifts_migration (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        muscle_group_id INTEGER NOT NULL,
                        unit_id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        metric_type INTEGER NOT NULL,
                        note TEXT NOT NULL,
                        FOREIGN KEY(muscle_group_id) REFERENCES muscle_groups(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(unit_id) REFERENCES lift_units(id) ON UPDATE NO ACTION ON DELETE NO ACTION
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO lifts_migration (id, muscle_group_id, unit_id, name, metric_type, note)
                    SELECT id, muscle_group_id, unit_id, name, metric_type, note
                    FROM lifts
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE lifts")
                db.execSQL("ALTER TABLE lifts_migration RENAME TO lifts")
                db.execSQL("CREATE INDEX index_lifts_muscle_group_id ON lifts(muscle_group_id)")
                db.execSQL("CREATE INDEX index_lifts_unit_id ON lifts(unit_id)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE $LEGACY_SESSION_TABLE ADD COLUMN in_progress INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL("CREATE INDEX index_${LEGACY_SESSION_TABLE}_profile_id ON ${LEGACY_SESSION_TABLE}(profile_id)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                renumberLegacySessionsByProfile(db)
                db.execSQL("DROP INDEX IF EXISTS index_${LEGACY_SESSION_TABLE}_profile_id")
                db.execSQL(
                    "CREATE UNIQUE INDEX index_${LEGACY_SESSION_TABLE}_profile_id_${LEGACY_SESSION_NUMBER_COLUMN} ON ${LEGACY_SESSION_TABLE}(profile_id, ${LEGACY_SESSION_NUMBER_COLUMN})"
                )
            }

            private fun renumberLegacySessionsByProfile(db: SupportSQLiteDatabase) {
                val cursor = db.query(
                    """
                    SELECT id, profile_id
                    FROM $LEGACY_SESSION_TABLE
                    ORDER BY profile_id ASC, $LEGACY_SESSION_NUMBER_COLUMN ASC, id ASC
                    """.trimIndent()
                )

                cursor.use {
                    var currentProfileId: Int? = null
                    var nextSessionNumber = 1

                    while (it.moveToNext()) {
                        val sessionId = it.getInt(0)
                        val profileId = it.getInt(1)

                        if (profileId != currentProfileId) {
                            currentProfileId = profileId
                            nextSessionNumber = 1
                        }

                        db.execSQL(
                            "UPDATE $LEGACY_SESSION_TABLE SET $LEGACY_SESSION_NUMBER_COLUMN = ? WHERE id = ?",
                            arrayOf(nextSessionNumber, sessionId)
                        )
                        nextSessionNumber += 1
                    }
                }
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                deduplicateSetMetrics(db)
                renumberLiftSetsBySession(db)
                renumberLiftSetsByLiftAndSession(db)

                db.execSQL("DROP INDEX IF EXISTS index_set_metrics_set_id")
                db.execSQL("DROP INDEX IF EXISTS index_set_metrics_set_id_metric_position")
                db.execSQL("DROP INDEX IF EXISTS index_lift_sets_${LEGACY_SESSION_ID_COLUMN}")
                db.execSQL("DROP INDEX IF EXISTS index_lift_sets_${LEGACY_SESSION_ID_COLUMN}_${LEGACY_SESSION_SET_NUMBER_COLUMN}")
                db.execSQL("DROP INDEX IF EXISTS index_lift_sets_${LEGACY_SESSION_ID_COLUMN}_lift_id_lift_set_number")
                db.execSQL("DROP INDEX IF EXISTS index_lift_sets_lift_id")

                db.execSQL(
                    "CREATE UNIQUE INDEX index_set_metrics_set_id_metric_position ON set_metrics(set_id, metric_position)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX index_lift_sets_${LEGACY_SESSION_ID_COLUMN}_${LEGACY_SESSION_SET_NUMBER_COLUMN} ON lift_sets(${LEGACY_SESSION_ID_COLUMN}, ${LEGACY_SESSION_SET_NUMBER_COLUMN})"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX index_lift_sets_${LEGACY_SESSION_ID_COLUMN}_lift_id_lift_set_number ON lift_sets(${LEGACY_SESSION_ID_COLUMN}, lift_id, lift_set_number)"
                )
                db.execSQL("CREATE INDEX index_lift_sets_lift_id ON lift_sets(lift_id)")
            }

            private fun deduplicateSetMetrics(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    DELETE FROM set_metrics
                    WHERE id NOT IN (
                        SELECT MIN(id)
                        FROM set_metrics
                        GROUP BY set_id, metric_position
                    )
                    """.trimIndent()
                )
            }

            private fun renumberLiftSetsBySession(db: SupportSQLiteDatabase) {
                val cursor = db.query(
                    """
                    SELECT id, $LEGACY_SESSION_ID_COLUMN
                    FROM lift_sets
                    ORDER BY $LEGACY_SESSION_ID_COLUMN ASC, $LEGACY_SESSION_SET_NUMBER_COLUMN ASC, id ASC
                    """.trimIndent()
                )

                cursor.use {
                    var currentSessionId: Int? = null
                    var nextSessionSetNumber = 1

                    while (it.moveToNext()) {
                        val liftSetId = it.getInt(0)
                        val sessionId = it.getInt(1)

                        if (sessionId != currentSessionId) {
                            currentSessionId = sessionId
                            nextSessionSetNumber = 1
                        }

                        db.execSQL(
                            "UPDATE lift_sets SET $LEGACY_SESSION_SET_NUMBER_COLUMN = ? WHERE id = ?",
                            arrayOf(nextSessionSetNumber, liftSetId)
                        )
                        nextSessionSetNumber += 1
                    }
                }
            }

            private fun renumberLiftSetsByLiftAndSession(db: SupportSQLiteDatabase) {
                val cursor = db.query(
                    """
                    SELECT id, ${LEGACY_SESSION_ID_COLUMN}, lift_id, lift_set_number, set_label
                    FROM lift_sets
                    ORDER BY $LEGACY_SESSION_ID_COLUMN ASC, lift_id ASC, lift_set_number ASC, id ASC
                    """.trimIndent()
                )

                cursor.use {
                    var currentSessionId: Int? = null
                    var currentLiftId: Int? = null
                    var nextLiftSetNumber = 1

                    while (it.moveToNext()) {
                        val liftSetId = it.getInt(0)
                        val sessionId = it.getInt(1)
                        val liftId = it.getInt(2)
                        val oldLiftSetNumber = it.getInt(3)
                        val setLabel = it.getString(4)

                        if (sessionId != currentSessionId || liftId != currentLiftId) {
                            currentSessionId = sessionId
                            currentLiftId = liftId
                            nextLiftSetNumber = 1
                        }

                        if (setLabel == "Set $oldLiftSetNumber") {
                            db.execSQL(
                                """
                                UPDATE lift_sets
                                SET lift_set_number = ?, set_label = ?
                                WHERE id = ?
                                """.trimIndent(),
                                arrayOf<Any>(
                                    nextLiftSetNumber,
                                    "Set $nextLiftSetNumber",
                                    liftSetId
                                )
                            )
                        } else {
                            db.execSQL(
                                "UPDATE lift_sets SET lift_set_number = ? WHERE id = ?",
                                arrayOf(nextLiftSetNumber, liftSetId)
                            )
                        }

                        nextLiftSetNumber += 1
                    }
                }
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                rebuildLiftSetsTable(db)

                renumberLiftSetsByMuscleGroupAndSession(db)

                db.execSQL(
                    "CREATE UNIQUE INDEX index_lift_sets_${LEGACY_SESSION_ID_COLUMN}_${LEGACY_SESSION_SET_NUMBER_COLUMN} ON lift_sets(${LEGACY_SESSION_ID_COLUMN}, ${LEGACY_SESSION_SET_NUMBER_COLUMN})"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX index_lift_sets_${LEGACY_SESSION_ID_COLUMN}_lift_id_lift_set_number ON lift_sets(${LEGACY_SESSION_ID_COLUMN}, lift_id, lift_set_number)"
                )
                db.execSQL("CREATE INDEX index_lift_sets_lift_id ON lift_sets(lift_id)")
                db.execSQL(
                    "CREATE UNIQUE INDEX index_lift_sets_${LEGACY_SESSION_ID_COLUMN}_muscle_group_id_${LEGACY_MUSCLE_GROUP_SESSION_SET_NUMBER_COLUMN} ON lift_sets(${LEGACY_SESSION_ID_COLUMN}, muscle_group_id, ${LEGACY_MUSCLE_GROUP_SESSION_SET_NUMBER_COLUMN})"
                )
                db.execSQL("CREATE INDEX index_lift_sets_muscle_group_id ON lift_sets(muscle_group_id)")
            }

            private fun rebuildLiftSetsTable(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS lift_sets_migration")
                db.execSQL(
                    """
                    CREATE TABLE lift_sets_migration (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        $LEGACY_SESSION_ID_COLUMN INTEGER NOT NULL,
                        lift_id INTEGER NOT NULL,
                        muscle_group_id INTEGER NOT NULL,
                        lift_set_number INTEGER NOT NULL,
                        $LEGACY_SESSION_SET_NUMBER_COLUMN INTEGER NOT NULL,
                        $LEGACY_MUSCLE_GROUP_SESSION_SET_NUMBER_COLUMN INTEGER NOT NULL,
                        set_label TEXT NOT NULL,
                        set_note TEXT NOT NULL,
                        FOREIGN KEY(${LEGACY_SESSION_ID_COLUMN}) REFERENCES ${LEGACY_SESSION_TABLE}(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(lift_id) REFERENCES lifts(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(muscle_group_id) REFERENCES muscle_groups(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO lift_sets_migration (
                        id,
                        ${LEGACY_SESSION_ID_COLUMN},
                        lift_id,
                        muscle_group_id,
                        lift_set_number,
                        ${LEGACY_SESSION_SET_NUMBER_COLUMN},
                        ${LEGACY_MUSCLE_GROUP_SESSION_SET_NUMBER_COLUMN},
                        set_label,
                        set_note
                    )
                    SELECT
                        ls.id,
                        ls.${LEGACY_SESSION_ID_COLUMN},
                        ls.lift_id,
                        l.muscle_group_id,
                        ls.lift_set_number,
                        ls.${LEGACY_SESSION_SET_NUMBER_COLUMN},
                        0,
                        ls.set_label,
                        ls.set_note
                    FROM lift_sets AS ls
                    INNER JOIN lifts AS l
                        ON l.id = ls.lift_id
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE lift_sets")
                db.execSQL("ALTER TABLE lift_sets_migration RENAME TO lift_sets")
            }

            private fun renumberLiftSetsByMuscleGroupAndSession(db: SupportSQLiteDatabase) {
                val cursor = db.query(
                    """
                    SELECT ls.id, ls.${LEGACY_SESSION_ID_COLUMN}, ls.muscle_group_id
                    FROM lift_sets AS ls
                    ORDER BY ls.${LEGACY_SESSION_ID_COLUMN} ASC, ls.muscle_group_id ASC, ls.${LEGACY_SESSION_SET_NUMBER_COLUMN} ASC, ls.id ASC
                    """.trimIndent()
                )

                cursor.use {
                    var currentSessionId: Int? = null
                    var currentMuscleGroupId: Int? = null
                    var nextMuscleGroupSessionSetNumber = 1

                    while (it.moveToNext()) {
                        val liftSetId = it.getInt(0)
                        val sessionId = it.getInt(1)
                        val muscleGroupId = it.getInt(2)

                        if (sessionId != currentSessionId || muscleGroupId != currentMuscleGroupId) {
                            currentSessionId = sessionId
                            currentMuscleGroupId = muscleGroupId
                            nextMuscleGroupSessionSetNumber = 1
                        }

                        db.execSQL(
                            "UPDATE lift_sets SET $LEGACY_MUSCLE_GROUP_SESSION_SET_NUMBER_COLUMN = ? WHERE id = ?",
                            arrayOf(nextMuscleGroupSessionSetNumber, liftSetId)
                        )
                        nextMuscleGroupSessionSetNumber += 1
                    }
                }
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (!columnExists(db, "lift_sets", "muscle_group_id")) {
                    rebuildLiftSetsTable(db)
                    renumberLiftSetsByMuscleGroupAndSession(db)
                }

                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_lift_sets_${LEGACY_SESSION_ID_COLUMN}_${LEGACY_SESSION_SET_NUMBER_COLUMN} ON lift_sets(${LEGACY_SESSION_ID_COLUMN}, ${LEGACY_SESSION_SET_NUMBER_COLUMN})"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_lift_sets_${LEGACY_SESSION_ID_COLUMN}_lift_id_lift_set_number ON lift_sets(${LEGACY_SESSION_ID_COLUMN}, lift_id, lift_set_number)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_lift_sets_lift_id ON lift_sets(lift_id)")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_lift_sets_${LEGACY_SESSION_ID_COLUMN}_muscle_group_id_${LEGACY_MUSCLE_GROUP_SESSION_SET_NUMBER_COLUMN} ON lift_sets(${LEGACY_SESSION_ID_COLUMN}, muscle_group_id, ${LEGACY_MUSCLE_GROUP_SESSION_SET_NUMBER_COLUMN})"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_lift_sets_muscle_group_id ON lift_sets(muscle_group_id)")
            }

            private fun columnExists(
                db: SupportSQLiteDatabase,
                tableName: String,
                columnName: String
            ): Boolean {
                val cursor = db.query("PRAGMA table_info($tableName)")

                return cursor.use {
                    val nameColumnIndex = it.getColumnIndex("name")
                    while (it.moveToNext()) {
                        if (it.getString(nameColumnIndex) == columnName) {
                            return@use true
                        }
                    }

                    false
                }
            }

            private fun rebuildLiftSetsTable(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS lift_sets_migration")
                db.execSQL(
                    """
                    CREATE TABLE lift_sets_migration (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        $LEGACY_SESSION_ID_COLUMN INTEGER NOT NULL,
                        lift_id INTEGER NOT NULL,
                        muscle_group_id INTEGER NOT NULL,
                        lift_set_number INTEGER NOT NULL,
                        $LEGACY_SESSION_SET_NUMBER_COLUMN INTEGER NOT NULL,
                        $LEGACY_MUSCLE_GROUP_SESSION_SET_NUMBER_COLUMN INTEGER NOT NULL,
                        set_label TEXT NOT NULL,
                        set_note TEXT NOT NULL,
                        FOREIGN KEY(${LEGACY_SESSION_ID_COLUMN}) REFERENCES ${LEGACY_SESSION_TABLE}(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(lift_id) REFERENCES lifts(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(muscle_group_id) REFERENCES muscle_groups(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO lift_sets_migration (
                        id,
                        ${LEGACY_SESSION_ID_COLUMN},
                        lift_id,
                        muscle_group_id,
                        lift_set_number,
                        ${LEGACY_SESSION_SET_NUMBER_COLUMN},
                        ${LEGACY_MUSCLE_GROUP_SESSION_SET_NUMBER_COLUMN},
                        set_label,
                        set_note
                    )
                    SELECT
                        ls.id,
                        ls.${LEGACY_SESSION_ID_COLUMN},
                        ls.lift_id,
                        l.muscle_group_id,
                        ls.lift_set_number,
                        ls.${LEGACY_SESSION_SET_NUMBER_COLUMN},
                        0,
                        ls.set_label,
                        ls.set_note
                    FROM lift_sets AS ls
                    INNER JOIN lifts AS l
                        ON l.id = ls.lift_id
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE lift_sets")
                db.execSQL("ALTER TABLE lift_sets_migration RENAME TO lift_sets")
            }

            private fun renumberLiftSetsByMuscleGroupAndSession(db: SupportSQLiteDatabase) {
                val cursor = db.query(
                    """
                    SELECT ls.id, ls.${LEGACY_SESSION_ID_COLUMN}, ls.muscle_group_id
                    FROM lift_sets AS ls
                    ORDER BY ls.${LEGACY_SESSION_ID_COLUMN} ASC, ls.muscle_group_id ASC, ls.${LEGACY_SESSION_SET_NUMBER_COLUMN} ASC, ls.id ASC
                    """.trimIndent()
                )

                cursor.use {
                    var currentSessionId: Int? = null
                    var currentMuscleGroupId: Int? = null
                    var nextMuscleGroupSessionSetNumber = 1

                    while (it.moveToNext()) {
                        val liftSetId = it.getInt(0)
                        val sessionId = it.getInt(1)
                        val muscleGroupId = it.getInt(2)

                        if (sessionId != currentSessionId || muscleGroupId != currentMuscleGroupId) {
                            currentSessionId = sessionId
                            currentMuscleGroupId = muscleGroupId
                            nextMuscleGroupSessionSetNumber = 1
                        }

                        db.execSQL(
                            "UPDATE lift_sets SET $LEGACY_MUSCLE_GROUP_SESSION_SET_NUMBER_COLUMN = ? WHERE id = ?",
                            arrayOf(nextMuscleGroupSessionSetNumber, liftSetId)
                        )
                        nextMuscleGroupSessionSetNumber += 1
                    }
                }
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val sessionMigrationSource = findSessionMigrationSource(db)

                rebuildSessionsTable(db, sessionMigrationSource)
                rebuildLiftSetsTable(db)

                db.execSQL("DROP TABLE lift_sets")
                db.execSQL("DROP TABLE ${quoteSqlIdentifier(sessionMigrationSource.tableName)}")
                db.execSQL("ALTER TABLE lift_sets_migration RENAME TO lift_sets")

                db.execSQL(
                    "CREATE UNIQUE INDEX index_sessions_profile_id_session_number ON sessions(profile_id, session_number)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX index_lift_sets_session_id_session_set_number ON lift_sets(session_id, session_set_number)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX index_lift_sets_session_id_lift_id_lift_set_number ON lift_sets(session_id, lift_id, lift_set_number)"
                )
                db.execSQL("CREATE INDEX index_lift_sets_lift_id ON lift_sets(lift_id)")
                db.execSQL(
                    "CREATE UNIQUE INDEX index_lift_sets_session_id_muscle_group_id_muscle_group_session_set_number ON lift_sets(session_id, muscle_group_id, muscle_group_session_set_number)"
                )
                db.execSQL("CREATE INDEX index_lift_sets_muscle_group_id ON lift_sets(muscle_group_id)")
            }

            private fun rebuildSessionsTable(
                db: SupportSQLiteDatabase,
                sessionMigrationSource: SessionMigrationSource
            ) {
                db.execSQL("DROP TABLE IF EXISTS sessions")
                db.execSQL(
                    """
                    CREATE TABLE sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        in_progress INTEGER NOT NULL DEFAULT 0,
                        profile_id INTEGER NOT NULL,
                        session_number INTEGER NOT NULL,
                        session_label TEXT NOT NULL,
                        date TEXT NOT NULL,
                        note TEXT NOT NULL,
                        FOREIGN KEY(profile_id) REFERENCES profiles(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO sessions (
                        id,
                        in_progress,
                        profile_id,
                        session_number,
                        session_label,
                        date,
                        note
                    )
                    SELECT
                        id,
                        in_progress,
                        profile_id,
                        ${quoteSqlIdentifier(sessionMigrationSource.sessionNumberColumnName)},
                        'Session ' || ${quoteSqlIdentifier(sessionMigrationSource.sessionNumberColumnName)},
                        date,
                        note
                    FROM ${quoteSqlIdentifier(sessionMigrationSource.tableName)}
                    """.trimIndent()
                )
            }

            private fun rebuildLiftSetsTable(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS lift_sets_migration")
                db.execSQL(
                    """
                    CREATE TABLE lift_sets_migration (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        session_id INTEGER NOT NULL,
                        lift_id INTEGER NOT NULL,
                        muscle_group_id INTEGER NOT NULL,
                        lift_set_number INTEGER NOT NULL,
                        session_set_number INTEGER NOT NULL,
                        muscle_group_session_set_number INTEGER NOT NULL,
                        set_label TEXT NOT NULL,
                        set_note TEXT NOT NULL,
                        FOREIGN KEY(session_id) REFERENCES sessions(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(lift_id) REFERENCES lifts(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(muscle_group_id) REFERENCES muscle_groups(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO lift_sets_migration (
                        id,
                        session_id,
                        lift_id,
                        muscle_group_id,
                        lift_set_number,
                        session_set_number,
                        muscle_group_session_set_number,
                        set_label,
                        set_note
                    )
                    SELECT *
                    FROM lift_sets
                    """.trimIndent()
                )
            }

            private fun findSessionMigrationSource(db: SupportSQLiteDatabase): SessionMigrationSource {
                val cursor = db.query(
                    """
                    SELECT name
                    FROM sqlite_master
                    WHERE type = 'table'
                        AND name NOT LIKE 'sqlite_%'
                    """.trimIndent()
                )

                cursor.use {
                    while (it.moveToNext()) {
                        val tableName = it.getString(0)
                        val source = getSessionMigrationSource(db, tableName)
                        if (tableName != "sessions" && source != null) {
                            return source
                        }
                    }
                }

                error("Unable to find session migration source table")
            }

            private fun getSessionMigrationSource(
                db: SupportSQLiteDatabase,
                tableName: String
            ): SessionMigrationSource? {
                val columns = getTableColumns(db, tableName)
                val commonColumnNames = setOf("id", "in_progress", "profile_id", "date", "note")
                val sessionSpecificColumns = columns.filter { it.name !in commonColumnNames }

                if (columns.size != 7 ||
                    !columns.map { it.name }.containsAll(commonColumnNames) ||
                    sessionSpecificColumns.size != 2
                ) {
                    return null
                }

                val numberColumn = sessionSpecificColumns.firstOrNull { column ->
                    column.type.uppercase().contains("INT")
                } ?: return null

                if (sessionSpecificColumns.none { column -> column.type.uppercase().contains("TEXT") }) {
                    return null
                }

                return SessionMigrationSource(tableName, numberColumn.name)
            }

            private fun getTableColumns(
                db: SupportSQLiteDatabase,
                tableName: String
            ): List<TableColumn> {
                val cursor = db.query("PRAGMA table_info(${quoteSqlIdentifier(tableName)})")
                val columns = mutableListOf<TableColumn>()

                cursor.use {
                    while (it.moveToNext()) {
                        columns.add(
                            TableColumn(
                                name = it.getString(1),
                                type = it.getString(2)
                            )
                        )
                    }
                }

                return columns
            }

            private fun quoteSqlIdentifier(identifier: String): String =
                "\"${identifier.replace("\"", "\"\"")}\""
        }
    }
}
