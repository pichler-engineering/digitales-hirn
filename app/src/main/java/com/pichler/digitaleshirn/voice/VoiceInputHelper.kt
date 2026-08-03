package com.pichler.digitaleshirn.voice

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import java.util.Locale

class VoiceInputHelper {

    fun createSpeechIntent(): Intent? {
        return try {
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMANY.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.GERMANY.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, Locale.GERMANY.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Bitte sprechen")
            }
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("DEPRECATION")
    fun startListening(activity: Activity, requestCode: Int) {
        val intent = createSpeechIntent() ?: return
        try {
            activity.startActivityForResult(intent, requestCode)
        } catch (_: ActivityNotFoundException) {
            // No speech recognition activity available on the device.
        }
    }

    fun parseResult(data: Intent?): String? {
        return data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.takeIf { it.isNotBlank() }
    }
}
