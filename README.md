# LiftTracker
A prototype of the Lift Tracker project. This is an evolving document. Everything in here is subject to change.

---

# Basic Structure

3 main sections:

- View/edit/add all data in the database
- Data analysis tab (view day summaries, add more sub-tabs for further analysis)
- Workout logger

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
- Backend:
    - Add start time and end time to each Day
    - Add names to each day (user-specified)
    - Export lift data to excel