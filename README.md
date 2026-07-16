# LiftTracker

A Data Vis project developed using Jetpack Compose.

---

# Immediate Notes

## 07-17

ran into an issue where when the user deletes more than one lift in progress or lift sets in progress, the numbering gets messed up. need to implement a system where the delete icons are not visible while something is being deleted.

also need a unique index on the new muscle group day set number.

make all lift set numbers more clear, make each of them their own row and put the numbers as their own font size/weight, place in a visual container of some sort.

and put the number before the label

use a label(note) to categorize groups of lifts

## 07-16

display the active lift day's date on the session in progress screen.

## 07-15

lift set notes don't wrap appropriately, they just take up the whole card.

lift set metric inputs go away when the viewmodel is recreated.

add a column to the lift sets table called muscle_group_day_set_number to indicate that muscle group's set number for the day.

add number of sets on each lift in progress card

## 07-14

add a show all button to existing lift entry card to show all lifts

## 07-13

Consider putting the analytics button toggle-able on the lift screen, so the user sees previous sessions OR the analytics 

## 07-11

days with the same day_label should display the day number in the note. when creating a lift, the user can click the day label and note to edit it, and when the name input pops up, some quick add buttons should also pop up. these should be able to be grouped in the analytics screen.

start also implementing the preferences table

## 07-10

- maybe keep the dislog titles fixed in place and have the scrollable column only contain the body of the Dialog. maybe create a function for a dialog and content.

- maybe include some muscle group data on the lift screen as well that the user can expand/collapse. maybe this could also be implemented into the muscle group card.

- viewmodels are what store ui state. To have data persist over multiple screens, those screens need to share the same view model, or they write data to the database. which one is more efficient?

- room database should store result values into a data class object which is then parsed thru on the front end. most if not all of the calculation done on the backend, minimal on the frontend.

- shorten the content visibility animation on the lift screen (or simplify it)
- also change the muscle group cards so the raw data doesn't load in until after the user clicks it.
- move the scrollable columns on the dialogs so the user can see the buttons the entire time (completed)
- a user may not want to see a crap ton of data thrown at their face when they open the app
    - change the muscle group screen and the lift screen. their data will eventually be represented by line graphs and pie charts.
    - raw data is displeasing to look at.
    - user wants to have a good idea of how their lifts are progressing but does not want to go thru the trouble of looking at numbers.

- raw data on the muscle group cards should not load in until after the user has clicked it. ^

## 07-09

- edit the profile note color on the switch profile nav drawer dropdown element to improve readability issues due to contrast.

## 07-08

- modify the info dialogs for the add/edit lift to be more descriptive
- ask codex how to make everything less laggy
- try to make lift screen take up the entire screen
- bring back entry animation for muscle group cards

## 07-07

- increase size of lift note
- eventually add a preferences table to store user preferences like setting a default unit.
- gradually add these changes to the settings screen
- eventually include a pie graph to show breakup of muscle groups.
    - user can filter for a specific day, the last week, a week interval, a specific time period chosen by a calendar
    - bar graph with each of the past weeks (rolling) and a bar graph of each of the past weeks (starting Mondays), and scroll through all of the previous weeks.
    - user should be able to customize this bar graph along the x axis with specific time frames, and then the y axis with muscle group data, averaged muscle group data, a specific lift, multiple specific lifts in groups.
        - should be able to create a function with all of these options just as parameters.
    - bar graph with editable x and y axis, pie graph with customizable categories, option to save the graph and store a name for it.
        - maybe eventually export to excel or even copy and put into your session message every time you finish a lift.
- will need to work on storing data as quickly as possible and is first priority. the working with data will come mostly after it. more personal testing will be needed with the app in order to more strongly and quickly continue development.

- also get data from Textlifttracker and convert it.

## 07-06

- the descriptions in the lift entry dialog should be hidden behind an info button
    - the descriptions take too long to read, also previous units should be shown in a grid format
    - entry screens should be read quickly since the user will be adding and editing the lifts often.

- read the codex suggestions on how to use a key for lifts view model so it is able to be copied and recomposed.

- look at viewmodelstore and viewmodelstoreowner


# Development

## Features:

- [x] F1: Backend database design
    - Add a set metric for time, in addition to weight and reps
    - Ensure that the viewModels have access to the database
    - Check if a user profile exists. If none do, then prompt the user to create one.
- [x] F2: Menu drawer with basic elements:
    - Begin Session
    - Muscle Groups
    - Sessions
    - Calendar
    - Switch Profile
    - Divider ---
    - Analytics
    - Tools
    - Settings
- [x] F2-1: Map menu drawer items with screens where applicable
- [ ] F2-3: (Maybe) Animate hamburger menu so that it travels and rotates into an arrow
- [ ] F3: Colorscheme:
    - Default to preset colorscheme
    - Toggle dynamic color
    - Toggle Dark theme
- [x] F4: Implement Switch Profile functionality
- [x] F5: Allow user to add, edit, and delete muscle groups in the muscle groups screen.
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
- [x] F6: When a muscle group card is clicked, it animates a dropdown container with cards representing all lifts for that muscle group
    - lifts should be shown in a flow row so that the user can easily reach the bottom add button.
    - Move the edit/delete buttons for the muscle group card to a 3-dot menu at the top right of the card itself, and replace the edit/delete button section with one big add lift button. Make this add lift button thinner too.
    - Clicking on a lift card will transition itself into a LiftScreen with all lift statistics. The three dot menu from the lift card will transform into a three dot menu on the lift screen which will allow the user to edit the lift and move it to a different muscle group.
    - Lift Screen will include:
        - Lift name and note
        - Lift's muscle group, metric type (reps/time), unit name (user-defined)
        - last date trained (today, yesterday, 2 days ago, etc.)
        - Filter chips for preset time frames of Past Month, Past Year, and Lifetime including the following data:
            - total # of sets performed
            - avg. # of sets per session
            - % of overall set volume
            - % of set volume for the muscle group
            - Avg. weight
            - Avg. # of reps per set \~or\~ Avg. time per set
- [ ] F7: Implement Minimal Line Graphs for Muscle Group Cards and Lift Cards.
    - user must be able to change timeframe in preferences
    - show color representing status of improving, maintaining, or declining.
- [ ] F6-1: Implement a [date range picker](https://developer.android.com/develop/ui/compose/components/datepickers#range) so the user can specify a custom range to view the lift statistics
- [ ] F6-2: Implement the most recent lift sets at the bottom of LiftScreen
- [ ] F6-3: Replace the "% of overall set volume" and "% of set volume for {muscleGroup}" with pie charts
- [ ] F8: (in progress) Implement the Record Session screen
    - Clicking the Begin session nav drawer element will bring the user to the Record Session screen, which contains one large central button containing the Play Arrow icon.
    - Clicking this play arrow icon will create a new lift_day in the database, with the in_progress column set to true
        - if a session (lift_day) is in progress, the Begin Session nav drawer element will change its text to "Resume Session" and will change color to tertiary container.
    - Clicking the play arrow will also transition the button into the Session In Progress Screen, which contains a header row at the top with a fixed position.
        - this header row contains a pencil icon on the left with the lift day's name and note directly beside it to the right.
        - All the way on the right is the Finish button, which will delete the lift_day if the user has not recorded any lifts for it, or it will change the in_progress column of the current lift_day to false (current functionality).
    - Clicking the header row section with the pencil icon and lift_day name and note will popup an element entry dialog allowing the user to edit the name and note of the current session.
        - **Need to implement:** days with the same day_label should display the day number in the note. when creating a lift, the user can click the day label and note to edit it, and when the name input pops up, some quick add buttons should also pop up. these should be able to be grouped in the analytics screen.
    - **Need to implement**
        - in the session in progress screen, A Horizontal divider with an add button in the middle will be present. When clicking the add button, a card (representing a lift) will popup with a name input.
        - As the user begins typing in the name input, suggestions will popup next to the input that the user can select.
        - These suggestions will be determined by finding lifts that begin with or contain the string that the user has typed thus far.
        - The suggestions should contain lift names, their notes, and their respective muscle group.
        - Suggestions will include lifts from all muscle groups, but the user should be able to restrict this through some form of filter menu.
        - The most likely match will be closest to the user's input field and highlighted, and when the user clicks enter, this match will be entered into the text field which will change into a simple label with a pencil icon next to it to edit.
        - The label will include the lift name and its note. The muscle group should also be displayed on this card.
        - After clicking enter to select the lift, The UI should automatically update to create the first set of the lift.
        - The set should also be a card, and the lift card should take on a similar format to the muscle group cards on the muscle group screen. It should also be collapsable like the muscle group cards. The set cards will be in place of where the lift cards would normally be, but it should flow a little better and will obviously contain more information.

### Misc. things to do:

- [x] edit the profile note color on the switch profile nav drawer dropdown element to improve readability issues due to contrast.
- [x] change the name of the ```units``` table to ```lift_units``` to avoid confusion with Kotlin's ```Unit``` type.
- [x] move the scrollable columns on the dialogs so the user can see the buttons the entire time
- [ ] add a setting for managing units
- [ ] keep the dialog titles fixed in place just like the confirm/cancel buttons
- [ ] raw data on the muscle group cards should not load until after the user has clicked it.

# Data Structure

All data is stored in lifting_data.db. Below is the structure of this data.

## Tables

### ```lift_days```

The purpose of the ```lift_days``` table is to keep track of all the days that the user has logged. A single lift_day is considered to be one single session for this current version, although the user may be able to record multiple "lift_days" on one single calendar day.

The ```lift_days``` table has 5 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lift_sets``` table uses to associate set data with a specific day.
- ```profile_id``` (INTEGER): foreign key. This is what links the lift day to the appropriate profile.
- ```day_number``` (INTEGER): the number of the day; e.g. ```1```, ```2```, ```3```, etc.
- ```day_label``` (TEXT): the name of the day; e.g. ```"Day 1"```, ```"Day 2"```, ```"Day 3"```, etc. as default. The user may be able to change this name in future versions.
- ```date``` (TEXT): The date of the day that the session was recorded in ISO-8601 format: YYYY-MM-DD
- ```note``` (TEXT): a user-written note for the day, may be blank

### ```lift_sets```

The purpose of the ```lift_sets``` table is to keep track of all sets that the user has completed. It also links each set with the specific lift that was trained, and the day that the user completed the set on.

The ```lift_sets``` table has 6 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```set_metrics``` table uses to associate specific set data (weight, reps) with a specific set that was completed for a specific lift on a specific day.
- ```lift_day_id``` (INTEGER): foreign key referring to ```lift_days```. This is what links this set to a particular day.
- ```lift_id``` (INTEGER): foreign key referring to ```lifts```. This is what links this set to a particular lift (e.g., bicep curls).
- ```lift_set_number``` (INTEGER): this set number identifies when this set took place, only in relation to the other sets completed for this specific lift on this specific day.
- ```day_set_number``` (INTEGER): this set number identifies when this set took place, in relation to all other sets completed on this specific day.
- ```set_label``` (TEXT): just a string containing the name of the set (e.g., ```"Set 1"```, ```"Set 2"```, ```"Set 3"```, etc. as default). The user may be able to change this label in future versions.
- ```set_note``` (TEXT): a user-written note for the set, may be blank

### ```lifts```

The purpose of the ```lifts``` table is to store the names of all the different user-created lifts and link them to their corresponding muscle groups.

The ```lifts``` table has 4 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lift_sets``` table uses to associate lift names with set data.
- ```muscle_group_id``` (INTEGER): foreign key referring to the ```muscle_groups``` table. This is what links each lift to its corresponding muscle group.
- ```unit_id``` (INTEGER): foreign key referring to the ```units``` table. This is what links each lift to its corresponding user-written unit.
- ```name``` (TEXT): the user-specified name for the lift.
- ```metric_type``` (INTEGER): Int indicating if the lift will be measured in reps (1) or time (2). If the metric type is time, then the unit_id will be overridden.
- ```note``` (TEXT): a user-written note for the lift, may be blank

### ```muscle_groups```

The purpose of the ```muscle_groups``` table is to store the user-generated names for all the muscle groups and link them to their corresponding profile.

The ```muscle_groups``` table has 4 columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lifts``` table uses to associate lift names with muscle groups.
- ```profile_id``` (INTEGER): foreign key referring to the ```profiles``` table. This is what links each muscle group to its corresponding user (profile).
- ```name``` (TEXT): the user-specified name for the muscle group.
- ```note``` (TEXT): a user-written note for the muscle group, may be blank.

### ```set_metrics```

The purpose of the ```set_metrics``` table is to store all of the user's set metrics (weight and rep data) and link them to their corresponding set (which is linked to the corresponding lift and the corresponding day via foreign keys).

The ```set_metrics``` table has 6 columns:

- ```id``` (INTEGER): primary key. This is the main identifier for each set metric.
- ```set_id``` (INTEGER): foreign key referring to the ```lift_sets``` table. This is what links each lift metric to its corresponding set.
- ```metric_position``` (INTEGER): this indicates whether the metric is a weight value or a rep value. ```1``` indicates weight, and ```2``` indicates reps.
- ```value``` (REAL): this indicates the number of reps performed, the weight value, or the time value for the specific set. Time values will be stored as doubles representing minutes, e.g. 1 minute and 30 seconds = 1.5 minutes
- ```note``` (TEXT): a user-written note for the metric, may be blank.

### ```profiles```

The purpose of the ```profiles``` table is to store the names of all the profiles that the user has created. The profile is linked to its data via the ```muscle_groups``` table.

The ```profiles``` table has 3 columns:

- ```id``` (INTEGER): primary key. This is the main identifier used to distinguish between each profile.
- ```name``` (TEXT): the user-written name for the profile.
- ```active``` (INTEGER): indicates whether the current profile is active (1) or not active (0)
- ```note``` (TEXT): a user-written note for the profile, may be blank.

### ```lift_units```

The purpose of the ```lift_units``` table is to store the names of all the user-written units, which are added to different lifts. The units table is designed to be independent of profiles, so multiple profiles can use the same unit.

The ```lift_units``` table has two columns:

- ```id``` (INTEGER): primary key. This is the main identifier that the ```lifts``` table uses to associate lifts with their appropriate units.
- ```name``` (TEXT): the user-written name of the unit

> [!NOTE]
>
> May need some protection to ensure that a lift unit that is being used by a lift cannot be deleted.

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

## F6_4.2:

### Calculate stats for each lift

Version: 5.5 High Reasoning

**The purpose of the prompt below was to calculate the lift statistics and store them in maps (label points to the value) to display to the user.**

For this operation, you are to populate the following values of the LiftScreenUiState inside the init {} block of the LiftScreenViewModel:

- lastDateTrained
- pastMonthStatMap
- pastYearStatMap
- lifetimeStatMap

You must populate these values inside of the init {} block of the LiftScreenViewModel, although you may create helper functions inside LiftScreenViewModel if needed for organization, and you may need to write functions in LiftRepository and queries in LiftDao. You must also consider the functions located in github.tom2433.lifttracker.data.utils to help you.

Below, I will specify the exact format you must use in populating this data.

lastDateTrained will hold a string informing the user of the last day that this lift was trained. To figure this out, you will need to be familiar with the structure of the database and look at the lift_sets with the appropriate lift_id, and find the one with the lift_day that has the most recent date. You must use this date to determine if lastDateTrained should be Today, Yesterday, 2 days ago, 3 days ago, etc. You must use the function formatLastDateTrained() from DateCalculator to determine the value of this string.

pastMonthStatMap, pastYearStatMap, and lifetimeStatMap will all contain the same format of data, but they will be for different periods of time. pastMonthStatMap will contain data applying to the last 4 weeks, with today being the last day (7th day) of the 4th week. pastYearStatMap will contain data applying to the last 365 days, with today being the last day (365th day) of the year. Each of these statMaps will have the following keys exactly:

- "Total # of sets performed"
- "Avg. # of sets per session"
- "% of overall set volume"
- "% of set volume for {muscleGroup}" ({muscleGroup} should be replaced with the muscle group that this lift belongs to)
- "Avg. weight"
- "Avg. # of reps per set" \~or\~ "Avg. time per set" if the lift has a metric_type of 2

The "Total # of sets performed" key will have a value in the format of "{total number of sets} sets" with {total number of sets} replaced with the total number of lift_sets that the user recorded for this lift during the appropriate time period. If the user has not recorded any sets for this lift within the appropriate time period, the value will just be "0 sets".

The "Avg. # of sets per session" key will have a value in the format of "{number of sets} sets/session" with {number of sets} replaced with the average number of sets that the user has recorded per session completed during the appropriate time period rounded to exactly two decimal places. This average will only take into account the sessions in which the user has actually trained this lift. If the user has not recorded any sets for this lift within the appropriate time period, the value will just be "0.00 sets/session".

The "% of overall set volume" key will have a value in the format of "{percentage} %" with {percentage} replaced with the result of the following equation rounded to exactly two decimal places: ((number of lift_sets completed for this lift during the appropriate time period) / (number of lift_sets completed for all lifts only belonging to muscle groups owned by the currently active profile during the appropriate time period)) * 100. If the user has not recorded any sets for this lift during the appropriate time period, the value will just be "0.00 %".

The "% of set volume for {muscleGroup}" key where {muscleGroup} is replaced with the name of the muscle group that this lift belongs to will have a value in the format of "{percentage} %" with {percentage} replaced with the result of the following equation rounded to exactly two decimal places: ((number of lift_sets completed for this lift during the appropriate time period) / (number of lift_sets completed for all lifts belonging to the same muscle group as the lift being represented by LiftScreen)) * 100. If the user has not recorded any sets for this lift during the appropriate time period, the value will just be "0.00 %".

The "Avg. weight" key will have a value in the format of "{weight value} {units}" where {weight value} is replaced with the average set_metric value of all set_metrics with metric_position equal to 1 belonging to lift_sets that were both completed during the appropriate time period and also belong to the lift represented by LiftScreen, rounded to exactly two decimal places. {units} will be replaced with the name of the unit pointed to by the unit_id of the lift being represented by LiftScreen. If the user has not recorded any sets for this lift during the appropriate time period, the value will just be "0.00 {units}", again where {units} is replaced with the name of the unit pointed to by the unit_id of the lift being represented by LiftScreen.

The last key that will be added to each stat map will be one of two keys: "Avg. # of reps per set" if the lift being represented by LiftScreen has a metric_type of 1, or "Avg. time per set" otherwise. Similarly, the value will also be one of two values: If the key is "Avg. # of reps per set", the value will be "{number of reps} reps" where {number of reps} is replaced with the average value of every set_metric with metric_position equal to 2 belonging to lift_sets that were both completed during the appropriate time period and also belong to the lift represented by LiftScreen, rounded to exactly two decimal places. If the user has not recorded any sets for this lift during the appropriate time period, the value will just be "0.00 reps" for this scenario. If the key is "Avg. time per set", the value will be "{time}" where {time} is replaced with the average value of every set_metric with metric_position equal to 2 belonging to lift_sets that were both completed during the appropriate time period and also belong to the lift being represented by LiftScreen. {time} should be of the format "HH:MM:SS". Keep in mind that the set_metric values being stored in the database where the set_metric's metric_position is 2 and the set_metric's set_id points to a lift_set which has a lift_id that points to a lift with a metric_type of 2 are Doubles, and the format in which they are saved in the database is as follows: A set_metric value of 1.0 in this scenario represents one minute or "00:01:00", 1.5 represents one minute and 30 seconds or "00:01:30", 1.01 would be "00:01:01" (round where applicable), 1.15 would be "00:01:09", and so on and so forth. You will need to make the appropriate conversions in order to achieve this when converting the value to a string.

For all of the code that you write, you must write comments in the code so that it is clear what each line is doing (the more detail, the better), and be sure to follow your comments with " - Codex" to indicate that the comment (and the code it applies to) was written by you. Try to use existing functions, but write your own where applicable. Try to preserve the existing coding style, but try to make everything efficient and logical so that it would make sense to a developer reading it. Make your code modular so that if a later feature were to be added to create another stat map with a custom timeframe, it would be easy to implement. When you are finished writing the necessary code to achieve this, make sure that it works as intended. Ask questions if you need further clarification.

**Output:**

Implemented the LiftScreen statistics population in init.
Key changes:
- Added reactive Room aggregation in [LiftDao.kt (line 31)](C:/Github/LiftTracker/Workspace/app/src/main/java/github/tom2433/lifttracker/data/LiftDao.kt:31).
- Added reusable projection model [LiftStatisticsData.kt (line 4)](C:/Github/LiftTracker/Workspace/app/src/main/java/github/tom2433/lifttracker/data/LiftStatisticsData.kt:4).
- Wired repository access.
- Populated all four UI-state values in [LiftScreenViewModel.kt (line 37)](C:/Github/LiftTracker/Workspace/app/src/main/java/github/tom2433/lifttracker/ui/viewModels/LiftScreenViewModel.kt:37).
- Added exact decimal, percentage, session-average, and HH:MM:SS formatting.
- Used inclusive 28-day and 365-day windows.
- Added detailed - Codex comments.
- Verification: gradlew.bat testDebugUnitTest passed, including Room query compilation. Existing unrelated LiftSet.kt and SetMetric.kt changes were untouched.

Edited LiftScreenViewModel.kt+153-10

## Table name change

Version: 5.5 High Reasoning

**The purpose of this prompt was to change the name of the units table to lift_units to avoid confusion with Kotlin's ```Unit``` type.**

I've changed the name of the units table to lift_units, and the name of its data class from Unit to LiftUnit. Look through the entire project and use your best judgement to determine where to change unit to liftUnit or Unit to LiftUnit. Also consider the queries that call upon this table, and search through comments. Keep in mind that kotlin has a type called Unit, so wherever an Kotlin Unit is used, ignore it, but wherever you can find the old com.example.lifttracker.Unit, change it to LiftUnit. Refactor everything to accommodate this table and data class name change. Ask if you need clarification on anything.

**Output:**

Done. I refactored the old Unit data-layer naming to LiftUnit naming and updated Room to use lift_units.
Changed:

- UnitDao -> LiftUnitDao
- UnitRepository -> LiftUnitRepository
- OfflineUnitRepository -> OfflineLiftUnitRepository
- DI/view-model references from unitRepository to liftUnitRepository
- Room queries from units to lift_units
- LiftDao statistics join to lift_units
- comments/docs in [README.md](C:/Github/LiftTracker/README.md) and data files
- added a Room 1 -> 2 migration in [LiftTrackerDatabase.kt](C:/Github/LiftTracker/Workspace/app/src/main/java/github/tom2433/lifttracker/data/LiftTrackerDatabase.kt) to rename existing units tables to lift_units
- I intentionally left Kotlin’s built-in Unit usages alone, including callback return types and LaunchedEffect(Unit).

Verification: .\gradlew.bat test passes. Only existing warning is Room’s profile_id foreign-key-index warning, unrelated to this refactor.

Edited 15 files+268-256

- README.md+15-15
- data/AppContainer.ktdata/AppContainer.kt+8-8
- data/Lift.ktdata/Lift.kt+4-4
- data/LiftDao.ktdata/LiftDao.kt+3-3
- data/LiftTrackerDatabase.ktdata/LiftTrackerDatabase.kt+37-26
- data/LiftUnit.ktdata/LiftUnit.kt+11-11
- data/LiftUnitDao.ktdata/LiftUnitDao.kt+17-17
- data/LiftUnitRepository.ktdata/LiftUnitRepository.kt+8-8
- data/OfflineLiftUnitRepository.ktdata/OfflineLiftUnitRepository.kt+14-13
- ui/AppViewModelProvider.ktui/AppViewModelProvider.kt+3-3
- ui/screens/LiftScreen.ktui/screens/LiftScreen.kt+6-6
- ui/screens/LiftsSection.ktui/screens/LiftsSection.kt+17-17
- ui/utils/Dialogs.ktui/utils/Dialogs.kt+25-25
- ui/viewModels/LiftScreenViewModel.ktui/viewModels/LiftScreenViewModel.kt+44-44
- ui/viewModels/LiftsViewModel.ktui/viewModels/LiftsViewModel.kt+56-56
