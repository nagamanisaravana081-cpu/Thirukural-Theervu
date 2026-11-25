package com.example.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreKuralRepository
import com.example.data.KuralAiRepository
import com.example.data.LocalStorageRepository
import com.example.model.DisplayLanguage
import com.example.model.KuralSolution
import com.example.model.RecentSearchEntry
import com.example.model.VoiceLanguage
import com.example.util.KuralPhotoCardGenerator
import com.example.util.ProfilePhotoManager
import com.example.voice.VoiceAndSpeechController
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "KuralGuideViewModel"

sealed interface SolverUiState {
    data object Idle : SolverUiState
    data class Listening(
        val partialTranscript: String = "",
        val audioLevel: Float = 0.25f
    ) : SolverUiState
    data class Understanding(val query: String) : SolverUiState
    data class FindingKural(
        val query: String,
        val isTakingLonger: Boolean = false
    ) : SolverUiState
    data class Success(val solution: KuralSolution) : SolverUiState
    data class Error(val query: String, val message: String) : SolverUiState
}

sealed interface SavedKuralsUiState {
    data object Loading : SavedKuralsUiState
    data class Success(val items: List<KuralSolution>) : SavedKuralsUiState
    data class Error(val message: String) : SavedKuralsUiState
}

class KuralGuideViewModel(
    private val userId: String?,
    private val userDisplayName: String?,
    private val userEmail: String?,
    private val localStorage: LocalStorageRepository,
    private val firestoreRepository: FirestoreKuralRepository?,
    private val aiRepository: KuralAiRepository,
    private val voiceController: VoiceAndSpeechController
) : ViewModel() {

    val isSignedInWithGoogle: Boolean = !userId.isNullOrBlank() && firestoreRepository != null

    private val _solverState = MutableStateFlow<SolverUiState>(SolverUiState.Idle)
    val solverState: StateFlow<SolverUiState> = _solverState.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _selectedSavedSolution = MutableStateFlow<KuralSolution?>(null)
    val selectedSavedSolution: StateFlow<KuralSolution?> = _selectedSavedSolution.asStateFlow()

    private val _isGeneratingPhotoCard = MutableStateFlow(false)
    val isGeneratingPhotoCard: StateFlow<Boolean> = _isGeneratingPhotoCard.asStateFlow()

    val isSpeaking: StateFlow<Boolean> = voiceController.isSpeaking

    val displayLanguage: StateFlow<DisplayLanguage> = localStorage.displayLanguageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DisplayLanguage.BILINGUAL)

    val voiceLanguage: StateFlow<VoiceLanguage> = localStorage.voiceLanguageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), VoiceLanguage.TANGLISH_EN_IN)

    val isDarkMode: StateFlow<Boolean> = localStorage.isDarkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    private val photoOwnerKey: String = ProfilePhotoManager.ownerKeyForUser(userId)

    val profilePhotoPath: StateFlow<String?> = localStorage.profilePhotoPathFlow(photoOwnerKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val guestDisplayName: StateFlow<String> = localStorage.guestDisplayNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), "Guest Seeker")

    // Last 5 queried problems stored persistently on-device
    val recentSearches: StateFlow<List<RecentSearchEntry>> = localStorage.recentSearchesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Saved Kurals: Cloud Firestore when signed in with Google; local DataStore in Guest mode
    val savedKuralsState: StateFlow<SavedKuralsUiState> = if (isSignedInWithGoogle && firestoreRepository != null) {
        firestoreRepository.observeSavedKurals()
            .map<List<KuralSolution>, SavedKuralsUiState> { SavedKuralsUiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                emit(SavedKuralsUiState.Error(rootError.message ?: "Unable to sync saved guidance."))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SavedKuralsUiState.Loading)
    } else {
        localStorage.guestSavedKuralsFlow
            .map<List<KuralSolution>, SavedKuralsUiState> { SavedKuralsUiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SavedKuralsUiState.Loading)
    }

    val savedKuralNumbers: StateFlow<Set<Int>> = savedKuralsState
        .map { state ->
            when (state) {
                is SavedKuralsUiState.Success -> state.items.map { it.kuralNumber }.toSet()
                else -> emptySet()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptySet())

    val savedKuralsCount: StateFlow<Int> = savedKuralNumbers
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    private var solveJob: Job? = null

    init {
        if (isSignedInWithGoogle && firestoreRepository != null) {
            syncSignedInProfile()
        }
    }

    private fun syncSignedInProfile() {
        val repo = firestoreRepository ?: return
        viewModelScope.launch {
            repo.ensureUserProfile(
                displayName = userDisplayName ?: "Thirukkural Reader",
                email = userEmail,
                displayLanguage = displayLanguage.value,
                voiceLanguage = voiceLanguage.value
            )
        }
        viewModelScope.launch {
            repo.observeUserProfile()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w(TAG, "Profile sync warning: ${error.message}")
                }
                .collect { doc ->
                    if (doc != null) {
                        val remoteDisplay = DisplayLanguage.fromRaw(doc.displayLanguage)
                        val remoteVoice = VoiceLanguage.fromRaw(doc.voiceLanguage)
                        if (remoteDisplay != displayLanguage.value) {
                            localStorage.setDisplayLanguage(remoteDisplay)
                        }
                        if (remoteVoice != voiceLanguage.value) {
                            localStorage.setVoiceLanguage(remoteVoice)
                        }
                    }
                }
        }
    }

    fun onInputTextChanged(value: String) {
        _inputText.value = value
    }

    fun startVoiceListening() {
        voiceController.stopSpeaking()
        val currentVoiceLocale = voiceLanguage.value
        _solverState.value = SolverUiState.Listening()

        voiceController.startListening(
            voiceLanguage = currentVoiceLocale,
            onReady = {
                _solverState.value = SolverUiState.Listening(partialTranscript = "", audioLevel = 0.25f)
            },
            onPartialResult = { partial ->
                val current = _solverState.value
                val level = if (current is SolverUiState.Listening) current.audioLevel else 0.4f
                _solverState.value = SolverUiState.Listening(partialTranscript = partial, audioLevel = level)
            },
            onRmsChanged = { level ->
                val current = _solverState.value
                if (current is SolverUiState.Listening) {
                    _solverState.value = current.copy(audioLevel = level)
                }
            },
            onFinalResult = { spokenText ->
                _inputText.value = spokenText
                solveProblem(spokenText)
            },
            onError = { errorMsg ->
                _solverState.value = SolverUiState.Idle
                _snackbarMessage.value = errorMsg
            }
        )
    }

    fun stopVoiceListening() {
        val current = _solverState.value
        if (current is SolverUiState.Listening) {
            voiceController.stopListening()
            if (current.partialTranscript.isNotBlank()) {
                val spoken = current.partialTranscript.trim()
                _inputText.value = spoken
                solveProblem(spoken)
            } else {
                _solverState.value = SolverUiState.Idle
            }
        }
    }

    fun cancelVoiceListening() {
        voiceController.cancelListening()
        if (_solverState.value is SolverUiState.Listening) {
            _solverState.value = SolverUiState.Idle
        }
    }

    fun solveProblem(problemText: String) {
        val cleaned = problemText.trim()
        if (cleaned.isBlank()) {
            _snackbarMessage.value = "Please speak or type your problem first."
            return
        }

        voiceController.stopSpeaking()
        solveJob?.cancel()
        solveJob = viewModelScope.launch {
            _solverState.value = SolverUiState.Understanding(cleaned)

            // Transition smoothly to "Finding the right Kural..." and show reassuring hint if slow
            val stageTransitionJob = launch {
                delay(900L)
                if (_solverState.value is SolverUiState.Understanding) {
                    _solverState.value = SolverUiState.FindingKural(cleaned, isTakingLonger = false)
                }
                delay(6500L)
                if (_solverState.value is SolverUiState.FindingKural) {
                    _solverState.value = SolverUiState.FindingKural(cleaned, isTakingLonger = true)
                }
            }

            try {
                val result = aiRepository.findKuralSolution(cleaned)
                result.fold(
                    onSuccess = { solution ->
                        _solverState.value = SolverUiState.Success(solution)
                        _inputText.value = ""
                        localStorage.addRecentSearch(cleaned, solution)
                    },
                    onFailure = { error ->
                        _solverState.value = SolverUiState.Error(
                            query = cleaned,
                            message = error.message ?: "Unable to find a Kural right now. Please try again."
                        )
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                Log.e(TAG, "Unexpected failure while solving problem", t)
                _solverState.value = SolverUiState.Error(
                    query = cleaned,
                    message = t.localizedMessage ?: "Something went wrong while finding your Kural. Please try again."
                )
            } finally {
                stageTransitionJob.cancel()
            }
        }
    }

    fun cancelSolving() {
        solveJob?.cancel()
        solveJob = null
        voiceController.stopSpeaking()
        if (_solverState.value is SolverUiState.Understanding ||
            _solverState.value is SolverUiState.FindingKural
        ) {
            _solverState.value = SolverUiState.Idle
        }
    }

    fun openRecentSearch(entry: RecentSearchEntry) {
        voiceController.stopSpeaking()
        solveJob?.cancel()
        _solverState.value = SolverUiState.Success(entry.solution)
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            localStorage.clearRecentSearches()
            _snackbarMessage.value = "Recent searches cleared"
        }
    }

    fun resetToIdle() {
        voiceController.stopSpeaking()
        solveJob?.cancel()
        _solverState.value = SolverUiState.Idle
    }

    fun openSavedSolution(solution: KuralSolution) {
        voiceController.stopSpeaking()
        _selectedSavedSolution.value = solution
    }

    fun closeSavedSolution() {
        voiceController.stopSpeaking()
        _selectedSavedSolution.value = null
    }

    fun toggleBookmark(solution: KuralSolution) {
        viewModelScope.launch {
            val isCurrentlySaved = savedKuralNumbers.value.contains(solution.kuralNumber)
            if (isSignedInWithGoogle && firestoreRepository != null) {
                if (isCurrentlySaved) {
                    val res = firestoreRepository.removeSavedKural(solution.kuralNumber)
                    if (res.isSuccess) {
                        _snackbarMessage.value = "Removed Kural #${solution.kuralNumber} from Saved Guidance"
                    } else {
                        _snackbarMessage.value = "Could not remove bookmark. Please try again."
                    }
                } else {
                    val res = firestoreRepository.saveKural(solution)
                    if (res.isSuccess) {
                        _snackbarMessage.value = "Saved Kural #${solution.kuralNumber} to your cloud library"
                    } else {
                        _snackbarMessage.value = "Could not save bookmark. Please check your connection."
                    }
                }
            } else {
                if (isCurrentlySaved) {
                    localStorage.removeGuestKural(solution.kuralNumber)
                    _snackbarMessage.value = "Removed Kural #${solution.kuralNumber} from Saved Guidance"
                } else {
                    localStorage.saveGuestKural(solution)
                    _snackbarMessage.value = "Saved Kural #${solution.kuralNumber} on this device"
                }
            }
        }
    }

    fun toggleReadAloud(solution: KuralSolution) {
        if (voiceController.isSpeaking.value) {
            voiceController.stopSpeaking()
        } else {
            val started = voiceController.speakSolution(solution, displayLanguage.value)
            if (!started) {
                _snackbarMessage.value = "Audio read-aloud engine is still initializing. Please try again."
            }
        }
    }

    fun stopReadAloud() {
        voiceController.stopSpeaking()
    }

    fun setDisplayLanguage(language: DisplayLanguage) {
        viewModelScope.launch {
            localStorage.setDisplayLanguage(language)
            if (isSignedInWithGoogle && firestoreRepository != null) {
                firestoreRepository.updatePreferences(language, voiceLanguage.value)
            }
        }
    }

    fun setVoiceLanguage(language: VoiceLanguage) {
        viewModelScope.launch {
            localStorage.setVoiceLanguage(language)
            if (isSignedInWithGoogle && firestoreRepository != null) {
                firestoreRepository.updatePreferences(displayLanguage.value, language)
            }
        }
    }

    fun updateGuestName(name: String) {
        viewModelScope.launch {
            localStorage.setGuestDisplayName(name)
            _snackbarMessage.value = "Display name updated"
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            localStorage.setDarkMode(enabled)
        }
    }

    fun updateProfilePhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            val result = ProfilePhotoManager.saveProfilePhotoFromUri(context, photoOwnerKey, uri)
            result.fold(
                onSuccess = { savedPath ->
                    localStorage.setProfilePhotoPath(photoOwnerKey, savedPath)
                    _snackbarMessage.value = "Profile photo updated"
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Could not update profile photo."
                }
            )
        }
    }

    fun removeProfilePhoto() {
        viewModelScope.launch {
            val existing = profilePhotoPath.value
            ProfilePhotoManager.deleteProfilePhoto(existing)
            localStorage.clearProfilePhotoPath(photoOwnerKey)
            _snackbarMessage.value = "Profile photo removed"
        }
    }

    fun shareSolutionAsPhotoCard(context: Context, solution: KuralSolution) {
        if (_isGeneratingPhotoCard.value) return
        viewModelScope.launch {
            _isGeneratingPhotoCard.value = true
            try {
                val res = KuralPhotoCardGenerator.generateAndSharePhotoCard(
                    context = context,
                    solution = solution,
                    language = displayLanguage.value,
                    isDarkMode = isDarkMode.value
                )
                if (res.isFailure) {
                    _snackbarMessage.value = "Unable to generate photo card. Please try again."
                }
            } finally {
                _isGeneratingPhotoCard.value = false
            }
        }
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        voiceController.release()
    }

    class Factory(
        private val context: Context,
        private val userId: String?,
        private val userDisplayName: String?,
        private val userEmail: String?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appCtx = context.applicationContext
            val localRepo = LocalStorageRepository(appCtx)
            val firestoreRepo = if (!userId.isNullOrBlank()) FirestoreKuralRepository(appCtx) else null
            return KuralGuideViewModel(
                userId = userId,
                userDisplayName = userDisplayName,
                userEmail = userEmail,
                localStorage = localRepo,
                firestoreRepository = firestoreRepo,
                aiRepository = KuralAiRepository(),
                voiceController = VoiceAndSpeechController(appCtx)
            ) as T
        }
    }
}
