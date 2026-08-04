package com.pichler.digitaleshirn.classification

import com.pichler.digitaleshirn.data.Category
import com.pichler.digitaleshirn.data.CategorySettingsRepository
import java.util.Locale

class RuleBasedClassificationService(
    private val dateTimeParser: GermanDateTimeParser = GermanDateTimeParser(),
    private val categorySettings: CategorySettingsRepository? = null
) : ClassificationService {

    private val reminderTriggers = listOf("erinnere mich", "erinnerung", "reminder")
    private val ideaTriggers = listOf(
        "idee:",
        "idee ",
        "man könnte",
        "man koennte",
        "wir sollten",
        "vielleicht könnte",
        "vielleicht koennte",
        "wie wäre",
        "wie waere",
        "was wäre wenn",
        "was waere wenn"
    )
    private val taskKeywords = listOf(
        "anrufen",
        "kaufen",
        "bestellen",
        "prüfen",
        "pruefen",
        "machen",
        "erledigen",
        "senden",
        "schicken",
        "liefern",
        "reparieren",
        "ich muss",
        "nicht vergessen",
        "kündigen",
        "kuendigen",
        "termin",
        "meeting",
        "besprechung"
    )
    private val dateTimeTriggers = listOf(
        "heute",
        "morgen",
        "übermorgen",
        "uebermorgen",
        "nächste woche",
        "naechste woche",
        "montag",
        "dienstag",
        "mittwoch",
        "donnerstag",
        "freitag",
        "samstag",
        "sonntag"
    )
    private val stopWords = setOf(
        "aber", "alle", "auch", "bald", "beim", "bereits", "dabei", "dann", "dass", "deine",
        "deinen", "deiner", "dem", "den", "der", "des", "dich", "die", "dies", "diese",
        "dieser", "doch", "dort", "eine", "einer", "eines", "einfach", "erledigen", "etwas",
        "euch", "fuer", "für", "gestern", "heute", "hier", "hinten", "ich", "idee", "ihnen",
        "ihrer", "immer", "jetzt", "kann", "keine", "mehr", "mein", "meine", "meiner",
        "morgen", "nicht", "noch", "notiz", "oder", "schon", "sein", "seine", "sich", "soll",
        "sollen", "später", "spaeter", "termine", "über", "ueber", "um", "und", "uns", "unter",
        "vielleicht", "wäre", "waere", "weil", "wenn", "werden", "wieder", "wir", "wird", "wochen"
    )

    override suspend fun classify(text: String): ClassificationResult {
        val cleanedText = text.trim()
        val normalized = cleanedText.lowercase(Locale.GERMAN)

        // 1. Try first-word detection (highest priority)
        val firstWordResult = detectFirstWordCategory(cleanedText, normalized)
        if (firstWordResult != null) {
            val (category, strippedText) = firstWordResult
            val parseResult = dateTimeParser.parse(strippedText)
            val title = extractTitle(strippedText)
            val keywords = extractKeywords(strippedText)
            val reminderEnabled = category == Category.REMINDER ||
                (parseResult.date != null && parseResult.time != null)
            val dueTime = parseResult.time ?: defaultDueTime(parseResult.date, reminderEnabled)
            return ClassificationResult(
                category = category,
                title = title.ifBlank { strippedText.take(80) },
                keywords = keywords,
                dueDate = parseResult.date,
                dueTime = dueTime,
                needsDateCheck = parseResult.needsDateCheck,
                reminderEnabled = reminderEnabled
            )
        }

        // 2. Fallback to heuristic classification
        val parseResult = dateTimeParser.parse(cleanedText)
        val hasReminderTrigger = reminderTriggers.any(normalized::contains)
        val hasIdeaTrigger = ideaTriggers.any(normalized::contains)
        val hasTaskKeyword = taskKeywords.any(normalized::contains)
        val hasDateTimeTrigger = dateTimeTriggers.any(normalized::contains) ||
            Regex("(?:\\bum\\s*)?([01]?\\d|2[0-3])(?::([0-5]\\d))?\\s*uhr\\b").containsMatchIn(normalized)

        val category = when {
            hasReminderTrigger -> Category.REMINDER
            hasIdeaTrigger -> Category.IDEA
            hasTaskKeyword || hasDateTimeTrigger || parseResult.date != null || parseResult.time != null -> Category.TASK
            else -> Category.NOTE
        }

        val title = extractTitle(cleanedText)
        val keywords = extractKeywords(cleanedText)
        val reminderEnabled = hasReminderTrigger || (parseResult.date != null && parseResult.time != null)
        val dueTime = parseResult.time ?: defaultDueTime(parseResult.date, reminderEnabled)

        return ClassificationResult(
            category = category,
            title = title,
            keywords = keywords,
            dueDate = parseResult.date,
            dueTime = dueTime,
            needsDateCheck = parseResult.needsDateCheck,
            reminderEnabled = reminderEnabled
        )
    }

    /**
     * Checks if the first word(s) match a category keyword (default or custom).
     * Returns (Category, textWithKeywordRemoved) or null if no match.
     */
    private fun detectFirstWordCategory(originalText: String, normalized: String): Pair<Category, String>? {
        val keywordMap = categorySettings?.buildKeywordMap() ?: Category.defaultKeywords
        // Sort by descending length so multi-word keywords are checked first
        val sortedKeywords = keywordMap.keys.sortedByDescending { it.length }

        for (keyword in sortedKeywords) {
            if (normalized.startsWith(keyword)) {
                val rest = originalText.drop(keyword.length).trimStart()
                // Capitalize first letter of the remaining text
                val strippedText = if (rest.isNotEmpty()) {
                    rest[0].uppercaseChar() + rest.drop(1)
                } else {
                    rest
                }
                return keywordMap[keyword]!! to strippedText
            }
        }
        return null
    }

    private fun defaultDueTime(dueDate: Long?, reminderEnabled: Boolean): Long? {
        return if (dueDate != null && reminderEnabled) DEFAULT_REMINDER_TIME else null
    }

    private fun extractTitle(text: String): String {
        val normalizedWhitespace = text.replace(Regex("\\s+"), " ").trim()
        val firstSentence = normalizedWhitespace
            .split(Regex("(?<=[.!?])\\s+|\\n"))
            .firstOrNull()
            .orEmpty()
            .replace(Regex("^idee\\s*:?\\s*", RegexOption.IGNORE_CASE), "")
            .trim()

        val candidate = firstSentence.ifBlank { normalizedWhitespace }.ifBlank { "Neuer Eintrag" }
        if (candidate.length <= 80) {
            return candidate.trimEnd('.', ',', ';', ':')
        }

        val shortened = candidate.take(80)
        val cutAt = shortened.lastIndexOf(' ').takeIf { it > 40 } ?: shortened.length
        return shortened.take(cutAt).trimEnd('.', ',', ';', ':')
    }

    private fun extractKeywords(text: String): List<String> {
        return Regex("[A-Za-zÄÖÜäöüß][A-Za-zÄÖÜäöüß-]+")
            .findAll(text)
            .map { it.value.lowercase(Locale.GERMAN) }
            .filter { it.length > 4 && it !in stopWords }
            .distinct()
            .take(8)
            .toList()
    }

    companion object {
        private const val DEFAULT_REMINDER_TIME = 9L * 60L * 60L * 1000L
    }
}
