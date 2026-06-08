package com.example.worknet.data.repository

import android.net.Uri
import com.example.worknet.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    private val usersCollection = db.collection("users")

    // ---------------------------------------------------------
    // GESTIONE SESSIONE
    // ---------------------------------------------------------

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun getCurrentUser(): User? {
        val userId = getCurrentUserId() ?: return null
        return getUserById(userId)
    }

    fun logout() {
        auth.signOut()
    }

    // ---------------------------------------------------------
    // CREAZIONE UTENTE
    // ---------------------------------------------------------
    suspend fun createUser(user: User) {
        usersCollection.document(user.id).set(user).await()
    }

    suspend fun signUp(email: String, password: String, userData: User): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("ID utente non trovato")

            val userWithId = userData.copy(id = userId)
            usersCollection.document(userId).set(userWithId).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------------------------------------------------------
    // LETTURA UTENTE
    // ---------------------------------------------------------
    suspend fun getUserById(userId: String): User? {
        val snapshot = usersCollection.document(userId).get().await()
        return if (snapshot.exists()) {
            snapshot.toObject(User::class.java)
        } else {
            val newUser = User(
                id = userId,
                name = "Utente anonimo",
                email = "",
                birthDate = "",
                description = "",
                education = "",
                residence = "",
                photoUrl = null,
                cvUrl = null,
                savedPlaces = emptyList()
            )
            usersCollection.document(userId).set(newUser).await()
            newUser
        }
    }

    // ---------------------------------------------------------
    // AGGIORNAMENTO UTENTE
    // ---------------------------------------------------------
    suspend fun updateUser(user: User) {
        usersCollection.document(user.id).set(user).await()
    }

    // ---------------------------------------------------------
    // GESTIONE PREFERITI
    // ---------------------------------------------------------
    suspend fun addFavoritePlace(userId: String, placeId: String) {
        usersCollection.document(userId)
            .update("savedPlaces", com.google.firebase.firestore.FieldValue.arrayUnion(placeId))
            .await()
    }

    suspend fun removeFavoritePlace(userId: String, placeId: String) {
        usersCollection.document(userId)
            .update("savedPlaces", com.google.firebase.firestore.FieldValue.arrayRemove(placeId))
            .await()
    }

    // ---------------------------------------------------------
    // UPLOAD FILE (STORAGE)
    // ---------------------------------------------------------
    suspend fun uploadProfilePhoto(uri: Uri, userId: String): String {
        val ref = storage.reference.child("users/$userId/profile.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadCv(uri: Uri, userId: String): String {
        val ref = storage.reference.child("users/$userId/cv.pdf")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
