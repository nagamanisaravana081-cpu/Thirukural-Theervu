package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.DefaultQuickProblemTopics
import com.example.model.DisplayLanguage
import com.example.model.KuralSolution
import com.example.model.RecentSearchEntry
import com.example.model.VoiceLanguage
import com.example.ui.components.SolutionCardView
import com.example.ui.theme.TamilBodyTextStyle
import com.example.ui.theme.TamilHeadingTextStyle
import com.example.viewmodel.SolverUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuideHomeScreen(
    solverState: SolverUiState,
    inputText: String,
    displayLanguage: DisplayLanguage,
    voiceLanguage: VoiceLanguage,
    recentSearches: List<RecentSearchEntry>,
    savedKuralNumbers: Set<Int>,
    isSpeaking: Boolean,
    isGeneratingPhotoCard: Boolean = false,
    onInputTextChanged: (String) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onCancelVoice: () -> Unit,
    onCancelSolving: () -> Unit,
    onSubmitProblem: (String) -> Unit,
    onOpenRecentSearch: (RecentSearchEntry) -> Unit,
    onClearRecentSearches: () -> Unit,
    onResetToIdle: () -> Unit,
    onSelectDisplayLanguage: (DisplayLanguage) -> Unit,
    onCycleVoiceLanguage: () -> Unit,
    onToggleReadAloud: (KuralSolution) -> Unit,
    onToggleBookmark: (KuralSolution) -> Unit,
    onSharePhotoCard: (KuralSolution) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onStartVoice()
        } else {
            onShowMessage("Microphone permission denied. You can still type your problem below.")
        }
    }

    fun requestMicAndListen() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            onStartVoice()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Intercept system back when viewing a solution, listening, or actively searching
    BackHandler(
        enabled = solverState is SolverUiState.Success ||
            solverState is SolverUiState.Listening ||
            solverState is SolverUiState.Understanding ||
            solverState is SolverUiState.FindingKural
    ) {
        when (solverState) {
            is SolverUiState.Listening -> onCancelVoice()
            is SolverUiState.Understanding, is SolverUiState.FindingKural -> onCancelSolving()
            else -> onResetToIdle()
        }
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
            if (solverState is SolverUiState.Success) {
                SolutionCardView(
                    solution = solverState.solution,
                    displayLanguage = displayLanguage,
                    isBookmarked = savedKuralNumbers.contains(solverState.solution.kuralNumber),
                    isSpeaking = isSpeaking,
                    isGeneratingPhotoCard = isGeneratingPhotoCard,
                    onSelectLanguage = onSelectDisplayLanguage,
                    onToggleReadAloud = { onToggleReadAloud(solverState.solution) },
                    onToggleBookmark = { onToggleBookmark(solverState.solution) },
                    onSharePhotoCard = { onSharePhotoCard(solverState.solution) },
                    onCopiedMessage = onShowMessage,
                    onBack = onResetToIdle
                )
            } else {
                // Header Row: Clean Single-Line App Identity + Settings Button
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
                            text = stringResource(R.string.app_subtitle),
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
                            .testTag("open_settings_button")
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

                // Explanation Language Toggle + Compact Voice Locale Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DisplayLanguage.entries.forEach { lang ->
                            val selected = displayLanguage == lang
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectDisplayLanguage(lang) },
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
                                modifier = Modifier.testTag("home_lang_toggle_${lang.name.lowercase()}")
                            )
                        }
                    }

                    val compactVoiceLabel = when (voiceLanguage) {
                        VoiceLanguage.TAMIL_IN -> "தமிழ்"
                        VoiceLanguage.TANGLISH_EN_IN -> "EN/TA"
                        VoiceLanguage.ENGLISH_US -> "EN"
                    }
                    Surface(
                        onClick = onCycleVoiceLanguage,
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("voice_locale_cycle_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Translate,
                                contentDescription = "Switch voice input language",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = compactVoiceLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Voice-First Hero Centerpiece Card
                VoiceHeroCard(
                    solverState = solverState,
                    voiceLanguage = voiceLanguage,
                    onMicClick = {
                        when (solverState) {
                            is SolverUiState.Listening -> onStopVoice()
                            is SolverUiState.Understanding, is SolverUiState.FindingKural -> Unit
                            else -> requestMicAndListen()
                        }
                    },
                    onCancelListening = onCancelVoice,
                    onCancelSolving = onCancelSolving,
                    onRetryError = { query -> onSubmitProblem(query) }
                )

                // Text Input Fallback
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = onInputTextChanged,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.input_placeholder),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = false,
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputText.isNotBlank()) {
                                        keyboardController?.hide()
                                        onSubmitProblem(inputText)
                                    }
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("problem_text_input")
                        )

                        val isBusy = solverState is SolverUiState.Understanding ||
                            solverState is SolverUiState.FindingKural

                        IconButton(
                            onClick = {
                                keyboardController?.hide()
                                onSubmitProblem(inputText)
                            },
                            enabled = inputText.isNotBlank() && !isBusy,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .background(
                                    if (inputText.isNotBlank() && !isBusy) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .testTag("send_problem_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.cd_send_problem),
                                tint = if (inputText.isNotBlank() && !isBusy) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Quick Problem Chips
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
                        val isBusy = solverState is SolverUiState.Understanding ||
                            solverState is SolverUiState.FindingKural
                        DefaultQuickProblemTopics.forEach { topic ->
                            AssistChip(
                                onClick = {
                                    if (!isBusy) {
                                        onSubmitProblem(topic.promptText)
                                    }
                                },
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
                                modifier = Modifier.testTag("topic_chip_${topic.id}")
                            )
                        }
                    }
                }

                // Recent Searches Section (Last 5 Problems stored locally in DataStore)
                RecentSearchesSection(
                    recentSearches = recentSearches,
                    onOpenRecent = onOpenRecentSearch,
                    onClearRecent = onClearRecentSearches
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun VoiceHeroCard(
    solverState: SolverUiState,
    voiceLanguage: VoiceLanguage,
    onMicClick: () -> Unit,
    onCancelListening: () -> Unit,
    onCancelSolving: () -> Unit,
    onRetryError: (String) -> Unit
) {
    val isListening = solverState is SolverUiState.Listening
    val isProcessing = solverState is SolverUiState.Understanding ||
        solverState is SolverUiState.FindingKural

    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.32f else 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isListening) 850 else 1900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.42f else 0.18f,
        targetValue = 0.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isListening) 850 else 1900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.2.dp,
            color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isListening) 0.5f else 0.25f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 26.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Animated Microphone Centerpiece
                Box(
                    modifier = Modifier.size(136.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer breathing aura ring
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                (if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                                    .copy(alpha = pulseAlpha)
                            )
                    )

                    // Middle halo ring
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                (if (isListening) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer)
                                    .copy(alpha = 0.65f)
                            )
                    )

                    // Core clickable Microphone button (84dp >= 48dp accessible target)
                    Surface(
                        onClick = onMicClick,
                        enabled = !isProcessing,
                        shape = CircleShape,
                        color = if (isListening) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(84.dp)
                            .testTag("voice_mic_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(34.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                                    contentDescription = stringResource(R.string.cd_voice_mic),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }

                // Live Waveform Bars while Listening
                AnimatedVisibility(visible = isListening) {
                    val level = (solverState as? SolverUiState.Listening)?.audioLevel ?: 0.3f
                    LiveWaveformBars(audioLevel = level)
                }

                // Status Labels
                AnimatedContent(targetState = solverState, label = "voiceStatusText") { state ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        when (state) {
                            is SolverUiState.Idle -> {
                                Text(
                                    text = stringResource(R.string.mic_prompt_idle),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${stringResource(R.string.mic_prompt_subtitle)} • ${voiceLanguage.labelEn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }

                            is SolverUiState.Listening -> {
                                Text(
                                    text = stringResource(R.string.status_listening),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = stringResource(R.string.status_listening_ta),
                                    style = TamilBodyTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                if (state.partialTranscript.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.padding(top = 6.dp)
                                    ) {
                                        Text(
                                            text = "“${state.partialTranscript}”",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onCancelListening,
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.testTag("voice_cancel_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cancel")
                                    }
                                    Button(
                                        onClick = onMicClick,
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.testTag("voice_done_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.GraphicEq,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Find Kural")
                                    }
                                }
                            }

                            is SolverUiState.Understanding -> {
                                Text(
                                    text = stringResource(R.string.status_understanding),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = stringResource(R.string.status_understanding_ta),
                                    style = TamilBodyTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                TextButton(
                                    onClick = onCancelSolving,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .testTag("cancel_solving_button")
                                ) {
                                    Text("Cancel")
                                }
                            }

                            is SolverUiState.FindingKural -> {
                                Text(
                                    text = stringResource(R.string.status_finding_kural),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = stringResource(R.string.status_finding_kural_ta),
                                    style = TamilBodyTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                if (state.isTakingLonger) {
                                    Text(
                                        text = "Still connecting… almost there",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                TextButton(
                                    onClick = onCancelSolving,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .testTag("cancel_solving_button")
                                ) {
                                    Text("Cancel")
                                }
                            }

                            is SolverUiState.Error -> {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = state.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            textAlign = TextAlign.Center
                                        )
                                        Button(
                                            onClick = { onRetryError(state.query) },
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.testTag("retry_problem_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Try Again")
                                        }
                                    }
                                }
                            }

                            is SolverUiState.Success -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveWaveformBars(audioLevel: Float) {
    val infinite = rememberInfiniteTransition(label = "waveform")
    val multipliers = listOf(0.45f, 0.8f, 1.0f, 0.65f, 0.9f, 0.7f, 0.45f)

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(32.dp)
    ) {
        multipliers.forEachIndexed { index, baseFactor ->
            val phase by infinite.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 360 + index * 70, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
            val barHeight = (8f + 22f * baseFactor * phase * audioLevel.coerceIn(0.3f, 1f)).dp
            Box(
                modifier = Modifier
                    .width(4.5.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (index % 2 == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
            )
        }
    }
}

@Composable
private fun RecentSearchesSection(
    recentSearches: List<RecentSearchEntry>,
    onOpenRecent: (RecentSearchEntry) -> Unit,
    onClearRecent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recent_searches_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp)
                )
                Text(
                    text = stringResource(R.string.section_recent_searches),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${recentSearches.size}/5",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (recentSearches.isNotEmpty()) {
                TextButton(
                    onClick = onClearRecent,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("clear_recent_searches_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.action_clear),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        if (recentSearches.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = "Your last 5 spoken or typed problems are saved here locally so you can revisit their Thirukkural solutions instantly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recentSearches.forEachIndexed { index, entry ->
                    RecentSearchItemCard(
                        entry = entry,
                        onClick = { onOpenRecent(entry) },
                        modifier = Modifier.testTag("recent_search_item_$index")
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentSearchItemCard(
    entry: RecentSearchEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeLabel = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        .format(Date(entry.timestampMillis))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = entry.query,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "குறள் #${entry.solution.kuralNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "${entry.solution.topicTag} • $timeLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open saved solution",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
