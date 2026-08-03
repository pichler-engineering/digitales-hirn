package at.pichler.digitaleshirn.speech

import at.pichler.digitaleshirn.model.Priority
import at.pichler.digitaleshirn.vm.TaskInput
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

object GermanSpeechParser {
    data class ParseResult(
        val taskInput: TaskInput?,
        val shouldFallbackToInbox: Boolean
    )

    fun parse(text: String, now: LocalDate = LocalDate.now()): ParseResult {
        val normalized = text.lowercase().trim()
        val date = parseDate(normalized, now)
        val time = parseTime(normalized)
        val title = parseTitle(text)

        if (title.isBlank()) {
            return ParseResult(null, true)
        }

        return if (date != null && time != null) {
            ParseResult(
                TaskInput(
                    title = title,
                    description = text,
                    projectId = null,
                    priority = Priority.NORMAL,
                    dueDate = date,
                    dueTime = time,
                    reminderEnabled = true,
                    completed = false
                ),
                shouldFallbackToInbox = false
            )
        } else {
            ParseResult(null, true)
        }
    }

    private fun parseDate(text: String, now: LocalDate): LocalDate? {
        return when {
            "übermorgen" in text -> now.plusDays(2)
            "morgen" in text -> now.plusDays(1)
            "heute" in text -> now
            else -> parseWeekday(text, now)
        }
    }

    private fun parseWeekday(text: String, now: LocalDate): LocalDate? {
        val weekdays = mapOf(
            "montag" to DayOfWeek.MONDAY,
            "dienstag" to DayOfWeek.TUESDAY,
            "mittwoch" to DayOfWeek.WEDNESDAY,
            "donnerstag" to DayOfWeek.THURSDAY,
            "freitag" to DayOfWeek.FRIDAY,
            "samstag" to DayOfWeek.SATURDAY,
            "sonntag" to DayOfWeek.SUNDAY
        )
        val day = weekdays.entries.firstOrNull { it.key in text }?.value ?: return null
        var candidate = now
        repeat(7) {
            candidate = candidate.plusDays(1)
            if (candidate.dayOfWeek == day) return candidate
        }
        return null
    }

    private fun parseTime(text: String): LocalTime? {
        val match = Regex("um\\s+(\\d{1,2})(?::(\\d{1,2}))?\\s*uhr").find(text) ?: return null
        val hour = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0
        if (hour !in 0..23 || minute !in 0..59) return null
        return LocalTime.of(hour, minute)
    }

    private fun parseTitle(raw: String): String {
        return raw
            .replace(Regex("(?i)^erinnere mich"), "")
            .replace(Regex("(?i)heute|morgen|übermorgen|montag|dienstag|mittwoch|donnerstag|freitag|samstag|sonntag"), "")
            .replace(Regex("(?i)um\\s+\\d{1,2}(:\\d{1,2})?\\s*uhr"), "")
            .replace(",", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .removePrefix("daran")
            .removePrefix("mich")
            .trim()
    }
}
