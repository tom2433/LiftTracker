package github.tom2433.lifttracker.data.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.round

object DateTimeCalculator {
    const val ISO_DATE_PATTERN = "yyyy-MM-dd"
    const val MILLIS_PER_DAY = 86_400_000L
    const val DAYS_PER_WEEK = 7L
    const val DAYS_PER_MONTH = 28L
    const val DAYS_PER_YEAR = 12L * DAYS_PER_MONTH

    // This produces today's local calendar date in the same ISO-8601 format used by sessions.date. - Codex
    fun getCurrentIsoDate(): String {
        // Formatting in the device time zone ensures "Today" follows the user's local calendar day. - Codex
        val localFormatter = SimpleDateFormat(ISO_DATE_PATTERN, Locale.US)
        return localFormatter.format(Date())
    }

    // This creates the strict UTC formatter shared by the date-difference calculations. - Codex
    fun createIsoDateFormatter(): SimpleDateFormat {
        // UTC makes date-only arithmetic independent of the device's daylight-saving rules. - Codex
        return SimpleDateFormat(ISO_DATE_PATTERN, Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    // This parses one yyyy-MM-dd value into an epoch-day number without requiring API 26 java.time classes. - Codex
    fun parseIsoDateToEpochDay(date: String): Long? {
        // A fresh formatter is used because SimpleDateFormat is mutable and not thread-safe. - Codex
        val formatter = createIsoDateFormatter()

        // The parse position check rejects values that contain extra characters after a valid date prefix. - Codex
        val parsePosition = ParsePosition(0)
        val parsedDate = formatter.parse(date, parsePosition)

        // Strict parsing and full input consumption guarantee the database value is a valid ISO date. - Codex
        if (parsedDate == null || parsePosition.index != date.length) {
            return null
        }

        // UTC midnight milliseconds divide evenly into whole epoch days. - Codex
        return parsedDate.time / MILLIS_PER_DAY
    }

    // This returns the number of whole date boundaries between two strict ISO-8601 calendar dates. - Codex
    fun calculateDaysBetween(olderDate: String, newerDate: String): Long? {
        // Parse date-only values in UTC so daylight-saving transitions cannot create fractional days. - Codex
        val olderEpochDay = parseIsoDateToEpochDay(olderDate) ?: return null
        val newerEpochDay = parseIsoDateToEpochDay(newerDate) ?: return null

        // Subtracting epoch-day values produces an exact calendar-day difference. - Codex
        return newerEpochDay - olderEpochDay
    }

    // This counts rolling seven-day buckets whose final day is today rather than using calendar weeks. - Codex
    fun calculateNumWeeks(firstDateTrained: String?, today: String): Double {
        // An untrained muscle group still includes the current week, which keeps both weekly averages at zero. - Codex
        if (firstDateTrained == null) {
            return 1.0
        }

        // Invalid legacy dates fall back to the current week instead of crashing collection of the Room Flow. - Codex
        val daysSinceFirstSession = calculateDaysBetween(firstDateTrained, today) ?: return 1.0

        // Dividing by seven assigns today through today minus six to the current week, then adds that current week. - Codex
        return (daysSinceFirstSession.coerceAtLeast(0L) / DAYS_PER_WEEK + 1L).toDouble()
    }

    // This formats exact boundaries as "2 weeks ago" and in-between values as "Over 2 weeks ago". - Codex
    fun formatElapsedUnit(daysAgo: Long, daysPerUnit: Long, unitName: String): String {
        // Integer division gives the number of fully completed units in the elapsed period. - Codex
        val completedUnits = daysAgo / daysPerUnit

        // Add a plural suffix for every count other than one. - Codex
        val displayUnit = if (completedUnits == 1L) unitName else "${unitName}s"

        // A remainder means the date is beyond the exact unit boundary but has not reached the next one. - Codex
        return if (daysAgo % daysPerUnit == 0L) {
            "$completedUnits $displayUnit ago"
        } else {
            "Over $completedUnits $displayUnit ago"
        }
    }

    // This translates the latest ISO date into the requested relative training-date category. - Codex
    fun formatLastDateTrained(lastDateTrained: String?, today: String): String {
        // A null date means no set belonging to this muscle group has ever been recorded. - Codex
        if (lastDateTrained == null) {
            return "N/A"
        }

        // A malformed legacy date cannot be categorized reliably, so it is treated as unavailable. - Codex
        val calculatedDaysAgo = calculateDaysBetween(lastDateTrained, today) ?: return "N/A"

        // Future dates are clamped to today because the requested categories only describe elapsed time. - Codex
        val daysAgo = calculatedDaysAgo.coerceAtLeast(0L)

        // Categorize recent dates by day, then exact/partial weeks, then exact/partial four-week months. - Codex
        return when {
            daysAgo == 0L -> "Today"
            daysAgo == 1L -> "Yesterday"
            daysAgo < DAYS_PER_WEEK -> "$daysAgo days ago"
            daysAgo < DAYS_PER_MONTH -> formatElapsedUnit(daysAgo,
                DAYS_PER_WEEK, "week")
            daysAgo < DAYS_PER_YEAR -> formatElapsedUnit(daysAgo,
                DAYS_PER_MONTH, "month")
            daysAgo == DAYS_PER_YEAR -> "1 year ago"
            else -> "Over 1 year ago"
        }
    }

    // precondition: minutes must be >= 0
    fun convertDoubleTimeToTripleTime(minutes: Double): Triple<Int, Int, Double> {
        val resultHours: Int = minutes.toInt() / 60
        val resultMinutes: Int = minutes.toInt() % 60
        val resultSeconds: Double = (minutes - minutes.toInt()) * 60

        return Triple(resultHours, resultMinutes, round(resultSeconds * 100) / 100)
    }

    fun convertTripleTimeToDoubleTime(hours: Int, minutes: Int, seconds: Double): Double {
        // calculate minutes as double
        return (hours * 60.0) + minutes + (seconds / 60.0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertIsoDateToReadableFormat(isoDate: String): String {
        return LocalDate.parse(isoDate).format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))
    }

    // This calculates an inclusive rolling-window start date using DateCalculator's strict UTC epoch-day parsing. - Codex
    fun calculateStartDate(today: String, daysBeforeToday: Long): String {
        // Today's value is generated internally and is therefore valid; this fallback keeps initialization safe if that contract changes. - Codex
        val todayEpochDay = parseIsoDateToEpochDay(today) ?: return today

        // Convert the shifted UTC epoch day back to the ISO format stored by sessions.date. - Codex
        return createIsoDateFormatter().format(
            Date((todayEpochDay - daysBeforeToday) * MILLIS_PER_DAY)
        )
    }

    fun calculateEndDate(today: String, daysAfterToday: Long): String {
        val todayEpochDay = parseIsoDateToEpochDay(today) ?: return today

        return createIsoDateFormatter().format(
            Date((todayEpochDay + daysAfterToday) * MILLIS_PER_DAY)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeekStringFromIsoDate(isoDate: String): String {
        val subtractToBeginning: Long = when (convertIsoDateToReadableFormat(isoDate).substring(0, 3)) {
            "Mon" -> 0L
            "Tue" -> 1L
            "Wed" -> 2L
            "Thu" -> 3L
            "Fri" -> 4L
            "Sat" -> 5L
            "Sun" -> 6L
            else -> 0L
        }
        val addToEnd: Long = when (convertIsoDateToReadableFormat(isoDate).substring(0, 3)) {
            "Sun" -> 0L
            "Sat" -> 1L
            "Fri" -> 2L
            "Thu" -> 3L
            "Wed" -> 4L
            "Tue" -> 5L
            "Mon" -> 6L
            else -> 0L
        }
        val beginString = convertIsoDateToReadableFormat(
            isoDate = calculateStartDate(
                today = isoDate,
                daysBeforeToday = subtractToBeginning
            )
        )
        val endString = convertIsoDateToReadableFormat(
            isoDate = calculateEndDate(
                today = isoDate,
                daysAfterToday = addToEnd
            )
        )

        return "$beginString - $endString"
    }

    fun getStartAndEndDatesFromNullable(
        startDate: String?,
        endDate: String?
    ): Pair<String, String> {
        return Pair(
            first = startDate ?: "2025-07-03",
            second = endDate ?: getCurrentIsoDate()
        )
    }
}