package com.pichler.digitaleshirn.classification

import java.util.Calendar
import java.util.Locale

data class ParseResult(
    val date: Long?,
    val time: Long?,
    val needsDateCheck: Boolean
)

class GermanDateTimeParser {
    private val weekdayMap = mapOf(
        "montag" to Calendar.MONDAY,
        "dienstag" to Calendar.TUESDAY,
        "mittwoch" to Calendar.WEDNESDAY,
        "donnerstag" to Calendar.THURSDAY,
        "freitag" to Calendar.FRIDAY,
        "samstag" to Calendar.SATURDAY,
        "sonntag" to Calendar.SUNDAY
    )

    private val timeRegex = Regex(
        pattern = "(?:\\bum\\s*)?([01]?\\d|2[0-3])(?::([0-5]\\d))?\\s*uhr\\b",
        option = RegexOption.IGNORE_CASE
    )

    private val vagueExpressions = listOf("nächste woche", "naechste woche", "bald", "demnächst", "spaeter", "später")

    fun parse(text: String): ParseResult {
        val normalized = text.lowercase(Locale.GERMAN)
        var needsDateCheck = vagueExpressions.any { normalized.contains(it) }
        val date = when {
            "übermorgen" in normalized -> dayOffset(2)
            "heute" in normalized -> dayOffset(0)
            Regex("\\bmorgen\\b").containsMatchIn(normalized) -> dayOffset(1)
            normalized.contains("nächste woche") || normalized.contains("naechste woche") -> nextWeekMonday()
            else -> parseWeekday(normalized)
        }

        if (date == null && (normalized.contains("später") || normalized.contains("spaeter") || normalized.contains("bald"))) {
            needsDateCheck = true
        }

        return ParseResult(
            date = date,
            time = parseTime(normalized),
            needsDateCheck = needsDateCheck
        )
    }

    private fun parseWeekday(text: String): Long? {
        val match = Regex("\\b(montag|dienstag|mittwoch|donnerstag|freitag|samstag|sonntag)\\b")
            .find(text)
            ?: return null
        val targetDay = weekdayMap[match.value] ?: return null
        val calendar = Calendar.getInstance().apply { clearTime() }
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        var daysUntil = (targetDay - today + 7) % 7
        if (daysUntil == 0) {
            daysUntil = 7
        }
        calendar.add(Calendar.DAY_OF_YEAR, daysUntil)
        return calendar.timeInMillis
    }

    private fun parseTime(text: String): Long? {
        val match = timeRegex.find(text) ?: return null
        val hour = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }?.toIntOrNull() ?: 0
        return ((hour * 60L) + minute) * 60_000L
    }

    private fun dayOffset(days: Int): Long {
        return Calendar.getInstance().apply {
            clearTime()
            add(Calendar.DAY_OF_YEAR, days)
        }.timeInMillis
    }

    private fun nextWeekMonday(): Long {
        return Calendar.getInstance().apply {
            clearTime()
            add(Calendar.WEEK_OF_YEAR, 1)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }.timeInMillis
    }

    private fun Calendar.clearTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
