package github.tom2433.lifttracker.data.liftset

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import github.tom2433.lifttracker.data.lift.Lift
import github.tom2433.lifttracker.data.session.Session
import github.tom2433.lifttracker.data.musclegroup.MuscleGroup

/**
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
*/

@Suppress("PropertyName")
@Entity(
    tableName = "lift_sets",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Lift::class,
            parentColumns = ["id"],
            childColumns = ["lift_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MuscleGroup::class,
            parentColumns = ["id"],
            childColumns = ["muscle_group_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["session_id", "session_set_number"], unique = true),
        Index(value = ["session_id", "lift_id", "lift_set_number"], unique = true),
        Index(value = ["lift_id"]),
        Index(value = ["session_id", "muscle_group_id", "muscle_group_session_set_number"], unique = true),
        Index(value = ["muscle_group_id"])
    ]
)
data class LiftSet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                        // primary key
    val session_id: Int,                   // FK to sessions.id
    val lift_id: Int,                       // FK to lifts.id
    val muscle_group_id: Int,               // FK to muscle_groups.id
    val lift_set_number: Int,               // set # in relation to completed sets for this lift id and session id
    val session_set_number: Int,                // set # in relation to completed sets for this session_id
    val muscle_group_session_set_number: Int,   // set # in relation to completed sets for this muscle group and session_id
    val set_label: String,                  // name of set (default to "Set 1", "Set 2", etc.)
    val set_note: String                    // optional user-written note for set
)
