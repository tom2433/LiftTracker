package com.example.lifttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
## ```profiles```

The purpose of the ```profiles``` table is to store the names of all the profiles that the user has created. The profile is linked to its data via the ```muscle_groups``` table.

The ```profiles``` table has 3 columns:

- ```id``` (INTEGER): primary key. This is the main identifier used to distinguish between each profile.
- ```name``` (TEXT): the user-written name for the profile.
- ```note``` (TEXT): a user-written note for the profile, may be blank.
*/
@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // primary key
    val name: String,                   // name of profile
    val note: String                    // optional user-written note for profile
)