package github.tom2433.lifttracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
# Tables:

## ```lift_days```

The purpose of the ```lift_days``` table is to keep track of all the days that the user has logged.

The ```lift_days``` table has 7 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lift_sets``` table uses to associate set data with a specific day.
- ```in_progress``` (INTEGER): indicates 1 if the day is still in progress, or 0 if the user has already completed this day.
- ```profile_id``` (INTEGER): foreign key. This is what links the lift day to the appropriate profile.
- ```day_number``` (INTEGER): the number of the day; e.g. ```1```, ```2```, ```3```, etc.
- ```day_label``` (TEXT): the name of the day; e.g. ```"Day 1"```, ```"Day 2"```, ```"Day 3"```, etc. as default. The user may be able to change this name in future versions.
- ```date``` (TEXT): The date of the day that the session was recorded in ISO-8601 format: YYYY-MM-DD
- ```note``` (TEXT): a user-written note for the day, may be blank

## ```lift_sets```

The purpose of the ```lift_sets``` table is to keep track of all sets that the user has completed. It also links each set with the specific lift that was trained, and the day that the user completed the set on.

The ```lift_sets``` table has 6 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```set_metrics``` table uses to associate specific set data (weight, reps) with a specific set that was completed for a specific lift on a specific day.
- ```lift_day_id``` (INTEGER): foreign key referring to ```lift_days```. This is what links this set to a particular day.
- ```lift_id``` (INTEGER): foreign key referring to ```lifts```. This is what links this set to a particular lift (e.g., bicep curls).
- ```lift_set_number``` (INTEGER): this set number identifies when this set took place, only in relation to the other sets completed for this specific lift on this specific day.
- ```day_set_number``` (INTEGER): this set number identifies when this set took place, in relation to all other sets completed on this specific day.
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

The purpose of the ```set_metrics``` table is to store all of the user's set metrics (weight and rep data) and link them to their corresponding set (which is linked to the corresponding lift and the corresponding day via foreign keys).

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
        LiftDay::class,
        Lift::class,
        LiftSet::class,
        MuscleGroup::class,
        Profile::class,
        SetMetric::class,
        LiftUnit::class
    ],
    version = 4,
    exportSchema = false
)
abstract class LiftTrackerDatabase : RoomDatabase() {
    abstract fun liftDao(): LiftDao
    abstract fun liftDayDao(): LiftDayDao
    abstract fun liftSetDao(): LiftSetDao
    abstract fun muscleGroupDao(): MuscleGroupDao
    abstract fun profileDao(): ProfileDao
    abstract fun setMetricDao(): SetMetricDao
    abstract fun liftUnitDao(): LiftUnitDao


    companion object {
        @Volatile
        private var Instance: LiftTrackerDatabase? = null
        fun getDatabase(context: Context): LiftTrackerDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    LiftTrackerDatabase::class.java,
                    "lift_tracker_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
                    "ALTER TABLE lift_days ADD COLUMN in_progress INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL("CREATE INDEX index_lift_days_profile_id ON lift_days(profile_id)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                renumberLiftDaysByProfile(db)
                db.execSQL("DROP INDEX IF EXISTS index_lift_days_profile_id")
                db.execSQL(
                    "CREATE UNIQUE INDEX index_lift_days_profile_id_day_number ON lift_days(profile_id, day_number)"
                )
            }

            private fun renumberLiftDaysByProfile(db: SupportSQLiteDatabase) {
                val cursor = db.query(
                    """
                    SELECT id, profile_id
                    FROM lift_days
                    ORDER BY profile_id ASC, day_number ASC, id ASC
                    """.trimIndent()
                )

                cursor.use {
                    var currentProfileId: Int? = null
                    var nextDayNumber = 1

                    while (it.moveToNext()) {
                        val liftDayId = it.getInt(0)
                        val profileId = it.getInt(1)

                        if (profileId != currentProfileId) {
                            currentProfileId = profileId
                            nextDayNumber = 1
                        }

                        db.execSQL(
                            "UPDATE lift_days SET day_number = ? WHERE id = ?",
                            arrayOf(nextDayNumber, liftDayId)
                        )
                        nextDayNumber += 1
                    }
                }
            }
        }
    }
}
