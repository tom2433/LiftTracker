package com.example.lifttracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
## ```lifts```

The purpose of the ```lifts``` table is to store the names of all the different user-created lifts and link them to their corresponding muscle groups.

The ```lifts``` table has 4 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lift_sets``` table uses to associate lift names with set data.
- ```muscle_group_id``` (INTEGER): foreign key referring to the ```muscle_groups``` table. This is what links each lift to its corresponding muscle group.
- ```name``` (TEXT): the user-specified name for the lift.
- ```note``` (TEXT): a user-written note for the lift, may be blank

> Note:
> The ```lifts``` table will eventually need to be updated to include a ```metric_type``` column, which will indicate whether the lift data will be measured in reps or time.
*/
@Entity(
    tableName = "lifts",
    foreignKeys = [
        ForeignKey(
            entity = MuscleGroup::class,
            parentColumns = ["id"],
            childColumns = ["muscle_group_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["muscle_group_id"])
    ]
)
data class Lift(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // primary key
    val muscle_group_id: Int,           // FK to muscle_groups.id
    val name: String,                   // name of lift
    val note: String                    // optional user-written note for lift
)