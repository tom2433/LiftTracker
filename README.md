# LiftTracker
A prototype of the Lift Tracker project. This is an evolving document. Everything in here is subject to change.

---

# Basic Structure

3 main sections:

- View/edit/add all data in the database
- Data analysis tab (view day summaries, add more sub-tabs for further analysis)
- Workout logger

## Data Structure

All data is stored in lifting_data.db. Below is the structure of this data.

### Tables

#### lift_days

The purpose of the ```lift_days``` table is to keep track of all the days that the user has logged.

The ```lift_days``` table has 5 columns:

- ```lift_day_id``` (INTEGER): primary key. This is the main identifier that the ```lift_sets``` table uses to associate set data with a specific day.
- ```day_number``` (INTEGER): the number of the day; e.g. ```1```, ```2```, ```3```, etc.
- ```day_label``` (TEXT): the name of the day; e.g. ```"Day 1"```, ```"Day 2"```, ```"Day 3"```, etc.
- ```note``` (TEXT): a user-written note for the day, may be blank

> [!WARNING]
> The ```lift_days``` table must be modified to remove the day label since it can be generated at runtime.

#### lift_sets

The purpose of the ```lift_sets``` table is to keep track of all sets that the user has completed. It also links each set with the specific lift that was trained, and the day that the user completed the set on.

The ```lift_sets``` table has 6 columns:

- ```set_id``` (INTEGER): primary key. This is the main identifier that the ```set_metrics``` table uses to associate specific set data (weight, reps) with a specific set that was completed for a specific lift on a specific day.
- ```lift_day_id``` (INTEGER): foreign key referring to ```lift_days```. This is what links this set to a particular day.
- ```lift_id``` (INTEGER): foreign key referring to ```lifts```. This is what links this set to a particular lift (e.g., bicep curls).
- ```set_number``` (INTEGER): this set number identifies when this set took place, only in relation to the other sets completed for this specific lift on this specific day.
- ```set_label``` (TEXT): just a string containing the name of the set (e.g., ```"Set 1"```, ```"Set 2"```, ```"Set 3"```, etc.)
- ```set_note``` (TEXT): a user-written note for the set, may be blank

> [!WARNING]
> The ```lift_sets``` table must be modified to remove the set_label since it can be generated at runtime, and it also needs an overall set number to track when the set was completed in relation to all sets completed on the specific day.

#### lifts

The purpose of the ```lifts``` table is to store the names of all the different user-created lifts and link them to their corresponding muscle groups.

The ```lifts``` table has 4 columns:

- ```lift_id``` (INTEGER): primary key. This is the main identifier that the ```lift_sets``` table uses to associate lift names with set data.
- ```muscle_group_id``` (INTEGER): foreign key referring to the ```muscle_groups``` table. This is what links each lift to its corresponding muscle group.
- ```name``` (TEXT): the user-specified name for the lift.
- ```note``` (TEXT): a user-written note for the lift, may be blank

#### muscle_groups

The purpose of the ```muscle_groups``` table is to store the user-generated names for all the muscle groups and link them to their corresponding profile.

The ```muscle_groups``` table has 4 columns:

- ```muscle_group_id``` (INTEGER): primary key. This is the main identifier that the ```lifts``` table uses to associate lift names with muscle groups.
- ```user_id``` (INTEGER): foreign key referring to the ```users``` table. This is what links each muscle group to its corresponding user (profile).
- ```name``` (TEXT): the user-specified name for the muscle group.
- ```note``` (TEXT): a user-written note for the muscle group, may be blank.

#### set_metrics

The purpose of the ```set_metrics``` table is to store all of the user's set metrics (weight and rep data) and link them to their corresponding set (which is linked to the corresponding lift and the corresponding day via foreign keys).

The ```set_metrics``` table has 6 columns:

- ```metric_id``` (INTEGER): primary key. This is the main identifier for each set metric.
- ```set_id``` (INTEGER): foreign key referring to the ```lift_sets``` table. This is what links each lift metric to its corresponding set.
- ```metric_position``` (INTEGER): this indicates whether the metric is a weight value or a rep value. ```1``` indicates weight, and ```2``` indicates reps.
- ```metric_name``` (TEXT): this indicates whether the metric is a weight value or a rep value (e.g., ```"weight"``` or ```"reps"```)
- ```value``` (REAL): this indicates the number of reps performed, or the weight value for the specific set.
- ```note``` (TEXT): a user-written note for the metric, may be blank.

> [!WARNING]
> The ```set_metrics``` table must be edited to remove the ```metric_name``` column since it is redundant. The metric name can be determined from the ```metric_position```, which indicates ```1``` for weight and ```2``` for reps.

#### users

The purpose of the ```users``` table is to store the names of all the profiles that the user has created. The profile is linked to its data via the ```muscle_groups``` table.

The ```users``` table has 3 columns:

- ```user_id``` (INTEGER): primary key. This is the main identifier used to distinguish between each profile.
- ```name``` (TEXT): the user-written name for the profile.
- ```note``` (TEXT): a user-written note for the profile, may be blank.

> [!WARNING]
> The name of the ```users``` table must be changed to ```profiles``` since the name ```users``` is now obsolete. This change will also require a modification to the name of the ```user_id``` column in the ```muscle_groups``` table so that it will become ```profile_id```.

---

# Pre-Dev Notes

## Things I'd like to see

- In the workout logger:
    - Completed sets should show up as collapsable/expandable cards when completed.
    - All weight/rep/note data for the given session should always be able to be viewed at any point throughout the lift.
    - Sets from earlier days should be displayed as a different color.
    - Stopwatch, potentially the ability to lap for sets/rests
- In general:
    - Ability to switch between different color schemes or turn on/off dynamic color
    - Ability to switch between light/dark theme or use system default
    - Potentially providing insights on certain lift days based on probability distributions
    - Ability to add a lift to multiple muscle groups?
    - Add the option to use time metrics instead of reps!!
- Backend:
    - Add start time and end time to each Day
    - Add names to each day (user-specified)
    - Export lift data to excel
