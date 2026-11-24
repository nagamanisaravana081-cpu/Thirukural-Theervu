package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AuthenticKuralCatalog
import com.example.model.DefaultQuickProblemTopics
import com.example.model.DisplayLanguage
import com.example.model.RecentSearchEntry
import com.example.ui.theme.KuralCoupletTextStyle
import com.example.ui.theme.TamilBodyTextStyle
import com.example.ui.theme.TamilHeadingTextStyle
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeDashboardScreen(
    displayName: String,
    displayLanguage: DisplayLanguage,
    savedKuralsCount: Int,
    recentSearches: List<RecentSearchEntry>,
    onOpenGuide: () -> Unit,
    onAskTopic: (String) -> Unit,
    onOpenRecentSearch: (RecentSearchEntry) -> Unit,
    onOpenSaved: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allKurals = AuthenticKuralCatalog.allKurals
    val dayIndex = remember {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        dayOfYear % allKurals.size
    }
    var kuralOffset by rememberSaveable { mutableIntStateOf(0) }
    val dailyKural = allKurals[(dayIndex + kuralOffset) % allKurals.size]

    val firstName = remember(displayName) {
        displayName.trim().substringBefore(" ").ifBlank { "Friend" }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Single-Line Header + Settings Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_title_tamil),
                        style = TamilHeadingTextStyle.copy(
                            fontSize = 20.sp,
                            lineHeight = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (displayLanguage == DisplayLanguage.TAMIL) {
                            "வணக்கம், $firstName"
                        } else {
                            "Vanakkam, $firstName • Daily Wisdom"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    onClick = onOpenSettings,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("home_open_settings_button")
                ) {
                    Box(
                        modifier = Modifier.size(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Open Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            }

            // 1. Voice-First Quick Launch Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenGuide)
                    .testTag("home_voice_guide_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (displayLanguage == DisplayLanguage.TAMIL) {
                                "உங்கள் பிரச்சனைக்குத் தீர்வு"
                            } else {
                                "Speak Your Problem"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = if (displayLanguage == DisplayLanguage.TAMIL) {
                                "குரல் அல்லது எழுத்தில் கேட்டு உடனடி திருக்குறள் வழிகாட்டுதல் பெறுங்கள்."
                            } else {
                                "Ask in Tamil, English, or Tanglish for practical Thirukkural guidance."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Voice Guide",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 2. Daily Authentic Thirukkural Plaque
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_daily_kural_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.3.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = if (displayLanguage == DisplayLanguage.TAMIL) {
                                            "இன்றைய குறள் #${dailyKural.number}"
                                        } else {
                                            "Daily Kural #${dailyKural.number}"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { kuralOffset += 1 },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("home_next_daily_kural_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Show another Kural",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            if (displayLanguage == DisplayLanguage.TAMIL || displayLanguage == DisplayLanguage.BILINGUAL) {
                                Text(
                                    text = "அதிகாரம் ${dailyKural.athikaramNumber} • ${dailyKural.athikaramTa}",
                                    style = TamilBodyTextStyle.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (displayLanguage == DisplayLanguage.ENGLISH || displayLanguage == DisplayLanguage.BILINGUAL) {
                                Text(
                                    text = "Chapter ${dailyKural.athikaramNumber}: ${dailyKural.athikaramEn}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = dailyKural.line1Ta,
                                style = KuralCoupletTextStyle,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = dailyKural.line2Ta,
                                style = KuralCoupletTextStyle,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        if (displayLanguage != DisplayLanguage.TAMIL) {
                            Text(
                                text = dailyKural.transliteration,
                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                onAskTopic("Explain Kural #${dailyKural.number} (${dailyKural.athikaramEn}) and how to apply it to daily life.")
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("home_explore_daily_kural_button")
                        ) {
                            Text(
                                text = if (displayLanguage == DisplayLanguage.TAMIL) {
                                    "இந்தக் குறளின் முழுப் பொருள் அறிக"
                                } else {
                                    "Explore Full Meaning & Advice"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // 3. Quick Shortcuts Row (Saved + Shop)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeQuickTile(
                    icon = Icons.Filled.Bookmark,
                    title = if (displayLanguage == DisplayLanguage.TAMIL) "சேமித்தவை" else "Saved",
                    subtitle = "$savedKuralsCount Kurals",
                    onClick = onOpenSaved,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_shortcut_saved")
                )
                HomeQuickTile(
                    icon = Icons.Filled.ShoppingBag,
                    title = if (displayLanguage == DisplayLanguage.TAMIL) "அங்காடி" else "Shop",
                    subtitle = "விரைவில்…",
                    onClick = onOpenShop,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_shortcut_shop")
                )
            }

            // 4. Quick Life Challenges
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.section_quick_topics),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DefaultQuickProblemTopics.forEach { topic ->
                        AssistChip(
                            onClick = { onAskTopic(topic.promptText) },
                            label = {
                                Text(
                                    text = when (displayLanguage) {
                                        DisplayLanguage.TAMIL -> topic.labelTa
                                        DisplayLanguage.ENGLISH -> topic.labelEn
                                        DisplayLanguage.BILINGUAL -> "${topic.labelEn} • ${topic.labelTa}"
                                    },
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("home_topic_chip_${topic.id}")
                        )
                    }
                }
            }

            // 5. Recent Guidance Preview (if any)
            if (recentSearches.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.section_recent_searches),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    recentSearches.take(2).forEachIndexed { idx, entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenRecentSearch(entry) }
                                .testTag("home_recent_item_$idx"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = entry.query,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "குறள் #${entry.solution.kuralNumber} • ${entry.solution.athikaramTa}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open solution",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HomeQuickTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
