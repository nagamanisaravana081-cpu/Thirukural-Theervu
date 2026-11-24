package com.example.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.model.DisplayLanguage
import com.example.model.KuralSolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object KuralPhotoCardGenerator {

    private const val CARD_WIDTH = 1080
    private const val CARD_HEIGHT = 1350

    suspend fun generateAndSharePhotoCard(
        context: Context,
        solution: KuralSolution,
        language: DisplayLanguage,
        isDarkMode: Boolean
    ): Result<Unit> {
        return try {
            val imageFile = withContext(Dispatchers.IO) {
                val bitmap = renderCardBitmap(context, solution, language, isDarkMode)
                val cacheFolder = File(context.cacheDir, "shared_cards").apply { mkdirs() }
                val file = File(cacheFolder, "thirukkural_${solution.kuralNumber}.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                bitmap.recycle()
                file
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "திருக்குறள் #${solution.kuralNumber} — ${solution.athikaramTa} (${solution.athikaramEn})"
                )
                clipData = ClipData.newRawUri("Thirukkural Photo Card", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Thirukkural Photo Card").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun renderCardBitmap(
        context: Context,
        solution: KuralSolution,
        language: DisplayLanguage,
        isDarkMode: Boolean
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val tamilFont = ResourcesCompat.getFont(context, R.font.noto_sans_tamil) ?: Typeface.DEFAULT
        val serifFont = ResourcesCompat.getFont(context, R.font.merriweather) ?: Typeface.SERIF
        val sansFont = ResourcesCompat.getFont(context, R.font.inter) ?: Typeface.SANS_SERIF

        // Palette adapting smoothly to Light Parchment or Dark Parchment
        val bgTop = if (isDarkMode) Color.parseColor("#211A17") else Color.parseColor("#FAF7F2")
        val bgBottom = if (isDarkMode) Color.parseColor("#14100E") else Color.parseColor("#F1E7D7")
        val cardSurface = if (isDarkMode) Color.parseColor("#2A221E") else Color.parseColor("#FFFDF9")
        val primaryColor = if (isDarkMode) Color.parseColor("#E58C97") else Color.parseColor("#8B2635")
        val onPrimaryColor = if (isDarkMode) Color.parseColor("#3B0A12") else Color.parseColor("#FFFDF9")
        val goldColor = if (isDarkMode) Color.parseColor("#E2BE5C") else Color.parseColor("#C59B27")
        val goldSoftBg = if (isDarkMode) Color.parseColor("#47360A") else Color.parseColor("#F6E7BD")
        val goldSoftText = if (isDarkMode) Color.parseColor("#F6E7BD") else Color.parseColor("#332500")
        val inkPrimary = if (isDarkMode) Color.parseColor("#F5EFE6") else Color.parseColor("#231F1D")
        val inkMuted = if (isDarkMode) Color.parseColor("#C8BDB2") else Color.parseColor("#5E544E")
        val sageBg = if (isDarkMode) Color.parseColor("#1D3326") else Color.parseColor("#DCEEE2")
        val sageAccent = if (isDarkMode) Color.parseColor("#8CC9A7") else Color.parseColor("#3E6B52")

        // 1. Background gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, CARD_HEIGHT.toFloat(),
                bgTop, bgBottom, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), bgPaint)

        // 2. Outer and inner heritage gold border
        val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = goldColor
            strokeWidth = 3f
            alpha = 185
        }
        canvas.drawRoundRect(RectF(36f, 36f, CARD_WIDTH - 36f, CARD_HEIGHT - 36f), 42f, 42f, outerBorderPaint)

        val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = goldColor
            strokeWidth = 1.5f
            alpha = 110
        }
        canvas.drawRoundRect(RectF(52f, 52f, CARD_WIDTH - 52f, CARD_HEIGHT - 52f), 34f, 34f, innerBorderPaint)

        val contentLeft = 88f
        val contentWidth = (CARD_WIDTH - 176).coerceAtLeast(400)
        var cursorY = 104f

        // 3. Brand Header
        val brandTaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(tamilFont, Typeface.BOLD)
            textSize = 44f
            color = primaryColor
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("திருக்குறள் தீர்வு", CARD_WIDTH / 2f, cursorY, brandTaPaint)
        cursorY += 38f

        val brandEnPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = serifFont
            textSize = 24f
            color = inkMuted
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("THIRUKKURAL LIFE GUIDE", CARD_WIDTH / 2f, cursorY, brandEnPaint)
        cursorY += 28f

        // Gold ornamental divider
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            strokeWidth = 2.5f
            alpha = 180
        }
        canvas.drawLine(CARD_WIDTH / 2f - 120f, cursorY, CARD_WIDTH / 2f + 120f, cursorY, dividerPaint)
        cursorY += 36f

        // 4. Main Kural Plaque Card
        val badgeText = "குறள் #${solution.kuralNumber}"
        val topicText = solution.topicTag.take(24)

        val kuralTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(tamilFont, Typeface.BOLD)
            textSize = 36f
            color = inkPrimary
        }
        val coupletFullText = "${solution.kuralLine1Ta}\n${solution.kuralLine2Ta}"
        val coupletLayout = buildStaticLayout(coupletFullText, kuralTextPaint, contentWidth - 72)

        val athikaramPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(tamilFont, Typeface.NORMAL)
            textSize = 24f
            color = inkMuted
        }
        val athikaramText = "அதிகாரம் ${solution.athikaramNumber} • ${solution.athikaramTa} (${solution.athikaramEn})"
        val athikaramLayout = buildStaticLayout(athikaramText, athikaramPaint, contentWidth - 72)

        val translitPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(sansFont, Typeface.ITALIC)
            textSize = 23f
            color = inkMuted
        }
        val translitLayout = buildStaticLayout(solution.transliteration, translitPaint, contentWidth - 72)

        val plaqueHeight = 96f + athikaramLayout.height + 28f + coupletLayout.height + 24f + translitLayout.height + 38f
        val plaqueRect = RectF(contentLeft, cursorY, contentLeft + contentWidth, cursorY + plaqueHeight)

        val plaqueFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = cardSurface
            style = Paint.Style.FILL
        }
        val plaqueStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            alpha = 190
        }
        canvas.drawRoundRect(plaqueRect, 30f, 30f, plaqueFillPaint)
        canvas.drawRoundRect(plaqueRect, 30f, 30f, plaqueStrokePaint)

        // Draw Kural # badge & Topic pill inside plaque
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primaryColor }
        val pillTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(tamilFont, Typeface.BOLD)
            textSize = 24f
            color = onPrimaryColor
        }
        val kuralBadgeWidth = pillTextPaint.measureText(badgeText) + 46f
        val kuralBadgeRect = RectF(contentLeft + 36f, cursorY + 28f, contentLeft + 36f + kuralBadgeWidth, cursorY + 76f)
        canvas.drawRoundRect(kuralBadgeRect, 24f, 24f, pillPaint)
        canvas.drawText(badgeText, kuralBadgeRect.left + 23f, kuralBadgeRect.top + 33f, pillTextPaint)

        val topicBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = goldSoftBg }
        val topicTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(sansFont, Typeface.BOLD)
            textSize = 22f
            color = goldSoftText
        }
        val topicWidth = topicTextPaint.measureText(topicText) + 40f
        val topicRect = RectF(
            contentLeft + contentWidth - 36f - topicWidth,
            cursorY + 30f,
            contentLeft + contentWidth - 36f,
            cursorY + 74f
        )
        canvas.drawRoundRect(topicRect, 22f, 22f, topicBgPaint)
        canvas.drawText(topicText, topicRect.left + 20f, topicRect.top + 30f, topicTextPaint)

        var innerY = cursorY + 96f
        drawLayout(canvas, athikaramLayout, contentLeft + 36f, innerY)
        innerY += athikaramLayout.height + 16f

        canvas.drawLine(contentLeft + 36f, innerY, contentLeft + contentWidth - 36f, innerY, innerBorderPaint)
        innerY += 18f

        drawLayout(canvas, coupletLayout, contentLeft + 36f, innerY)
        innerY += coupletLayout.height + 18f

        drawLayout(canvas, translitLayout, contentLeft + 36f, innerY)

        cursorY = plaqueRect.bottom + 28f

        // 5. Meaning & Practical Guidance Section
        val sectionTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(serifFont, Typeface.BOLD)
            textSize = 25f
            color = primaryColor
        }
        val bodyTamilPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tamilFont
            textSize = 25f
            color = inkPrimary
        }
        val bodyEnPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = sansFont
            textSize = 25f
            color = inkPrimary
        }

        val meaningHeader = when (language) {
            DisplayLanguage.TAMIL -> "பொருள்"
            DisplayLanguage.ENGLISH -> "Meaning"
            DisplayLanguage.BILINGUAL -> "பொருள் • Meaning"
        }
        val meaningText = when (language) {
            DisplayLanguage.TAMIL -> solution.meaningTa
            DisplayLanguage.ENGLISH -> solution.meaningEn
            DisplayLanguage.BILINGUAL -> "${solution.meaningTa}\n\n${solution.meaningEn}"
        }
        val meaningPaint = if (language == DisplayLanguage.ENGLISH) bodyEnPaint else bodyTamilPaint
        val meaningLayout = buildStaticLayout(meaningText.take(280), meaningPaint, contentWidth - 64, maxLines = 5)

        val guidanceHeader = when (language) {
            DisplayLanguage.TAMIL -> "நடைமுறை தீர்வு"
            DisplayLanguage.ENGLISH -> "Practical Guidance"
            DisplayLanguage.BILINGUAL -> "நடைமுறை தீர்வு • Practical Guidance"
        }
        val guidanceText = when (language) {
            DisplayLanguage.TAMIL -> solution.practicalSolutionTa
            DisplayLanguage.ENGLISH -> solution.practicalSolutionEn
            DisplayLanguage.BILINGUAL -> "${solution.practicalSolutionTa}\n\n${solution.practicalSolutionEn}"
        }
        val guidancePaint = if (language == DisplayLanguage.ENGLISH) bodyEnPaint else bodyTamilPaint
        val guidanceLayout = buildStaticLayout(guidanceText.take(280), guidancePaint, contentWidth - 64, maxLines = 5)

        // Draw Meaning box
        val meaningBoxHeight = 68f + meaningLayout.height + 24f
        val meaningRect = RectF(contentLeft, cursorY, contentLeft + contentWidth, cursorY + meaningBoxHeight)
        canvas.drawRoundRect(meaningRect, 24f, 24f, plaqueFillPaint)
        canvas.drawText(meaningHeader, contentLeft + 32f, cursorY + 46f, sectionTitlePaint)
        drawLayout(canvas, meaningLayout, contentLeft + 32f, cursorY + 66f)
        cursorY = meaningRect.bottom + 20f

        // Draw Practical Guidance box if vertical space allows
        val remainingBeforeAction = (CARD_HEIGHT - 255f) - cursorY
        if (remainingBeforeAction >= 120f) {
            val clippedGuidanceHeight = (68f + guidanceLayout.height + 24f).coerceAtMost(remainingBeforeAction)
            val guidanceRect = RectF(contentLeft, cursorY, contentLeft + contentWidth, cursorY + clippedGuidanceHeight)
            canvas.drawRoundRect(guidanceRect, 24f, 24f, plaqueFillPaint)
            canvas.save()
            canvas.clipRect(guidanceRect)
            canvas.drawText(guidanceHeader, contentLeft + 32f, cursorY + 46f, sectionTitlePaint)
            drawLayout(canvas, guidanceLayout, contentLeft + 32f, cursorY + 66f)
            canvas.restore()
            cursorY = guidanceRect.bottom + 20f
        }

        // 6. Actionable Step Highlight Box (anchored cleanly above footer)
        val actionTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(sansFont, Typeface.BOLD)
            textSize = 23f
            color = sageAccent
        }
        val actionBodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = if (language == DisplayLanguage.ENGLISH) sansFont else tamilFont
            textSize = 23f
            color = inkPrimary
        }
        val actionText = when (language) {
            DisplayLanguage.TAMIL -> solution.actionableStepTa
            DisplayLanguage.ENGLISH -> solution.actionableStepEn
            DisplayLanguage.BILINGUAL -> "${solution.actionableStepTa} • ${solution.actionableStepEn}"
        }
        val actionLayout = buildStaticLayout(actionText.take(200), actionBodyPaint, contentWidth - 64, maxLines = 3)
        val actionBoxHeight = (62f + actionLayout.height + 20f).coerceAtMost(170f)
        val actionTop = (CARD_HEIGHT - 115f - actionBoxHeight).coerceAtLeast(cursorY)
        val actionRect = RectF(contentLeft, actionTop, contentLeft + contentWidth, actionTop + actionBoxHeight)
        val actionFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = sageBg }
        canvas.drawRoundRect(actionRect, 22f, 22f, actionFill)
        canvas.save()
        canvas.clipRect(actionRect)
        canvas.drawText(
            if (language == DisplayLanguage.TAMIL) "இன்றைய செயல்பாடு" else "TODAY'S ACTION STEP",
            contentLeft + 32f,
            actionTop + 40f,
            actionTitlePaint
        )
        drawLayout(canvas, actionLayout, contentLeft + 32f, actionTop + 56f)
        canvas.restore()

        // 7. Footer Signature
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = sansFont
            textSize = 21f
            color = inkMuted
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Shared via Thirukkural Life Guide • திருக்குறள் தீர்வு",
            CARD_WIDTH / 2f,
            CARD_HEIGHT - 66f,
            footerPaint
        )

        return bitmap
    }

    private fun buildStaticLayout(
        text: String,
        paint: TextPaint,
        width: Int,
        maxLines: Int = 10
    ): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(6f, 1.05f)
            .setIncludePad(false)
            .setMaxLines(maxLines)
            .setEllipsize(android.text.TextUtils.TruncateAt.END)
            .build()
    }

    private fun drawLayout(canvas: Canvas, layout: StaticLayout, x: Float, y: Float) {
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
    }
}
