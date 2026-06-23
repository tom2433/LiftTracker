package com.example.lifttracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
## ```muscle_groups```

The purpose of the ```muscle_groups``` table is to store the user-generated names for all the muscle groups and link them to their corresponding profile.

The ```muscle_groups``` table has 4 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lifts``` table uses to associate lift names with muscle groups.
- ```profile_id``` (INTEGER): foreign key referring to the ```profiles``` table. This is what links each muscle group to its corresponding user (profile).
- ```name``` (TEXT): the user-specified name for the muscle group.
- ```note``` (TEXT): a user-written note for the muscle group, may be blank.
*/
@Entity(
    tableName = "muscle_groups",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profile_id"])
    ]
)
data class MuscleGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // primary key
    val profile_id: Int,                // FK to profiles.id
    val name: String,                   // name of muscle group
    val note: String                    // optional user-written note for muscle group
)