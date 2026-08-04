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
            Category.TASK to 0,
            Category.NOTE to 1,
            Category.IDEA to 2,
            Category.REMINDER to 3
        )

        return repository.searchEntries(query)
            .first()
            .sortedWith(
                compareBy<Entry> { categoryOrder[it.category] ?: Int.MAX_VALUE }
                    .thenByDescending { it.createdAt }
            )
    }
}
