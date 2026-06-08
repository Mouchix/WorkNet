package com.example.worknet.data.repository

import android.net.Uri
import android.util.Log
import com.example.worknet.data.model.Place
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class PlaceRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    private val placesCollection = db.collection("places")

    // ---------------------------------------------------------
    // CREAZIONE PLACE
    // ---------------------------------------------------------
    suspend fun createPlace(place: Place) {
        placesCollection.document(place.id).set(place).await()
    }

    // ---------------------------------------------------------
    // LETTURA PLACE
    // ---------------------------------------------------------
    suspend fun getPlaceById(placeId: String): Place? {
        val snapshot = placesCollection.document(placeId).get().await()
        return snapshot.toObject(Place::class.java)
    }

    suspend fun getAllPlaces(): List<Place> {
        val snapshot = placesCollection.get().await()
        return snapshot.toObjects(Place::class.java)
    }

    suspend fun getPlacesByOwner(ownerId: String): List<Place> {
        val snapshot = placesCollection
            .whereEqualTo("ownerId", ownerId)
            .get()
            .await()

        return snapshot.toObjects(Place::class.java)
    }

    // ---------------------------------------------------------
    // AGGIORNAMENTO PLACE
    // ---------------------------------------------------------
    suspend fun updatePlace(place: Place) {
        placesCollection.document(place.id).set(place).await()
    }

    // ---------------------------------------------------------
    // ELIMINAZIONE PLACE
    // ---------------------------------------------------------
    suspend fun deletePlace(placeId: String) {
        placesCollection.document(placeId).delete().await()
    }

    // ---------------------------------------------------------
    // UPLOAD IMMAGINE PLACE (STORAGE)
    // ---------------------------------------------------------
    suspend fun uploadPlaceImage(uri: Uri, placeId: String): String {
        val uriString = uri.toString()

        if (uriString.contains("firebasestorage.googleapis.com") || uriString.startsWith("http")) {
            Log.d("REPO_DEBUG", "L'immagine è già su Firebase, salto l'upload.")
            return uriString
        }

        val ref = storage.reference.child("places/$placeId.jpg")

        try {
            Log.d("REPO_DEBUG", "Inizio upload per nuovo file locale: $uriString")
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            return url
        } catch (e: Exception) {
            Log.e("REPO_DEBUG", "Errore dentro uploadPlaceImage: ${e.message}", e)
            throw e
        }
    }

}
