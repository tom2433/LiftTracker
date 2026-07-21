package github.tom2433.lifttracker.data.liftunit

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
## ```lift_units```

The purpose of the ```lift_units``` table is to store the names of all the user-written lift units, which are added to different lifts. The lift_units table is designed to be independent of profiles, so multiple profiles can use the same lift unit.

The ```lift_units``` table has two columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lifts``` table uses to associate lifts with their appropriate lift units.
- ```name``` (TEXT): the user-written name of the lift unit

> [!NOTE]
>
> May need some protection to ensure that a lift unit that is being used cannot be deleted.
*/
@Entity(
    tableName = "lift_units"
)
data class LiftUnit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // primary key
    val name: String                    // user-written name of the lift unit
)