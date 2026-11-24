package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.LocalStorageRepository
import com.example.firebase.configureAppCheck
import com.example.firebase.signOutFromGoogle
import com.example.model.VoiceLanguage
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.GuideHomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SavedGuidanceScreen
import com.example.ui.screens.ShoppingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.ThirukkuralTheme
import com.example.viewmodel.KuralGuideViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

private enum class MainTab(
    val routeId: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    GUIDE("guide_home", R.string.nav_guide, Icons.Filled.Mic, Icons.Outlined.MicNone),
    SAVED("saved_guidance", R.string.nav_saved, Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
    SHOP("shopping", R.string.nav_shop, Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
    PROFILE("profile", R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.PersonOutline)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent BEFORE any UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            )
        )

        setContent {
            val localStorage = remember { LocalStorageRepository(applicationContext) }
            val isDarkMode by localStorage.isDarkModeFlow.collectAsStateWithLifecycle(
                initialValue = localStorage.getInitialDarkModeSync()
            )

            DisposableEffect(isDarkMode) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkMode) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDarkMode) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            ThirukkuralTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ThirukkuralAppRoot(isDarkMode = isDarkMode)
                }
            }
        }
    }
}

@Composable
private fun ThirukkuralAppRoot(isDarkMode: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val localStorage = remember(context) { LocalStorageRepository(context) }

    var showSplash by rememberSaveable { mutableStateOf(true) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    val isGuestModeState by localStorage.isGuestModeFlow.collectAsStateWithLifecycle(
        initialValue = if (localStorage.getInitialGuestModeSync()) true else null
    )
    val isGuestMode = isGuestModeState == true

    // Listen to Firebase Auth state changes across sign-in / sign-out / account switch
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    when {
        showSplash || (currentUser == null && isGuestModeState == null) -> {
            SplashScreen(
                isDarkMode = isDarkMode,
                onFinished = { showSplash = false }
            )
        }

        currentUser == null && !isGuestMode -> {
            AuthScreen(
                onGoogleAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                    scope.launch { localStorage.setGuestMode(false) }
                },
                onContinueAsGuest = {
                    scope.launch { localStorage.setGuestMode(true) }
                }
            )
        }

        else -> {
            val uid = currentUser?.uid
            val sessionKey = if (!uid.isNullOrBlank()) "google_$uid" else "guest_local"

            // Scope ViewModels and Firestore listeners cleanly per authenticated UID or Guest session
            key(sessionKey) {
                MainWorkspaceShell(
                    userId = uid,
                    userDisplayName = currentUser?.displayName,
                    userEmail = currentUser?.email,
                    sessionKey = sessionKey,
                    onGoogleSignInUpgrade = {
                        currentUser = Firebase.auth.currentUser
                        scope.launch { localStorage.setGuestMode(false) }
                    },
                    onSignOutOrExitGuest = {
                        if (!uid.isNullOrBlank()) {
                            signOutFromGoogle(
                                credentialManager = credentialManager,
                                scope = scope,
                                onSignOutComplete = {
                                    currentUser = null
                                    scope.launch { localStorage.setGuestMode(false) }
                                }
                            )
                        } else {
                            scope.launch { localStorage.setGuestMode(false) }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MainWorkspaceShell(
    userId: String?,
    userDisplayName: String?,
    userEmail: String?,
    sessionKey: String,
    onGoogleSignInUpgrade: () -> Unit,
    onSignOutOrExitGuest: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: KuralGuideViewModel = viewModel(
        key = sessionKey,
        factory = KuralGuideViewModel.Factory(
            context = context,
            userId = userId,
            userDisplayName = userDisplayName,
            userEmail = userEmail
        )
    )

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.GUIDE) }
    var showSettingsSheet by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val solverState by viewModel.solverState.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val displayLanguage by viewModel.displayLanguage.collectAsStateWithLifecycle()
    val voiceLanguage by viewModel.voiceLanguage.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val profilePhotoPath by viewModel.profilePhotoPath.collectAsStateWithLifecycle()
    val isGeneratingPhotoCard by viewModel.isGeneratingPhotoCard.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val savedKuralsState by viewModel.savedKuralsState.collectAsStateWithLifecycle()
    val savedKuralNumbers by viewModel.savedKuralNumbers.collectAsStateWithLifecycle()
    val savedKuralsCount by viewModel.savedKuralsCount.collectAsStateWithLifecycle()
    val selectedSavedSolution by viewModel.selectedSavedSolution.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val guestName by viewModel.guestDisplayName.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        MainTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    viewModel.stopReadAloud()
                                    selectedTab = tab
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.routeId}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        MainTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    viewModel.stopReadAloud()
                                    selectedTab = tab
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("rail_tab_${tab.routeId}")
                            )
                        }
                    }
                }

                when (selectedTab) {
                    MainTab.GUIDE -> {
                        GuideHomeScreen(
                            solverState = solverState,
                            inputText = inputText,
                            displayLanguage = displayLanguage,
                            voiceLanguage = voiceLanguage,
                            recentSearches = recentSearches,
                            savedKuralNumbers = savedKuralNumbers,
                            isSpeaking = isSpeaking,
                            isGeneratingPhotoCard = isGeneratingPhotoCard,
                            onInputTextChanged = viewModel::onInputTextChanged,
                            onStartVoice = viewModel::startVoiceListening,
                            onStopVoice = viewModel::stopVoiceListening,
                            onCancelVoice = viewModel::cancelVoiceListening,
                            onCancelSolving = viewModel::cancelSolving,
                            onSubmitProblem = viewModel::solveProblem,
                            onOpenRecentSearch = viewModel::openRecentSearch,
                            onClearRecentSearches = viewModel::clearRecentSearches,
                            onResetToIdle = viewModel::resetToIdle,
                            onSelectDisplayLanguage = viewModel::setDisplayLanguage,
                            onCycleVoiceLanguage = {
                                val all = VoiceLanguage.entries
                                val next = all[(all.indexOf(voiceLanguage) + 1) % all.size]
                                viewModel.setVoiceLanguage(next)
                                viewModel.showSnackbar("Voice input set to ${next.labelEn}")
                            },
                            onToggleReadAloud = viewModel::toggleReadAloud,
                            onToggleBookmark = viewModel::toggleBookmark,
                            onSharePhotoCard = { solution ->
                                viewModel.shareSolutionAsPhotoCard(context, solution)
                            },
                            onOpenSettings = { showSettingsSheet = true },
                            onShowMessage = viewModel::showSnackbar,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    MainTab.SAVED -> {
                        SavedGuidanceScreen(
                            savedState = savedKuralsState,
                            selectedSolution = selectedSavedSolution,
                            displayLanguage = displayLanguage,
                            isSpeaking = isSpeaking,
                            isGeneratingPhotoCard = isGeneratingPhotoCard,
                            isCloudSynced = viewModel.isSignedInWithGoogle,
                            onSelectSolution = viewModel::openSavedSolution,
                            onCloseSolution = viewModel::closeSavedSolution,
                            onSelectLanguage = viewModel::setDisplayLanguage,
                            onToggleReadAloud = viewModel::toggleReadAloud,
                            onToggleBookmark = viewModel::toggleBookmark,
                            onSharePhotoCard = { solution ->
                                viewModel.shareSolutionAsPhotoCard(context, solution)
                            },
                            onShowMessage = viewModel::showSnackbar,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    MainTab.SHOP -> {
                        ShoppingScreen(
                            displayLanguage = displayLanguage,
                            onOpenSettings = { showSettingsSheet = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    MainTab.PROFILE -> {
                        ProfileScreen(
                            isSignedInWithGoogle = viewModel.isSignedInWithGoogle,
                            displayName = userDisplayName?.takeIf { it.isNotBlank() } ?: guestName,
                            email = userEmail,
                            displayLanguage = displayLanguage,
                            voiceLanguage = voiceLanguage,
                            isDarkMode = isDarkMode,
                            profilePhotoPath = profilePhotoPath,
                            savedKuralsCount = savedKuralsCount,
                            recentSearchesCount = recentSearches.size,
                            onSelectDisplayLanguage = viewModel::setDisplayLanguage,
                            onSelectVoiceLanguage = viewModel::setVoiceLanguage,
                            onToggleDarkMode = viewModel::setDarkMode,
                            onPhotoSelected = { uri -> viewModel.updateProfilePhoto(context, uri) },
                            onRemovePhoto = viewModel::removeProfilePhoto,
                            onOpenSettings = { showSettingsSheet = true },
                            onGoogleSignInSuccess = onGoogleSignInUpgrade,
                            onSignOut = onSignOutOrExitGuest,
                            onSwitchAccount = onSignOutOrExitGuest,
                            onShowMessage = viewModel::showSnackbar,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showSettingsSheet) {
        SettingsBottomSheet(
            isDarkMode = isDarkMode,
            displayLanguage = displayLanguage,
            voiceLanguage = voiceLanguage,
            onToggleDarkMode = viewModel::setDarkMode,
            onSelectDisplayLanguage = viewModel::setDisplayLanguage,
            onSelectVoiceLanguage = viewModel::setVoiceLanguage,
            onDismiss = { showSettingsSheet = false },
            onShowMessage = viewModel::showSnackbar
        )
    }
}
