package github.tom2433.lifttracker.data

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
- ```unit_id``` (INTEGER): foreign key referring to the ```lift_units``` table. This is what links each lift to its corresponding user-written lift unit.
- ```name``` (TEXT): the user-specified name for the lift.
- ```metric_type``` (INTEGER): Int indicating if the lift will be measured in reps (1) or time (2). If the metric type is time, then the unit_id will be overridden.
- ```note``` (TEXT): a user-written note for the lift, may be blank
*/
@Suppress("PropertyName")
@Entity(
    tableName = "lifts",
    foreignKeys = [
        ForeignKey(
            entity = MuscleGroup::class,
            parentColumns = ["id"],
            childColumns = ["muscle_group_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LiftUnit::class,
            parentColumns = ["id"],
            childColumns = ["unit_id"],
            // this lift will not be deleted if this lift unit is deleted
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["muscle_group_id"]),
        Index(value = ["unit_id"])
    ]
)
data class Lift(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // primary key
    val muscle_group_id: Int,           // FK to muscle_groups.id
    val unit_id: Int,                   // FK to lift_units.id
    val name: String,                   // name of lift
    val metric_type: Int,               // Int indicating if the lift is measured in reps (1) or time (2)
    val note: String                    // optional user-written note for lift
)
