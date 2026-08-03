package com.pichler.digitaleshirn.search

import com.pichler.digitaleshirn.data.Entry

interface SearchService {
    suspend fun search(query: String): List<Entry>
}
