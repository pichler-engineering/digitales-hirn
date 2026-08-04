package com.pichler.digitaleshirn.ui.shared

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pichler.digitaleshirn.classification.RuleBasedClassificationService
import com.pichler.digitaleshirn.data.AppDatabase
import com.pichler.digitaleshirn.data.CategorySettingsRepository
import com.pichler.digitaleshirn.data.EntryRepository
import com.pichler.digitaleshirn.search.LocalSearchService

class EntryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryViewModel::class.java)) {
            val db = AppDatabase.getInstance(context.applicationContext)
            val repository = EntryRepository(db.entryDao())
            val categorySettings = CategorySettingsRepository.getInstance(context.applicationContext)
            val classificationService = RuleBasedClassificationService(categorySettings = categorySettings)
            val searchService = LocalSearchService(repository)
            return EntryViewModel(
                context.applicationContext as android.app.Application,
                repository,
                classificationService,
                searchService
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
