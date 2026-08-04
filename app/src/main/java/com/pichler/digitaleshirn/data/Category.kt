package com.pichler.digitaleshirn.data

enum class Category(val defaultName: String) {
    TASK("Aufgabe"),
    REMINDER("Erinnerung"),
    NOTE("Notiz"),
    IDEA("Idee");

    companion object {
        /** All German keyword aliases that map to a category (default + common variants). */
        val defaultKeywords: Map<String, Category> = mapOf(
            "aufgabe" to TASK,
            "task" to TASK,
            "erinnerung" to REMINDER,
            "reminder" to REMINDER,
            "notiz" to NOTE,
            "note" to NOTE,
            "idee" to IDEA,
            "idea" to IDEA
        )

        /** Parse from stored DB string – handles old German enum names for migration safety. */
        fun fromDbValue(value: String): Category = when (value.uppercase()) {
            "TASK", "AUFGABE" -> TASK
            "REMINDER", "ERINNERUNG" -> REMINDER
            "NOTE", "NOTIZ" -> NOTE
            "IDEA", "IDEE" -> IDEA
            else -> runCatching { valueOf(value.uppercase()) }.getOrDefault(TASK)
        }
    }
}
