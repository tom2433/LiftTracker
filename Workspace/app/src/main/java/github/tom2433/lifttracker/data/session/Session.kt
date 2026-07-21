package github.tom2433.lifttracker.data.session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import github.tom2433.lifttracker.data.profile.Profile

/**
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
*/
@Suppress("PropertyName")
@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [
        Index(value = ["profile_id", "session_number"], unique = true)
    ]
)
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // primary key
    @ColumnInfo(defaultValue = "0")
    val in_progress: Boolean = false,   // indicates whether this session is currently in progress or not
    val profile_id: Int,                // FK to profiles.id
    val session_number: Int,            // chronological number of session
    val session_label: String,          // name of the session (default to "Session 1", "Session 2", etc.)
    val date: String,                   // date that the session was recorded in ISO-8601 format
    val note: String                    // optional user-written note for session
)
