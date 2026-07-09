package github.tom2433.lifttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
## ```units```

The purpose of the ```units``` table is to store the names of all the user-written units, which are added to different lifts. The units table is designed to be independent of profiles, so multiple profiles can use the same unit.

The ```units``` table has two columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lifts``` table uses to associate lifts with their appropriate units.
- ```name``` (TEXT): the user-written name of the unit

> [!NOTE]
>
> May need some protection to ensure that a unit that is being used cannot be deleted.
*/
@Entity(
    tableName = "units"
)
data class Unit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // primary key
    val name: String                    // user-written name of the unit
)