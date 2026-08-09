package com.example.ai

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AssistantState {
    IDLE, LISTENING, THINKING, SPEAKING, ERROR
}

class VoiceAssistantViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _state = MutableStateFlow(AssistantState.IDLE)
    val state: StateFlow<AssistantState> = _state
    
    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText

    private var speechRecognizer: SpeechRecognizer? = null
    private val audioPlayer = AudioPlayer()

    init {
        setupSpeechRecognizer()
    }

    private fun setupSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(getApplication())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication())
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _state.value = AssistantState.THINKING
                }
                override fun onError(error: Int) {
                    _state.value = AssistantState.ERROR
                    // Reset to idle after a short delay
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(2000)
                        _state.value = AssistantState.IDLE
                    }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        _spokenText.value = text
                        processInputWithGemini(text)
                    } else {
                        _state.value = AssistantState.IDLE
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun toggleListening() {
        when (_state.value) {
            AssistantState.IDLE, AssistantState.ERROR -> startListening()
            AssistantState.LISTENING -> stopListening()
            AssistantState.THINKING, AssistantState.SPEAKING -> {
                // Interruption (Barge-in)
                audioPlayer.stop()
                startListening()
            }
        }
    }

    private fun startListening() {
        if (speechRecognizer == null) {
            _state.value = AssistantState.ERROR
            _spokenText.value = "Speech recognition is not supported on this device/emulator."
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                _state.value = AssistantState.IDLE
                _spokenText.value = ""
            }
            return
        }
        
        _state.value = AssistantState.LISTENING
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun stopListening() {
        speechRecognizer?.stopListening()
    }

    private fun processInputWithGemini(text: String) {
        _state.value = AssistantState.THINKING
        viewModelScope.launch {
            try {
                val apiKey = com.example.data.prefs.AppPreferences(getApplication()).geminiApiKeyFlow.first()
                if (apiKey.isBlank()) {
                    _spokenText.value = "Please configure your Gemini API key in Settings."
                    _state.value = AssistantState.ERROR
                    kotlinx.coroutines.delay(3000)
                    _state.value = AssistantState.IDLE
                    return@launch
                }

                val generativeModel = com.google.ai.client.generativeai.GenerativeModel(
                    modelName = "gemini-3.1-pro-preview",
                    apiKey = apiKey,
                    systemInstruction = com.google.ai.client.generativeai.type.content {
                        text("""
                            You are a helpful device assistant. You can control the user's Android device.
                            Determine the user's intent and output a JSON response matching this schema:
                            {
                              "action": "one of: playMusic, pauseMusic, nextTrack, previousTrack, openApp, setAlarm, setTimer, dialPhoneNumber, composeSms, getDirections, capturePhoto, openSettings, toggleFlashlight, setVolume, getBattery, getStorage, openWifiSettings, openBluetoothSettings, goHome, goBack, openRecents, takeScreenshot, lockScreen, setBrightness, readNotifications, clearNotifications, readClipboard, setClipboard, openQuickSettings, none",
                              "parameters": { // Include only parameters relevant to the action
                                "query": "string (for playMusic, openApp, web search)",
                                "packageName": "string (optional package name)",
                                "message": "string (for alarm/timer label, sms message, or clipboard)",
                                "hour": "int (0-23 for alarm)",
                                "minutes": "int (0-59 for alarm)",
                                "lengthSeconds": "int (for timer)",
                                "phoneNumber": "string",
                                "location": "string",
                                "level": "int (volume/brightness percentage 0-100)",
                                "on": "boolean (for flashlight)"
                              },
                              "replyText": "What you should say back to the user out loud."
                            }
                            Output ONLY valid JSON. No markdown formatting.
                        """.trimIndent())
                    },
                    generationConfig = com.google.ai.client.generativeai.type.generationConfig {
                        responseMimeType = "application/json"
                    }
                )

                val response = generativeModel.generateContent(text)
                val responseText = response.text ?: ""
                
                // Parse JSON manually or with Moshi
                val jsonObject = org.json.JSONObject(responseText)
                val action = jsonObject.optString("action", "none")
                val params = jsonObject.optJSONObject("parameters") ?: org.json.JSONObject()
                val replyText = jsonObject.optString("replyText", "I'm not sure how to do that.")

                // Execute action
                val overrideText = executeDeviceAction(action, params)
                val finalReply = overrideText ?: replyText

                _spokenText.value = finalReply
                _state.value = AssistantState.SPEAKING

                // Simulate speaking time based on text length
                kotlinx.coroutines.delay((finalReply.length * 50).toLong().coerceAtLeast(1000L))
                
                _state.value = AssistantState.IDLE
            } catch (e: Exception) {
                e.printStackTrace()
                _spokenText.value = "Sorry, I encountered an error."
                _state.value = AssistantState.ERROR
                kotlinx.coroutines.delay(2000)
                _state.value = AssistantState.IDLE
            }
        }
    }

    private fun executeDeviceAction(action: String, params: org.json.JSONObject): String? {
        val manager = com.example.ai.tools.DeviceControlManager(getApplication())
        val mediaManager = com.example.ai.tools.MediaControllerManager(getApplication())
        try {
            when (action) {
                "playMusic" -> {
                    if (params.has("query") && params.getString("query").isNotBlank()) {
                         manager.playMusic(params.optString("query"), if (params.has("packageName")) params.optString("packageName") else null)
                    } else {
                         mediaManager.play()
                    }
                }
                "pauseMusic" -> mediaManager.pause()
                "nextTrack" -> mediaManager.nextTrack()
                "previousTrack" -> mediaManager.previousTrack()
                "openApp" -> {
                    val packageName = params.optString("packageName")
                    if (packageName.isNotEmpty()) {
                        if (!manager.openApp(packageName)) {
                            manager.searchWeb(params.optString("query", packageName))
                        }
                    } else {
                        manager.searchWeb(params.optString("query"))
                    }
                }
                "setAlarm" -> manager.setAlarm(params.optString("message", "Alarm"), params.optInt("hour", 0), params.optInt("minutes", 0))
                "setTimer" -> manager.setTimer(params.optString("message", "Timer"), params.optInt("lengthSeconds", 60))
                "dialPhoneNumber" -> manager.dialPhoneNumber(params.optString("phoneNumber"))
                "composeSms" -> manager.composeSms(params.optString("phoneNumber"), params.optString("message"))
                "getDirections" -> manager.getDirections(params.optString("location"))
                "capturePhoto" -> manager.capturePhoto()
                "openSettings" -> manager.openSettings()
                "toggleFlashlight" -> manager.toggleFlashlight(params.optBoolean("on", true))
                "setVolume" -> mediaManager.setVolume(params.optInt("level", 50))
                "getBattery" -> return "You have ${manager.getBatteryPercentage()}% battery remaining."
                "getStorage" -> return "You have ${manager.getAvailableStorageMb()} megabytes of storage available."
                "openWifiSettings" -> manager.openWifiSettings()
                "openBluetoothSettings" -> manager.openBluetoothSettings()
                "goHome" -> manager.goHome()
                "goBack" -> manager.goBack()
                "openRecents" -> manager.openRecents()
                "takeScreenshot" -> manager.takeScreenshot()
                "lockScreen" -> manager.lockScreen()
                "setBrightness" -> manager.setBrightness(params.optInt("level", 50))
                "readNotifications" -> return manager.readNotifications()
                "clearNotifications" -> manager.clearNotifications()
                "readClipboard" -> return manager.readClipboard()
                "setClipboard" -> manager.setClipboard(params.optString("message"))
                "openQuickSettings" -> manager.openQuickSettings()
                "none" -> { /* Just chat */ }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        audioPlayer.release()
    }
}
