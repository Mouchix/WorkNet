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
    // GESTIONE SESSIONE (Metodi Standard aggiunti)
    // ---------------------------------------------------------

    // Restituisce l'ID dell'utente attualmente loggato in Firebase Auth
    fun getCurrentUserId(): String? = auth.currentUser?.uid


    // Recupera i dati completi dell'utente loggato
    suspend fun getCurrentUser(): User? {
        val userId = getCurrentUserId() ?: return null
        return getUserById(userId)
    }

    // Effettua il logout dalla sessione
    fun logout() {
        auth.signOut()
    }

    // ---------------------------------------------------------
    // CREAZIONE UTENTE
    // ---------------------------------------------------------
    suspend fun createUser(user: User) {
        usersCollection.document(user.id).set(user).await()
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
                savedJobs = emptyList()
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

    suspend fun updateName(userId: String, name: String) {
        usersCollection.document(userId).update("name", name).await()
    }

    suspend fun updateDescription(userId: String, description: String) {
        usersCollection.document(userId).update("description", description).await()
    }

    suspend fun updateEducation(userId: String, education: String) {
        usersCollection.document(userId).update("education", education).await()
    }

    suspend fun updateResidence(userId: String, residence: String) {
        usersCollection.document(userId).update("residence", residence).await()
    }

    suspend fun updateProfilePhoto(userId: String, url: String) {
        usersCollection.document(userId).update("photoUrl", url).await()
    }

    suspend fun updateCv(userId: String, url: String) {
        usersCollection.document(userId).update("cvUrl", url).await()
    }

    // ---------------------------------------------------------
    // ELIMINAZIONE UTENTE
    // ---------------------------------------------------------
    suspend fun deleteUser(userId: String) {
        usersCollection.document(userId).delete().await()
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

    suspend fun getFavoritePlaceIds(userId: String): List<String> {
        val user = getUserById(userId)
        return user?.savedPlaces ?: emptyList()
    }

    // ---------------------------------------------------------
    // TOKEN FCM
    // ---------------------------------------------------------
    suspend fun updateFcmToken(userId: String, token: String) {
        usersCollection.document(userId).update("fcmToken", token).await()
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
