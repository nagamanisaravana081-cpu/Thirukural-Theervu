package com.example.data

import android.util.Log
import com.example.model.KuralSolution
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "KuralAiRepository"

class KuralAiRepository {

    private val solutionSchema = Schema.obj(
        properties = mapOf(
            "kuralNumber" to Schema.integer(
                description = "Best matching Kural number chosen from the provided catalog list."
            ),
            "detectedSummaryEn" to Schema.string(
                description = "Empathetic 1-sentence reflection in English."
            ),
            "detectedSummaryTa" to Schema.string(
                description = "Empathetic 1-sentence reflection in pure natural Tamil (no English words)."
            ),
            "meaningTa" to Schema.string(
                description = "Clear 1-2 sentence explanation of the Kural in pure Tamil (no English words)."
            ),
            "meaningEn" to Schema.string(
                description = "Clear 1-2 sentence explanation of the Kural in English."
            ),
            "practicalSolutionTa" to Schema.string(
                description = "Practical 2-sentence modern-life advice in pure Tamil (no English words)."
            ),
            "practicalSolutionEn" to Schema.string(
                description = "Practical 2-sentence modern-life advice in English."
            ),
            "actionableStepTa" to Schema.string(
                description = "One concrete 1-sentence action step for today in pure Tamil."
            ),
            "actionableStepEn" to Schema.string(
                description = "One concrete 1-sentence action step for today in English."
            ),
            "topicTag" to Schema.string(
                description = "One-word English category like Stress, Anger, Career, Relationships, Money, Failure, or Overthinking."
            )
        )
    )

    private val systemPrompt = content {
        text(
            """
            You are Thirukkural Life Guide (திருக்குறள் தீர்வு), a warm Tamil scholar and life counselor.
            Understand the user's problem in Tamil, English, or Tanglish.
            Pick the best matching `kuralNumber` strictly from this verified catalog:
            ${AuthenticKuralCatalog.promptSelectionIndex}
            Write all `*Ta` fields in pure, natural modern Tamil script only (never mix English or other scripts).
            Write all `*En` fields in clean English only. Keep every field concise (1-2 sentences).
            """.trimIndent()
        )
    }

    private val structuredConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = solutionSchema
        temperature = 0.2f
        maxOutputTokens = 1024
    }

    // Ultra-fast Gemini 3.5 Flash-Lite primary: since AuthenticKuralCatalog supplies 100% verbatim
    // classical Tamil Kurals, Flash-Lite delivers 1-3s responses with zero Kural hallucinations.
    private val fastPrimaryModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = structuredConfig,
            systemInstruction = systemPrompt
        )
    }

    // Frontier Gemini 3.8 Flash fallback if Flash-Lite fails or returns insufficient Tamil prose
    private val fallbackModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = structuredConfig,
            systemInstruction = systemPrompt
        )
    }

    suspend fun findKuralSolution(userProblem: String): Result<KuralSolution> {
        val cleaned = userProblem.trim()
        if (cleaned.isBlank()) {
            return Result.failure(IllegalArgumentException("Please speak or type your problem first."))
        }

        val prompt = "Problem: \"$cleaned\"\nChoose the best kuralNumber from the catalog and return concise JSON guidance."

        return withContext(Dispatchers.IO) {
            try {
                withTimeout(55_000L) {
                    val primaryResult = runModelWithTimeout(
                        model = fastPrimaryModel,
                        modelLabel = "gemini-3.5-flash-lite",
                        timeoutMs = 22_000L,
                        cleanedQuery = cleaned,
                        prompt = prompt
                    )
                    if (primaryResult.isSuccess) {
                        primaryResult
                    } else {
                        val firstError = primaryResult.exceptionOrNull()
                        Log.w(TAG, "Primary fast model failed (${firstError?.message}), retrying with gemini-3.8-flash")
                        runModelWithTimeout(
                            model = fallbackModel,
                            modelLabel = "gemini-3.8-flash",
                            timeoutMs = 30_000L,
                            cleanedQuery = cleaned,
                            prompt = prompt
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Thirukkural AI request timed out", e)
                Result.failure(
                    IllegalStateException(
                        "Taking too long to reach the guidance server. Please check your internet connection and tap Try Again."
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error finding Thirukkural solution", e)
                Result.failure(IllegalStateException(formatUserFriendlyError(e)))
            }
        }
    }

    private suspend fun runModelWithTimeout(
        model: GenerativeModel,
        modelLabel: String,
        timeoutMs: Long,
        cleanedQuery: String,
        prompt: String
    ): Result<KuralSolution> {
        return try {
            withTimeout(timeoutMs) {
                Log.d(TAG, "Requesting Thirukkural guidance from $modelLabel")
                val response = model.generateContent(prompt)
                val rawJson = response.text?.trim()?.takeIf { it.isNotBlank() }
                    ?: return@withTimeout Result.failure(IllegalStateException("Received an empty response. Please try again."))

                val json = JSONObject(rawJson)
                val requestedNumber = json.optInt("kuralNumber", -1)

                // ALWAYS pull the 2-line Tamil couplet, Kural #, Athikaram, and Transliteration
                // verbatim from AuthenticKuralCatalog so corrupted/mixed Kurals are impossible.
                val canonical = AuthenticKuralCatalog.findByNumber(requestedNumber)
                    ?: AuthenticKuralCatalog.findBestFallbackForQuery(cleanedQuery)

                val cleanSummaryTa = sanitizeToModernTamilProse(json.optString("detectedSummaryTa", ""))
                val cleanMeaningTa = sanitizeToModernTamilProse(json.optString("meaningTa", ""))
                val cleanPracticalTa = sanitizeToModernTamilProse(json.optString("practicalSolutionTa", ""))
                val cleanStepTa = sanitizeToModernTamilProse(json.optString("actionableStepTa", ""))

                // Only reject if Tamil explanation is genuinely missing/corrupted
                if (cleanMeaningTa.count(::isModernTamilChar) < 8 || cleanPracticalTa.count(::isModernTamilChar) < 8) {
                    Log.w(TAG, "Insufficient Tamil prose from $modelLabel for Kural #${canonical.number}")
                    return@withTimeout Result.failure(IllegalStateException("Incomplete Tamil explanation received."))
                }

                val solution = KuralSolution(
                    docId = "kural_${canonical.number}",
                    query = cleanedQuery,
                    detectedSummaryEn = json.optString("detectedSummaryEn", "You are seeking clarity for what you are going through.").trim(),
                    detectedSummaryTa = cleanSummaryTa.takeIf { it.count(::isModernTamilChar) >= 8 }
                        ?: "உங்கள் தற்போதைய சூழலுக்கு அமைதியும் தெளிவும் தரும் வழிகாட்டுதலைத் தேடுகிறீர்கள்.",
                    kuralNumber = canonical.number,
                    kuralLine1Ta = canonical.line1Ta,
                    kuralLine2Ta = canonical.line2Ta,
                    athikaramNumber = canonical.athikaramNumber,
                    athikaramTa = canonical.athikaramTa,
                    athikaramEn = canonical.athikaramEn,
                    transliteration = canonical.transliteration,
                    meaningTa = cleanMeaningTa,
                    meaningEn = json.optString("meaningEn", "").trim(),
                    practicalSolutionTa = cleanPracticalTa,
                    practicalSolutionEn = json.optString("practicalSolutionEn", "").trim(),
                    actionableStepTa = cleanStepTa.takeIf { it.count(::isModernTamilChar) >= 6 }
                        ?: "இன்று ஐந்து நிமிடம் அமைதியாக அமர்ந்து இந்தக் குறளின் பொருளைச் சிந்தித்து முதல் சிறிய அடியை எடுத்து வையுங்கள்.",
                    actionableStepEn = json.optString("actionableStepEn", "").trim(),
                    topicTag = json.optString("topicTag", "Guidance").trim().ifBlank { "Guidance" },
                    createdAtMillis = System.currentTimeMillis()
                )
                Log.d(TAG, "Resolved verified Kural #${canonical.number} via $modelLabel (requested=$requestedNumber)")
                Result.success(solution)
            }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Model $modelLabel timed out after ${timeoutMs}ms")
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Model $modelLabel failed: ${e.message}", e)
            Result.failure(IllegalStateException(formatUserFriendlyError(e)))
        }
    }

    /**
     * Strips foreign scripts (e.g. Thai, Devanagari, Malayalam, archaic Tamil symbols like ௴)
     * and stray Latin letters while preserving all modern Tamil Unicode characters, ZWJ/ZWNJ joiners,
     * digits, spaces, and standard punctuation.
     */
    private fun sanitizeToModernTamilProse(text: String): String {
        val sb = StringBuilder(text.length)
        for (ch in text) {
            val isAllowedJoiner = ch == '\u200C' || ch == '\u200D'
            if (isModernTamilChar(ch) ||
                isAllowedJoiner ||
                ch.isWhitespace() ||
                ch in '0'..'9' ||
                ch in ".,!?;:'\"“”‘’()-–—•…/%"
            ) {
                sb.append(ch)
            }
        }
        return sb.toString()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("\\s+([.,!?;:])"), "$1")
            .trim()
    }

    private fun isModernTamilChar(ch: Char): Boolean {
        val code = ch.code
        return code == 0x0B82 || code == 0x0B83 ||
            code in 0x0B85..0x0B8A ||
            code in 0x0B8E..0x0B90 ||
            code in 0x0B92..0x0B95 ||
            code == 0x0B99 || code == 0x0B9A || code == 0x0B9C ||
            code == 0x0B9E || code == 0x0B9F ||
            code == 0x0BA3 || code == 0x0BA4 ||
            code in 0x0BA8..0x0BAA ||
            code in 0x0BAE..0x0BB9 ||
            code in 0x0BBE..0x0BC2 ||
            code in 0x0BC6..0x0BC8 ||
            code in 0x0BCA..0x0BCD ||
            code == 0x0BD7
    }

    private fun formatUserFriendlyError(e: Exception): String {
        val raw = e.localizedMessage.orEmpty()
        return when {
            raw.contains("403", ignoreCase = true) || raw.contains("App Check", ignoreCase = true) ->
                "App verification failed (HTTP 403). Please reopen the app or check your network connection. ($raw)"
            raw.contains("Unable to resolve host", ignoreCase = true) ||
                raw.contains("network", ignoreCase = true) ||
                raw.contains("ConnectException", ignoreCase = true) ->
                "No internet connection detected. Please check your mobile data or Wi-Fi and try again."
            raw.isNotBlank() -> raw
            else -> "Unable to connect to Thirukkural Guide right now. Please try again."
        }
    }
}
