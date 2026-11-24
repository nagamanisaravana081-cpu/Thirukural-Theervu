package com.example.data

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.DisplayLanguage
import com.example.model.KuralSolution
import com.example.model.SavedKuralDoc
import com.example.model.UserProfileDoc
import com.example.model.VoiceLanguage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreKuralRepository(private val db: FirebaseFirestore) {

    // Always bind to the named provisioned database ID from firebase_applet_config.xml
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        }
        return uid
    }

    suspend fun ensureUserProfile(
        displayName: String,
        email: String?,
        displayLanguage: DisplayLanguage,
        voiceLanguage: VoiceLanguage
    ): Result<Unit> {
        var docPath = "users/unauthenticated"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val snapshot = docRef.get().await()
            val safeName = displayName.trim().ifBlank { "Thirukkural Reader" }.take(100)
            val safeEmail = email?.trim()?.take(200)

            if (!snapshot.exists()) {
                val payload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "displayName" to safeName,
                    "displayLanguage" to displayLanguage.name,
                    "voiceLanguage" to voiceLanguage.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (!safeEmail.isNullOrBlank()) {
                    payload["email"] = safeEmail
                }
                docRef.set(payload).await()
            } else {
                val updatePayload = mutableMapOf<String, Any>(
                    "displayName" to safeName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (!safeEmail.isNullOrBlank()) {
                    updatePayload["email"] = safeEmail
                }
                docRef.update(updatePayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    fun observeUserProfile(): Flow<UserProfileDoc?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users")
                .document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfileDoc::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun updatePreferences(
        displayLanguage: DisplayLanguage,
        voiceLanguage: VoiceLanguage
    ): Result<Unit> {
        var docPath = "users/unauthenticated"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                docRef.update(
                    mapOf(
                        "displayLanguage" to displayLanguage.name,
                        "voiceLanguage" to voiceLanguage.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val user = auth.currentUser
                val safeName = user?.displayName?.trim()?.takeIf { it.isNotBlank() }?.take(100)
                    ?: "Thirukkural Reader"
                val payload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "displayName" to safeName,
                    "displayLanguage" to displayLanguage.name,
                    "voiceLanguage" to voiceLanguage.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                user?.email?.trim()?.takeIf { it.isNotBlank() }?.take(200)?.let {
                    payload["email"] = it
                }
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    fun observeSavedKurals(): Flow<List<KuralSolution>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/savedKurals"
        emitAll(
            db.collection("users")
                .document(uid)
                .collection("savedKurals")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SavedKuralDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).map { it.toKuralSolution() }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveKural(solution: KuralSolution): Result<Unit> {
        var docPath = "users/unauthenticated/savedKurals"
        return try {
            val uid = requireUserId()
            val safeKuralNum = solution.kuralNumber.coerceIn(1, 1330)
            val safeAthikaramNum = solution.athikaramNumber.coerceIn(1, 133)
            val docId = "kural_$safeKuralNum"
            val docRef = db.collection("users")
                .document(uid)
                .collection("savedKurals")
                .document(docId)
            docPath = docRef.path

            val existing = docRef.get().await()
            val safeQuery = solution.query.trim().ifBlank { "Life guidance" }.take(500)
            val safeSummaryEn = solution.detectedSummaryEn.trim().ifBlank { "Seeking clarity and guidance." }.take(600)
            val safeSummaryTa = solution.detectedSummaryTa.trim().ifBlank { "வாழ்வின் தெளிவைத் தேடுகிறீர்கள்." }.take(800)
            val safeLine1 = solution.kuralLine1Ta.trim().ifBlank { "அகர முதல எழுத்தெல்லாம் ஆதி" }.take(200)
            val safeLine2 = solution.kuralLine2Ta.trim().ifBlank { "பகவன் முதற்றே உலகு." }.take(200)
            val safeAthikaramTa = solution.athikaramTa.trim().ifBlank { "திருக்குறள்" }.take(150)
            val safeAthikaramEn = solution.athikaramEn.trim().ifBlank { "Thirukkural" }.take(150)
            val safeTranslit = solution.transliteration.trim().ifBlank { "Thirukkural couplet" }.take(300)
            val safeMeaningTa = solution.meaningTa.trim().ifBlank { safeLine1 }.take(1000)
            val safeMeaningEn = solution.meaningEn.trim().ifBlank { safeSummaryEn }.take(1000)
            val safePracticalTa = solution.practicalSolutionTa.trim().ifBlank { safeMeaningTa }.take(1500)
            val safePracticalEn = solution.practicalSolutionEn.trim().ifBlank { safeMeaningEn }.take(1500)
            val safeActionTa = solution.actionableStepTa.trim().ifBlank { safePracticalTa }.take(600)
            val safeActionEn = solution.actionableStepEn.trim().ifBlank { safePracticalEn }.take(600)
            val safeTopic = solution.topicTag.trim().ifBlank { "Guidance" }.take(60)

            if (existing.exists()) {
                // Partial update preserving immutable createdAt, userId, and kuralNumber
                docRef.update(
                    mapOf(
                        "query" to safeQuery,
                        "detectedSummaryEn" to safeSummaryEn,
                        "detectedSummaryTa" to safeSummaryTa,
                        "practicalSolutionTa" to safePracticalTa,
                        "practicalSolutionEn" to safePracticalEn,
                        "actionableStepTa" to safeActionTa,
                        "actionableStepEn" to safeActionEn,
                        "topicTag" to safeTopic,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                // Create payload matching firestore.rules allow create (no @DocumentId `id` field)
                val payload = mapOf(
                    "userId" to uid,
                    "query" to safeQuery,
                    "detectedSummaryEn" to safeSummaryEn,
                    "detectedSummaryTa" to safeSummaryTa,
                    "kuralNumber" to safeKuralNum,
                    "kuralLine1Ta" to safeLine1,
                    "kuralLine2Ta" to safeLine2,
                    "athikaramNumber" to safeAthikaramNum,
                    "athikaramTa" to safeAthikaramTa,
                    "athikaramEn" to safeAthikaramEn,
                    "transliteration" to safeTranslit,
                    "meaningTa" to safeMeaningTa,
                    "meaningEn" to safeMeaningEn,
                    "practicalSolutionTa" to safePracticalTa,
                    "practicalSolutionEn" to safePracticalEn,
                    "actionableStepTa" to safeActionTa,
                    "actionableStepEn" to safeActionEn,
                    "topicTag" to safeTopic,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun removeSavedKural(kuralNumber: Int): Result<Unit> {
        var docPath = "users/unauthenticated/savedKurals"
        return try {
            val uid = requireUserId()
            val docId = "kural_${kuralNumber.coerceIn(1, 1330)}"
            val docRef = db.collection("users")
                .document(uid)
                .collection("savedKurals")
                .document(docId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}
