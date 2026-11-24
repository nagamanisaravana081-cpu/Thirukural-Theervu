package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.DisplayLanguage
import com.example.model.KuralSolution
import com.example.model.VoiceLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

private const val TAG = "VoiceSpeechController"

class VoiceAndSpeechController(context: Context) {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        try {
            textToSpeech = TextToSpeech(appContext) { status ->
                ttsReady = (status == TextToSpeech.SUCCESS)
                if (ttsReady) {
                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            if (utteranceId == "kural_final") {
                                _isSpeaking.value = false
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _isSpeaking.value = false
                        }
                    })
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "TTS init failed", e)
            ttsReady = false
        }
    }

    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(appContext)
    }

    fun startListening(
        voiceLanguage: VoiceLanguage,
        onReady: () -> Unit,
        onPartialResult: (String) -> Unit,
        onRmsChanged: (Float) -> Unit,
        onFinalResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        stopSpeaking()
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                onError("Voice recognition service is unavailable on this device. Please type your problem below.")
                return@post
            }

            try {
                destroySpeechRecognizerInternal()
                val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
                speechRecognizer = recognizer

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        onReady()
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS dB roughly from -2..12 into 0f..1f
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.08f, 1f)
                        onRmsChanged(normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {}

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH ->
                                "Couldn't catch that clearly. Please tap the mic and try again, or type below."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                                "No speech detected. Tap the mic when you are ready to speak."
                            SpeechRecognizer.ERROR_AUDIO ->
                                "Microphone audio error. Please try again."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "Speech recognition needs a stable connection. You can also type your problem."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                "Microphone permission is required for voice input."
                            else ->
                                "Voice input interrupted. Please try again or type your problem below."
                        }
                        onError(message)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spokenText = matches?.firstOrNull()?.trim().orEmpty()
                        if (spokenText.isNotBlank()) {
                            onFinalResult(spokenText)
                        } else {
                            onError("Didn't catch any words. Please try speaking again.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()?.trim().orEmpty()
                        if (partial.isNotBlank()) {
                            onPartialResult(partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceLanguage.localeTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, voiceLanguage.localeTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                recognizer.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start SpeechRecognizer", e)
                onError("Unable to start voice recognition. Please type your problem below.")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.w(TAG, "stopListening failed: ${e.message}")
            }
        }
    }

    fun cancelListening() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.w(TAG, "cancelListening failed: ${e.message}")
            }
        }
    }

    fun speakSolution(solution: KuralSolution, displayLanguage: DisplayLanguage): Boolean {
        val tts = textToSpeech
        if (!ttsReady || tts == null) {
            return false
        }
        tts.stop()
        tts.setSpeechRate(0.92f)

        val tamilLocale = Locale.forLanguageTag("ta-IN")
        val englishLocale = Locale.forLanguageTag("en-IN")
        val tamilAvailable = tts.isLanguageAvailable(tamilLocale) >= TextToSpeech.LANG_AVAILABLE

        val kuralTamilText = "குறள் எண் ${solution.kuralNumber}. ${solution.kuralLine1Ta} ${solution.kuralLine2Ta}."
        val tamilGuidance = "பொருள்: ${solution.meaningTa}. நடைமுறை தீர்வு: ${solution.practicalSolutionTa}. செயல்பாடு: ${solution.actionableStepTa}."
        val englishGuidance = "Kural number ${solution.kuralNumber}, from ${solution.athikaramEn}. " +
            "Meaning: ${solution.meaningEn}. Practical guidance: ${solution.practicalSolutionEn}. " +
            "Action step: ${solution.actionableStepEn}."

        _isSpeaking.value = true

        when (displayLanguage) {
            DisplayLanguage.TAMIL -> {
                if (tamilAvailable) {
                    tts.language = tamilLocale
                    tts.speak("$kuralTamilText $tamilGuidance", TextToSpeech.QUEUE_FLUSH, null, "kural_final")
                } else {
                    tts.language = englishLocale
                    tts.speak(
                        "${solution.transliteration}. $englishGuidance",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "kural_final"
                    )
                }
            }
            DisplayLanguage.ENGLISH -> {
                if (tamilAvailable) {
                    tts.language = tamilLocale
                    tts.speak(kuralTamilText, TextToSpeech.QUEUE_FLUSH, null, "kural_part_1")
                    tts.language = englishLocale
                    tts.speak(englishGuidance, TextToSpeech.QUEUE_ADD, null, "kural_final")
                } else {
                    tts.language = englishLocale
                    tts.speak(
                        "${solution.transliteration}. $englishGuidance",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "kural_final"
                    )
                }
            }
            DisplayLanguage.BILINGUAL -> {
                if (tamilAvailable) {
                    tts.language = tamilLocale
                    tts.speak("$kuralTamilText பொருள்: ${solution.meaningTa}.", TextToSpeech.QUEUE_FLUSH, null, "kural_part_1")
                    tts.language = englishLocale
                    tts.speak(englishGuidance, TextToSpeech.QUEUE_ADD, null, "kural_final")
                } else {
                    tts.language = englishLocale
                    tts.speak(
                        "${solution.transliteration}. $englishGuidance",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "kural_final"
                    )
                }
            }
        }
        return true
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "stopSpeaking error: ${e.message}")
        }
        _isSpeaking.value = false
    }

    private fun destroySpeechRecognizerInternal() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "destroySpeechRecognizer error: ${e.message}")
        }
        speechRecognizer = null
    }

    fun release() {
        stopSpeaking()
        mainHandler.post {
            destroySpeechRecognizerInternal()
        }
        try {
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.w(TAG, "TTS shutdown error: ${e.message}")
        }
        textToSpeech = null
    }
}
