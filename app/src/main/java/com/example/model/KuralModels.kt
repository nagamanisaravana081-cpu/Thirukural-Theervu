package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import org.json.JSONObject

enum class DisplayLanguage(val labelEn: String, val labelTa: String) {
    BILINGUAL("Bilingual", "இருமொழி"),
    TAMIL("Tamil", "தமிழ்"),
    ENGLISH("English", "English");

    companion object {
        fun fromRaw(raw: String?): DisplayLanguage =
            entries.firstOrNull { it.name == raw } ?: BILINGUAL
    }
}

enum class VoiceLanguage(
    val localeTag: String,
    val labelEn: String,
    val labelTa: String,
    val shortBadge: String
) {
    TAMIL_IN("ta-IN", "Tamil (தமிழ்)", "தமிழ்", "தமிழ்"),
    TANGLISH_EN_IN("en-IN", "English / Tanglish (India)", "ஆங்கிலம் / தங்கிலீஷ்", "EN / Tanglish"),
    ENGLISH_US("en-US", "English (US)", "ஆங்கிலம் (US)", "EN-US");

    companion object {
        fun fromRaw(raw: String?): VoiceLanguage =
            entries.firstOrNull { it.name == raw } ?: TANGLISH_EN_IN
    }
}

data class KuralSolution(
    val docId: String = "",
    val query: String = "",
    val detectedSummaryEn: String = "",
    val detectedSummaryTa: String = "",
    val kuralNumber: Int = 1,
    val kuralLine1Ta: String = "",
    val kuralLine2Ta: String = "",
    val athikaramNumber: Int = 1,
    val athikaramTa: String = "",
    val athikaramEn: String = "",
    val transliteration: String = "",
    val meaningTa: String = "",
    val meaningEn: String = "",
    val practicalSolutionTa: String = "",
    val practicalSolutionEn: String = "",
    val actionableStepTa: String = "",
    val actionableStepEn: String = "",
    val topicTag: String = "Guidance",
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val stableDocId: String
        get() = docId.ifBlank { "kural_$kuralNumber" }

    fun toJson(): JSONObject = JSONObject().apply {
        put("docId", stableDocId)
        put("query", query)
        put("detectedSummaryEn", detectedSummaryEn)
        put("detectedSummaryTa", detectedSummaryTa)
        put("kuralNumber", kuralNumber)
        put("kuralLine1Ta", kuralLine1Ta)
        put("kuralLine2Ta", kuralLine2Ta)
        put("athikaramNumber", athikaramNumber)
        put("athikaramTa", athikaramTa)
        put("athikaramEn", athikaramEn)
        put("transliteration", transliteration)
        put("meaningTa", meaningTa)
        put("meaningEn", meaningEn)
        put("practicalSolutionTa", practicalSolutionTa)
        put("practicalSolutionEn", practicalSolutionEn)
        put("actionableStepTa", actionableStepTa)
        put("actionableStepEn", actionableStepEn)
        put("topicTag", topicTag)
        put("createdAtMillis", createdAtMillis)
    }

    fun formatForShare(language: DisplayLanguage): String = buildString {
        appendLine("✨ திருக்குறள் தீர்வு | Thirukkural Life Guide ✨")
        appendLine()
        appendLine("குறள் #$kuralNumber — $athikaramTa ($athikaramEn)")
        appendLine("“$kuralLine1Ta")
        appendLine("$kuralLine2Ta”")
        appendLine()
        appendLine(transliteration)
        appendLine()
        when (language) {
            DisplayLanguage.TAMIL -> {
                appendLine("பொருள்: $meaningTa")
                appendLine()
                appendLine("நடைமுறை தீர்வு: $practicalSolutionTa")
                appendLine()
                appendLine("இன்று செய்ய வேண்டியது: $actionableStepTa")
            }
            DisplayLanguage.ENGLISH -> {
                appendLine("Meaning: $meaningEn")
                appendLine()
                appendLine("Practical Guidance: $practicalSolutionEn")
                appendLine()
                appendLine("Action Step: $actionableStepEn")
            }
            DisplayLanguage.BILINGUAL -> {
                appendLine("பொருள்: $meaningTa")
                appendLine("Meaning: $meaningEn")
                appendLine()
                appendLine("நடைமுறை தீர்வு: $practicalSolutionTa")
                appendLine("Guidance: $practicalSolutionEn")
                appendLine()
                appendLine("செயல்பாடு / Action Step:")
                appendLine("• $actionableStepTa")
                appendLine("• $actionableStepEn")
            }
        }
        appendLine()
        append("— Thirukkural Life Guide (திருக்குறள் தீர்வு)")
    }

    companion object {
        fun fromJson(json: JSONObject): KuralSolution = KuralSolution(
            docId = json.optString("docId", ""),
            query = json.optString("query", ""),
            detectedSummaryEn = json.optString("detectedSummaryEn", ""),
            detectedSummaryTa = json.optString("detectedSummaryTa", ""),
            kuralNumber = json.optInt("kuralNumber", 1).coerceIn(1, 1330),
            kuralLine1Ta = json.optString("kuralLine1Ta", ""),
            kuralLine2Ta = json.optString("kuralLine2Ta", ""),
            athikaramNumber = json.optInt("athikaramNumber", 1).coerceIn(1, 133),
            athikaramTa = json.optString("athikaramTa", ""),
            athikaramEn = json.optString("athikaramEn", ""),
            transliteration = json.optString("transliteration", ""),
            meaningTa = json.optString("meaningTa", ""),
            meaningEn = json.optString("meaningEn", ""),
            practicalSolutionTa = json.optString("practicalSolutionTa", ""),
            practicalSolutionEn = json.optString("practicalSolutionEn", ""),
            actionableStepTa = json.optString("actionableStepTa", ""),
            actionableStepEn = json.optString("actionableStepEn", ""),
            topicTag = json.optString("topicTag", "Guidance"),
            createdAtMillis = json.optLong("createdAtMillis", System.currentTimeMillis())
        )
    }
}

data class RecentSearchEntry(
    val query: String,
    val timestampMillis: Long,
    val solution: KuralSolution
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("query", query)
        put("timestampMillis", timestampMillis)
        put("solution", solution.toJson())
    }

    companion object {
        fun fromJson(json: JSONObject): RecentSearchEntry? {
            val solObj = json.optJSONObject("solution") ?: return null
            val query = json.optString("query", "").trim()
            if (query.isBlank()) return null
            return RecentSearchEntry(
                query = query,
                timestampMillis = json.optLong("timestampMillis", System.currentTimeMillis()),
                solution = KuralSolution.fromJson(solObj)
            )
        }
    }
}

data class QuickProblemTopic(
    val id: String,
    val labelEn: String,
    val labelTa: String,
    val promptText: String
)

val DefaultQuickProblemTopics = listOf(
    QuickProblemTopic(
        id = "stress",
        labelEn = "Stress",
        labelTa = "மன அழுத்தம்",
        promptText = "I am feeling overwhelmed by daily stress and pressure, and my mind feels restless."
    ),
    QuickProblemTopic(
        id = "anger",
        labelEn = "Anger",
        labelTa = "கோபம்",
        promptText = "I lose my temper quickly when things go wrong and regret my harsh words later."
    ),
    QuickProblemTopic(
        id = "career",
        labelEn = "Career",
        labelTa = "தொழில்",
        promptText = "I feel stuck in my career and unsure how to work steadily toward growth."
    ),
    QuickProblemTopic(
        id = "relationships",
        labelEn = "Relationships",
        labelTa = "உறவுகள்",
        promptText = "I am facing misunderstandings and hurt feelings with someone close to me."
    ),
    QuickProblemTopic(
        id = "money",
        labelEn = "Money",
        labelTa = "பணம்",
        promptText = "I am worried about financial stability and managing expenses wisely."
    ),
    QuickProblemTopic(
        id = "failure",
        labelEn = "Failure",
        labelTa = "தோல்வி",
        promptText = "I recently faced a painful setback and feel discouraged about trying again."
    ),
    QuickProblemTopic(
        id = "overthinking",
        labelEn = "Overthinking",
        labelTa = "அதிக சிந்தனை",
        promptText = "I cannot stop overthinking past mistakes and worrying about what might happen next."
    )
)

@Keep
@IgnoreExtraProperties
data class UserProfileDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String? = null,
    val displayLanguage: String = DisplayLanguage.BILINGUAL.name,
    val voiceLanguage: String = VoiceLanguage.TANGLISH_EN_IN.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SavedKuralDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val query: String = "",
    val detectedSummaryEn: String = "",
    val detectedSummaryTa: String = "",
    val kuralNumber: Long = 1L,
    val kuralLine1Ta: String = "",
    val kuralLine2Ta: String = "",
    val athikaramNumber: Long = 1L,
    val athikaramTa: String = "",
    val athikaramEn: String = "",
    val transliteration: String = "",
    val meaningTa: String = "",
    val meaningEn: String = "",
    val practicalSolutionTa: String = "",
    val practicalSolutionEn: String = "",
    val actionableStepTa: String = "",
    val actionableStepEn: String = "",
    val topicTag: String = "Guidance",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toKuralSolution(): KuralSolution = KuralSolution(
        docId = id.ifBlank { "kural_${kuralNumber.toInt()}" },
        query = query,
        detectedSummaryEn = detectedSummaryEn,
        detectedSummaryTa = detectedSummaryTa,
        kuralNumber = kuralNumber.toInt().coerceIn(1, 1330),
        kuralLine1Ta = kuralLine1Ta,
        kuralLine2Ta = kuralLine2Ta,
        athikaramNumber = athikaramNumber.toInt().coerceIn(1, 133),
        athikaramTa = athikaramTa,
        athikaramEn = athikaramEn,
        transliteration = transliteration,
        meaningTa = meaningTa,
        meaningEn = meaningEn,
        practicalSolutionTa = practicalSolutionTa,
        practicalSolutionEn = practicalSolutionEn,
        actionableStepTa = actionableStepTa,
        actionableStepEn = actionableStepEn,
        topicTag = topicTag,
        createdAtMillis = createdAt?.toDate()?.time ?: System.currentTimeMillis()
    )
}
