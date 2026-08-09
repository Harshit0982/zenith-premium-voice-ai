package com.example.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import com.example.data.prefs.AppPreferences

class WakeWordService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var WAKE_WORD = "hey assistant"
    private val TAG = "WakeWordService"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        
        serviceScope.launch {
            val prefs = AppPreferences(applicationContext)
            val name = prefs.assistantNameFlow.first()
            WAKE_WORD = "hey ${name.lowercase()}"
            withContext(Dispatchers.Main) {
                initializeSpeechRecognizer()
                startListening()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Will start listening after datastore is read
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "wake_word_channel",
                "Wake Word Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "wake_word_channel")
            .setContentTitle("Listening for wake word")
            .setContentText("Say '$WAKE_WORD' to activate.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    Log.d(TAG, "SpeechRecognizer Error: $error")
                    // Restart listening after a short delay
                    startListening()
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    handleResults(matches)
                    startListening()
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    handleResults(matches)
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun handleResults(matches: ArrayList<String>?) {
        matches?.forEach { match ->
            Log.d(TAG, "Heard: $match")
            if (match.lowercase().contains(WAKE_WORD)) {
                wakeUpAssistant()
            }
        }
    }

    private fun wakeUpAssistant() {
        Log.d(TAG, "Wake word detected!")
        // To wake up the screen, we launch our MainActivity which has flags to turn on screen
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("wake_word_detected", true)
        }
        startActivity(intent)
    }

    private fun startListening() {
        speechRecognizer?.cancel() // Cancel any existing before starting
        speechRecognizer?.startListening(recognizerIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        speechRecognizer?.destroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
