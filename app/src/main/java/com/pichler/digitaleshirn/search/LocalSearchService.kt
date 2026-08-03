package com.pichler.digitaleshirn.search

import com.pichler.digitaleshirn.data.Category
import com.pichler.digitaleshirn.data.Entry
import com.pichler.digitaleshirn.data.EntryRepository
import kotlinx.coroutines.flow.first

class LocalSearchService(
    private val repository: EntryRepository
) : SearchService {

    override suspend fun search(query: String): List<Entry> {
        val categoryOrder = mapOf(
            Category.AUFGABE to 0,
            Category.NOTIZ to 1,
            Category.IDEE to 2,
            Category.ERINNERUNG to 3
        )

        return repository.searchEntries(query)
            .first()
            .sortedWith(
                compareBy<Entry> { categoryOrder[it.category] ?: Int.MAX_VALUE }
                    .thenByDescending { it.createdAt }
            )
    }
}
