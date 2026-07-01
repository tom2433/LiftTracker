# LiftTracker

A prototype of the Lift Tracker project. This is an evolving document. Everything in here is subject to change.

---

# Development

## Features:

1. (Completed) Backend database design (F1)
    - Add a set metric for time, in addition to weight and reps
    - Ensure that the viewModels have access to the database
    - Check if a user profile exists. If none do, then prompt the user to create one.
2. (Completed) Menu drawer with basic elements (F2):
    - Begin Session
    - Muscle Groups
    - Sessions
    - Calendar
    - Switch Profile
    - Divider ---
    - Analytics
    - Tools
    - Settings
3. (Completed) Map menu drawer items with screens where applicable (F2-1)
5. (Maybe) Animate hamburger menu so that it travels and rotates into an arrow (F2-3)
4. Colorscheme (F3):
    - Default to preset colorscheme
    - Toggle dynamic color
    - Toggle Dark theme
5. (Completed) Implement Switch Profile functionality (F4)
6. (Completed) Allow user to add, edit, and delete muscle groups in the muscle groups screen. (F5)
    - each muscle group will be displayed as a card.
    - each card will display the muscle group name and note, as well as the following:
        - number of lifts created
        - number of sessions trained (total)
        - average number of sessions/week
        - average number of sets/session
        - average number of sets/week
        - average number of reps per set
        - last date trained (today, yesterday, two days ago, three, ... six, one week, over a week, one month, over a month, two months...)
        - button to edit
        - button to delete
        - eventually:
            - a minimal line graph showing average progress over time (user can change timeframe in preferences)
            - color representing status of improving, maintaining, or declining
7. (In Progress) When a muscle group card is clicked, it animates a dropdown container with cards representing all lifts for that muscle group (F6)
    - The card shall expand itself, but a surface must be put on top of it so that it appears as if it is not. A border shall animate so that the card can connect itself to the edit and delete buttons at the bottom. An alternative option is to have the card elevate and maybe show the lift cards and edit/delete buttons at the bottom a different way.
    - figure out a way to make it so that the cursor can be moved around the text field and the text in the text field moves to the left to make room for the rest of the text.
    - Clicking on a lift card will transition itself into a lift Dialog (LiftScreen) with all lift statistics.
    - Lift Statistics will include:
        - lifetime number of sets performed
        - lifetime number of sessions trained
        - lifetime average weight
        - average weight from past month
        - average weight from past year
        - average number of reps per set
        - last date trained (today, yesterday, two days ago, three, six, 1 week, over 1 week, one month, over 1 month, two months...)
        - button to edit
        - button to delete
        - eventually:
            - a minimal line graph showing average progress over time (user can change timeframe in preferences)
            - color representing status of improving, maintaining, or declining.
        - all subject to change ^ see F6_lift-screen branch for updates.
8. Implement Minimal Line Graphs for Muscle Group Cards and Lift Cards. Determine if this makes the Analytics page obsolete.
    - user must be able to change timeframe in preferences
    - show color representing status of improving, maintaining, or declining.

# Basic Structure

3 main sections:

- View/edit/add all data in the database
- Data analysis tab (view day summaries, add more sub-tabs for further analysis)
- Workout logger

## Data Structure

All data is stored in lifting_data.db. Below is the structure of this data.

### Tables

#### ```lift_days```

The purpose of the ```lift_days``` table is to keep track of all the days that the user has logged.

The ```lift_days``` table has 5 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lift_sets``` table uses to associate set data with a specific day.
- ```profile_id``` (INTEGER): foreign key. This is what links the lift day to the appropriate profile.
- ```day_number``` (INTEGER): the number of the day; e.g. ```1```, ```2```, ```3```, etc.
- ```day_label``` (TEXT): the name of the day; e.g. ```"Day 1"```, ```"Day 2"```, ```"Day 3"```, etc. as default. The user may be able to change this name in future versions.
- ```date``` (TEXT): The date of the day that the session was recorded in ISO-8601 format: YYYY-MM-DD
- ```note``` (TEXT): a user-written note for the day, may be blank

#### ```lift_sets```

The purpose of the ```lift_sets``` table is to keep track of all sets that the user has completed. It also links each set with the specific lift that was trained, and the day that the user completed the set on.

The ```lift_sets``` table has 6 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```set_metrics``` table uses to associate specific set data (weight, reps) with a specific set that was completed for a specific lift on a specific day.
- ```lift_day_id``` (INTEGER): foreign key referring to ```lift_days```. This is what links this set to a particular day.
- ```lift_id``` (INTEGER): foreign key referring to ```lifts```. This is what links this set to a particular lift (e.g., bicep curls).
- ```lift_set_number``` (INTEGER): this set number identifies when this set took place, only in relation to the other sets completed for this specific lift on this specific day.
- ```day_set_number``` (INTEGER): this set number identifies when this set took place, in relation to all other sets completed on this specific day.
- ```set_label``` (TEXT): just a string containing the name of the set (e.g., ```"Set 1"```, ```"Set 2"```, ```"Set 3"```, etc. as default). The user may be able to change this label in future versions.
- ```set_note``` (TEXT): a user-written note for the set, may be blank

#### ```lifts```

The purpose of the ```lifts``` table is to store the names of all the different user-created lifts and link them to their corresponding muscle groups.

The ```lifts``` table has 4 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lift_sets``` table uses to associate lift names with set data.
- ```muscle_group_id``` (INTEGER): foreign key referring to the ```muscle_groups``` table. This is what links each lift to its corresponding muscle group.
- ```unit_id``` (INTEGER): foreign key referring to the ```units``` table. This is what links each lift to its corresponding user-written unit.
- ```name``` (TEXT): the user-specified name for the lift.
- ```metric_type``` (INTEGER): Int indicating if the lift will be measured in reps (1) or time (2). If the metric type is time, then the unit_id will be overridden.
- ```note``` (TEXT): a user-written note for the lift, may be blank

#### ```muscle_groups```

The purpose of the ```muscle_groups``` table is to store the user-generated names for all the muscle groups and link them to their corresponding profile.

The ```muscle_groups``` table has 4 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lifts``` table uses to associate lift names with muscle groups.
- ```profile_id``` (INTEGER): foreign key referring to the ```profiles``` table. This is what links each muscle group to its corresponding user (profile).
- ```name``` (TEXT): the user-specified name for the muscle group.
- ```note``` (TEXT): a user-written note for the muscle group, may be blank.

#### ```set_metrics```

The purpose of the ```set_metrics``` table is to store all of the user's set metrics (weight and rep data) and link them to their corresponding set (which is linked to the corresponding lift and the corresponding day via foreign keys).

The ```set_metrics``` table has 6 columns:

- ```id``` (INTEGER): primary key. This is the main identifier for each set metric.
- ```set_id``` (INTEGER): foreign key referring to the ```lift_sets``` table. This is what links each lift metric to its corresponding set.
- ```metric_position``` (INTEGER): this indicates whether the metric is a weight value or a rep value. ```1``` indicates weight, and ```2``` indicates reps.
- ```value``` (REAL): this indicates the number of reps performed, the weight value, or the time value for the specific set. Time values will be stored as doubles representing minutes, e.g. 1 minute and 30 seconds = 1.5 minutes
- ```note``` (TEXT): a user-written note for the metric, may be blank.

#### ```profiles```

The purpose of the ```profiles``` table is to store the names of all the profiles that the user has created. The profile is linked to its data via the ```muscle_groups``` table.

The ```profiles``` table has 3 columns:

- ```id``` (INTEGER): primary key. This is the main identifier used to distinguish between each profile.
- ```name``` (TEXT): the user-written name for the profile.
- ```active``` (INTEGER): indicates whether the current profile is active (1) or not active (0)
- ```note``` (TEXT): a user-written note for the profile, may be blank.

#### ```units```

The purpose of the ```units``` table is to store the names of all the user-written units, which are added to different lifts. The units table is designed to be independent of profiles, so multiple profiles can use the same unit.

The ```units``` table has two columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lifts``` table uses to associate lifts with their appropriate units.
- ```name``` (TEXT): the user-written name of the unit

> [!NOTE]
>
> May need some protection to ensure that a unit that is being used by a lift cannot be deleted.

---

# Pre-Dev Notes

## Things I'd like to see

- add an option in settings to turn the entire app into a task tracker (why not)
- In the workout logger:
    - Completed sets should show up as collapsable/expandable cards when completed.
    - All weight/rep/note data for the given session should always be able to be viewed at any point throughout the lift.
    - Sets from earlier days should be displayed as a different color.
    - **maybe** Stopwatch, potentially the ability to lap for sets/rests
- In general:
    - have the graphs on the lift screens, maybe not the analytics page.
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

# Prompts sent to Codex for development

## F5_3.1:

### Calculate stats for each muscle group

**The purpose of the prompt below was to calculate the muscle group statistics and store them in a list of objects that could be shown on the MuscleGroupsScreen:**

Now I want you to implement the necessary code (now you will modify the project) in order to calculate and populate the muscleGroupList, which you will have to change to a list of MuscleGroupDetail objects. You may need to fully study the way that the data is formatted and stored in the database to ensure that your code works and is accurate. Each of the MuscleGroupDetail objects must contain the following fields:

name (String) - the name of the muscle group.

note (String) - the note of the muscle group (keep as is, even if it is blank).

numLifts (Int) - the number of lifts that are stored in the lifts table with the given muscle_group_id; if the user has not added any lifts to the muscle group, this should just end up being zero.

numSessions (Int) - the number of days that the user has trained this muscle group; this must be calculated by looking at the lift_sets table where you can find the specific days that lifts were trained on, then looking at the lift that the lift_set is categorized under, then looking at the muscle group that that lift belongs to, and counting the number of days that have lift_sets that are categorized under lifts that belong to the given muscle group.

avgNumSessionsPerWeek (Double) - this averages the number of sessions that the user has recorded for each week; today, no matter the current day of the real calendar week, should be considered the seventh and last day of the current week; the current week should be included in this average, along with every week before it, with the last day of the second most recent week being 7 days ago (today - 7) and the first day of the second most recent week being 13 days ago (today - 13), and so on and so forth for the 3rd most recent week, 4th, 5th, 6th, etc. Keep in mind that the dates are stored in the database in ISO-8601 format.

avgNumSetsPerSession (Double) - You should average the number of sets in lift_sets that belong to any lift that is categorized under the given muscle group performed on each day_id. Keep in mind that a day_id represents one and only one session. This average will be the average number of sets performed for this muscle group for each day.

avgNumSetsPerWeek (Double) - You should average the number of sets that the user has performed for this muscle group for each week. Similar to the other attributes that involve weekly averages, today will always be considered the last day of the most recent week.

avgNumRepsPerSet (Double) - You should average the number of reps that the user has performed for each set that has been performed for each lift that belongs to this muscle group. Note that some lifts may have a metric_type of 2, which indicates that the lift is measured in time, not reps. Ignore these lifts that use time for this calculation. If the muscle group contains only lifts that are measured in time, assign the Double value of -1.0 to this attribute.

lastDateTrained (String) - This string should contain the value "Today" if the muscle group has any lift that was last performed today, "Yesterday" if it has a lift that was last performed yesterday (today - 1), "2 days ago" if it has a lift that was last performed the day before yesterday (today - 2), and so on and so forth until you reach "7 days ago", where instead of assigning "7 days ago", you shall assign "1 week ago" for this day, then "Over 1 week ago" for each day included in the range today - 8 to today - 13 inclusive, then "2 weeks ago" for today - 14, then "Over 2 weeks ago" for each day included in the range today - 15 to today - 20 inclusive, then "3 weeks ago" for today - 21, and so on and so forth until you reach what would normally be "4 weeks ago" from following this pattern, which will instead be "1 month ago". After this point, follow a similar pattern but for "Over 1 month ago", "2 months ago", "Over 2 months ago", "3 months ago", "Over 3 months ago" and so on and so forth until you reach "1 year ago", after which it will be "Over 1 year ago" which is the farthest back that shall be categorized. Everything before that point will just be considered "Over 1 year ago". If the user has not yet trained any lift belonging to this muscle group, the value should just be "N/A".

You must populate this List of MuscleGroupDetail objects in the init block of MuscleGroupsViewModel such that its values may change if the database changes during runtime. Be sure to comment your new code thoroughly so it is obvious what each line does, and add " - Codex" at the end of each of your comments to show that your code was written by you. You may need to add more query functions to the DAO, and you may need to adjust some things in the MuscleGroupsViewModel, but don't forget to ensure that your code works and makes logical sense. Do NOT edit the UI. This means DO NOT TOUCH any files in com.example.lifttracker.ui.screens. Try to keep your coding style similar to what is already present, and try not to do anything outlandish, but again, make sure that your code not only WORKS, but also would make logical sense to a developer.

**Output:**

Implemented the reactive muscle-group detail calculations without touching ui/screens.

Key changes:

- Added aggregate Room projection: [MuscleGroupDetailData.kt](C:/Github/LiftTracker/Workspace/app/src/main/java/com/example/lifttracker/data/MuscleGroupDetailData.kt)

- Added the multi-table aggregate query in [MuscleGroupDao.kt (line 35)](C:/Github/LiftTracker/Workspace/app/src/main/java/com/example/lifttracker/data/MuscleGroupDao.kt:35)

- Exposed it through the repository.

- Changed muscleGroupList to List<MuscleGroupDetail>.

- Added rolling-week averages, rep handling, and relative-date formatting in [MuscleGroupsViewModel.kt (line 44)](C:/Github/LiftTracker/Workspace/app/src/main/java/com/example/lifttracker/ui/viewModels/MuscleGroupsViewModel.kt:44).

- Room observes all six referenced tables, so the list recalculates when database data changes.

- Weekly averages begin with the muscle group’s earliest recorded session.

- Months follow the requested four-week/28-day definition.

Verification passed:
:app:compileDebugKotlin
:app:testDebugUnitTest
git diff --check

All newly added comments have the requested - Codex suffix.

Edited 4 files+241-36

- data/MuscleGroupDao.ktdata/MuscleGroupDao.kt+42-2
- data/MuscleGroupRepository.ktdata/MuscleGroupRepository.kt+5-2
- data/OfflineMuscleGroupRepository.ktdata/OfflineMuscleGroupRepository.kt+6-2
- ui/viewModels/MuscleGroupsViewModel.ktui/viewModels/MuscleGroupsViewModel.kt+188-30

### Follow-up prompt

**The purpose of this prompt was to include the muscle group id in each MuscleGroupDetail object in the muscleGroupList:**

looks good - now just modify it slightly so that each MuscleGroupDetailObject also contains an id attribute of type Int which stores the muscle_groups.id for the muscle group that the object represents.

**Output:**

Added id: Int to MuscleGroupDetail and its Room projection, populated from muscle_groups.id.

No UI screen files were changed. :app:compileDebugKotlin passes successfully.

Edited 3 files+4-0

data/MuscleGroupDao.ktdata/MuscleGroupDao.kt+1-0
data/MuscleGroupDetailData.ktdata/MuscleGroupDetailData.kt+1-0
ui/viewModels/MuscleGroupsViewModel.ktui/viewModels/MuscleGroupsViewModel.kt+2-0