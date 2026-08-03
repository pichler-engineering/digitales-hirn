package com.pichler.digitaleshirn.classification

import com.pichler.digitaleshirn.data.Category
import java.util.Locale

class RuleBasedClassificationService(
    private val dateTimeParser: GermanDateTimeParser = GermanDateTimeParser()
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
        val parseResult = dateTimeParser.parse(cleanedText)
        val hasReminderTrigger = reminderTriggers.any(normalized::contains)
        val hasIdeaTrigger = ideaTriggers.any(normalized::contains)
        val hasTaskKeyword = taskKeywords.any(normalized::contains)
        val hasDateTimeTrigger = dateTimeTriggers.any(normalized::contains) ||
            Regex("(?:\\bum\\s*)?([01]?\\d|2[0-3])(?::([0-5]\\d))?\\s*uhr\\b").containsMatchIn(normalized)

        val category = when {
            hasReminderTrigger -> Category.ERINNERUNG
            hasIdeaTrigger -> Category.IDEE
            hasTaskKeyword || hasDateTimeTrigger || parseResult.date != null || parseResult.time != null -> Category.AUFGABE
            else -> Category.NOTIZ
        }

        val title = extractTitle(cleanedText)
        val keywords = extractKeywords(cleanedText)
        val reminderEnabled = hasReminderTrigger || (parseResult.date != null && parseResult.time != null)

        return ClassificationResult(
            category = category,
            title = title,
            keywords = keywords,
            dueDate = parseResult.date,
            dueTime = parseResult.time,
            needsDateCheck = parseResult.needsDateCheck,
            reminderEnabled = reminderEnabled
        )
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
}
