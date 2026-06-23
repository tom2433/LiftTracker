package com.example.lifttracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
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
*/

@Entity(
    tableName = "lift_sets",
    foreignKeys = [
        ForeignKey(
            entity = LiftDay::class,
            parentColumns = ["id"],
            childColumns = ["lift_day_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Lift::class,
            parentColumns = ["id"],
            childColumns = ["lift_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["lift_day_id"]),
        Index(value = ["lift_id"])
    ]
)
data class LiftSet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // primary key
    val lift_day_id: Int,               // FK to lift_days.id
    val lift_id: Int,                   // FK to lifts.id
    val lift_set_number: Int,           // set # in relation to completed sets for this lift id and lift_day id
    val day_set_number: Int,            // set # in relation to completes sets for this lift_day_id
    val set_label: String,              // name of set (default to "Set 1", "Set 2", etc.)
    val set_note: String                // optional user-written note for set
)