package com.pichler.digitaleshirn.classification

interface ClassificationService {
    suspend fun classify(text: String): ClassificationResult
}
