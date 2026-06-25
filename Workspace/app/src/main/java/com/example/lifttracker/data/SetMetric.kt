package com.example.lifttracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
## ```set_metrics```

The purpose of the ```set_metrics``` table is to store all of the user's set metrics (weight and rep data) and link them to their corresponding set (which is linked to the corresponding lift and the corresponding day via foreign keys).

The ```set_metrics``` table has 6 columns:

- ```id``` (INTEGER): primary key. This is the main identifier for each set metric.
- ```set_id``` (INTEGER): foreign key referring to the ```lift_sets``` table. This is what links each lift metric to its corresponding set.
- ```metric_position``` (INTEGER): this indicates whether the metric is a weight value or a rep value. ```1``` indicates weight, and ```2``` indicates reps.
- ```value``` (REAL): this indicates the number of reps performed, the weight value, or the time value for the specific set. Time values will be stored as doubles representing minutes, e.g. 1 minute and 30 seconds = 1.5 minutes
- ```note``` (TEXT): a user-written note for the metric, may be blank.
*/
@Suppress("PropertyName")
@Entity(
    tableName = "set_metrics",
    foreignKeys = [
        ForeignKey(
            entity = LiftSet::class,
            parentColumns = ["id"],
            childColumns = ["set_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["set_id"])
    ]
)
data class SetMetric(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // primary key
    val set_id: Int,                    // FK to lift_sets.id
    val metric_position: Int,           // indicates whether weight (1) or rep (2) value
    val value: Double,                  // number of reps performed, weight value, or time value
    val note: String                    // optional user-written note for metric
)