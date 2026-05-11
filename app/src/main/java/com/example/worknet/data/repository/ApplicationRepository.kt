package com.example.worknet.data.repository

import android.system.Os.close
import com.example.worknet.data.model.Application
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ApplicationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun applicationsCollection(placeId: String, jobId: String) =
        db.collection("places")
            .document(placeId)
            .collection("jobs")
            .document(jobId)
            .collection("applications")

    // ---------------------------------------------------------
    // CREAZIONE CANDIDATURA
    // ---------------------------------------------------------
    suspend fun createApplication(application: Application) {
        applicationsCollection(application.placeId, application.jobId)
            .document(application.id)
            .set(application)
            .await()
    }

    // ---------------------------------------------------------
    // LETTURA CANDIDATURE DI UN JOB
    // ---------------------------------------------------------
    suspend fun getApplicationsByJob(placeId: String, jobId: String): List<Application> {
        val snapshot = applicationsCollection(placeId, jobId).get().await()
        return snapshot.toObjects(Application::class.java)
    }

    // ---------------------------------------------------------
    // LETTURA CANDIDATURE INVIATE DA UN UTENTE
    // ---------------------------------------------------------
    suspend fun getApplicationsByUser(userId: String): List<Application> {
        val snapshot = db.collectionGroup("applications")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        return snapshot.toObjects(Application::class.java)
    }

    // ---------------------------------------------------------
    // LETTURA CANDIDATURE RICEVUTE DAL DATORE
    // ---------------------------------------------------------
    suspend fun getApplicationsReceivedByOwner(ownerId: String): List<Application> {
        val snapshot = db.collectionGroup("applications")
            .whereEqualTo("ownerId", ownerId)
            .get()
            .await()

        return snapshot.toObjects(Application::class.java)
    }

    // ---------------------------------------------------------
    // AGGIORNAMENTO CANDIDATURA
    // ---------------------------------------------------------
    suspend fun updateApplication(application: Application) {
        applicationsCollection(application.placeId, application.jobId)
            .document(application.id)
            .set(application)
            .await()
    }

    suspend fun updateApplicationStatus(
        placeId: String,
        jobId: String,
        applicationId: String,
        status: String
    ) {
        applicationsCollection(placeId, jobId)
            .document(applicationId)
            .update("status", status)
            .await()
    }

    // ---------------------------------------------------------
    // ELIMINAZIONE CANDIDATURA
    // ---------------------------------------------------------
    suspend fun deleteApplication(placeId: String, jobId: String, applicationId: String) {
        applicationsCollection(placeId, jobId)
            .document(applicationId)
            .delete()
            .await()
    }

    fun observeApplications(placeId: String, jobId: String): Flow<List<Application>> = callbackFlow {
        val listener = applicationsCollection(placeId, jobId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val apps = snapshot?.toObjects(Application::class.java) ?: emptyList()
                trySend(apps)
            }

        awaitClose { listener.remove() }
    }

}