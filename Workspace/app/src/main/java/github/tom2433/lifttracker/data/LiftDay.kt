package github.tom2433.lifttracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
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
*/
@Suppress("PropertyName")
@Entity(
    tableName = "lift_days",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profile_id", "day_number"], unique = true)
    ]
)
data class LiftDay(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // primary key
    @ColumnInfo(defaultValue = "0")
    val in_progress: Boolean = false,   // indicates whether this day is currently in progress or not
    val profile_id: Int,                // FK to profiles.id
    val day_number: Int,                // chronological number of lift day
    val day_label: String,              // name of the Lift Day (default to "Day 1", "Day 2", etc.)
    val date: String,                   // date that the session was recorded in ISO-8601 format
    val note: String                    // optional user-written note for lift day
)
