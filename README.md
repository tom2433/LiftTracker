# LiftTracker
A prototype of the Lift Tracker project. This is an evolving document. Everything in here is subject to change.

---

# Development

## Features:

1. Backend database design (F1)
    - Add a set metric for time, in addition to weight and reps (F1A)
2. (Completed) Menu drawer with basic elements (F2):
    - Begin Session (F2A)
    - Muscle Groups (F2B)
    - Sessions (F2C)
    - Calendar (F2D)
    - Switch Profile (F2E)
    - Divider ---
    - Analytics (F2F)
    - Tools (F2G)
    - Settings (F2H)
3. (Completed) Map menu drawer items with screens where applicable (F2-1)
4. Implement dropdowns for applicable items in drawer (F2-2)
5. (Maybe) Animate hamburger menu so that it travels and rotates into an arrow (F2-3)
4. Colorscheme (F3):
    - Default to preset colorscheme (F3A)
    - Toggle dynamic color (F3B)
    - Toggle Dark theme (F3C)

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
- ```day_label``` (TEXT): the name of the day; e.g. ```"Day 1"```, ```"Day 2"```, ```"Day 3"```, etc. as default. The user may be able to change this name in future versions.
- ```note``` (TEXT): a user-written note for the day, may be blank

#### lift_sets

The purpose of the ```lift_sets``` table is to keep track of all sets that the user has completed. It also links each set with the specific lift that was trained, and the day that the user completed the set on.

The ```lift_sets``` table has 6 columns:

- ```set_id``` (INTEGER): primary key. This is the main identifier that the ```set_metrics``` table uses to associate specific set data (weight, reps) with a specific set that was completed for a specific lift on a specific day.
- ```lift_day_id``` (INTEGER): foreign key referring to ```lift_days```. This is what links this set to a particular day.
- ```lift_id``` (INTEGER): foreign key referring to ```lifts```. This is what links this set to a particular lift (e.g., bicep curls).
- ```set_number``` (INTEGER): this set number identifies when this set took place, only in relation to the other sets completed for this specific lift on this specific day.
- ```set_label``` (TEXT): just a string containing the name of the set (e.g., ```"Set 1"```, ```"Set 2"```, ```"Set 3"```, etc. as default). The user may be able to change this label in future versions.
- ```set_note``` (TEXT): a user-written note for the set, may be blank

#### lifts

The purpose of the ```lifts``` table is to store the names of all the different user-created lifts and link them to their corresponding muscle groups.

The ```lifts``` table has 4 columns:

- ```lift_id``` (INTEGER): primary key. This is the main identifier that the ```lift_sets``` table uses to associate lift names with set data.
- ```muscle_group_id``` (INTEGER): foreign key referring to the ```muscle_groups``` table. This is what links each lift to its corresponding muscle group.
- ```name``` (TEXT): the user-specified name for the lift.
- ```note``` (TEXT): a user-written note for the lift, may be blank

> [!WARNING]
> The ```lifts``` table will eventually need to be updated to include a ```metric_type``` column, which will indicate whether the lift data will be measured in reps or time.

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
- ```metric_name``` (TEXT): this indicates whether the metric is a weight value or a rep value (e.g., ```"weight"``` or ```"reps"```). In future versions, ```"reps"``` may be replaced with ```"time"``` depending on the user's discretion.
- ```value``` (REAL): this indicates the number of reps performed, or the weight value for the specific set.
- ```note``` (TEXT): a user-written note for the metric, may be blank.

#### profiles

The purpose of the ```profiles``` table is to store the names of all the profiles that the user has created. The profile is linked to its data via the ```muscle_groups``` table.

The ```profiles``` table has 3 columns:

- ```id``` (INTEGER): primary key. This is the main identifier used to distinguish between each profile.
- ```name``` (TEXT): the user-written name for the profile.
- ```note``` (TEXT): a user-written note for the profile, may be blank.

---

# Pre-Dev Notes

## Things I'd like to see

- In the workout logger:
    - Completed sets should show up as collapsable/expandable cards when completed.
    - All weight/rep/note data for the given session should always be able to be viewed at any point throughout the lift.
    - Sets from earlier days should be displayed as a different color.
    - **maybe** Stopwatch, potentially the ability to lap for sets/rests
- In general:
    - Ability to switch between different color schemes or turn on/off dynamic color
    - Ability to switch between light/dark theme or use system default
    - Potentially providing insights on certain lift days based on probability distributions
    - Ability to add a lift to multiple muscle groups?
    - Add the option to use time metrics instead of reps!!
    - Sets should be able to be viewed at any point for the duration of the workout
        - earlier sets could be displayed as a different color
    - ability to save workout summary text and upload automatically to Garmin/Strava
        - I think strava provides the ability to do this. Garmin does not. Maybe give the user some text to copy.
    - Should be able to enter a previous workout day and continue it in play mode.
    - Ability to give an unknown value for reps if you don't remember - should just average the before and after for graph points.
- Backend:
    - Add start time and end time to each Day
    - Add names to each day (user-specified)
    - Export lift data to excel

---

# Product Requirements

## UI Elements

### Navigation Drawer via hamburger menu on TopAppBar

The Left hand side will have a menu that animates from the left to allow the user to switch between screens.

**Menu drawer elements**

1. Begin session (change to resume session when one is in progress, with a flag icon to finish session)
2. Muscle groups
    - list with muscle group names/notes, each has a pencil icon to edit name/note.
    - '+' option at the bottom to add a muscle group
    - when a muscle group is clicked, lifts for that muscle group are displayed
        - dropdown list with lift names/notes, each has a pencil icon to edit
        - when a lift is clicked, days in which that lift was trained are displayed
            - dropdown list with day names/notes, each has a pencil icon to edit
            - when a day is clicked, the menu disappears to show the Day screen.
3. Sessions (Days, most recent one is first)
    - list with most recent workout day names/notes/day #, each has a pencil icon to edit name/note.
    - when a workout day is clicked, the card expands to the entire screen
4. Calendar (still deciding how to implement)
5. Switch profile
    - dropdown list with profile names & notes, each has a pencil icon to edit name/notes.
    - '+' option at the bottom to add a profile
    - when a profile is clicked, the entire UI switches to that profile.
6. (Put at bottom) Settings
    - Toggle dynamic color
    - Toggle dark mode