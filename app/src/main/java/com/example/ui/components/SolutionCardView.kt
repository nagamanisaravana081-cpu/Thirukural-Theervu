package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DisplayLanguage
import com.example.model.KuralSolution
import com.example.ui.theme.KuralCoupletTextStyle
import com.example.ui.theme.TamilBodyTextStyle

@Composable
fun SolutionCardView(
    solution: KuralSolution,
    displayLanguage: DisplayLanguage,
    isBookmarked: Boolean,
    isSpeaking: Boolean,
    isGeneratingPhotoCard: Boolean = false,
    onSelectLanguage: (DisplayLanguage) -> Unit,
    onToggleReadAloud: () -> Unit,
    onToggleBookmark: () -> Unit,
    onSharePhotoCard: () -> Unit = {},
    onCopiedMessage: (String) -> Unit,
    onBack: () -> Unit,
    backLabel: String = stringResource(R.string.action_ask_another),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top navigation & Language Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("solution_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_close_solution),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DisplayLanguage.entries.forEach { lang ->
                    val selected = displayLanguage == lang
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectLanguage(lang) },
                        label = {
                            Text(
                                text = when (lang) {
                                    DisplayLanguage.TAMIL -> "தமிழ்"
                                    DisplayLanguage.ENGLISH -> "English"
                                    DisplayLanguage.BILINGUAL -> "Both"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("lang_toggle_${lang.name.lowercase()}")
                    )
                }
            }
        }

        // 1. Detected Problem Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (displayLanguage == DisplayLanguage.TAMIL) {
                            "உங்கள் சூழலைப் புரிந்துகொண்டோம்"
                        } else {
                            "What We Heard"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (solution.query.isNotBlank()) {
                    Text(
                        text = "“${solution.query}”",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                BilingualBlock(
                    displayLanguage = displayLanguage,
                    tamilText = solution.detectedSummaryTa,
                    englishText = solution.detectedSummaryEn
                )
            }
        }

        // 2. Authentic Thirukkural Couplet Plaque
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.4.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.38f),
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Kural Number + Topic Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "குறள் #${solution.kuralNumber}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = solution.topicTag,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Athikaram Header (respects selected display language)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (displayLanguage == DisplayLanguage.TAMIL || displayLanguage == DisplayLanguage.BILINGUAL) {
                            Text(
                                text = "அதிகாரம் ${solution.athikaramNumber} • ${solution.athikaramTa}",
                                style = TamilBodyTextStyle.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (displayLanguage == DisplayLanguage.ENGLISH || displayLanguage == DisplayLanguage.BILINGUAL) {
                            Text(
                                text = "Chapter ${solution.athikaramNumber}: ${solution.athikaramEn}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
                    )

                    // Traditional 2-line Tamil Couplet (always pure classical Tamil)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = solution.kuralLine1Ta,
                            style = KuralCoupletTextStyle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = solution.kuralLine2Ta,
                            style = KuralCoupletTextStyle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Show phonetic English transliteration only in English or Bilingual mode
                    if (displayLanguage != DisplayLanguage.TAMIL && solution.transliteration.isNotBlank()) {
                        Text(
                            text = solution.transliteration,
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Action Bar: Listen, Save, Copy, Share
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SolutionActionPill(
                icon = if (isSpeaking) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                label = stringResource(if (isSpeaking) R.string.action_stop_reading else R.string.action_read_aloud),
                highlighted = isSpeaking,
                onClick = onToggleReadAloud,
                modifier = Modifier
                    .weight(1f)
                    .testTag("solution_read_aloud_button")
            )
            SolutionActionPill(
                icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                label = stringResource(if (isBookmarked) R.string.action_saved else R.string.action_save),
                highlighted = isBookmarked,
                onClick = onToggleBookmark,
                modifier = Modifier
                    .weight(1f)
                    .testTag("solution_bookmark_button")
            )
            SolutionActionPill(
                icon = Icons.Filled.ContentCopy,
                label = stringResource(R.string.action_copy),
                highlighted = false,
                onClick = {
                    val shareText = solution.formatForShare(displayLanguage)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Thirukkural Guidance", shareText))
                    onCopiedMessage("Copied formatted Kural for WhatsApp / Instagram")
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("solution_copy_button")
            )
            SolutionActionPill(
                icon = Icons.Filled.Share,
                label = stringResource(R.string.action_share),
                highlighted = false,
                onClick = {
                    val shareText = solution.formatForShare(displayLanguage)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Thirukkural #${solution.kuralNumber}")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Thirukkural Guidance"))
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("solution_share_button")
            )
        }

        // Prominent Share as Photo Card CTA
        FilledTonalButton(
            onClick = onSharePhotoCard,
            enabled = !isGeneratingPhotoCard,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("solution_share_photo_card_button")
        ) {
            if (isGeneratingPhotoCard) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Creating Photo Card…",
                    style = MaterialTheme.typography.labelLarge
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (displayLanguage == DisplayLanguage.TAMIL) {
                        "பட அட்டையாகப் பகிர்க • Share as Photo Card"
                    } else {
                        "Share as Photo Card"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // 3. Meaning (பொருள்) Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = when (displayLanguage) {
                            DisplayLanguage.TAMIL -> "பொருள்"
                            DisplayLanguage.ENGLISH -> "Meaning"
                            DisplayLanguage.BILINGUAL -> "பொருள் • Meaning"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                BilingualBlock(
                    displayLanguage = displayLanguage,
                    tamilText = solution.meaningTa,
                    englishText = solution.meaningEn
                )
            }
        }

        // 4. Practical Modern Solution (நடைமுறை தீர்வு) Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(140.dp)
                        .align(Alignment.CenterVertically)
                        .padding(vertical = 14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = when (displayLanguage) {
                                DisplayLanguage.TAMIL -> "நடைமுறை தீர்வு"
                                DisplayLanguage.ENGLISH -> "Practical Modern Solution"
                                DisplayLanguage.BILINGUAL -> "நடைமுறை தீர்வு • Practical Solution"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    BilingualBlock(
                        displayLanguage = displayLanguage,
                        tamilText = solution.practicalSolutionTa,
                        englishText = solution.practicalSolutionEn
                    )
                }
            }
        }

        // 5. Immediate Actionable Step Callout
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.78f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = when (displayLanguage) {
                            DisplayLanguage.TAMIL -> "இப்போது செய்ய வேண்டிய சிறிய செயல்"
                            DisplayLanguage.ENGLISH -> "One Actionable Step Right Now"
                            DisplayLanguage.BILINGUAL -> "செயல்பாடு • Actionable Step"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                BilingualBlock(
                    displayLanguage = displayLanguage,
                    tamilText = solution.actionableStepTa,
                    englishText = solution.actionableStepEn
                )
            }
        }

        // Return / Ask Another CTA
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("ask_another_button")
        ) {
            Text(
                text = backLabel,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun BilingualBlock(
    displayLanguage: DisplayLanguage,
    tamilText: String,
    englishText: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (displayLanguage == DisplayLanguage.TAMIL || displayLanguage == DisplayLanguage.BILINGUAL) {
            if (displayLanguage == DisplayLanguage.BILINGUAL) {
                Text(
                    text = "தமிழ்",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = tamilText,
                style = TamilBodyTextStyle,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (displayLanguage == DisplayLanguage.BILINGUAL && tamilText.isNotBlank() && englishText.isNotBlank()) {
            HorizontalDivider(
                thickness = 0.75.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = "English",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        if (displayLanguage == DisplayLanguage.ENGLISH || displayLanguage == DisplayLanguage.BILINGUAL) {
            Text(
                text = englishText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SolutionActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (highlighted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (highlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}
