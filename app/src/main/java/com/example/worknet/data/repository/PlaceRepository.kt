package com.example.worknet.data.repository

import android.net.Uri
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

    suspend fun updateTitle(placeId: String, title: String) {
        placesCollection.document(placeId).update("title", title).await()
    }

    suspend fun updateDescription(placeId: String, description: String) {
        placesCollection.document(placeId).update("description", description).await()
    }

    suspend fun updateAddress(placeId: String, address: String) {
        placesCollection.document(placeId).update("address", address).await()
    }

    suspend fun updateImageUrl(placeId: String, url: String) {
        placesCollection.document(placeId).update("imageUrl", url).await()
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
        val ref = storage.reference.child("places/$placeId/image.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
